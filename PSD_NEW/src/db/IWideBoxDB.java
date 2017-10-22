package db;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWideBoxDB extends Remote {
	

	public String put(String key, Status Value) throws RemoteException;

	public Status get(String key) throws RemoteException;

	public String delete(String key) throws RemoteException;
	
	public List<String> listTheatres() throws RemoteException;
	
	public Status[][] listSeats(String theatre) throws RemoteException;
	
	
}
