package db;

import java.io.File;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import utilities.Status;


public class WideBoxDB extends UnicastRemoteObject implements IWideBoxDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NRTH = 300;
	private static final int NRRW = 26;
	private static final int NRCL = 40;
	private static final int NROPS = 400;

	private ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;	
	private File log;
	private File fileHash;
	private AtomicInteger requests;
	private AtomicInteger ops;
	
	
	private FileChannel logChannel;
	private FileChannel TheatreChannel;
	private ByteBuffer mByteBuffer;
	private static final int BUFFER_SIZE = 30;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private FileOutputStream fos;	
	
	ReentrantLock lock;
	
	String alf = "abcdefghijklmnopqrstuvwxyz";
	char[] alphabet = alf.toUpperCase().toCharArray();
	
	protected WideBoxDB() throws RemoteException {
		super();
		loadDB();
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
			mByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			
		} catch (IOException e) {
			System.out.println("Log file already exists.");
			e.printStackTrace();
		}
	}

	public boolean put(String theatre, String key, Status value, Status oldValue) throws RemoteException {
		boolean result = false;
		ConcurrentHashMap<String,Status> curr = null;
		String linha = null;
		try {
			
			//lock.lock();
			
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
			
	}

	public void printStatus(String theatre) throws RemoteException {
		ConcurrentHashMap<String,Status> curr = map.get(theatre);
		System.out.println(curr.toString());
	}

	public List<String> listTheatres() {
		List<String> result = new ArrayList<String>();
		for (int j = 1; j <= NRTH; j++) 
			result.add(String.valueOf(j));
		
		requests.incrementAndGet();
		return result;
	}

	public ConcurrentHashMap<String,Status> listSeats(String theatre) throws RemoteException {
		requests.incrementAndGet();
		return map.get(theatre);
	}

	private void loadDB () {
		this.map = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>(NRTH);
		ConcurrentHashMap<String,Status> curr; 
		for(int k = 1; k <= NRTH; k++) {
			curr = new ConcurrentHashMap<String,Status>(NRCL * NRRW);
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
		try {
	         FileOutputStream fileOut =
	         new FileOutputStream(fileHash);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(map);
	         fileOut.flush();
	         fileOut.getFD().sync();
	         out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in Theatres.txt");
	      } catch (IOException i) {
	         i.printStackTrace();
	      }
	}

	@Override
	public String get(String theatre) throws RemoteException {
		ConcurrentHashMap<String,Status> curr = map.get(theatre);
		return curr.search(1, (k, v) -> {
				if (v.equals(Status.FREE))
					return k; 
				return null;
				});
	}
	
	public void crash() throws RemoteException {
		System.exit(0);
		System.out.println("System crashed by failure generator!");
	}
	
	public int getRate() throws RemoteException {
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
	}


}
