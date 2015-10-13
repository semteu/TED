/**
 * @author semteu
 *
 * 6 oct. 2015
 * GstreamerTedAdapter.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.iterators.EventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * @author semteu
 * 6 oct. 2015
 * GstreamerTedAdapter.java
 */
public class GStreamerTedAdapter extends TedBaseAdapter {
	
	/**
	 * 
	 */
	public GStreamerTedAdapter(TraceDBObject traceDB, EventIterator eventIterator) {
		super(traceDB, eventIterator);
	}

	/* (non-Javadoc)
	 * @see fr.ujf.soctrace.tools.analyzer.ted.model.TedBaseAdapter#loadMapping()
	 */
	@Override
	protected void loadMapping() throws SoCTraceException {
		
		Map<Integer, IModelElement> mapEventTypes = traceDB.getEventTypeCache().getElementMap(EventType.class);
		Iterator< Entry<Integer, IModelElement> > itMapEventTypes = mapEventTypes.entrySet().iterator();
		while(itMapEventTypes.hasNext()){
			Entry<Integer, IModelElement> entry = itMapEventTypes.next();
			Integer eventId = (Integer) entry.getKey();
			mapTedEventIdToTedEventType.put(eventId, new TedEventType(eventId, ((EventType)entry.getValue()).getName()));
		}
	}

	/* (non-Javadoc)
	 * @see fr.ujf.soctrace.tools.analyzer.ted.model.TedBaseAdapter#adapt(fr.inria.soctrace.lib.model.Event)
	 */
	@Override
	public TedEvent adapt(Event e) throws SoCTraceException {
		return new TedEvent(e.getTimestamp(), getMapping().get(e.getType().getId()), e);
	}

}
