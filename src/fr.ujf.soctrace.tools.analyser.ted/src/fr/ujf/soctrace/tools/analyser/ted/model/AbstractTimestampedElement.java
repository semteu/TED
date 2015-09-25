package fr.ujf.soctrace.tools.analyser.ted.model;

public abstract class AbstractTimestampedElement {
	
	private int timestamp;
	
	public int getTimestamp() {
		return timestamp;
	}
	
	public AbstractTimestampedElement(){
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public abstract Object getElement();
	
	@Override
	public abstract boolean equals(Object obj);
	

}
