package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWideBox extends Remote {
	
	/**
	 * List of theaters that exists
	 * @return
	 */
	public List<Theatre> search() throws RemoteException;
	
	/**
	 * Returns a list of seats available
	 * number of the seat - Available-1, Occupied-2, Reserved-3
	 * @return
	 */
	public List<String> seatsAvailable(int theatre) throws RemoteException;

	public boolean acceptSeat(int theatre, int seat) throws RemoteException;
	
	public boolean reserveSeat(int theatre, int seat) throws RemoteException;
	
	public boolean cancelSeat(int theatre, int seat) throws RemoteException;
	
}
