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
import loadBal.ILoadBalancer;
import server.IWideBox;
import server.Message;
import utilities.Cache;
import utilities.Session;
import zooKeeper.IZKClient;

public class Generator {

	public static AtomicInteger rate;
	public static AtomicInteger duration;

	public static AtomicInteger requests;
	public static AtomicLong avglatency;
	
	public Cache cache;

	private static final int ratePS = 165;

	private static final int NRCL = 100000;
	
	public static void main(String[] args) {
		new Generator(args);
	}

	public Generator(String[] args) {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		//podem ficar para ver o codigo para testar quantos pedidos estao a servir?

		ILoadBalancer lb = null;
		ExecutorService es= null;

		//args[0] - loadBalancer IP
		//args[1] - loadBalancer Port
		//args[2] - zooKeeper IP
		//args[3] - zooKeeper Port

		try {
			Registry reg = LocateRegistry.getRegistry(args[0], 
					Integer.parseInt(args[1]));
			lb = (ILoadBalancer) reg.lookup("LoadBalancer");
			
			/**Registry registry = LocateRegistry.getRegistry("127.0.0.1", 5004);
			wb = (IWideBox) registry.lookup("WideBoxServer");
			Registry registry2 = LocateRegistry.getRegistry(DB_IP, DB_PORT);
			wbDB = (IWideBoxDB) registry2.lookup("WideBoxDBServer");
			 */
		} catch(RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		requests = new AtomicInteger();
		avglatency = new AtomicLong();
		es = Executors.newCachedThreadPool();
		
		cache = new Cache();

		System.out.println("LOAD GENERATOR STARTED");
		while(true) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Comando do tipo: ORIGIN,TARGET,OPERATION,RATE,DURATION");
			System.out.println("Comando----->");
			String command = sc.nextLine();
			String[] split = command.split(",");

			//SSQ
			if (!split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("q")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 1, split[4], Integer.parseInt(split[0]), split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 1, split[4], args, (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), split[1], es);
				}
			}

			//SRQ
			if (!split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("q")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 2, split[4], Integer.parseInt(split[0]), null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 2, split[4], args, (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), null, es);
				}
			}

			//RSQ
			if (split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("q")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 3, split[4], 0, split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 3, split[4], args, (int) nThreads + 1 ,  lag, 0, split[1], es);
				}

			}

			//RRQ
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("q")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 4, split[4], 0, null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 4, split[4], args, (int) nThreads + 1 ,  lag, 0, null, es);
				}

			}

			//SSP
			if (!split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("p")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 5, split[4], Integer.parseInt(split[0]), split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 5, split[4], args, (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), split[1], es);
				}

			}

			//RSP
			if (split[0].equals("r") && 
					!split[1].equals("r") && split[2].equals("p")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 6, split[4], 0, split[1], es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 6, split[4], args, (int) nThreads + 1 ,  lag, 0, split[1], es);
				}
			}

			//SRP
			if (!split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("p")) {

				if(split[3].equals(" ")) {
					starOpWithoutRate(lb, args, 7, split[4], Integer.parseInt(split[0]), null, es);
				} else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 7, split[4], args, (int) nThreads + 1 ,  lag, Integer.parseInt(split[0]), null, es);
				}
			}

			//RRP
			if (split[0].equals("r") && 
					split[1].equals("r") && split[2].equals("p")) {

				if(split[3].equals(" ")) { 
					starOpWithoutRate(lb, args, 8, split[4], 0, null, es);

				}else {
					int n = Integer.parseInt(split[3]);
					double nThreads = n/ratePS;
					//em media 500, disparar 1 thread para medir o valor medio
					double lag = Math.abs(nThreads - ((int) nThreads + 1));
					starOp(lb, 8, split[4], args, (int) nThreads + 1 ,  lag, 0, null, es);
				}
			}
		}

	}

	public void starOpWithoutRate(ILoadBalancer lb, String[] args,
			int op, String duration, int clientId, String theatre, ExecutorService es) {
		try {
			new AllAppServersRate(duration, args).start();
			GenRate gr = new GenRate(Integer.parseInt(duration));

			Message m = lb.requestSearch();
			Gen g = new Gen(lb, m, op, clientId, theatre);
			//es.execute(g);
			g.start();

			
			//es.execute(app);
			//es.execute(db);
			//es.execute(gr);
			gr.start();

			Thread.sleep(Integer.parseInt(duration) * 1000);
			//app.kill();
			//db.kill();
			g.kill();
		} catch (NumberFormatException | InterruptedException | RemoteException e) {
			e.printStackTrace();
		} 
	}

	public void starOp(ILoadBalancer lb, int op, String duration,  String[] args,
			int numThreads, double lag, int clientId, String theatre, ExecutorService es) {
		try {
			new AllAppServersRate(duration, args).start();
			GenRate gr = new GenRate(Integer.parseInt(duration));

			Message m = lb.requestSearch();
			List<Gen> list = new ArrayList<>();
			for(int i = 0; i < numThreads-1; i++) {
				Gen g = new Gen(lb, m, op, clientId, theatre);
				list.add(g);
				g.start();
				//es.execute(g);
			}
			if(!(lag == 0)) {
				Gen last = new Gen(lb, m, op, clientId, theatre);
				//es.execute(last);
				last.start();
				Delay d = new Delay(last, (int) lag * 1000);
				//es.execute(d);
				d.start();
			}

			
			//es.execute(app);
			//es.execute(db);
			gr.start();
			//es.execute(gr);

			Thread.sleep(Integer.parseInt(duration) * 1000);
			//app.kill();
			//db.kill();
			for(int i = 0; i < numThreads-1; i++) {
				list.get(i).kill();
			}
		} catch (NumberFormatException | InterruptedException | RemoteException e) {
			e.printStackTrace();
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

	public class Gen extends Thread {
		private ILoadBalancer lb;
		private volatile boolean keepGoing = true;
		private Message m;
		private int op;
		private int clientId;
		private String theatre;
		private int i;

		public Gen(ILoadBalancer lb, Message m, int op, 
				int clientId, String theatre) {
			this.lb = lb;
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
			i = 0;
			switch(op) {
				
			//SSQ
			case 1:
				while(keepGoing) {
					
					query(clientId, theatre, m2, false);
				}
				break;

				//SRQ	
			case 2:			
				while(keepGoing) {
					theatre = m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));

					query(clientId, theatre, m2, true);
				}
				break;

				//RSQ	
			case 3:
				while(keepGoing) {
					clientId = rand.nextInt(NRCL);
					query(clientId, theatre, m2, false);i++;
				}
				break;

				//RRQ
			case 4:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);

					query(clientId, theatre, m2, true);
				}
				break;

				//SSP	
			case 5:
				while(keepGoing) {
					purch(clientId, theatre, m2, ses, rand, false);
				}
				break;

				//RSP
			case 6:
				while(keepGoing) {
					clientId = rand.nextInt(NRCL);
					purch(clientId, theatre, m2, ses, rand, false);
				}
				break;

				//SRP
			case 7:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					purch(clientId, theatre, m2, ses, rand, true);
				}
				break;

				//RRP	
			case 8:
				while(keepGoing) {
					theatre= m.getTheatres().get(rand.nextInt(
							m.getTheatres().size()));
					clientId = rand.nextInt(NRCL);
					purch(clientId, theatre, m2, ses, rand, true);
				}
				break;
			}

		}

		public void purchase(int client, String theatre, List<String> listTheatres) throws RemoteException {
			Message m2 = null;
			Session ses = null;
			int value = 0;
			Random rand = new Random();

			m2 = lb.requestSeatAvailable(client, theatre);
			ses = m2.getSession();
			
			IWideBox server = cache.get(m2.getServer());

			if(m2.getStatus().equals(Message.AVAILABLE)) {
				server.acceptSeat(ses);
			}

			else if (m2.getStatus().equals(Message.FULL)) {
				value = rand.nextInt(listTheatres.size()+1);
				purchase(client, listTheatres.get(value), listTheatres);
			}

			else if(m2.getStatus().equals(Message.BUSY)) {
				System.out.println("The system is busy!");
			}
		}

		private void query (int clientId, String theatre, Message m2, boolean search) {
			long t0 = System.nanoTime();
			try {
				//CONNECT WITH THE LOAD BALANCER AND WAIT FOR RESPONSE
				m2 = lb.requestSeatAvailable(clientId,theatre);
				long t2 = System.nanoTime();
				System.out.println("Latency at the loadBalancer the time - " + TimeUnit.NANOSECONDS.toMillis(t2-t0));
				IWideBox server = cache.get(m2.getServer());
				
				if(m2.getStatus().equals(Message.AVAILABLE)) {
					//REQUEST GOEST DIRECTLY TO THE APP SERVER THAT IS ASSIGNED
					//TO THIS REQUEST!
					server.cancelSeat(m2.getSession());
					
				}

				else if(m2.getStatus().equals(Message.FULL)) {
				}

				else if(m2.getStatus().equals(Message.BUSY)) {
					System.out.println("The system is busy!");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			long t1 = System.nanoTime();
			System.out.println("Latency at the time - " + TimeUnit.NANOSECONDS.toMillis(t1-t0));
			avglatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(t1-t0));
			requests.incrementAndGet();
		}

		private void purch(int clientId, String theatre, Message m2, 
				Session ses, Random rand, boolean search) {
			long t0 = System.nanoTime();
			try {
				//CONNECT WITH THE LOAD BALANCER AND WAIT FOR A RESPONSE
				m2 = lb.requestSeatAvailable(clientId, theatre);
				ses = m2.getSession();
				IWideBox server = cache.get(m2.getServer());

				//REQUEST GOEST DIRECTLY TO THE APP SERVER THAT IS ASSIGNED
				//TO THIS REQUEST!
				if(m2.getStatus().equals(Message.AVAILABLE)) {
					server.acceptSeat(ses);
				}

				else if (m2.getStatus().equals(Message.FULL)) {
					purchase(clientId, m.getTheatres().get(rand.nextInt(
							m.getTheatres().size())), m.getTheatres());
				} 

				else if(m2.getStatus().equals(Message.BUSY)) {
					System.out.println("The system is busy!");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			long t1 = System.nanoTime();
			System.out.println("Latency at the time - " + TimeUnit.NANOSECONDS.toMillis(t1-t0));
			avglatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(t1-t0));
			requests.incrementAndGet();
		}

		public void kill() {
			keepGoing = false;
		}
	}

	public class GenRate extends Thread {

		private int duration;

		public GenRate(int duration) {
			this.duration = duration;
		}

		public void run() {
			int res1 = requests.get();
			try {
				Thread.sleep(duration * 999);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int res2 = requests.get();
			long lat = avglatency.get();
			System.out.println("Lat = " + lat);
			System.out.println("Avg latency - " + lat/((res2-res1)+1));
			avglatency.set(0);
			System.out.println("Load Generator rate - " + (res2-res1));

		}
	}

	public class GetRateServer extends Thread{

		private IWideBox server;
		private int duration;
		private int id;

		public GetRateServer(int id, IWideBox server, int duration) {
			this.server = server;
			this.duration = duration;
			this.id = id;
		}

		public void run() {

			try {
				server.startRate();
				Thread.sleep(duration * 1000);
				int rate = 0;
				try {
					rate = server.getRate(duration);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				System.out.println("App server-" + id + ", serving " + rate
						+ " req/sec ");

			} catch (InterruptedException | RemoteException e) {
				e.printStackTrace();
			}

		}

	}

	public class AllAppServersRate extends Thread {

		private IZKClient zooKeeper;
		private String duration;

		public AllAppServersRate(String duration, String[] args) {
			this.duration = duration;
			Registry registry;
			try {
				registry = LocateRegistry.getRegistry(args[2], 
						Integer.parseInt(args[3]));
				zooKeeper = (IZKClient) registry.lookup("ZooKeeperServer");
			} catch (NumberFormatException | NotBoundException | RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void run() {
			List<String> ips = null;
			try {
				ips = zooKeeper.getAllAppServerNodes();
			} catch (RemoteException e1) {
				System.err.println("Problems connecting to ZooKeeper");
				e1.printStackTrace();
			}
			int id = 1;
			for(String ip: ips) {
				String[] split = ip.split(":");

				try {
					IWideBox server = null;
					Registry registry = LocateRegistry.getRegistry(split[0],
							Integer.parseInt(split[1]));
					server = (IWideBox) registry.lookup("WideBoxServer");
					new GetRateServer(id, server, Integer.parseInt(this.duration)).start();
				} catch (NotBoundException e) {
					System.err.println("Problem connecting with AppServer"
							+ "with Ip:Port-" + ip);
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				id++;
			}

		}
	}

	public class DbServerRate implements Runnable {

		private IWideBoxDB wbDB;
		private volatile boolean keepGoing = true;

		public DbServerRate(String duration, String[] args) {
			
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


}