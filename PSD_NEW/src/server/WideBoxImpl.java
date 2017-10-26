package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import db.IWideBoxDB;
import utilities.Session;
import utilities.Status;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;
	private static final int TIMEOUT = 1500000;
	
	private  ConcurrentHashMap<String, TimeoutThread> sessions;
	
	ReentrantLock lock = new ReentrantLock();
	
	String alf = "abcdefghijklmnopqrstuvwxyz";
	
	char[] alphabet = alf.toUpperCase().toCharArray();

	IWideBoxDB wideboxDBStub;
	
	public WideBoxImpl(IWideBoxDB db) throws RemoteException {
		this.wideboxDBStub = db;
		//ver params iniciais
		this.sessions = new ConcurrentHashMap<String,TimeoutThread>();
	}

	//TO-DO ver quando da full
	@Override
	public Message search() throws RemoteException {
		Message m = new Message(Message.THEATRES);
		m.setTheatres(wideboxDBStub.listTheatres());
		
		return m;
	}
	
	@Override
	public Message seatsAvailable(int clientId, String theater) throws RemoteException {
		Status[][] seats = wideboxDBStub.listSeats(theater);
		List<String> available;
		
		if(!(available = getEmptySeats(seats)).isEmpty()) {
			return assignSeat(theater, available, clientId, seats);
		
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
	private Message assignSeat(String theater, List<String> available, int id, Status[][] seats) throws RemoteException {
			lock.lock();
			try {
				return assignSeatAux(theater, available, id, seats);
				
			} finally {
				lock.unlock();
			}
	}
	
	public Message assignSeatAux(String theater, List<String> available, int id, Status[][] seats) throws RemoteException {
		//choose free seat at random
		Random rand = new Random();
		boolean result = false;
		Message response = null;
		
		String seat = available.get(rand.nextInt(available.size()));
		result = wideboxDBStub.put(theater + "-" + 
				seat, Status.OCCUPIED, Status.FREE);
		
		if (result) {
			response = new Message(Message.AVAILABLE);
			seats[getCharacterIndex(seat.substring(0, 1))]
					[Integer.parseInt(seat.substring(1))] = Status.RESERVED;
			response.setSeats(seats);
			Session sess = new Session(id);
			sess.setSeat(seat);
			sess.setTheatre(theater);
			response.setSession(sess);
			
			TimeoutThread tt = new TimeoutThread(theater+ "-" + seat, id);
			tt.start();
			sessions.put(id + "-" + seat, tt);
			
		}
		else {
			response = new Message(Message.ACCEPT_ERROR);
		}
		
		
		return response;
	}
	
	private int getCharacterIndex(String c) {
		for(int i = 0; i < alphabet.length; i++) {
			if(c.equals(Character.toString(alphabet[i])))
				return i;				
		}
		return -1;
	}
	
	/**
	 * Thread that after a timeout sees if the client as accepted a seat,
	 * and if not it frees up the seat
	 */
	public class TimeoutThread extends Thread {

		private String tSeat;
		private int clientId;
		
		public TimeoutThread(String tSeat,int clientId) {
			this.tSeat = tSeat;
			this.clientId = clientId;
		}
		
		public void run() {
			//timeout of 15 seconds
			
			try {
				Thread.sleep(TIMEOUT);
				
				//Libertar lugar
				wideboxDBStub.put(tSeat, Status.FREE, Status.OCCUPIED);
				sessions.remove(clientId + "-" + tSeat);
			} catch (InterruptedException e) {
				System.out.println("Timeout to accept seat " + tSeat + 
						" from client " + clientId + " was terminated!");
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
			for(int j = 0; j < seats[i].length; j++) {
				if(seats[i][j].equals(Status.FREE))
					empty.add(new String(String.valueOf(alphabet[i])+ j));
			}
		
		return empty;
	}

	@Override
	public Message acceptSeat(Session ses) throws RemoteException {
		TimeoutThread t = sessions.get(ses.getId() + "-" + ses.getSeat());
		Message m = null;
		//try {
		if (t != null && !t.isInterrupted()) {
			t.interrupt();
			sessions.remove(ses.getId() + "-" + ses.getSeat());
			m = new Message(Message.ACCEPT_OK);
		}
		else
			m = new Message(Message.ACCEPT_ERROR);
		/*} catch (InterruptedException e) {
			
		}*/
		return m;
		/*try {
		 * t.interrupt();
		 * 
		 * }
		 * catch (Exception e) {
		 * m = new Message(Message.ACCEPT_OK);
		 * }
		 *return m; 
		 */
		
	}

	@Override
	public Message reserveNewSeat(Session ses, String result) throws RemoteException {
		TimeoutThread t = sessions.get(ses.getId() + "-" + ses.getSeat());
		Message m = null;
		boolean res2 = false;
		Status[][] seats = null;
		
		lock.lock();
		try {
			
			
			if (t != null && !t.isInterrupted()) {
				res2 = wideboxDBStub.put(ses.getTheatre() + "-" + 
						result, Status.OCCUPIED, Status.FREE);
				
				m = new Message(Message.AVAILABLE);
				if (res2) {
					wideboxDBStub.put(ses.getTheatre() + "-" + 
							ses.getSeat(), Status.FREE, Status.OCCUPIED);
					sessions.remove(ses.getId() + "-" + ses.getSeat());
					t.interrupt();
					TimeoutThread tt = new TimeoutThread(ses.getTheatre()+ "-" + result, ses.getId());
					sessions.put(ses.getId() + "-" + ses.getSeat(), tt);
					seats = wideboxDBStub.listSeats(ses.getTheatre());
					seats[getCharacterIndex(result.substring(0, 1))]
							[Integer.parseInt(result.substring(1))] = Status.RESERVED;
					m.setSeats(seats);
					Session sess = new Session(ses.getId());
					sess.setSeat(result);
					sess.setTheatre(ses.getTheatre());
					m.setSession(sess);					
				} 
				else {
					sessions.remove(ses.getId() + "-" + ses.getSeat());
					t.interrupt();
					TimeoutThread tt = new TimeoutThread(ses.getTheatre()+ "-" + ses.getSeat(), ses.getId());
					sessions.put(ses.getId() + "-" + ses.getSeat(), tt);
					seats = wideboxDBStub.listSeats(ses.getTheatre());
					seats[getCharacterIndex(result.substring(0, 1))]
							[Integer.parseInt(result.substring(1))] = Status.RESERVED;
					m.setSeats(seats);
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
		return m;
	}

	@Override
	public Message cancelSeat(Session ses) throws RemoteException {
		TimeoutThread t = sessions.get(ses.getId() + "-" + ses.getSeat());
		Message m = null;
		//try {
		if (t != null && !t.isInterrupted()) {
			t.interrupt();
			sessions.remove(ses.getId() + "-" + ses.getSeat());
			wideboxDBStub.put(ses.getTheatre() + "-" + ses.getSeat(), Status.FREE, Status.OCCUPIED);
			m = new Message(Message.CANCEL_OK);
		}
		else
			m = new Message(Message.CANCEL_ERROR);
		/*} catch (InterruptedException e) {
			
		}*/
		return m;
	}
	
	public void crash() throws RemoteException {
		System.exit(0);
		System.out.println("System crashed by failure generator");
	}

}

