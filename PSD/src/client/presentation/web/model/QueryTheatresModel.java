package client.presentation.web.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import db.State;
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
	private String result;
	private boolean hasTheatres;
	private List<String> theatres;
	private State[][] seats; 
	
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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean getHasTheatres() {
		return hasTheatres;
	}

	public void setHasTheatrtheatrees(boolean hasTheatres) {
		this.hasTheatres = hasTheatres;
	}

	public List<String> getTheatres () {
		return theatres;
	}
	
	public void setTheatres(List<String> ths) {
		this.theatres = ths;
	}
	
	public State[][] getSeats() {
		return seats;
	}

	public void setSeats(State[][] seats) {
		this.seats = seats;
	}

	public void clearFields() {
		movie = clientId = theatreId = reservedSeat = result = "";
		hasTheatres = false;
	}
	
}
