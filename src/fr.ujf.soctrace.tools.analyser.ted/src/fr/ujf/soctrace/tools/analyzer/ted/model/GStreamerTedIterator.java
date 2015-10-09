/**
 * @author semteu
 *
 * 6 oct. 2015
 * GstreamerTedIterator.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.iterators.PageEventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * @author semteu
 * 6 oct. 2015
 * GstreamerTedIterator.java
 */
public class GStreamerTedIterator extends PageEventIterator {
	
	public GStreamerTedIterator(TraceDBObject traceDB) throws SoCTraceException{
		super(traceDB);
	}
	
	@Override
	public Event getNext() throws SoCTraceException {
		checkValid();
		if(eIterator == null || !eIterator.hasNext()){

			debug("load page " + nextPage);
			
			if (nextPage > MAX_PAGE) {
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
		return eIterator.next();
	}

	
	/**
	 * Fill only the event id and the event type
	 * @param traceDB trace db
	 * @param i page
	 * @return a list of not completely filled events
	 * @throws SoCTraceException
	 */
	private List<Event> getEventsByPage(TraceDBObject traceDB, long i) throws SoCTraceException {
		List<Event> list = new LinkedList<Event>();
		try {
			String query = "SELECT ID, TIMESTAMP, EVENT_TYPE_ID FROM EVENT WHERE PAGE="+i+" ORDER BY TIMESTAMP ASC";
			Map<Integer, IModelElement> etmap = traceDB.getEventTypeCache().getElementMap(EventType.class);
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			while (rs.next()) {
				Integer id = rs.getInt(1);
				Long ts = rs.getLong(2);
				Integer tid = rs.getInt(3);
				Event e = new Event(id);
				EventType et = (EventType)etmap.get(tid); 
				e.setCategory(et.getCategory());
				e.setType(et);
				e.setTimestamp(ts);
				list.add(e);
			}
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return list;
	}

}
