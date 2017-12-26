package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import utilities.Session;

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
	public Message seatsAvailable(int clientId, String theatre) throws RemoteException;

	public Message acceptSeat(Session ses) throws RemoteException;
	
	public Message reserveNewSeat(Session ses, String result) throws RemoteException;
	
	public Message cancelSeat(Session ses) throws RemoteException;
	
}
