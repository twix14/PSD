package db;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import utilities.Status;

public interface IWideBoxDB extends Remote {
	

	public boolean put(String key, Status value, Status oldValue) throws RemoteException;

	public String get(String key) throws RemoteException;
	
	public List<String> listTheatres() throws RemoteException;
	
	public Status[][] listSeats(String theatre) throws RemoteException;
	
	public void printStatus(String theatre) throws RemoteException;

	public void crash() throws RemoteException;
	
	public int getRate() throws RemoteException;
	
	public void fullTheatre(String theatre) throws RemoteException;
}
