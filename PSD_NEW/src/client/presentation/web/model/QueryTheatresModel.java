package client.presentation.web.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import db.Status;
import server.Session;
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
	private Session session;
	private String result;
	private boolean hasTheatres;
	private List<String> theatres;
	private Status[][] seats; 
	
	public void setMovie(String movie) {
		this.movie = movie;	
	}
	
	public String getMovie() {
		return movie;
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
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

	public void setHasTheatres(boolean hasTheatres) {
		this.hasTheatres = hasTheatres;
	}

	public List<String> getTheatres () {
		return theatres;
	}
	
	public void setTheatres(List<String> ths) {
		this.theatres = ths;
	}
	
	public Status[][] getSeats() {
		return seats;
	}

	public void setSeats(Status[][] seats) {
		this.seats = seats;
	}

	public void clearFields() {
		movie = result = "";
		hasTheatres = false;
	}
	
}
