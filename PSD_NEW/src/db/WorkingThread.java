package db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import utilities.Status;

public class WorkingThread implements Runnable{
	
	private File fileHash;
	private ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map;
	
	public WorkingThread(ConcurrentHashMap<String, ConcurrentHashMap<String,Status>> map, int num) {
		this.map = map;
		fileHash  = new File("theatres.txt");
	}
	
	@Override
	public void run() {
		try {
	        FileOutputStream fileOut = new FileOutputStream(fileHash);
	        Kryo kryo = new Kryo();
			kryo.register(ConcurrentHashMap.class, new MapSerializer());
	        final Output kryoOutput = new Output(fileOut);
			kryo.writeObject(kryoOutput, map);
	        kryoOutput.flush();
	        fileOut.flush();
	        fileOut.getFD().sync();
	        kryoOutput.close();

	        System.out.println("Serialized data is saved in "+ fileHash);
	     } catch (IOException i) {
	        //Thread.currentThread().run();
	     }
		
	}

}
