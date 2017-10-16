package client.presentation.web.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.Theatre;

//import facade.dto.DiscountDTO;
//import facade.exceptions.ApplicationException;
//import facade.handlers.ICustomerServiceRemote;

/**
 * Helper class to assist in the response of novo cliente.
 * This class is the response information expert.
 * 
 * @author fmartins
 *
 */
public class QueryTheatresModel extends Model {

	private String movie;
	private String clientId;
	private String theatreId;
	private String reservedSeat;
	private boolean hasTheatres;
	private List<Theatre> theatres;
	private Map<String, Integer> seats; 
	
	public void setMovie(String movie) {
		this.movie = movie;	
	}
	
	public String getMovie() {
		return movie;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	

	public String getTheatreId() {
		return theatreId;
	}

	public void setTheatreId(String theatreId) {
		this.theatreId = theatreId;
	}

	public String getReservedSeat() {
		return reservedSeat;
	}

	public void setReservedSeat(String reservedSeat) {
		this.reservedSeat = reservedSeat;
	}

	public boolean getHasTheatres() {
		return hasTheatres;
	}

	public void setHasTheatres(boolean hasTheatres) {
		this.hasTheatres = hasTheatres;
	}

	public Iterable<Theatre> getTheatres () {
		return theatres;
	}
	
	public void setTheatres(List<Theatre> ths) {
		this.theatres = ths;
	}
	
	public Map<String, Integer> getSeats() {
		return seats;
	}

	public void setSeats(Map<String, Integer> seats) {
		this.seats = seats;
	}

	public void clearFields() {
		movie = clientId = theatreId = "";
		hasTheatres = false;
	}
	
}
