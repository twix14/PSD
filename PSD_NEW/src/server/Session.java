package server;

import java.util.UUID;

public class Session {
	
	private int id;
	private String seat;
	private String theatre;
	
	/**
	 * creates a session for the user with the id 
	 * @param id for the user
	 */
	public Session(int id) {
		this.setId(id);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTheatre() {
		return theatre;
	}

	public void setTheatre(String theatre) {
		this.theatre = theatre;
	}


	public String getSeat() {
		return seat;
	}


	public void setSeat(String seat) {
		this.seat = seat;
	}
	
}
