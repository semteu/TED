/**
 * @author semteu
 *
 * 12 oct. 2015
 * KptraceTedAdapter.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.iterators.EventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * @author semteu
 * 12 oct. 2015
 * KptraceTedAdapter.java
 */
public class KPTraceTedAdapter extends TedBaseAdapter {

	private IdManager idm = new IdManager();
	private Map<String, Integer> nameToId = new HashMap<String, Integer>();
	
	/**
	 * @param traceDB
	 * @param iterator
	 */
	public KPTraceTedAdapter(TraceDBObject traceDB, EventIterator iterator) {
		super(traceDB, iterator);
	}

	/* (non-Javadoc)
	 * @see fr.ujf.soctrace.tools.analyzer.ted.model.TedBaseAdapter#loadMapping()
	 */
	@Override
	protected void loadMapping() throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		int i = 0;
		Map<Integer, IModelElement> etmap = traceDB.getEventTypeCache().getElementMap(EventType.class);
		EventProducerQuery epq = new EventProducerQuery(traceDB);
		List<EventProducer> epl = epq.getList();
		
		Iterator<Entry<Integer, IModelElement>> it = etmap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, IModelElement> entry = it.next();
			for (EventProducer ep: epl) {
				String name = buildName(ep,(EventType)entry.getValue());
				if (nameToId.containsKey(name))
					continue; // already present
				int id = idm.getNextId();
				nameToId.put(name, id);
			mapTedEventIdToTedEventType.put(id, new TedEventType(id, name));	
				i++;
			}
		}
		dm.end("types: " + i);
	}

	/* (non-Javadoc)
	 * @see fr.ujf.soctrace.tools.analyzer.ted.model.TedBaseAdapter#adapt(fr.inria.soctrace.lib.model.Event)
	 */
	@Override
	public TedEvent adapt(Event e) throws SoCTraceException {
		getMapping(); // ensure mappings are loaded
		String name = buildName(e.getEventProducer(), e.getType());
		// DRO 30/01/2014 - FMEvent include a reference to the event use to generate them 
		return new TedEvent(e.getTimestamp(), mapTedEventIdToTedEventType.get(nameToId.get(name)), e);
	}
	
	private String buildName(EventProducer ep, EventType et) {
		return ep.getName() + " : " + et.getName();
	}
	

}
