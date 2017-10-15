package client.presentation.web.model;

import java.util.LinkedList;
import java.util.List;

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
	private int clientId;
	private boolean hasTheatres;
	private List<Theatre> theatres;
	
	public void setMovie(String movie) {
		this.movie = movie;	
	}
	
	public String getMovie() {
		return movie;
	}
	
	public int getClientId() {
		return clientId;
	}
	
	public void setClientId(int clientId) {
		this.clientId = clientId;
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
	
	public void clearFields() {
		movie = clientId = "";
	}
	
}
