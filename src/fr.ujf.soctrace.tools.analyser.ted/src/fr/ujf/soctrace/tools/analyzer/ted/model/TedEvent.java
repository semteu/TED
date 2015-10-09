/**
 * @author semteu
 *
 * 5 oct. 2015
 * TedEvent.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import fr.inria.soctrace.lib.model.Event;

/**
 * 
 * Ted Event structure
 * 
 * @author semteu
 * 5 oct. 2015
 * TedEvent.java
 */
public class TedEvent {
	
	private long timestamp;
	
	private TedEventType eventType;
	
	/**
	 * Link between a Ted event and a Framesoc event
	 */
	
	private Event event;
	
	
	public TedEvent(long timestamp, TedEventType eventType, Event event){
		this.timestamp = timestamp;
		this.eventType = eventType;
		this.event = event;
	}

	public TedEvent(long timestamp, TedEventType eventType){
		this.timestamp = timestamp;
		this.eventType = eventType;
		this.event = null;
	}



	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}


	public TedEventType getEventType() {
		return eventType;
	}


	public void setEventType(TedEventType eventType) {
		this.eventType = eventType;
	}


	public Event getEvent() {
		return event;
	}


	public void setEvent(Event event) {
		this.event = event;
	}
	
	@Override
	public String toString(){
		return "TedEvent [timestamp: " + timestamp +", eventType: "+ eventType +"]";
	}
	
	@Override
	public boolean equals(Object e){
		if(e.getClass() == this.getClass()){
			if(this.eventType.getId() == ((TedEvent)e).getEventType().getId())
				return true;
		}
		return false;
	}

}
