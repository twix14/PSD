package loadGen;
import java.rmi.RemoteException;
import java.util.Random;

import server.IWideBox;
import server.Message;
import server.Session;

public class Requests {
	
	private static IWideBox wb;
	
	public Requests(IWideBox wb) {
		this.wb = wb;
	}
	
	public Message search() throws RemoteException {
		return wb.search();	
	}
	
	public void query(String theatre) throws RemoteException {
		Message m = null;
		Message m2 = null;
		
		m = search();
		m2 = wb.seatsAvailable(theatre);
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.cancelSeat(m.getSession());
		}
		
		else if(m.getStatus().equals(Message.FULL)) {
			
		}
	}
	
	public void purchase(int client, String theatre) throws RemoteException {
		Message m = null;
		Message m2 = null;
		Session ses = null;
		int size = 0;
		int value = 0;
		Random rand = new Random();
		
		m = wb.search();
		size = m.getTheatres().size();
		
		//value = rand.nextInt(size);
		m2 = wb.seatsAvailable(Integer.toString(client));
		ses = m2.getSession();
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.acceptSeat(ses);
		}
		
		else if (m2.getStatus().equals(Message.FULL)) {
			purchase(client, );
		}
	}

}
