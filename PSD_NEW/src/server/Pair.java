package server;

public class Pair<T1, T2> {
	
	private T1 a;
	private T2 b;
	
	public Pair(T1 a, T2 b) {
		this.a = a;
		this.b = b;
	}
	
	public void setSt(T1 a) {
		this.a = a;
	}
	
	public void setNd(T2 b) {
		this.b = b;
	}
	
	public T1 getSt(T1 a) {
		return a;
	}
	
	public T2 getNd(T2 b) {
		return b;
	}

}