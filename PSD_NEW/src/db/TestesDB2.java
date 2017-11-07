package db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Status;

public class TestesDB2 {

		public static void main(String[] args) {
			
			ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;
			ConcurrentHashMap<String, ConcurrentHashMap<String,Integer>> map2;
			String alf = "abcdefghijklmnopqrstuvwxyz";
			char[] alphabet = alf.toUpperCase().toCharArray();
			
			File fileHash = new File("theatres.txt");
			File fileHash2 = new File("theatres2.txt");
			
			   int NRTH = 300;
			   int NRRW = 26;
			   int NRCL = 40;
			   int NROPS = 100;
			
			map = new ConcurrentHashMap<String, ConcurrentHashMap<String,Status>>(NRTH);
			map2 = new ConcurrentHashMap<String, ConcurrentHashMap<String,Integer>>(NRTH);
			
			ConcurrentHashMap<String,Status> curr; 
			for(int k = 1; k <= NRTH; k++) {
				curr = new ConcurrentHashMap<String,Status>(NRCL * NRRW);
				for(int i = 0; i < NRRW; i++)
					for (int j = 1; j <= NRCL; j++) {
						curr.put(alphabet[i] + Integer.toString(j), Status.FREE);
					}
				map.put(Integer.toString(k), curr);
			}
			
			ConcurrentHashMap<String,Integer> curr1; 
			for(int k = 1; k <= NRTH; k++) {
				curr1 = new ConcurrentHashMap<String,Integer>(NRCL * NRRW);
				for(int i = 0; i < NRRW; i++)
					for (int j = 1; j <= NRCL; j++) {
						curr1.put(alphabet[i] + Integer.toString(j), 0);
					}
				map2.put(Integer.toString(k), curr1);
			}
			
			
			
			try {
				long t0 = System.currentTimeMillis();
		         FileOutputStream fileOut =
		         new FileOutputStream(fileHash);
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		         out.writeObject(map);
		         fileOut.flush();
		         fileOut.getFD().sync();
		         out.close();
		         fileOut.close();
		         System.out.println("Serialized data is saved in Theatres.txt");
			long t1 = System.currentTimeMillis();
			
			long t2 = System.currentTimeMillis();
		         FileOutputStream fileOut1 =
		         new FileOutputStream(fileHash2);
		         ObjectOutputStream out1 = new ObjectOutputStream(fileOut1);
		         out1.writeObject(map2);
		         fileOut1.flush();
		         fileOut1.getFD().sync();
		         out1.close();
		         fileOut1.close();
		         System.out.println("Serialized data is saved in Theatres2.txt");
		         long t3 = System.currentTimeMillis();
		         System.out.println("Primeiro "  + Long.toString(t1-t0));
		         System.out.println("Segundo "  + Long.toString(t3-t2));
		         System.out.println("Ya " + Long.toString((t1-t0)/(t3-t2)));
		      } catch (IOException i) {
		         i.printStackTrace();
		      }
			
			
			
			/*// TODO Auto-generated method stub
			try {
				WideBoxDB db = new WideBoxDB();
				db.put("20", "A2", Status.OCCUPIED, Status.FREE);
				db.put("20", "A3", Status.OCCUPIED, Status.FREE);
				db.put("20", "A4", Status.OCCUPIED, Status.FREE);
				db.printStatus("20");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
