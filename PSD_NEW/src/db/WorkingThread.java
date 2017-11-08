package db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Status;

public class WorkingThread implements Runnable{
	
	private File fileHash;
	private ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;
	
	public WorkingThread(ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map, int num) {
		this.map = map;
		fileHash  = new File("theatres" + num + ".txt");
	}
	
	@Override
	public void run() {
		try {
	        FileOutputStream fileOut =
	        new FileOutputStream(fileHash);
	        ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(map);
	        fileOut.flush();
	        fileOut.getFD().sync();
	        out.close();
	        fileOut.close();

	        System.out.println("Serialized data is saved in "+ fileHash);
	     } catch (IOException i) {
	        //Thread.currentThread().run();
	     }
		
	}

}
