package db;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWideBoxDB extends Remote {
	

	public String put(String key, State Value) throws RemoteException;

	public State get(String key) throws RemoteException;

	public String delete(String key) throws RemoteException;
	
	public List<String> listTheatres() throws RemoteException, IOException;
	
	public State[][] listSeats(String theatre) throws RemoteException;
	
	
}
