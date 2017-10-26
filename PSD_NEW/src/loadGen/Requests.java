package loadGen;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

import server.IWideBox;
import server.Message;
import server.Session;

public class Requests {
	
	private IWideBox wb;
	
	public Requests(IWideBox wb) {
		this.wb = wb;
	}
	
	public void query(int client, String theatre) throws RemoteException {
		Message m2 = null;
		
		m2 = wb.seatsAvailable(client, theatre);
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.cancelSeat(m2.getSession());
		}
		
		else if(m2.getStatus().equals(Message.FULL)) {
			
		}
	}
	
	public void purchase(int client, String theatre, List<String> listTheatres) throws RemoteException {
		Message m2 = null;
		Session ses = null;
		int value = 0;
		Random rand = new Random();
		String curr = null;
		
		m2 = wb.seatsAvailable(Integer.toString(client), theatre);
		ses = m2.getSession();
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.acceptSeat(ses);
		}
		
		else if (m2.getStatus().equals(Message.FULL)) {
			value = rand.nextInt(listTheatres.size()+1);
			purchase(client, listTheatres.get(value), listTheatres);
		}
	}
	
	public void singleIdsingleTheatreQuery(int client, String theatre) {
		try {
			query(client, theatre);
		}
		catch (RemoteException e) {
			
		}
		
	}
	
	public void singleIdsingleTheatrePurchase(int client, String theatre) {
		try {
			purchase(client, theatre, wb.search().getTheatres());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void singleIdrandomTheatre(int client, int min, int max, String op) {
		try {
			Random rand = new Random();
			List<String> m = wb.search().getTheatres().subList(min-1, max);
			int size = m.size();
			int value = rand.nextInt(size+1);
		
			if (op.equals("QUERY"))
				query(client, m.get(value));
			
			else
				purchase(client, m.get(value), m);
			}
		catch (RemoteException e) {
				
		}
	}

}
