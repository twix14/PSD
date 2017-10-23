package server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import db.*;

import db.WideBoxDB;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;
	private static final int TIMEOUT = 15000;
	
	List<Pair<Session, Integer>> clientIds;
	private List<Session> sessions;
	
	ReentrantLock lock = new ReentrantLock();
	ReentrantLock lockReserved = new ReentrantLock();
	
	char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();

	IWideBoxDB wideboxDBStub;
	
	public WideBoxImpl(IWideBoxDB db) throws RemoteException {
		this.wideboxDBStub = db;
		this.sessions = new ArrayList<Session>();
	}

	@Override
	public Message search() throws RemoteException {
		Message m = new Message(Message.THEATRES);
		m.setTheatres(wideboxDBStub.listTheatres());
		
		return m;
	}
	
	@Override
	public Message seatsAvailable(String theater) throws RemoteException {
		Status[][] seats = wideboxDBStub.listSeats(theater);
		List<String> available;
		
		if(!(available = getEmptySeats(seats)).isEmpty()) {
		
			//chave CINEMA-XYY value
			//X de A-Z e YY de 1-40
			
			//client id
			/**
			 * A VER A UTILIDADE DO ID
			 * ALMOST IMPOSSIBLE TO HAVE THE SAME ID
			 */
			UUID id = UUID.randomUUID();
			
			return assignSeat(theater, available, id);
		
		} else {
			return new Message(Message.FULL);
		}
	}

	/**
	 * Like this you can only assign one seat for any theater, at a time
	 * REVIEW, COULD BE A PERFORMANCE ISSUE
	 * @param theater
	 * @throws RemoteException 
	 */
	private Message assignSeat(String theater, List<String> available, UUID id) throws RemoteException {
		if(available.size() < 10) {
		
			lock.lock();
			try {
				return assignSeatAux(theater, available, id);
				
			} finally {
				lock.unlock();
			}
		
		} else 
			return assignSeatAux(theater, available, id);
	}
	
	//TODO fazer o caso de dar erro
	public Message assignSeatAux(String theater, List<String> available, UUID id) throws RemoteException {
		//choose free seat at random
		Random rand = new Random();
		boolean result = false;
		Message response = null;
		
		String seat = available.get(rand.nextInt(available.size()));
		result = wideboxDBStub.put(theater + "-" + 
				seat, Status.OCCUPIED, Status.FREE);
		
		if (result) {
			response = new Message(Message.AVAILABLE);
			Session sess = new Session(id);
			sess.setSeat(seat);
			sess.setTheatre(theater);
			response.setSession(sess);
			lockReserved.lock();
			int pos;
			try {
				sessions.add(sess);
				pos = sessions.size()-1;
			} finally {
				lockReserved.unlock();
			}
			TimeoutThread tt = new TimeoutThread(sess);
			lockReserved.lock();
			try{
				Session s = sessions.get(pos);
				s.setThreadId(tt.getId());
				sessions.set(pos, s);
			} finally {
				lockReserved.unlock();
			}
			
		}
		else {
			response = new Message(Message.AVAILABLE);
		}
		
		
		return response;
	}
	
	/**
	 * Thread that after a timeout sees if the client as accepted a seat,
	 * and if not it frees up the seat
	 */
	public class TimeoutThread extends Thread {

		private Session ses;
		
		public TimeoutThread(Session ses) {
			this.ses = ses;
		}
		
		public void run() {
			//timeout of 30 seconds
			
			try {
				Thread.sleep(TIMEOUT);
				
				//Libertar lugar
				wideboxDBStub.put(ses.getTheatre() + "-" + ses.getSeat(), Status.FREE, Status.OCCUPIED);
				lockReserved.lock();
				sessions.remove(ses);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Can't connect to the DB");
				e.printStackTrace();
			} finally {
				lockReserved.unlock();
			}
			
			return;
		}
		
	}
	
	/**
	 * chave CINEMA-XYY value ... X de A-Z e YY de 1-40
	 * @param seats
	 * @return
	 */
	private List<String> getEmptySeats(Status[][] seats){
		List<String> empty = new LinkedList<>();
		
		for(int i = 0; i < seats.length; i++)
			for(int j = 0; i < seats[i].length; j++) {
				if(seats[i][j].equals(Status.FREE))
					empty.add(new String(String.valueOf(alphabet[i]) + j));
			}
		
		return empty;
	}

	@Override
	public Message acceptSeat(Session sesId) throws RemoteException {
		sesId.getThreadId()
		return null;
	}

	@Override
	public Message reserveNewSeat(int clientId, String seat) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message cancelSeat(String seat) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}

