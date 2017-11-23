package loadBal;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.Message;

public interface ILoadBalancer extends Remote {

	public Message requestSearch() throws RemoteException;
	
	public Message requestSeatAvailable(int clientId, String theatre) throws RemoteException;
	
}
