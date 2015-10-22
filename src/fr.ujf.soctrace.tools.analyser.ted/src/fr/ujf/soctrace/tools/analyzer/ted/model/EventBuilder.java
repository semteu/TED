/**
 * @author semteu
 *
 * 20 oct. 2015
 * EventBuilder.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * @author semteu
 * 20 oct. 2015
 * EventBuilder.java
 */
public class EventBuilder {
	
	private TraceDBObject traceDB;
	private ModelElementCache eventProducerCache;
	
	public EventBuilder(TraceDBObject traceDB){
		this.traceDB = traceDB; 
	}
	
	/**
	 * Rebuilds the events corresponding to the result set.
	 * @param rs Result set corresponding to a SELECT * FROM EVENT ...
	 * @return a list of Event
	 * @throws SoCTraceException
	 */
	public List<Event> rebuildEvents(ResultSet rs) throws SoCTraceException {

		List<Event> list = new LinkedList<Event>();
		
		try {		

			while (rs.next()) {
				Event e = rebuildEvent(rs);
				list.add(e);
			}
			return list;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	/**
	 * Rebuild an Event, given the corresponding EVENT table row.
	 * 
	 * @param rs EVENT table row
	 * @param epVls 
	 * @return the Event
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private Event rebuildEvent(ResultSet rs) throws SQLException, SoCTraceException {
		int category = rs.getInt(7);
		Event e = Event.createCategorizedEvent(category, rs.getInt(1));
		EventType et = traceDB.getEventTypeCache().get(EventType.class, rs.getInt(2));
		EventProducer s = getEventProducer(rs.getInt(3));
		e.setEventProducer(s); 
		e.setCategory(rs.getInt(7));
		e.setType(et);
		e.setTimestamp(rs.getLong(4));
		e.setCpu(rs.getInt(5));
		e.setPage(rs.getInt(6));
		e.setLongPar(rs.getLong(8));
		e.setDoublePar(rs.getDouble(9));
		if (e.getCategory() == EventCategory.LINK){
			((Link)e).setEndProducer(getEventProducer(((Double)e.getDoublePar()).intValue()));
		}
		return e;
	}
	
	protected EventProducer getEventProducer(int id) throws SoCTraceException {
		if (eventProducerCache==null) {
			eventProducerCache = new ModelElementCache();
			eventProducerCache.addElementMap(EventProducer.class);
		}

		EventProducer eventProducer;
		if (( eventProducer = eventProducerCache.get(EventProducer.class, id)) != null)
			return eventProducer;

		try {
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PRODUCER + " WHERE ID="+id);
			if (rs.next()) {
				eventProducer = new EventProducer(id);
				eventProducer.setType(rs.getString("TYPE"));
				eventProducer.setLocalId(rs.getString("LOCAL_ID"));
				eventProducer.setName(rs.getString("NAME"));
				eventProducer.setParentId(rs.getInt("PARENT_ID"));
				eventProducerCache.put(eventProducer);
				return eventProducer;
			}
			return null;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}	
	}
	

}
