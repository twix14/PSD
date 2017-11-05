package loadGen;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import db.IWideBoxDB;
import server.IWideBox;
import server.Message;
import utilities.Session;

public class Generator {
	
	public static AtomicInteger rate;
	public static AtomicInteger duration;
	
	public static AtomicInteger requests;
	public static AtomicLong avglatency;
	
	private static final String SERVER_IP = "192.168.1.82";
	private static final int SERVER_PORT = 5000;
	private static final String DB_IP = "192.168.1.82";
	private static final int DB_PORT = 5001;
	private static final int ratePS = 165;
	
	private static final int NRCL = 100000;
	
	public static void main(String[] args) {
		new Generator(args);
	}
	
	public Generator(String[] args) {
		Scanner sc = new Scanner(System.in);
		IWideBox wb = null;
		IWideBoxDB wbDB = null;
		ExecutorService es= null;
		try {
			Registry registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
			wb = (IWideBox) registry.lookup("WideBoxServer");
			Registry registry2 = LocateRegistry.getRegistry(DB_IP, DB_PORT);
			wbDB = (IWideBoxDB) registry2.lookup("WideBoxDBServer");
		} catch(RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		requests = new AtomicInteger();
		avglatency = new AtomicLong();
		es = Executors.newWorkStealingPool();
		
		System.out.println("LOAD GENERATOR STARTED");
		while(true) {
			System.out.println("Comando do tipo: ORIGIN,TARGET,OPERATION,RATE,DURATION");
			System.out.println("Comando----->");
			String command = sc.nextLine();
			String[] split = command.split(",");
		
			//SSQ
			if (!split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 1, split[4], Integer.parseInt(split[0]), split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 1, split[4], (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), split[1], es);
				}
			}
			
			//SRQ
			if (!split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 2, split[4], Integer.parseInt(split[0]), null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 2, split[4], (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), null, es);
				}
			}
			
			//RSQ
			if (split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 3, split[4], 0, split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 3, split[4], (int) nThreads + 1 ,  lag, 0, split[1], es);
				}
					
			}
			
			//RRQ
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 4, split[4], 0, null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 4, split[4], (int) nThreads + 1 ,  lag, 0, null, es);
				}
					
			}
			
			//SSP
			if (!split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 5, split[4], Integer.parseInt(split[0]), split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 5, split[4], (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), split[1], es);
				}
					
			}
			
			//RSP
			if (split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 6, split[4], 0, split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 6, split[4], (int) nThreads + 1 ,  lag, 0, split[1], es);
				}
			}
			
			//SRP
			if (!split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					starOpWithoutRate(wb, wbDB, 7, split[4], Integer.parseInt(split[0]), null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 7, split[4], (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), null, es);
				}
			}
			
			//RRP
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) { 
					starOpWithoutRate(wb, wbDB, 8, split[4], 0, null, es);
				
				}else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 8, split[4], (int) nThreads + 1 ,  lag, 0, null, es);
				}
			}
		}
		
	}
	
	public void starOpWithoutRate(IWideBox wb, IWideBoxDB wbDB, int op, String duration, int clientId, String theatre, 
			ExecutorService es) {
		try {
				
			Message m = wb.search();
			AppServerRate app = new AppServerRate(wb);
			DbServerRate db = new DbServerRate(wbDB);
			Gen g = new Gen(wb, m, op, clientId, theatre);
			es.execute(g);
			
			GenRate gr = new GenRate();
			es.execute(app);
			es.execute(db);
			gr.start();
			
			Thread.sleep(Integer.parseInt(duration) * 1000);
			app.kill();
			db.kill();
			gr.kill();
			g.kill();
		} catch (NumberFormatException | InterruptedException | RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	public void starOp(IWideBox wb, IWideBoxDB wbDB, int op, String duration, 
			int numThreads, double lag, int clientId, String theatre, ExecutorService es) {
		try {
				
			Message m = wb.search();
			AppServerRate app = new AppServerRate(wb);
			DbServerRate db = new DbServerRate(wbDB);
			List<Gen> list = new ArrayList<>();
			for(int i = 0; i < numThreads-1; i++) {
				Gen g = new Gen(wb, m, op, clientId, theatre);
				list.add(g);
				es.execute(g);
			}
			if(!(lag == 0)) {
				Gen last = new Gen(wb, m, op, clientId, theatre);
				es.execute(last);
				Delay d = new Delay(last, (int) lag * 1000);
				es.execute(d);
			}
			
			GenRate gr = new GenRate();
			es.execute(app);
			es.execute(db);
			gr.start();
			
			Thread.sleep(Integer.parseInt(duration) * 1000);
			app.kill();
			db.kill();
			gr.kill();
			for(int i = 0; i < numThreads-1; i++) {
				list.get(i).kill();
			}
		} catch (NumberFormatException | InterruptedException | RemoteException e) {
			e.printStackTrace();
		} 
	}

	public class AppServerRate implements Runnable {
		
		private IWideBox wb;
		private volatile boolean keepGoing = true;
		
		public AppServerRate(IWideBox wb) {
			this.wb = wb;
		}
		
		public void run() {
			while(keepGoing) {
				try {
					System.out.println("App server, serving " + wb.getRate()
						+ " req/sec ");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	
	public class DbServerRate implements Runnable {
		
		private IWideBoxDB wbDB;
		private volatile boolean keepGoing = true;
		
		public DbServerRate(IWideBoxDB wbDB) {
			this.wbDB = wbDB;
		}
		
		public void run() {
			while(keepGoing) {
				try {
					System.out.println("DB server, serving " + wbDB.getRate()
						+ " req/sec ");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	
	public class Delay implements Runnable {
		
		private Gen last;
		private int duration;
		
		public Delay(Gen last, int duration) {
			this.last = last;
			this.duration = duration;
		}
		
		public void run() {
			try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			last.kill();
		}
		
	}
	
	public class Gen implements Runnable {
		
		private IWideBox wb;
		private volatile boolean keepGoing = true;
		private Message m;
		private int op;
		private int clientId;
		private String theatre;
		
		public Gen(IWideBox wb, Message m, int op, int clientId, String theatre) {
			this.wb = wb;
			this.m = m;
			this.op = op;
			this.clientId = clientId;
			this.theatre = theatre;
		}
		
		public void run() {
			Random rand = new Random();
			//Random rand2 = new Random()
			Message m2 = null;
			Session ses = null;
			switch(op) {
			
			//SSQ
			case 1:
				while(keepGoing) {
					query(clientId, theatre, m2);
				}
				break;
				
			//SRQ	
			case 2:			
				while(keepGoing) {
					theatre = m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					
					query(clientId, theatre, m2);
				}
				break;
				
			//RSQ	
			case 3:
				while(keepGoing) {
					clientId = rand.nextInt(NRCL);
					query(clientId, theatre, m2);
				}
				break;
			
			//RRQ
			case 4:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
					
					query(clientId, theatre, m2);
				}
				break;
				
			//SSP	
			case 5:
				while(keepGoing) {
					purch(clientId, theatre, m2, ses, rand);
				}
				break;
				
			//RSP
			case 6:
				while(keepGoing) {
					clientId = rand.nextInt(NRCL);
					purch(clientId, theatre, m2, ses, rand);
				}
				break;
			
			//SRP
			case 7:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					purch(clientId, theatre, m2, ses, rand);
				}
				break;
				
			//RRP	
			case 8:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
					purch(clientId, theatre, m2, ses, rand);
				}
				break;
			}
			
		}
		
		public void purchase(int client, String theatre, List<String> listTheatres) throws RemoteException {
			Message m2 = null;
			Session ses = null;
			int value = 0;
			Random rand = new Random();
			
			m2 = wb.seatsAvailable(client, theatre);
			ses = m2.getSession();
			
			if(m2.getStatus().equals(Message.AVAILABLE)) {
				wb.acceptSeat(ses);
			}
			
			else if (m2.getStatus().equals(Message.FULL)) {
				value = rand.nextInt(listTheatres.size()+1);
				purchase(client, listTheatres.get(value), listTheatres);
			}
		}
		
		private void query (int clientId, String theatre, Message m2) {
			long t0 = System.nanoTime();
			try {
				m2 = wb.seatsAvailable(clientId,theatre);
				
				if(m2.getStatus().equals(Message.AVAILABLE)) {
					wb.cancelSeat(m2.getSession());
				}
				
				else if(m2.getStatus().equals(Message.FULL)) {
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			long t1 = System.nanoTime();
			avglatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(t1-t0));
			requests.incrementAndGet();
		}
		
		private void purch(int clientId, String theatre, Message m2, Session ses, Random rand) {
			long t0 = System.nanoTime();
			try {
			m2 = wb.seatsAvailable(clientId, theatre);
			ses = m2.getSession();
			
			if(m2.getStatus().equals(Message.AVAILABLE)) {
				wb.acceptSeat(ses);
			}
			
			else if (m2.getStatus().equals(Message.FULL)) {
				purchase(clientId, m.getTheatres().get(rand.nextInt(
						m.getTheatres().size())), m.getTheatres());
			}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			long t1 = System.nanoTime();
			avglatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(t1-t0));
			requests.incrementAndGet();
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	
	public class GenRate extends Thread {
		
		private volatile boolean keepGoing = true;
		
		public void run() {
			while(keepGoing) {
					int res1 = requests.get();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int res2 = requests.get();
					long lat = avglatency.get();
					System.out.println("Avg latency - " + lat/(res2-res1));
					avglatency.set(0);
					System.out.println("Load Generator rate - " + (res2-res1));
			}
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	

}
