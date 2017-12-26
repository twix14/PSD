package db;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Status;

public interface IWideBoxDB extends Remote {
	

	public boolean put(String theatre, String key, Status value, Status oldValue) throws RemoteException;

	public String get(String key) throws RemoteException;
	
	public List<String> listTheatres() throws RemoteException;
	
	public ConcurrentHashMap<String,Status> listSeats(String theatre) throws RemoteException;
	
	public void printStatus(String theatre) throws RemoteException;

	public void crash() throws RemoteException;
	
	public void reset() throws RemoteException;
	
	public int getRate() throws RemoteException;
	
	public void fullTheatre(String theatre) throws RemoteException;
	
	public boolean ping() throws RemoteException;
	
	public boolean connectToSecondary(String ip, int port) throws RemoteException;
	public void updateSecondary (int numOfTheatresPerDB, int rangeMin, int rangeMax) throws RemoteException;

	public void newRange(int numOfTheatresPerDB, int rangeMin, int rangeMax, String[] split) throws RemoteException;

	public void sendValues(ConcurrentHashMap<String, ConcurrentHashMap<String, Status>> mapSend) throws RemoteException;
}
