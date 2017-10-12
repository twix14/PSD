package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;

	protected WideBoxImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Theatre> search() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> seatsAvailable(int theatre) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean acceptSeat(int theatre, int seat) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveSeat(int theatre, int seat) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelSeat(int theatre, int seat)throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
