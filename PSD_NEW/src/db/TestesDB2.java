package db;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Status;

public class TestesDB2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			WideBoxDB db = new WideBoxDB();
			db.put("20", "A2", Status.OCCUPIED, Status.FREE);
			db.put("20", "A3", Status.OCCUPIED, Status.FREE);
			db.put("20", "A4", Status.OCCUPIED, Status.FREE);
			db.printStatus("20");
			ConcurrentHashMap<String,Status> d = db.listSeats("10");
			System.out.println(d.size());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
