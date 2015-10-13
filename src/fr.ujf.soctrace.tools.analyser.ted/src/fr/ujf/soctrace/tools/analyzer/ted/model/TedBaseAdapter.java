/**
 * @author semteu
 *
 * 5 oct. 2015
 * TedBaseAdapter.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.iterators.EventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * @author semteu
 * 5 oct. 2015
 * TedBaseAdapter.java
 */
public abstract class TedBaseAdapter implements TedAdapter {

	/**
	 * Event iterator.
	 */
	private EventIterator iterator;
		
	/**
	 * Flag for TedEventType id <-> TedEventType mapping.
	 */
	private boolean mappingLoaded = false;
	
	/**
	 * TedEventType id <-> FrameSoC event instance id
	 */
	private Map<Integer, Integer> mapTedEventIdToEventId= new HashMap<Integer, Integer>();
	
	/**
	 * TedEvent id <-> TedEventType
	 */
	protected Map<Integer, TedEventType> mapTedEventIdToTedEventType = new HashMap<Integer, TedEventType>();	
	
	/**
	 * Trace DB Object 
	 */
	protected TraceDBObject traceDB;

	/*---------------------------------------------------------* 
	 * Abstract methods to be implemented in concrete classes
	 *---------------------------------------------------------*/

	protected abstract void loadMapping() throws SoCTraceException;

	@Override
	public abstract TedEvent adapt(Event e) throws SoCTraceException;

	/*----------------------------------------------------------*
	 * 
	 *----------------------------------------------------------*/
	
	/**
	 * Base constructor.
	 * @param iterator the iterator to be used to read events
	 */
	public TedBaseAdapter(TraceDBObject traceDB, EventIterator iterator) {
		this.traceDB = traceDB;
		this.iterator = iterator;
	}
	
	@Override
	public TedEvent getNext() throws SoCTraceException {
		Event e = iterator.getNext();
		if (e==null)
			return null;
		// manage mapping with event
		TedEvent tEvent = adapt(e);
		if (!mapTedEventIdToEventId.containsKey(tEvent.getEventType().getId()))
			mapTedEventIdToEventId.put(tEvent.getEventType().getId(), e.getId());
		return tEvent;
	}

	@Override
	public boolean hasNext() throws SoCTraceException {
		return iterator.hasNext();
	}

	@Override
	public void clear() throws SoCTraceException {
		iterator.clear();
		traceDB.close();
	}

	@Override
	public TedEventType getTedEventType(Event e) throws SoCTraceException {
		return adapt(e).getEventType();
	}
	
	@Override
	public Map<Integer, TedEventType> getMapping() throws SoCTraceException {
		if(!mappingLoaded) { // lazy initialization
			loadMapping();
			mappingLoaded = true;
		}
		return mapTedEventIdToTedEventType;
	}

	@Override
	public Map<Integer, Event> reverseMapping() throws SoCTraceException {
		// create query and reverse map (eid - fmid)
		Collection<TedEventType> tedEventTypes = getMapping().values();
		ValueListString vls = new ValueListString();
		Map<Integer, Integer> revMap = new HashMap<Integer, Integer>();
		for (TedEventType evType: tedEventTypes) {
			if (!mapTedEventIdToEventId.containsKey(evType.getId()))
				continue;
			Integer eid = mapTedEventIdToEventId.get(evType.getId());
			vls.addValue(String.valueOf(eid));
			revMap.put(eid, evType.getId());
		}
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN, vls.getValueString()));
		List<Event> elist = query.getList();
		// build return map
		Map<Integer, Event> rmap = new HashMap<Integer, Event>();
		for (Event e: elist) {
			rmap.put(revMap.get(e.getId()), e);
		}
		return rmap;
	}

}
