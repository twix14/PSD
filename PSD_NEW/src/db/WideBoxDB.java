package db;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


public class WideBoxDB implements IWideBoxDB {

	private HashMap<String, Status> map;

	private static final String PUT_OK = "put_ok";
	private static final String PUT_OCC = "lugar ja esta ocupado";
	private static final String PUT_RES = "lugar ja esta reservado";
	private static final String DELETE_OK = "delete_ok";
	private static final String DELETE_NOT_OK = "delete_not_ok";

	public String put(String key, Status value) throws RemoteException {

		if(map.get(key).equals(Status.OCCUPIED)) {
			return PUT_OCC;
		} else if (map.get(key).equals(Status.RESERVED) && value.equals(Status.RESERVED)) {
			return PUT_RES;
		} else if (map.get(key).equals(Status.RESERVED) && value.equals(Status.OCCUPIED)) {
			map.remove(key);
			map.put(key, value);
			try {
				BufferedWriter bw = new BufferedWriter (new FileWriter ("log.txt"));
				bw.write("put " + key + " " + value.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return PUT_OK;
			//TODO TENHO DUVIDAS NESTE PQ NAO SEI SE É POSSIVEL
		} else if(map.get(key).equals(Status.RESERVED) && value.equals(Status.FREE)) {
			map.remove(key);
			map.put(key, value);
			try {
				BufferedWriter bw = new BufferedWriter (new FileWriter ("log.txt"));
				bw.write("put " + key + " " + value.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PUT_OK;
		} else if(map.get(key).equals(Status.FREE) && value.equals(Status.RESERVED)) {
			map.remove(key);
			map.put(key, value);
			try {
				BufferedWriter bw = new BufferedWriter (new FileWriter ("log.txt"));
				bw.write("put " + key + " " + value.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PUT_OK;
		} else if(map.get(key).equals(Status.FREE) && value.equals(Status.FREE)) {
			return PUT_OK;
		} else {
			//When the seat is free, and you are changing it to occupied
			map.remove(key);
			map.put(key, value);
			try {
				BufferedWriter bw = new BufferedWriter (new FileWriter ("log.txt"));
				bw.write("put " + key + " " + value.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PUT_OK;
		}
	}

	//TODO NAO SEI SE VAI BUSCAR AO FICHEIRO OU NAO
	public Status get(String key) throws RemoteException {
		/*  //read from file 
	    try{
	        File toRead=new File("Seats.txt");
	        FileInputStream fis=new FileInputStream(toRead);
	        ObjectInputStream ois=new ObjectInputStream(fis);

	        Map<String,State> mapInFile=(HashMap<String,State>)ois.readObject();

	        ois.close();
	        fis.close();

	        //print All data in MAP
	        //for(Map.Entry<String,String> m :mapInFile.entrySet()){
	           // System.out.println(m.getKey()+" : "+m.getValue());
	        }
	        return mapInFile;
	    }catch(Exception e){}
	    return null;*/

		if(map.containsKey(key)) {
			return map.get(key);
		} else
			return null;
	}

	public String delete(String key) throws RemoteException {
		if (map.get(key) != null) {
			map.remove(key);
			map.put(key, Status.FREE);
			try {
				BufferedWriter bw = new BufferedWriter (new FileWriter ("log.txt"));
				bw.write("delete " + key);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return DELETE_OK;
		} else
			return DELETE_NOT_OK;
	}

	public List<String> listTheatres() {
		List<String> teatros = new ArrayList<String>();
		try {
		BufferedReader br = new BufferedReader(new FileReader("Teatros.txt"));
		String l = br.readLine();
		
		while(l!=null) {
			teatros.add(l);
			l = br.readLine();
		}
		br.close();
		} catch (IOException e) {
			
		}
		return teatros;
	}

	public Status[][] listSeats(String theatre) throws RemoteException {
		Status [][] listSeats = new Status[26][40];
		int coluna = 0;
		for(int i = 0; i < 26; i++)
			for (int j = 0; j < 40; j++) {
				char linha = getCharLine(i);
				coluna = j+1;
				listSeats[i][j] = map.get(theatre+"-"+linha+coluna);
			}

		return listSeats;

	}

	@SuppressWarnings("unused")
	private void loadDB () {
		this.map = new HashMap<String,Status>();
		int coluna = 0;
		for(int k = 1; k <= 1500; k++) {
			for(int i = 0; i < 26; i++)
				for (int j = 0; j < 40; j++) {
					char linha = getCharLine(i);
					coluna = j+1;
					map.put("T"+k+"-"+linha+coluna, Status.FREE);
				}
		}
	}


	private char getCharLine(int i) {
		char character = 'A';
		switch(i) {
		case 0 :
			character = 'A';
			break;
		case 1 :
			character = 'B';
			break;
		case 2 :
			character = 'C';
			break;
		case 3 :
			character = 'D';
			break;
		case 4 :
			character = 'E';
			break;		   
		case 5 :
			character = 'F';
			break;
		case 6 :
			character = 'G';
			break;		   
		case 7 :
			character = 'H';
			break;
		case 8 :
			character = 'I';
			break;   
		case 9 :
			character = 'J';
			break;
		case 10 :
			character = 'K';
			break;	   
		case 11 :
			character = 'L';
			break;
		case 12 :
			character = 'M';
			break; 
		case 13 :
			character = 'N';
			break;
		case 14 :
			character = 'O';
			break;	   
		case 15 :
			character = 'P';
			break;
		case 16 :
			character = 'Q';
			break;		   
		case 17 :
			character = 'R';
			break;
		case 18 :
			character = 'S';
			break;   
		case 19 :
			character = 'T';
			break;
		case 20 :
			character = 'U';
			break;
		case 21 :
			character = 'V';
			break;
		case 22 :
			character = 'W';
			break;   
		case 23 :
			character = 'X';
			break;
		case 24 :
			character = 'Y';
			break;
		case 25 :
			character = 'Z';
			break;
		}
		return character;
	}


}
