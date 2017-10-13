package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;

import db.WideBoxDB;

public class WideBoxImpl extends UnicastRemoteObject implements IWideBox {

	private static final long serialVersionUID = 240458129728788662L;

	WideBoxDB wideboxDBStub;

	protected WideBoxDB getWideBoxDB() {
		
		/**
		 * REVER SITIO ONDE COLOCAR ESTE METODO, EM VEZ DE FAZER AQUI
		 * FAZER NO MAIN ASSIM QUE O SERVIDOR ARRANCA
		 */
		
		//hardcoded A REVER
		String ipDB = "127.0.0.1";
		int port = 1717;
		
		try {
			Registry registry = LocateRegistry.getRegistry(ipDB, port);
			return (WideBoxDB) registry.lookup("WideBoxDB");
		} catch (Exception e) {
			System.err.println("Error in Impl");
			return null;
		}
	}
	
	protected WideBoxImpl() throws RemoteException {
	}
	
	private void getDBStub() throws RemoteException {
		if (wideboxDBStub == null) {
		      wideboxDBStub = getWideBoxDB();
		      if (wideboxDBStub == null) 
		        throw new RemoteException("Unavailable");
		}
	}

	@Override
	public List<Theatre> search() throws RemoteException {
		getDBStub();
		
		/**
		 * Idealmente so enviaria os nomes dos cinemas, para evitar
		 * enviar todos os assentos que todos os cinemas têm 
		 */
		return wideboxDBStub.getTheatres();
	}
	
	@Override
	public List<String> seatsAvailable(String theatre) throws RemoteException {
		getWideBoxDB();
		List<String> seats = wideboxDBStub.getAvailableSeats(theatre);
		
		if(!seats.isEmpty()) {
			Random rand = new Random();
			int seat =  rand.nextInt(seats.size());
			/**
			 * TIMEOUT DO RESERVED SEAT PASSADO X TEMPO SE UTILIZADOR NAO ACEITAR
			 * O LUGAR
			 */
			wideboxDBStub.reserveSeat(theatre, seat);
			seats.set(seat, "RESERVED");
		}
		
		return seats;
	}

	@Override
	public boolean acceptSeat(String theatre, int seat) throws RemoteException {
		getWideBoxDB();
		
		

		return false;
	}

	@Override
	public boolean reserveSeat(String theatre, int seat) throws RemoteException {
		// TODO Auto-generated method stub
		
		/**
		 * EH PRECISO VER O TIMEOUT DE UMA RESERVA
		 */
		return false;
	}

	@Override
	public boolean cancelSeat(String theatre, int seat)throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
