package server;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Session;
import utilities.Status;

public class Message implements Serializable {

	private static final long serialVersionUID = 121329610L;
	
	public static final String THEATRES = "theatres";
	public static final String AVAILABLE = "available";
	public static final String FULL = "full";
	public static final String ACCEPT_OK = "accept_ok";
	public static final String ACCEPT_ERROR = "accept_error";
	public static final String CANCEL_OK = "cancel_ok";
	public static final String CANCEL_ERROR = "cancel_error";
	public static final String BUSY = "busy";
	
	private String status;
	private List<String> theatres;
	private ConcurrentHashMap<String,Status> seats;
	private Session sess;
	private String server;
		
	public Message(String status) {
		this.status = status;
	}
	
	public Session getSession() {
		return sess;
	}

	public void setSession(Session sess) {
		this.sess = sess;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getTheatres() {
		return theatres;
	}

	public void setTheatres(List<String> theatres) {
		this.theatres = theatres;
	}

	public ConcurrentHashMap<String,Status> getSeats() {
		return seats;
	}

	public void setSeats(ConcurrentHashMap<String,Status> seats) {
		this.seats = seats;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}

