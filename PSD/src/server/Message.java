package server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
	private String reservedSeat;
	private int clientId;
	private List<String> theatres;
	private String[][] seats;
	private Session sess;
		
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

	public String getReservedSeat() {
		return reservedSeat;
	}

	public void setReservedSeat(String reservedSeat) {
		this.reservedSeat = reservedSeat;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public List<String> getTheatres() {
		return theatres;
	}

	public void setTheatres(List<String> theatres) {
		this.theatres = theatres;
	}

	public String[][] getSeats() {
		return seats;
	}

	public void setSeats(String[][] seats) {
		this.seats = seats;
	}

}

