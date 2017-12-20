package loadGen;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import db.IWideBoxDB;
import loadBal.ILoadBalancer;
import server.IWideBox;
import server.Message;
import utilities.Cache;
import utilities.Session;
import zooKeeper.ZKClient;

public class Generator {

	public static AtomicInteger rate;
	public static AtomicInteger duration;

	public static AtomicInteger requests;
	public static AtomicLong avglatency;

	public Cache cache;

	public List<ILoadBalancer> lbs;
	public ZKClient zk;

	private static final int ratePS = 165;

	private static final int NRCL = 100000;

	public static void main(String[] args) {
		new Generator(args);
	}

	public Generator(String[] args) {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		//podem ficar para ver o codigo para testar quantos pedidos estao a servir?

		ScheduledExecutorService es= null;

		//args[0] - zooKeeper IP

		Registry registry;
		BlockingQueue<WatchedEvent> events = new LinkedBlockingQueue<WatchedEvent>();
		try {
			try {
				zk = new ZKClient(args[0], events);
				System.out.println("Connected to ZooKeeper");
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			lbs = new LinkedList<>();

			List<String> lbsZK = zk.getAllLBNodes();
			for(String s : lbsZK){
				String[] split = s.split(":");
				registry = LocateRegistry.getRegistry(split[0],
						Integer.parseInt(split[1]));
				ILoadBalancer server = null;
				server = (ILoadBalancer) registry.lookup("LoadBalancer");
				lbs.add(server);
			}
		} catch (NumberFormatException | NotBoundException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ILoadBalancer lb = null;
		if(!lbs.isEmpty()) {
			lb = lbs.get(0);
		} else {
			System.out.println("No LoadBalancer online!");
			System.exit(0);
		}


		requests = new AtomicInteger();
		avglatency = new AtomicLong();
		es = Executors.newSingleThreadScheduledExecutor();

		Runnable task = () -> {
			while(true) {
				if(!events.isEmpty()) {
					WatchedEvent we = null;
					try {
						we = events.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					//LoadBalancer offline
					if(we.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
						System.out.println("LoadBalancer is offline, ZooKeeper timeout");
					} else if (we.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) { 
						//LoadBalancer back online, since the children
						List<String> lbNodes = zk.getAllLBNodes();
						if(lbNodes.size() == 2) {
							//node was added to children
							try {
								String s = lbNodes.get(1);
								String[] split = s.split(":");
								Registry registry2 = LocateRegistry.getRegistry(split[0],
										Integer.parseInt(split[1]));
								ILoadBalancer server = null;
								server = (ILoadBalancer) registry2.lookup("LoadBalancer");
								lbs.add(server);
							} catch (NumberFormatException | NotBoundException | RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} //else node was deleted from children

					}
				}
			}
		};

		es.execute(task);

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
			//new AllAppServersRate(duration, args).start();
			GenRate gr = new GenRate(Integer.parseInt(duration));

			Message m = null;
			try{
				m = lb.requestSearch();
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");

				if(lbs.contains(lb)) {
					lbs.remove(0);

				}

				try {
					lb = lbs.get(0);
				} catch (IndexOutOfBoundsException e2) {
					System.out.println("System is offline");
					System.exit(0);
				}

				try {
					m = lb.requestSearch();
				} catch (ConnectException e1) {
					System.out.println("System is offline");
					System.exit(0);
				}
			}

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
			//new AllAppServersRate(duration, args).start();
			GenRate gr = new GenRate(Integer.parseInt(duration));

			Message m = null;
			try{
				m = lb.requestSearch();
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");

				if(lbs.contains(lb)) {
					lbs.remove(0);

				}

				try {
					lb = lbs.get(0);
				} catch (IndexOutOfBoundsException e2) {
					System.out.println("System is offline");
					System.exit(0);
				}

				try {
					m = lb.requestSearch();
				} catch (ConnectException e1) {
					System.out.println("System is offline");
					System.exit(0);
				}

			}

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
					query(clientId, theatre, m2, false);
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

		public void purchase(int client, String theatre, List<String> listTheatres) {
			Message m2 = null;
			Session ses = null;
			int value = 0;
			Random rand = new Random();

			try{
				m2 = lb.requestSeatAvailable(client, theatre);
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");

				if(lbs.contains(lb)) {
					lbs.remove(0);

				}

				try {
					lb = lbs.get(0);
				} catch (IndexOutOfBoundsException e2) {
					if(lbs.size() == 0) {
						System.out.println("System is offline");
						System.exit(0);
					} else {
						purchase(client, theatre, listTheatres);
					}

				}

				try {
					m2 = lb.requestSeatAvailable(client, theatre);
				} catch (RemoteException e1) {
					System.out.println("System is offline");
					System.exit(0);
				}
			}

			ses = m2.getSession();

			IWideBox server = cache.get(m2.getServer());

			if(m2.getStatus().equals(Message.AVAILABLE)) {
				try {
					Message result = server.acceptSeat(ses);
					while(true) {
						if(result.getStatus().equals("Retry"))
							result = server.acceptSeat(ses);
						else 
							break;
					}
				} catch (RemoteException e) {
					System.out.println("AppServer offline, contacting the load balancer again");
					purchase(client, theatre, listTheatres);
				}
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
			//CONNECT WITH THE LOAD BALANCER AND WAIT FOR RESPONSE
			try{
				m2 = lb.requestSeatAvailable(clientId, theatre);
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");

				if(lbs.contains(lb)) {
					lbs.remove(0);

				}

				try {
					lb = lbs.get(0);
				} catch (IndexOutOfBoundsException e2) {
					if(lbs.size() == 0) {
						System.out.println("System is offline");
						System.exit(0);
					} else {
						query(clientId, theatre, m2, search);
					}


				}


				try {
					m2 = lb.requestSeatAvailable(clientId, theatre);
				} catch (RemoteException e1) {
					System.out.println("System is offline");
					System.exit(0);
				}
			}

			IWideBox server = cache.get(m2.getServer());

			if(m2.getStatus().equals(Message.AVAILABLE)) {
				//REQUEST GOEST DIRECTLY TO THE APP SERVER THAT IS ASSIGNED
				//TO THIS REQUEST!
				try {
					Message result = server.cancelSeat(m2.getSession());
					while(true) {
						if(result.getStatus().equals("Retry"))
							result = server.cancelSeat(m2.getSession());
						else 
							break;
					}
				} catch (RemoteException e) {
					System.out.println("AppServer offline, contacting the load balancer again");
					query(clientId, theatre, m2, search);
				}


			}

			else if(m2.getStatus().equals(Message.FULL)) {
			}

			else if(m2.getStatus().equals(Message.BUSY)) {
				System.out.println("The system is busy!");
			}
			long t1 = System.nanoTime();
			avglatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(t1-t0));
			requests.incrementAndGet();
		}

		private void purch(int clientId, String theatre, Message m2, 
				Session ses, Random rand, boolean search) {
			long t0 = System.nanoTime();
			//CONNECT WITH THE LOAD BALANCER AND WAIT FOR A RESPONSE
			try{
				m2 = lb.requestSeatAvailable(clientId, theatre);
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");

				if(lbs.contains(lb)) {
					lbs.remove(0);

				}

				try {
					lb = lbs.get(0);
				} catch (IndexOutOfBoundsException e2) {
					if(lbs.size() == 0) {
						System.out.println("System is offline");
						System.exit(0);
					} else {
						purch(clientId, theatre, m2, ses, rand, search);
					}

				}

				try {

					m2 = lb.requestSeatAvailable(clientId, theatre);
				} catch (RemoteException e1) {
					System.out.println("System is offline");
					System.exit(0);
				}
			}

			ses = m2.getSession();
			IWideBox server = cache.get(m2.getServer());

			//REQUEST GOEST DIRECTLY TO THE APP SERVER THAT IS ASSIGNED
			//TO THIS REQUEST!
			if(m2.getStatus().equals(Message.AVAILABLE)) {
				try {
					Message result = server.acceptSeat(ses);
					
					while(true) {
						if(result.getStatus().equals("Retry"))
							result = server.acceptSeat(ses);
						else 
							break;
					}
					
				} catch (RemoteException e) {
					System.out.println("AppServer offline, contacting the load balancer again");
					purch(clientId, theatre, m2, ses, rand, search);
				}
			}

			else if (m2.getStatus().equals(Message.FULL)) {
				purchase(clientId, m.getTheatres().get(rand.nextInt(
						m.getTheatres().size())), m.getTheatres());
			} 

			else if(m2.getStatus().equals(Message.BUSY)) {
				System.out.println("The system is busy!");
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

		private ZKClient zooKeeper;
		private String duration;

		public AllAppServersRate(String duration, String[] args) {
			this.duration = duration;
			try {
				zooKeeper = new ZKClient(args[0], new LinkedBlockingQueue<WatchedEvent>());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		}

		public void run() {
			List<String> ips = null;
			ips = zooKeeper.getAllAppServerNodes();
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