package loadGen;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
					
					try {
						
						Message m = wb.search();
						Gen g = new Gen(wb, m);
						AppServerRate app = new AppServerRate(wb);
						DbServerRate db = new DbServerRate(wbDB);
						g.start();
						app.start();
						db.start();
						
						Thread.sleep(Integer.parseInt(split[4]) * 1000);
						g.kill();
						app.kill();
						db.kill();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}	catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			/*
			if (split[0].equals("single") && 
					split[1].equals("single") && split[2].equals("purchase")) {
				System.out.println("ClientId,TheatreId:");
				vals = sc.nextLine();
				split2 = command.split(",");
				singleIdsingleTheatrePurchase(split2[0], split2[1])
			}
			
			if (split[0].equals("single") && 
					split[1].equals("random") && split[2].equals("purchase")) {
				
			}**/
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
		
		public Gen(IWideBox wb, Message m) {
			this.wb = wb;
			this.m = m;
		}
		
		public void run() {
			Random rand = new Random();
			int clientId = rand.nextInt(NRCL);
			String theatre = m.getTheatres().get(rand.nextInt(
					m.getTheatres().size()));
			while(keepGoing) {
				long t0 = System.nanoTime();
				try {
					Message m2 = wb.seatsAvailable(clientId,theatre);
					
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
		}
		
		public void kill() {
			keepGoing = false;
		}
	}
	

}
