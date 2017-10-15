package server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
	
	public static final String THEATRES = "theatres";
	private static final String AVAILABLE = "available";
	private static final String FULL = "theatres";
	private static final String ACCEPT_OK = "theatres";
	private static final String ACCEPT_ERROR = "theatres";
	private static final String CANCEL_OK = "theatres";
	private static final String CANCEL_ERROR = "theatres";
	
	private String status;
	private String reservedSeat;
	private int clientId;
	private List<Theatre> theatres;
	private Map<String, Integer> seats;
		
	public Message(String status) {
		this.status = status;
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

	public List<Theatre> getTheatres() {
		return theatres;
	}

	public void setTheatres(List<Theatre> theatres) {
		this.theatres = theatres;
	}

	public Map<String, Integer> getSeats() {
		return seats;
	}

	public void setSeats(Map<String, Integer> seats) {
		this.seats = seats;
	}

}
