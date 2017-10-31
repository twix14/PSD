package db;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import utilities.Status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class WideBoxDB2 extends UnicastRemoteObject implements IWideBoxDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NRTH = 300;
	private static final int NRRW = 26;
	private static final int NRCL = 40;

	private ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;	
	private File log;
	private File fileHash;
	private AtomicInteger requests;
	private AtomicInteger ops;
	ReentrantLock lock;
	
	String alf = "abcdefghijklmnopqrstuvwxyz";
	char[] alphabet = alf.toUpperCase().toCharArray();
	
	protected WideBoxDB2() throws RemoteException {
		super();
		loadDB();
		requests = new AtomicInteger();
		ops = new AtomicInteger(1000);
		lock = new ReentrantLock();
		log = new File("log.txt");
		fileHash = new File("theatres.txt");
		try {
			if (log.createNewFile()){
			    System.out.println("Log file created!");
			  }else{
			    System.out.println("Log file already exists.");
			  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean put(String theatre, String key, Status value, Status oldValue) throws RemoteException {
		//BufferedWriter bw = null;
		//PrintWriter out = null;
		FileOutputStream fl = null;
		boolean result = false;
		ConcurrentHashMap<String,Status> curr = null;
		String linha = null;
		try {
			//bw = new BufferedWriter (new FileWriter (log, true));
			fl = new FileOutputStream(log, true);
			//out = new PrintWriter(bw);
			lock.lock();
			//bw.append("put");
			//bw.newLine();
			linha = "put(" + theatre + "," + key + "," + value + "," + oldValue + ")";
			fl.write(linha.getBytes());
			fl.write(System.getProperty("line.separator").toString().getBytes());
			fl.flush();
			fl.getFD().sync();
			
			curr = map.get(theatre);
			if (curr.replace(key, oldValue, value)) {
				System.out.println("Changed seat " + key + " from " + 
						oldValue + " to " + value);
				result =  true;
			}
			ops.decrementAndGet();
			if(ops.get() == 0) {
				updateFileHash();
			}
			fl.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result =  false;
			
		} finally {
			lock.unlock();
		}
		
		//bw.close();
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
	

	public Status[][] listSeats(String theatre) throws RemoteException {
		Status [][] listSeats = new Status[NRRW][NRCL];
		ConcurrentHashMap<String,Status> curr = map.get(theatre);
		String key = null;
		for(int i = 0; i < NRRW; i++)
			for (int j = 1; j <= NRCL; j++) {
				key = alphabet[i] + Integer.toString(j);
				listSeats[i][j] = curr.get(key);
			}
		
		requests.incrementAndGet();
		return listSeats;

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
		
	}


	@Override
	public String get(String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return  (res2-res1);
	}


}
