package server;

import java.io.Serializable;

public class Session implements Serializable {

	private static final long serialVersionUID = -4834965041546308787L;
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
