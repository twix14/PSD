package zooKeeper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IZKClient extends Remote {
	
	public int createAppServerNode(String ip, String port) throws RemoteException;
	
	public List<String> getAllAppServerNodes() throws RemoteException;
	
	public int createDBNode(String ip, String port, int numberOfTheatres, int numberDBs) throws RemoteException;
	
	public List<String> getAllDBNodes() throws RemoteException;

}
