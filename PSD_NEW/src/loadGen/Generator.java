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

import db.IWideBoxDB;
import server.IWideBox;
import server.Message;
import utilities.Session;

public class Generator {
	
	public static AtomicInteger rate;
	public static AtomicInteger duration;
	
	public static AtomicInteger requests;
	
	private static final String SERVER_IP = "10.101.148.102";
	private static final int SERVER_PORT = 5000;
	private static final String DB_IP = "10.101.148.102";
	private static final int DB_PORT = 5001;
	
	private static final int NRCL = 100000;
	private static final int def = 20;
	
	public static void main(String[] args) {
		new Generator(args);
	}
	
	public Generator(String[] args) {
		Scanner sc = new Scanner(System.in);
		IWideBox wb = null;
		IWideBoxDB wbDB = null;
		try {
			Registry registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
			wb = (IWideBox) registry.lookup("WideBoxServer");
			Registry registry2 = LocateRegistry.getRegistry(DB_IP, DB_PORT);
			wbDB = (IWideBoxDB) registry2.lookup("WideBoxDBServer");
		} catch(RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		requests = new AtomicInteger();
		
		System.out.println("LOAD GENERATOR STARTED");
		while(true) {
			System.out.println("Comando do tipo: ORIGIN,TARGET,OPERATION,RATE,DURATION");
			System.out.println("Comando----->");
			String command = sc.nextLine();
			String[] split = command.split(",");
			//db.printStatus(split[2]);
		
			
			if (split[0].equals("s") && 
					split[1].equals("s") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 1, split[4]);
				}
			}
			
			if (split[0].equals("s") && 
					split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 2, split[4]);
				}
			}
			
			if (split[0].equals("r") && 
					split[1].equals("s") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 3, split[4]);
				}
					
			}
			
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("q")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 4, split[4]);
				}
					
			}
			
			if (split[0].equals("s") && 
					split[1].equals("s") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 5, split[4]);
				}
					
			}
			
			if (split[0].equals("r") && 
					split[1].equals("s") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 6, split[4]);
				}
			}
			
			if (split[0].equals("s") && 
					split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) {
					//starOp(wb, wbDB, 7, split[4]);
				}
			}
			
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("p")) {
				
				if(split[3].equals(" ")) { 
					//starOp(wb, wbDB, 8, split[4]);
				
				}else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/500;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(wb, wbDB, 8, split[4], (int) nThreads + 1 ,  lag);
				}
			}
		}
		
	}
	
	public class Delay extends Thread {
		
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
	
	public void starOp(IWideBox wb, IWideBoxDB wbDB, int op, String duration, 
			int numThreads, double lag) {
		try {
				
			Message m = wb.search();
			AppServerRate app = new AppServerRate(wb);
			DbServerRate db = new DbServerRate(wbDB);
			List<Gen> list = new ArrayList<>();
			for(int i = 0; i < numThreads-1; i++) {
				Gen g = new Gen(wb, m, op);
				list.add(g);
				g.start();
			}
			Gen last = new Gen(wb, m, op);
			last.start();
			Delay d = new Delay(last, (int) lag * 1000);
			d.start();
			
			GenRate gr = new GenRate();
			app.start();
			db.start();
			gr.start();
			
			Thread.sleep(Integer.parseInt(duration) * 1000);
			app.kill();
			db.kill();
			gr.kill();
			for(int i = 0; i < numThreads; i++) {
				list.get(i).kill();
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}	catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	
	public class AppServerRate extends Thread {
		
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
	
	public class DbServerRate extends Thread {
		
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
	
	public class Gen extends Thread {
		
		private IWideBox wb;
		private volatile boolean keepGoing = true;
		private Message m;
		private int op;
		
		public Gen(IWideBox wb, Message m, int op) {
			this.wb = wb;
			this.m = m;
			this.op = op;
		}
		
		public void run() {
			Random rand = new Random();
			//Random rand2 = new Random()
			int clientId = 0;
			String theatre = null;
			List<String> st= null;
			Message m2 = null;
			Session ses = null;
			int value = 0;
			switch(op) {
			
			//SSQ
			case 1:
				clientId = rand.nextInt(NRCL);
				 theatre= m.getTheatres().get(rand.nextInt(
						m.getTheatres().size()));
				while(keepGoing) {
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
				
			//SRQ	
			case 2:
				clientId = rand.nextInt(NRCL);				
				while(keepGoing) {
					long t0 = System.nanoTime();
					theatre = m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
				
			//RSQ	
			case 3:
				 theatre= m.getTheatres().get(rand.nextInt(
						m.getTheatres().size()));
				while(keepGoing) {
					long t0 = System.nanoTime();
					clientId = rand.nextInt(NRCL);
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
			
			//RRQ
			case 4:
				while(keepGoing) {
					long t0 = System.nanoTime();
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
				
			//SSP	
			case 5:
				clientId = rand.nextInt(NRCL);
				theatre= m.getTheatres().get(rand.nextInt(
						m.getTheatres().size()));
				while(keepGoing) {
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
				
			//RSP
			case 6:
				theatre= m.getTheatres().get(rand.nextInt(
						m.getTheatres().size()));
				while(keepGoing) {
					long t0 = System.nanoTime();
					clientId = rand.nextInt(NRCL);
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
			
			//SRP
			case 7:
				clientId = rand.nextInt(NRCL);
				while(keepGoing) {
					long t0 = System.nanoTime();
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
				}
				break;
				
			//RRP	
			case 8:
				while(keepGoing) {
					long t0 = System.nanoTime();
					clientId = rand.nextInt(NRCL);
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
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
					System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
					requests.incrementAndGet();
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
					System.out.println("Load Generator processing " + (res2-res1)
						+ " req/sec ");
			}
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	

}
