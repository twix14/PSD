package server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.KeeperException;

import java.util.Collections;

import db.IWideBoxDB;
import javafx.util.Pair;
import utilities.Session;
import utilities.Status;
import zooKeeper.ZKClient;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;
	private static final int TIMEOUT = 15000;

	private  ConcurrentHashMap<String, Long> sessions;
	private AtomicInteger requests;

	ReentrantLock lock = new ReentrantLock();

	String alf = "abcdefghijklmnopqrstuvwxyz";

	char[] alphabet = alf.toUpperCase().toCharArray();

	//List with starting servers
	private List<Pair<IWideBoxDB, IWideBoxDB>> servers;
	private List<Pair<Boolean, Integer>> ups;

	private ZKClient zooKeeper;
	private int div;
	private int max;

	List<String> theatresList = new LinkedList<String>();
	public WideBoxImpl(ZKClient zooKeeper, BlockingQueue<String> queue, int numberOfTheatres) throws RemoteException {
		this.zooKeeper = zooKeeper;
		//ver params iniciais
		requests = new AtomicInteger(0);
		this.sessions = new ConcurrentHashMap<String,Long>();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		ExecutorService ex = Executors.newSingleThreadExecutor();

		//get all ips and remote appserver objects
		List<Pair<String, String>> ips = this.zooKeeper.getAllDBNodesPairs(true);
		servers = new LinkedList<>();
		ups = new LinkedList<>();

		/*
		 * The Round Robin algorithm is best for clusters 
		 * consisting of servers with identical specs
		 */
		int i = 0;

		for(Pair<String, String> ip: ips) {
			String[] splitPrim = ip.getKey().split(":");
			String[] splitSec = ip.getValue().split(":");
			Registry registryPrim = LocateRegistry.getRegistry(splitPrim[0],
					Integer.parseInt(splitPrim[1]));
			Registry registrySec = LocateRegistry.getRegistry(splitSec[0],
					Integer.parseInt(splitSec[1]));
			IWideBoxDB serverPrim = null;
			IWideBoxDB serverSec = null;
			try {
				serverPrim = (IWideBoxDB) registryPrim.lookup("WideBoxDBServer");
				serverSec = (IWideBoxDB) registrySec.lookup("WideBoxDBServer");
				servers.add(new Pair<IWideBoxDB,IWideBoxDB>(serverPrim, serverSec));
				ups.add(new Pair<Boolean, Integer>(true, i));
			} catch (NotBoundException e) {
				System.err.println("Problem connecting with DBServer"
						+ "with Ip:Port-" + ip);
				e.printStackTrace();
			}
			System.out.println("Connected to DBServer with Ip:Port-"
					+ ip);
			i++;
		}

		div = numberOfTheatres/servers.size();
		max = numberOfTheatres/servers.size() * ips.size();

		Runnable task2 = () -> {
			int j = 0;
			while(true) {
				if(!queue.isEmpty()) {
					String s = null;
					try {
						s = queue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					if (s.contains("down")){
						System.out.println("DBServer died, removing it from DBServers list...");
						String[] split = s.split(":");
						ups.set((int)Integer.valueOf(split[0]), new Pair<Boolean, Integer>(false, (int)Integer.valueOf(split[0])));
					}else {
						List<Pair<String, String>> dbs = zooKeeper.getAllDBNodesPairs(true);
						if(j % 2 != 0){

							Pair<String, String> ip = dbs.get(dbs.size()-1);

							try{
								String[] splitPrim = ip.getKey().split(":");
								String[] splitSec = ip.getValue().split(":");
								Registry registryPrim = LocateRegistry.getRegistry(splitPrim[0],
										Integer.parseInt(splitPrim[1]));
								Registry registrySec = LocateRegistry.getRegistry(splitSec[0],
										Integer.parseInt(splitSec[1]));
								IWideBoxDB serverPrim = null;
								IWideBoxDB serverSec = null;
								try {
									serverPrim = (IWideBoxDB) registryPrim.lookup("WideBoxDBServer");
									serverSec = (IWideBoxDB) registrySec.lookup("WideBoxDBServer");
									servers.add(new Pair<IWideBoxDB,IWideBoxDB>(serverPrim, serverSec));
									ups.add(new Pair<Boolean, Integer>(true, dbs.size()-1));
								} catch (NotBoundException e) {
									System.err.println("Problem connecting with DBServer"
											+ "with Ip:Port-" + ip);
									e.printStackTrace();
								}
								System.out.println("DBServer added Ip:Port-"
										+ ip);
							} catch (RemoteException e1) {
								e1.printStackTrace();
							}

							div = div/2;
						} 
						j++;
					}

				}
			}
		};
		
		ex.execute(task2);

		Runnable task = () -> {
			sessions.forEach((k, v) -> {
				int pos = 0;
				try {
					long time = System.currentTimeMillis();
					if(v <= time) {
						String[] split = k.split("-");
						IWideBoxDB wideboxDBStub = null;

						if((Integer.parseInt(split[1]) % div )== 0) {
							pos = (Integer.parseInt(split[1]) / div)-1;
							if(ups.get(pos).getKey())
								wideboxDBStub = servers.get(pos).getKey();
							else
								wideboxDBStub = servers.get(pos).getValue();

						} if(Integer.parseInt(split[1]) != max) {
							pos = (int) (Integer.parseInt(split[1]) / div);
							if(ups.get(pos).getKey())
								wideboxDBStub = servers.get(pos).getKey();
							else
								wideboxDBStub = servers.get(pos).getValue();
						} else {
							pos = servers.size()-1;
							if(ups.get(pos).getKey())
								wideboxDBStub = servers.get(pos).getKey();
							else
								wideboxDBStub = servers.get(pos).getValue();
						}
						pos = (int) (Integer.parseInt(split[1]) / div);
						if(ups.get(pos).getKey())
							wideboxDBStub = servers.get(pos).getKey();
						else
							wideboxDBStub = servers.get(pos).getValue();
						wideboxDBStub.put(split[1], split[2], Status.FREE, Status.RESERVED);
						sessions.remove(k);
						System.out.println("Timeout expired for seat " + split[2] + " in theatre " + split[1]);
					}
				} catch (RemoteException e) {
					System.err.println("Retrying connection...");
					ups.set(pos, new Pair<Boolean, Integer>(false, pos));

				}
			});
		};

		executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
		new Rate().start();
	}

	@Override
	public Message search() throws RemoteException {
		long start = System.currentTimeMillis();
		Message m = null;
		requests.incrementAndGet();
		m = new Message(Message.THEATRES);
		if (theatresList.isEmpty()) {

			int pos = 0;
			for(Pair<IWideBoxDB,IWideBoxDB> s : servers) {
				try {
					if(ups.get(pos).getKey())
						theatresList.addAll(s.getKey().listTheatres());
					else 
						theatresList.addAll(s.getValue().listTheatres());
				} catch (RemoteException e) {
					System.err.println("Retrying connection...");
					ups.set(pos, new Pair<Boolean, Integer>(false, pos));
					theatresList.addAll(s.getValue().listTheatres());
				}
				pos++;
			}			
		}
		m.setTheatres(theatresList);
		System.out.println("Demorou search " + (System.currentTimeMillis()-start));
		return m;
	}

	@Override
	public Message seatsAvailable(int clientId, String theatre) throws RemoteException {
		long start = System.currentTimeMillis();
		String seat = null;
		boolean result = false;
		Message response = null;

		IWideBoxDB wideboxDBStub = null;
		int pos = 0;

		if((Integer.parseInt(theatre) % div )== 0) {
			pos = (Integer.parseInt(theatre) / div)-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}else if(Integer.parseInt(theatre) != max) {	
			System.out.println("Dispatching theatre"+ theatre 
					+" to dbserver - " + (int) (Integer.parseInt(theatre) / div));
			pos = (int) (Integer.parseInt(theatre) / div);
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		} else {
			pos = servers.size()-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}

		try {

			seat = wideboxDBStub.get(theatre);
			if(seat != null) {
				requests.incrementAndGet();
				result = wideboxDBStub.put(theatre, seat, Status.RESERVED, Status.FREE);

				if (result) {
					response = new Message(Message.AVAILABLE);
					response.setSeats(wideboxDBStub.listSeats(theatre));

					Session sess = new Session(clientId);
					sess.setSeat(seat);
					sess.setTheatre(theatre);
					response.setSession(sess);
					sessions.put(Integer.toString(clientId) + "-" + theatre + "-" + seat,
							System.currentTimeMillis()+TIMEOUT);

				} else 
					response = new Message(Message.ACCEPT_ERROR);


			} else {
				requests.incrementAndGet();
				response =  new Message(Message.FULL);
			}

		} catch (RemoteException e) {
			System.err.println("Retrying connection...");
			ups.set(pos, new Pair<Boolean, Integer>(false, pos));
			return new Message("Retry");
		}
		System.out.println("Demorou seatsAvail " + (System.currentTimeMillis()-start));
		return response;
	}

	@Override
	public Message acceptSeat(Session ses) throws RemoteException {
		long start = System.currentTimeMillis();
		Message m = null;
		boolean result = false;
		Long exists = null;
		exists = sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());
		IWideBoxDB wideboxDBStub = null;
		int pos = 0;

		if((Integer.parseInt(ses.getTheatre()) % div )== 0) {
			pos = (Integer.parseInt(ses.getTheatre()) / div)-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}else if(Integer.parseInt(ses.getTheatre()) != max) {
			System.out.println("Dispatching theatre"+ ses.getTheatre() 
			+" to dbserver - " + (int) (Integer.parseInt(ses.getTheatre()) / div));
			pos = (int) (Integer.parseInt(ses.getTheatre()) / div);
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		} else {
			pos = servers.size()-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}
		if (exists != null) {
			try {
				result = wideboxDBStub.put(ses.getTheatre(), ses.getSeat(), Status.OCCUPIED, Status.RESERVED);
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");
				ups.set(pos, new Pair<Boolean, Integer>(false, pos));
				return new Message("Retry");
			}


			if (result)
				m = new Message(Message.ACCEPT_OK);
			else
				m = new Message(Message.ACCEPT_ERROR);

		} else 
			m = new Message(Message.ACCEPT_ERROR);
		requests.incrementAndGet();
		System.out.println("Demorou accept " + (System.currentTimeMillis()-start));
		return m;	
	}

	@Override
	public Message reserveNewSeat(Session ses, String result) throws RemoteException {
		long start = System.currentTimeMillis();
		Long t = sessions.get(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());
		Message m = null;
		boolean res2 = false;
		IWideBoxDB wideboxDBStub = null;
		int pos = 0;

		if((Integer.parseInt(ses.getTheatre()) % div )== 0) {
			pos = (Integer.parseInt(ses.getTheatre()) / div)-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}else if(Integer.parseInt(ses.getTheatre()) != max) {
			System.out.println("Dispatching theatre"+ ses.getTheatre() 
			+" to dbserver - " + (int) (Integer.parseInt(ses.getTheatre()) / div));
			pos = (int) (Integer.parseInt(ses.getTheatre()) / div);
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		} else {
			pos = servers.size()-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}
		lock.lock();
		try {

			if (t != null ) {
				res2 = wideboxDBStub.put(ses.getTheatre(), result, Status.RESERVED, Status.FREE);

				m = new Message(Message.AVAILABLE);
				if (res2) {
					wideboxDBStub.put(ses.getTheatre(),	ses.getSeat(), Status.FREE, Status.RESERVED);
					sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());
					sessions.put(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + result,
							System.currentTimeMillis()+TIMEOUT);
					m.setSeats(wideboxDBStub.listSeats(ses.getTheatre()));
					Session sess = new Session(ses.getId());
					sess.setSeat(result);
					sess.setTheatre(ses.getTheatre());
					m.setSession(sess);					
				} 
				else {
					sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());
					sessions.put(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat(),
							System.currentTimeMillis()+TIMEOUT);
					m.setSeats(wideboxDBStub.listSeats(ses.getTheatre()));
					Session sess = new Session(ses.getId());
					sess.setSeat(ses.getSeat());
					sess.setTheatre(ses.getTheatre());
					m.setSession(sess);
				}

			}
			else {
				m = new Message(Message.ACCEPT_ERROR);
			}

		} catch (RemoteException e) { 
			System.err.println("Retrying connection...");
			ups.set(pos, new Pair<Boolean, Integer>(false, pos));
			return new Message("Retry");
		} finally {
			lock.unlock();
		}
		System.out.println("Demorou new " + (System.currentTimeMillis()-start));
		requests.incrementAndGet();
		return m;
	}

	@Override
	public Message cancelSeat(Session ses) throws RemoteException {
		long start = System.currentTimeMillis();
		Message m = null;
		boolean result = false;
		Long exists = null;
		exists = sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());

		IWideBoxDB wideboxDBStub = null;
		int pos = 0;

		if((Integer.parseInt(ses.getTheatre()) % div )== 0) {
			pos = (Integer.parseInt(ses.getTheatre()) / div)-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}else if(Integer.parseInt(ses.getTheatre()) != max) {
			System.out.println("Dispatching theatre"+ ses.getTheatre() 
			+" to dbserver - " + (int) (Integer.parseInt(ses.getTheatre()) / div));
			pos = (int) (Integer.parseInt(ses.getTheatre()) / div);
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		} else {
			pos = servers.size()-1;
			if(ups.get(pos).getKey())
				wideboxDBStub = servers.get(pos).getKey();
			else
				wideboxDBStub = servers.get(pos).getValue();
		}
		if (exists != null) {
			long t0 = System.currentTimeMillis();
			try {
				result = wideboxDBStub.put(ses.getTheatre(), ses.getSeat(), Status.FREE, Status.RESERVED);
			} catch (RemoteException e) {
				System.err.println("Retrying connection...");
				ups.set(pos, new Pair<Boolean, Integer>(false, pos));
				return new Message("Retry");
			}
			System.out.println("Demorou DB " + (System.currentTimeMillis()-t0));

			if (result)
				m = new Message(Message.CANCEL_OK);
			else
				m = new Message(Message.CANCEL_ERROR);

		} else 
			m = new Message(Message.CANCEL_ERROR);
		requests.incrementAndGet();
		System.out.println("Demorou cancel " + (System.currentTimeMillis()-start));
		return m;
	}
	
	public class Rate extends Thread {

		public void run() {
			while(true) {
				try {
					int res1 = requests.get();
					Thread.sleep(1000);
					int res2 = 0;
					res2 = requests.get();
					System.out.println("Serving " + (res2-res1) + "req/sec");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

}

