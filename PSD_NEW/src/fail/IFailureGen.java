package fail;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFailureGen extends Remote {
	
	public void crash(String type, String ip, int port) throws RemoteException;
	
	public void reset(String type, String ip, int port) throws RemoteException;
	
}
