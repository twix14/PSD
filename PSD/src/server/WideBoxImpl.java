package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
	ReentrantLock lock = new ReentrantLock();
	char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	WideBoxDB wideboxDBStub;
	
	public WideBoxImpl(WideBoxDB wideboxDBStub) throws RemoteException {
		this.wideboxDBStub = wideboxDBStub;
		
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
	
	public Message assignSeatAux(String theater, List<String> available, UUID id) throws RemoteException {
		//choose free seat at random
		Random rand = new Random();
		String seat = available.get(rand.nextInt(available.size()));
		String resp = wideboxDBStub.put(theater + "-" + 
				seat, Status.RESERVED);
		Message response = new Message(resp);
		Session sess = new Session(id);
		sess.addSeat(seat);
		response.setSession(sess);
		new TimeoutThread(theater + "-" + seat);
		return response;
	}
	
	/**
	 * Thread that after a timeout sees if the client as accepted a seat,
	 * and if not it frees up the seat
	 */
	public class TimeoutThread extends Thread {

		private String seat;
		
		public TimeoutThread(String seat) {
			this.seat = seat;
		}
		
		public void run() {
			//timeout of 30 seconds
			try {
				Thread.sleep(TIMEOUT);
				Status state = wideboxDBStub.get(seat);
				if(state.equals(Status.RESERVED)) {
					//change seat to free
					wideboxDBStub.delete(seat);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Can't connect to the DB");
				e.printStackTrace();
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
	public Message acceptSeat(int clientId) throws RemoteException {
		// TODO Auto-generated method stub
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

