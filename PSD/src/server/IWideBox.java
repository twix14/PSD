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
	public List<String> seatsAvailable(String theatre) throws RemoteException;

	public boolean acceptSeat(String theatre, int seat) throws RemoteException;
	
	public boolean reserveSeat(String theatre, int seat) throws RemoteException;
	
	public boolean cancelSeat(String theatre, int seat) throws RemoteException;
	
}
