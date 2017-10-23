package server;

import java.util.UUID;

public class Session {
	
	private UUID id;
	private String seat;
	private String theatre;
	
	/**
	 * creates a session for the user with the id 
	 * @param id for the user
	 */
	public Session(UUID id) {
		this.setId(id);
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
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
