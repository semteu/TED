/**
 * @author semteu
 *
 * 5 oct. 2015
 * TEDAdapter.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * 
 * Ted Adapter Event
 * 
 * @author semteu
 * 5 oct. 2015
 * TEDAdapter.java
 */
public interface TedAdapter {

	/**
	 * Get the next Ted Event, or null if not present.
	 * 
	 * @return the next event, or null if no next event.
	 * @throws SoCTraceException
	 */
	public TedEvent getNext() throws SoCTraceException;
	
	/**
	 * @return true if there is a following Ted, false otherwise
	 * @throws SoCTraceException 
	 */
	public boolean hasNext() throws SoCTraceException;
	
	/**
	 * Release resources, if any. 
	 * After calling this function, the adapter
	 * cannot be used anymore.
	 * 
	 * @throws SoCTraceException 
	 */
	public void clear() throws SoCTraceException;

	/**
	 * Get the mapping between all the possible Ted event type and their
	 * corresponding ID. 
	 * You must call loadMapping before calling this method.
	 * 
	 * @return a map between Ted event type id and Ted event type
	 * @throws SoCTraceException 
	 */
	public Map<Integer, TedEventType> getMapping() throws SoCTraceException;

	/**
	 * Extract the Ted event from the information in the 
	 * FrameSoC event. Given an Event, there is only
	 * one Ted event corresponding to it.
	 * 
	 * @param e a FrameSoC event
	 * @return a Ted event ID
	 * @throws SoCTraceException 
	 */
	public TedEvent adapt(Event e) throws SoCTraceException;
	
	
	/**
	 * Extract the Ted event type id from the information in the 
	 * FrameSoC event. Given an Event, there is only
	 * one Ted event type corresponding to it.
	 * 
	 * @param e a FrameSoC event
	 * @return a Ted event type
	 * @throws SoCTraceException 
	 */
	public TedEventType getTedEventType(Event e) throws SoCTraceException;
		
	/**
	 * Builds a map where each Ted event id is associated with a FrameSoC 
	 * event instance. Note that a Ted event type may correspond to several events.
	 * Whatever instance among the corresponding ones is OK here. 
	 * 
	 * Note that there is a valid mapping with an event instance
	 * only for events actually found in the trace by getNext().
	 * 
	 * @param tedET TedEventType
	 * @return a corresponding event instance, of null if any
	 * @throws SoCTraceException 
	 */
	public Map<Integer, Event> reverseMapping() throws SoCTraceException;
	

}
