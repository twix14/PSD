package db;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;

public class WorkingThread implements Runnable{
	
	private FileChannel TheatreChannel;
	
	
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
	        System.out.println("Serialized data is saved in Theatres.txt");
	     } catch (IOException i) {
	        i.printStackTrace();
	     }
		
	}

}
