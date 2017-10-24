package db;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWideBoxDB extends Remote {
	

	public boolean put(String key, Status value, Status oldValue) throws RemoteException;

	public String get(String key) throws RemoteException;

	public String delete(String key) throws RemoteException;
	
	public List<String> listTheatres() throws RemoteException;
	
	public Status[][] listSeats(String theatre) throws RemoteException;
	
	
}
