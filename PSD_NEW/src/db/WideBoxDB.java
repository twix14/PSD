package db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import jdk.nashorn.internal.ir.RuntimeNode.Request;
import utilities.Status;


public class WideBoxDB extends UnicastRemoteObject implements IWideBoxDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NRRW = 26;
	private static final int NRCL = 40;
	private static final int NROPS = 6000;
	
	private int sec;

	private ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;	
	private File log;

	private AtomicInteger requests;
	private AtomicInteger ops;

	private File fileHash;
	private FileChannel logChannel;

	private static final int BUFFER_SIZE = 30;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private FileOutputStream fos;	

	private volatile boolean down;

	ReentrantLock lock;
	private ExecutorService es;

	String alf = "abcdefghijklmnopqrstuvwxyz";
	char[] alphabet = alf.toUpperCase().toCharArray();

	private int[] range;

	//Secondary
	private IWideBoxDB secondaryServer = null;
	private boolean primary = false;
	private boolean secondaryUp = false;

	protected WideBoxDB(int [] range2, boolean primary) throws RemoteException {
		super();
		loadDB(range2[1], range2[2]);
		down = false;
		sec = 0;
		range = new int[2];
		range [0] = range2[0];
		range[1] = range2[1];

		es = Executors.newSingleThreadExecutor();

		requests = new AtomicInteger();
		ops = new AtomicInteger(NROPS);
		lock = new ReentrantLock();
		log = new File("log.txt");
		fileHash = new File("theatres.txt");

		this.primary = primary;
		try {
			fileHash.createNewFile();
			if (log.createNewFile())
				System.out.println("Log file created!");

			fos = new FileOutputStream(log, true);
			logChannel = fos.getChannel();

		} catch (IOException e) {
			System.out.println("Log file already exists.");
			e.printStackTrace();
		}
	}

	public boolean connectToSecondary(String ip, int port) throws RemoteException{
		boolean res = true;

		try {
			Registry primaryRegistry = LocateRegistry.getRegistry(ip, port);
			secondaryServer = (IWideBoxDB) primaryRegistry.lookup("WideBoxDBServer");
			primary = true;

			// Ping just to know if secondary is up
			if (secondaryServer.ping()) {
				secondaryUp = true;
				System.out.println("Connected to Secondary");
			}


		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
			secondaryUp = false;
			System.err.println("Error trying to connect to the secondary");
		}

		return res;
	}

	public boolean ping() throws RemoteException {
		primary = false;
		secondaryUp = false;
		System.out.println("Primary connected");
		return true;
	}

	public boolean put(String theatre, String key, Status value, Status oldValue) throws RemoteException {
		long start = System.currentTimeMillis();
		if (primary && secondaryUp && secondaryServer != null) {
			try {
				secondaryServer.put(theatre, key, value, oldValue);
			} catch (RemoteException e) {
				secondaryUp = false;
				System.out.println("Secondary is down");
			}
			System.out.println("Demorou secundario " + (System.currentTimeMillis()-start));
			System.out.println("Pedidos sec " + sec++);
		}
		boolean result = false;
		ConcurrentHashMap<String,Status> curr = null;
		String linha = null;
		try {
			long esc = System.currentTimeMillis();

			try {
				lock.lock();
				ByteBuffer mByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

				linha = theatre + "," + key + "," + value + "," + oldValue;
				mByteBuffer.put(linha.getBytes());
				mByteBuffer.put(LINE_SEPARATOR.getBytes());
				mByteBuffer.flip();

				while (mByteBuffer.hasRemaining())
					logChannel.write(mByteBuffer);

				logChannel.force(true);
				fos.getFD().sync();

				mByteBuffer.clear();
			}finally {
				lock.unlock();
				System.out.println("Demorou escrita " + (System.currentTimeMillis()-esc));
			}


			curr = map.get(theatre);
			if (curr.replace(key, oldValue, value)) {
				System.out.println("OP: " + ops.get() +" | Changed seat " + key + " from " + 
						oldValue + " to " + value);
				result =  true;
				ops.decrementAndGet();
			}

			if(ops.get() == 0) {
				System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				updateFileHash();
				ops.set(NROPS);
				//Files.delete(log.toPath());
				logChannel.truncate(0);
				//log.createNewFile();
			}
		} catch (IOException | BufferOverflowException e) {

			e.printStackTrace();
			result =  false;

		} /*finally {
			lock.unlock();
		}*/

		requests.incrementAndGet();
		System.out.println("Demorou put " + (System.currentTimeMillis()-start));
		return result;
	}

	public void printStatus(String theatre) throws RemoteException {
		ConcurrentHashMap<String,Status> curr = map.get(theatre);
	}

	public List<String> listTheatres() throws RemoteException {
		List<String> result = new ArrayList<String>();
		for (int j = range[0]; j <= range[1]; j++) 
			result.add(String.valueOf(j));

		requests.incrementAndGet();
		return result;
	}

	public ConcurrentHashMap<String,Status> listSeats(String theatre) throws RemoteException {
		if(!down) {
			requests.incrementAndGet();
			return map.get(theatre);
		} else 
			throw new RemoteException("Db Server down!");
	}

	private void loadDB (int last, int numOfTheatresPerDB) {
		System.out.println("My Theatres: " + (last-numOfTheatresPerDB+1) + " to " + last);
		this.map = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>(numOfTheatresPerDB);
		ConcurrentHashMap<String,Status> curr; 
		int o = last-numOfTheatresPerDB+1;
		for(int k = o; k <= last; k++) {
			curr = new ConcurrentHashMap<String,Status>(NRCL * NRRW);

			if(k == 1500)
				System.out.println("ddd");
			for(int i = 0; i < NRRW; i++)
				for (int j = 1; j <= NRCL; j++) {
					curr.put(alphabet[i] + Integer.toString(j), Status.FREE);
				}
			map.put(Integer.toString(k), curr);
		}
	}

	public void fullTheatre(String theatre) throws RemoteException {
		ConcurrentHashMap<String,Status> curr = map.get(theatre);
		curr.replaceAll((k, v) -> Status.OCCUPIED);
		System.out.println("Done");
	}

	private void updateFileHash() {
		ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> copy = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>(map);
		es.execute(new WorkingThread(copy, requests.get()));
	}

	@Override
	public String get(String theatre) throws RemoteException {
		long start = System.currentTimeMillis();
		try {
			System.out.println("Theatre: " + theatre);
			ConcurrentHashMap<String,Status> curr = map.get(theatre);
			return curr.search(1, (k, v) -> {
				if (v.equals(Status.FREE)) {
					System.out.println("Demorou get " + (System.currentTimeMillis()-start));
					return k;
				}
				return null;
			});
		} catch(Exception e) {
			System.out.println(theatre);
			e.printStackTrace();
			throw new RemoteException();
		}
	}

	public void updateSecondary (int numOfTheatresPerDB, int rangeMin, int rangeMax) throws RemoteException{
		System.out.println("My Theatres: " + rangeMin +" to " + rangeMax + "\n");
		for(Map.Entry<String, ConcurrentHashMap<String,Status>> entry : map.entrySet()) {
			String key = entry.getKey();
			ConcurrentHashMap<String,Status> value = entry.getValue();
			if(rangeMax < Integer.parseInt(key) && Integer.parseInt(key) <= range[1]) {
				map.remove(key);
			}
		}
	}

	@Override
	public void newRange(int numOfTheatresPerDB, int rangeMin, int rangeMax, String[] split) throws RemoteException {
		// range[0] old min
		// range [1] old max


		System.out.println("My Theatres: " + rangeMin +" to " + rangeMax + "\n");
		ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> mapSend = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>();

		for(Map.Entry<String, ConcurrentHashMap<String,Status>> entry : map.entrySet()) {
			String key = entry.getKey();
			ConcurrentHashMap<String,Status> value = entry.getValue();
			if(rangeMax < Integer.parseInt(key) && Integer.parseInt(key) <= range[1]) {
				mapSend.put(key, value);
				map.remove(key);
			}
		}

		try {
			Registry reg = LocateRegistry.getRegistry(split[0], 
					Integer.parseInt(split[1]));

			IWideBoxDB db = (IWideBoxDB) reg.lookup("WideBoxDBServer");
			db.sendValues(mapSend);
		} catch (Exception e) {
		}
		range [0] = rangeMin;
		range[1] = rangeMax;
		secondaryServer.updateSecondary(numOfTheatresPerDB, rangeMin, rangeMax);

	}

	@Override
	public void sendValues(ConcurrentHashMap<String, ConcurrentHashMap<String, Status>> mapSend) throws RemoteException {
		for(Map.Entry<String, ConcurrentHashMap<String,Status>> entry : mapSend.entrySet()) {
			String key = entry.getKey();
			ConcurrentHashMap<String,Status> value = entry.getValue();
			map.put(key, value);
		}
		secondaryServer.sendValues(mapSend);
	}

	public int getRequests() throws RemoteException {
		return requests.get();
	}
}
