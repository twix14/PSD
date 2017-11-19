package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import db.IWideBoxDB;
import utilities.Session;
import utilities.Status;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;
	private static final int TIMEOUT = 15000;

	private  ConcurrentHashMap<String, Long> sessions;
	private AtomicInteger requests;

	private volatile boolean down;

	ReentrantLock lock = new ReentrantLock();

	String alf = "abcdefghijklmnopqrstuvwxyz";

	char[] alphabet = alf.toUpperCase().toCharArray();

	IWideBoxDB wideboxDBStub;
	
	private int res1;

	public WideBoxImpl(IWideBoxDB db) throws RemoteException {
		this.wideboxDBStub = db;
		//ver params iniciais
		requests = new AtomicInteger();
		this.sessions = new ConcurrentHashMap<String,Long>();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		down = false;

		Runnable task = () -> {
			sessions.forEach((k, v) -> {
				try {
					long time = System.currentTimeMillis();
					if(v <= time) {
						String[] split = k.split("-");
						wideboxDBStub.put(split[1], split[2], Status.FREE, Status.RESERVED);
						sessions.remove(k);
						System.out.println("Timeout expired for seat " + split[2] + " in theatre " + split[1]);
					}
				} catch (RemoteException e) { }
			});
		};

		executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public Message search() throws RemoteException {
		if(!down) {
			Message m = new Message(Message.THEATRES);
			m.setTheatres(wideboxDBStub.listTheatres());
			requests.incrementAndGet();
			return m;
		} else
			throw new RemoteException("App Server down!");
	}

	@Override
	public Message seatsAvailable(int clientId, String theatre) throws RemoteException {
		if(!down) {
			String seat = null;
			boolean result = false;
			Message response = null;

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
			return response;
		} else
			throw new RemoteException("App Server down!");
	}

	@Override
	public Message acceptSeat(Session ses) throws RemoteException {
		if(!down) {
			Message m = null;
			boolean result = false;
			Long exists = null;
			exists = sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());

			if (exists != null) {
				result = wideboxDBStub.put(ses.getTheatre(), ses.getSeat(), Status.OCCUPIED, Status.RESERVED);

				if (result)
					m = new Message(Message.ACCEPT_OK);
				else
					m = new Message(Message.ACCEPT_ERROR);

			} else 
				m = new Message(Message.ACCEPT_ERROR);
			requests.incrementAndGet();
			return m;	
		} else
			throw new RemoteException("App Server down!");
	}

	@Override
	public Message reserveNewSeat(Session ses, String result) throws RemoteException {
		if(!down) {
			Long t = sessions.get(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());
			Message m = null;
			boolean res2 = false;

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

			} finally {
				lock.unlock();
			}
			requests.incrementAndGet();
			return m;
		} else
			throw new RemoteException("App Server down!");
	}

	@Override
	public Message cancelSeat(Session ses) throws RemoteException {
		if(!down) {
			Message m = null;
			boolean result = false;
			Long exists = null;
			exists = sessions.remove(Integer.toString(ses.getId()) + "-" + ses.getTheatre() + "-" + ses.getSeat());

			if (exists != null) {
				result = wideboxDBStub.put(ses.getTheatre(), ses.getSeat(), Status.FREE, Status.RESERVED);

				if (result)
					m = new Message(Message.CANCEL_OK);
				else
					m = new Message(Message.CANCEL_ERROR);

			} else 
				m = new Message(Message.CANCEL_ERROR);
			requests.incrementAndGet();
			return m;
		} else
			throw new RemoteException("App Server down!");
	}

	public void crash() throws RemoteException {
		down = true;
	} 

	public void reset() throws RemoteException {
		down = false;
	}
	
	public void startRate() throws RemoteException {
		res1 = requests.get();
	}

	public int getRate(int duration) throws RemoteException{
		if(!down) {
			int res2 = 0;
			res2 = requests.get();
			return  ((res2-res1)/duration);
		} else 
			throw new RemoteException("App Server down!");
	}

}

