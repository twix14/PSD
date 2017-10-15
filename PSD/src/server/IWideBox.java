package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IWideBox extends Remote {
	
	/**
	 * List of theaters that exists
	 * @return
	 */
	public Message search() throws RemoteException;
	
	/**
	 * Returns a list of seats available
	 * number of the seat - Available-1, Occupied-2, Reserved-3
	 * @return
	 */
	public Message seatsAvailable(String theatre) throws RemoteException;

	public Message acceptSeat(String theatre, int seat) throws RemoteException;
	
	public Message reserveSeat(String theatre, int seat) throws RemoteException;
	
	public Message cancelSeat(String theatre, int seat) throws RemoteException;
	
}
