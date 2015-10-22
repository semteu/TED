/**
 * @author semteu
 *
 * 12 oct. 2015
 * KptraceTedIterator.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.PunctualEvent;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.iterators.PageEventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * @author semteu
 * 12 oct. 2015
 * KptraceTedIterator.java
 */
public class KPTraceTedIterator extends PageEventIterator {

	private Map<Integer, EventProducer> epMap = new HashMap<Integer, EventProducer>();
	private EventBuilder evBuilder;
	
	/**
	 * @param traceDB
	 * @throws SoCTraceException
	 */
	public KPTraceTedIterator(TraceDBObject traceDB) throws SoCTraceException {
		super(traceDB);
		EventProducerQuery epq = new EventProducerQuery(traceDB);
		List<EventProducer> epl = epq.getList();
		for(EventProducer ep: epl) {
			epMap.put(ep.getId(), ep);
		}
		evBuilder = new EventBuilder(traceDB);
	}
	
	@Override
	public Event getNext() throws SoCTraceException {
		checkValid();
		if (eIterator==null || !eIterator.hasNext()) {
			debug("load page " + nextPage);
			
			if (nextPage>MAX_PAGE) {
				clear();
				return null;
			}
			
			eIterator = null;
			if (eList!=null) {
				eList.clear();
				eList = null;
			}			
			eList = getEventsByPage(traceDB, nextPage);
			debug("loaded events: " + eList.size());
			eIterator = eList.iterator();
			nextPage++;
		}
//		return eIterator.next();
		Event e = eIterator.next();
//		switch(e.getCategory()){
//		case EventCategory.LINK:
//			System.out.println("Link -> " + e.getCategory() + " " + (Link) e + " " + ((Link)e).getEventProducer()
//					+ " " + ((Link)e).getEndProducer());
//			break;
//		case EventCategory.STATE:
//			System.out.println("State -> " + e.getCategory() + " " + (State) e + " " + ((State)e).getEventProducer()
//					+ " " +((State) e).getImbricationLevel());
//			break;
//		case EventCategory.VARIABLE:
//			System.out.println("Variable -> " + e.getCategory() + " " + (Variable) e + " " + ((Variable)e).getEventProducer()
//					+ " " + ((Variable) e).getValue());
//			break;
//		case EventCategory.PUNCTUAL_EVENT:
//			System.out.println("Punctual -> " + e.getCategory() + " " + (PunctualEvent)e);
//			
//		}
		return e;
	}

	
	/**
	 * Fill only the event id and the event type
	 * @param traceDB trace db
	 * @param i page
	 * @return a list of not completely filled events
	 * @throws SoCTraceException
	 */
	private List<Event> getEventsByPage(TraceDBObject traceDB, long i) throws SoCTraceException {
//		List<Event> list = new LinkedList<Event>();
		List<Event> eventList = null;
		try {
//			String query = "SELECT ID, TIMESTAMP, EVENT_TYPE_ID, EVENT_PRODUCER_ID FROM EVENT WHERE PAGE="+i+" ORDER BY TIMESTAMP ASC";
//			Map<Integer, IModelElement> etmap = traceDB.getEventTypeCache().getElementMap(EventType.class);
//			Statement stm = traceDB.getConnection().createStatement();
//			ResultSet rs = stm.executeQuery(query);
			
			
			String query = "SELECT * FROM EVENT WHERE PAGE=" + i + " ORDER BY TIMESTAMP ASC";
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			eventList = evBuilder.rebuildEvents(rs);
			
//			while (rs.next()) {
//				Integer id = rs.getInt(1);
//				Long ts = rs.getLong(2);
//				Integer tid = rs.getInt(3);
//				Integer pid = rs.getInt(4);
//				Event e = new Event(id);
//				EventType et = (EventType)etmap.get(tid); 
//				e.setCategory(et.getCategory());
//				e.setType(et);
//				e.setEventProducer(epMap.get(pid));
//				e.setTimestamp(ts);
//				list.add(e);
//			}
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return eventList;
	}

}
