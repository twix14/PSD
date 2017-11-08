package client.presentation.web.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import utilities.Status;

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
	private String seat;
	private String result;
	private boolean hasTheatres;
	private List<String> theatres;
	private ConcurrentHashMap<String, Status> seats;
	
	private String alf = "abcdefghijklmnopqrstuvwxyz";
	
	private char[] alphabet = alf.toUpperCase().toCharArray();
	
	
	public void setMovie(String movie) {
		this.movie = movie;	
	}
	
	public String getMovie() {
		return movie;
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
		Status[][] result = new Status[26][40];
		int coluna = 0;
		
		for(int i = 0; i < result.length; i++)
			for(int j = 0; j < result[i].length; j++) {
				char linha = alphabet[i];
				coluna = j+1;
				result[i][j] = seats.get(linha + Integer.toString(coluna));
				
			}
		return result;
	}

	public void setSeats(ConcurrentHashMap<String, Status> seats) {
		this.seats = seats;
	}

	public void clearFields() {
		movie = result = "";
		hasTheatres = false;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getTheatreId() {
		return theatreId;
	}

	public void setTheatreId(String theatreId) {
		this.theatreId = theatreId;
	}
	
}
