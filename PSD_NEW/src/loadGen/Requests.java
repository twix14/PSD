package loadGen;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

import db.IWideBoxDB;
import server.IWideBox;
import server.Message;
import utilities.Session;

public class Requests {
	
	private IWideBox wb;
	private IWideBoxDB db;
	
	public void setWideBox(IWideBox s, IWideBoxDB db) {
		wb = s;
		this.db = db;
	}
	
	public int getRateActualRateAppServerRequests() throws RemoteException {
		return wb.getRate();
	}
	
	public int getRateActualRateDBServerRequests() throws RemoteException {
		return db.getRate();
	}
	
	public static int query(IWideBox wb, int client, String theatre) throws RemoteException {
		Message m2 = null;
		
		m2 = wb.seatsAvailable(client, theatre);
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.cancelSeat(m2.getSession());
			return 0;
		}
		
		else if(m2.getStatus().equals(Message.FULL)) {
			return 0;
		}
		
		return 0;
	}
	
	public  void purchase(int client, String theatre, List<String> listTheatres) throws RemoteException {
		Message m2 = null;
		Session ses = null;
		int value = 0;
		Random rand = new Random();
		
		m2 = wb.seatsAvailable(client, theatre);
		ses = m2.getSession();
		
		if(m2.getStatus().equals(Message.AVAILABLE)) {
			wb.acceptSeat(ses);
		}
		
		else if (m2.getStatus().equals(Message.FULL)) {
			value = rand.nextInt(listTheatres.size()+1);
			purchase(client, listTheatres.get(value), listTheatres);
		}
	}
	
	public static int singleIdsingleTheatreQuery(IWideBox wb, int client, String theatre) {
		try {
			return query(wb, client, theatre);
		}
		catch (RemoteException e) {
			
		}
		return 0;
	}
	
	public void singleIdsingleTheatrePurchase(int client, String theatre) {
		try {
			purchase(client, theatre, wb.search().getTheatres());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
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
	}**/

}
