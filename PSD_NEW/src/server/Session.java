package server;

import java.util.UUID;

public class Session {
	
	private UUID id;
	private String seat;
	private String theatre;
	//timeout thread id
	private long threadId;
	//position of the session on the sessions array
	private int pos;
	
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

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

}
