package db;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import utilities.Status;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class WideBoxDB extends UnicastRemoteObject implements IWideBoxDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ConcurrentHashMap<String, Status> map;
	//private File log;
	
	private static final int NRTH = 300;
	private static final int NRRW = 26;
	private static final int NRCL = 40;
	
	private AtomicInteger requests;
	
	protected WideBoxDB() throws RemoteException {
		super();
		loadDB();
		requests = new AtomicInteger();
		//log = new File("log.txt");
		// TODO Auto-generated constructor stub
	}

	public boolean put(String key, Status value, Status oldValue) throws RemoteException {
		//BufferedWriter bw = null;
		//PrintWriter out = null;
		boolean result = false;
		/*try {
			bw = new BufferedWriter (new FileWriter (log, true));
			out = new PrintWriter(bw);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result =  false;
		}*/
		
		if (map.replace(key, oldValue, value)) {
			//out.println("put(" + key + "," + value + ")" );
			System.out.println("Changed seat " + key + " from " + 
					oldValue + " to " + value);
			result =  true;
		}
		//out.close();
		requests.incrementAndGet();
		return result;
			
	}

	public void printStatus(String theatre) throws RemoteException {
		Status [][] listSeats = new Status[NRRW][NRCL];
		int coluna = 0;
		for(int i = 0; i < NRRW; i++) {
			for (int j = 0; j < NRCL; j++) {
				char linha = getCharLine(i);
				coluna = j+1;
				listSeats[i][j] = map.get(theatre + "-" + linha + Integer.toString(coluna));
				System.out.print(String.valueOf(linha) + (j+1) + "-" + listSeats[i][j] + " ");
			}
			System.out.print("\n");
		}
	}

	public List<String> listTheatres() {
		List<String> result = new ArrayList<String>();
		for (int j = 1; j <= NRTH; j++) 
			result.add(String.valueOf(j));
		
		requests.incrementAndGet();
		return result;
	}
	
			
		/*List<String> teatros = new ArrayList<String>();
		int coluna = 0;
		Status curr = null;
		for(int k = 1; k <= NRTH; k++) {
			label:
			for(int i = 0; i < NRRW; i++)
				for (int j = 0; j < NRCL; j++) {
					char linha = getCharLine(i);
					coluna = j+1;
					curr = map.get("T"+k+"-"+linha+coluna); 
					if (curr == Status.FREE ) {
						teatros.add("T"+k);
						break label;
					}
				}
		}
		return teatros;*/
		/*try {
		BufferedReader br = new BufferedReader(new FileReader("Teatros.txt"));
		String l = br.readLine();
		
		while(l!=null) {
			teatros.add(l);
			l = br.readLine();
		}
		br.close();
		} catch (IOException e) {
			
		}
		return teatros;*/
	//}

	public Status[][] listSeats(String theatre) throws RemoteException {
		Status [][] listSeats = new Status[NRRW][NRCL];
		int coluna = 0;
		for(int i = 0; i < NRRW; i++)
			for (int j = 0; j < NRCL; j++) {
				char linha = getCharLine(i);
				coluna = j+1;
				listSeats[i][j] = map.get(theatre + "-" + linha + Integer.toString(coluna));
			}
		
		requests.incrementAndGet();
		return listSeats;

	}

	//@SuppressWarnings("unused")
	private void loadDB () {
		this.map = new ConcurrentHashMap<String, Status>(NRTH * NRRW * NRCL);
		int coluna = 0;
		for(int k = 1; k <= NRTH; k++) {
			for(int i = 0; i < NRRW; i++)
				for (int j = 0; j < NRCL; j++) {
					char linha = getCharLine(i);
					coluna = j+1;
					map.put(k+"-"+linha+coluna, Status.FREE);
				}
		}
	}


	private char getCharLine(int i) {
		char character = 'A';
		switch(i) {
		case 0 :
			character = 'A';
			break;
		case 1 :
			character = 'B';
			break;
		case 2 :
			character = 'C';
			break;
		case 3 :
			character = 'D';
			break;
		case 4 :
			character = 'E';
			break;		   
		case 5 :
			character = 'F';
			break;
		case 6 :
			character = 'G';
			break;		   
		case 7 :
			character = 'H';
			break;
		case 8 :
			character = 'I';
			break;   
		case 9 :
			character = 'J';
			break;
		case 10 :
			character = 'K';
			break;	   
		case 11 :
			character = 'L';
			break;
		case 12 :
			character = 'M';
			break; 
		case 13 :
			character = 'N';
			break;
		case 14 :
			character = 'O';
			break;	   
		case 15 :
			character = 'P';
			break;
		case 16 :
			character = 'Q';
			break;		   
		case 17 :
			character = 'R';
			break;
		case 18 :
			character = 'S';
			break;   
		case 19 :
			character = 'T';
			break;
		case 20 :
			character = 'U';
			break;
		case 21 :
			character = 'V';
			break;
		case 22 :
			character = 'W';
			break;   
		case 23 :
			character = 'X';
			break;
		case 24 :
			character = 'Y';
			break;
		case 25 :
			character = 'Z';
			break;
		}
		return character;
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
	
	public int getRate() throws RemoteException{
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
