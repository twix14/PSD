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
import java.rmi.RemoteException;
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
	private static final int NROPS = 3000;

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

	protected WideBoxDB(int last, int numOfTheatresPerDB) throws RemoteException {
		super();
		loadDB(last, numOfTheatresPerDB);
		down = false;
		
		range = new int[2];
		range[0] = last-numOfTheatresPerDB+1;
		range[1] = last;
		
		es = Executors.newSingleThreadExecutor();

		requests = new AtomicInteger();
		ops = new AtomicInteger(NROPS);
		lock = new ReentrantLock();
		log = new File("log.txt");
		fileHash = new File("theatres.txt");

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

	public boolean put(String theatre, String key, Status value, Status oldValue) throws RemoteException {
		if(!down) {
			boolean result = false;
			ConcurrentHashMap<String,Status> curr = null;
			String linha = null;
			try {

				//lock.lock();
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
			return result;
		} else 
			throw new RemoteException("Db Server down!");
	}

	public void printStatus(String theatre) throws RemoteException {
		if(!down) {
			ConcurrentHashMap<String,Status> curr = map.get(theatre);
			System.out.println(curr.toString());		       
		} else 
			throw new RemoteException("Db Server down!");
	}

	public List<String> listTheatres() throws RemoteException {
		if(!down) {
			List<String> result = new ArrayList<String>();
			for (int j = range[0]; j <= range[1]; j++) 
				result.add(String.valueOf(j));

			requests.incrementAndGet();
			return result;
		} else 
			throw new RemoteException("Db Server down!");
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
			for(int i = 0; i < NRRW; i++)
				for (int j = 1; j <= NRCL; j++) {
					curr.put(alphabet[i] + Integer.toString(j), Status.FREE);
				}
			map.put(Integer.toString(k), curr);
		}
	}

	public void fullTheatre(String theatre) throws RemoteException {
		if(!down) {
			ConcurrentHashMap<String,Status> curr = map.get(theatre);
			curr.replaceAll((k, v) -> Status.OCCUPIED);
			System.out.println("Done");
		} else 
			throw new RemoteException("Db Server down!");
	}

	private void updateFileHash() {
		ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> copy = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>(map);
		es.execute(new WorkingThread(copy, requests.get()));
	}

	@Override
	public String get(String theatre) throws RemoteException {
		try {
		if(!down) {
			System.out.println("Theatre: " + theatre);
			ConcurrentHashMap<String,Status> curr = map.get(theatre);
			return curr.search(1, (k, v) -> {
				if (v.equals(Status.FREE))
					return k; 
				return null;
			});
		} else 
			throw new RemoteException("Db Server down!");
		} catch(Exception e) {
			System.out.println(theatre);
			e.printStackTrace();
			throw new RemoteException();
		}
	}

	public void crash() throws RemoteException {
		down = true;
	}

	public void reset() throws RemoteException {
		down = false;
	}

	public int getRate() throws RemoteException {
		if(!down) {
			int res1= 0;
			int res2 = 0;
			try {
				res1 = requests.get();
				Thread.sleep(1000);
				res2 = requests.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return  (res2-res1);
		} else 
			throw new RemoteException("Db Server down!");
	}


}
