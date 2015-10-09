/**
 * @author semteu
 *
 * 6 oct. 2015
 * TedProcessor.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput.Operation;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedStatus;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedAdapter;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedEvent;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedEventType;

/**
 * @author semteu
 * 6 oct. 2015
 * TedProcessor.java
 */
public class TedProcessor {

	private TedInput tedInput = null;
	
	private Distances distanceProcessor = null;
	
	Map<Integer, Float> distanceResults = new HashMap<Integer, Float>();
	
	Map<Integer, Integer> mapEventId2OccTrace1 = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapEventId2OccTrace2 = new HashMap<Integer, Integer>();
	
	List<TedEvent> eventTrace1 = new ArrayList<TedEvent>();
	
	List<TedEvent> eventTrace2 = new ArrayList<TedEvent>();
	
	public TedProcessor(TedInput input){
		tedInput = input;
		distanceProcessor = new Distances();
	}
	
	/*
	 * This method construct the list of events in a trace ordered by timestamp
	 */
	private void loadEventLists(){
		mapEventId2OccTrace1.clear();
		mapEventId2OccTrace2.clear();
		
		TedAdapter adapter1 = tedInput.refAdapter;
		TedAdapter adapter2 = tedInput.diagAdapter;
		TedEvent event =  null;
		try{
			while(adapter1.hasNext()){
				event = adapter1.getNext();
				eventTrace1.add(event);
				Integer value = mapEventId2OccTrace1.get(event.getEventType().getId());
				if(value != null){
					mapEventId2OccTrace1.put(event.getEventType().getId(), value + 1);
				}
				else{
					mapEventId2OccTrace1.put(event.getEventType().getId(), 1);
				}
			}
			while(adapter2.hasNext()){
				event = adapter2.getNext();
				eventTrace2.add(event);
				Integer value = mapEventId2OccTrace2.get(event.getEventType().getId());
				if(value != null){
					mapEventId2OccTrace2.put(event.getEventType().getId(), value + 1);
				}
				else{
					mapEventId2OccTrace2.put(event.getEventType().getId(), 1);
				}
			}
		}
		catch(SoCTraceException e){
			e.printStackTrace();
		}
	}
	
	
	private void loadEventListsForTest(List<TedEvent> trace1, List<TedEvent> trace2){
		mapEventId2OccTrace1.clear();
		mapEventId2OccTrace2.clear();

		for(TedEvent e: trace1){
			Integer value = mapEventId2OccTrace1.get(e.getEventType().getId());
			if(value != null){
				mapEventId2OccTrace1.put(e.getEventType().getId(), value + 1);
			}
			else{
				mapEventId2OccTrace1.put(e.getEventType().getId(), 1);
			}
		}
		for(TedEvent e: trace2){
			Integer value = mapEventId2OccTrace2.get(e.getEventType().getId());
			if(value != null){
				mapEventId2OccTrace2.put(e.getEventType().getId(), value + 1);
			}
			else{
				mapEventId2OccTrace2.put(e.getEventType().getId(), 1);
			}
		}
	}
	
		
	public TedStatus run(IProgressMonitor monitor){
		
		System.out.println("ProcessorRunning ...");
		run_tests();
		return TedStatus.RUN_OK;
		
//		loadEventLists();
//		
//		switch (tedInput.operation) {
//		
//		case OCCURRENCE_DISTANCE:
//			System.out.println("Executing occurence distance ...");
//			Map<Integer, Float> results = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
//					mapEventId2OccTrace1, mapEventId2OccTrace2);
//			for(Integer key : results.keySet()){
//				System.out.println("key: " + key +" distance: " + results.get(key));
//			}
//			break;
//			
//		case DROPPING_DISTANCE:
//			System.out.println("Executing occurence distance ...");
//			Set<Integer> droppingResults = distanceProcessor.droppingDistance(tedInput.threshold, 
//					mapEventId2OccTrace1, mapEventId2OccTrace2);
//			System.out.println(droppingResults);
//			System.out.println("Dropping distance: " + droppingResults.size());
//			
//		default:
//			System.out.println("Operation not yet implemented !");
//			break;
//		}
//		
//		
//		return TedStatus.RUN_OK;
		
	}
	
	public void run_tests(){
		
		//Setting data
		List<TedEvent> trace1 = new ArrayList<TedEvent>();
		List<TedEvent> trace2 = new ArrayList<TedEvent>();
		Map<Integer, Float> occDistResults = null;
		Set<Integer> dropDistResults = null;
		List< ArrayList <Double> > temporalDistances = null;
		
		
		trace1.add(new TedEvent(2, new TedEventType(1, "It")));
		trace1.add(new TedEvent(3, new TedEventType(2, "CS")));
		trace1.add(new TedEvent(4, new TedEventType(1, "It")));
		trace1.add(new TedEvent(6, new TedEventType(1, "It")));
		trace1.add(new TedEvent(8, new TedEventType(1, "It")));
		
		trace2.add(new TedEvent(2, new TedEventType(1, "It")));
		trace2.add(new TedEvent(3, new TedEventType(2, "CS")));
		trace2.add(new TedEvent(4, new TedEventType(1, "It")));
		trace2.add(new TedEvent(5, new TedEventType(2, "CS")));
		trace2.add(new TedEvent(6, new TedEventType(1, "It")));
		trace2.add(new TedEvent(8, new TedEventType(2, "CS")));
		
		loadEventListsForTest(trace1, trace2);
		
		System.out.println("Comparison between: ");
		System.out.println(trace1);
		System.out.println("and");
		System.out.println(trace2);
		System.out.println("---");
		System.out.println(mapEventId2OccTrace1);
		System.out.println(mapEventId2OccTrace2);
		
		occDistResults = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println("Occurrence distance results :");
		for(Map.Entry<Integer, Float> e : occDistResults.entrySet()){
			System.out.print(e + " ");
		}
		
		
		System.out.println("");
		System.out.println("Dropping distance results :");
		dropDistResults = distanceProcessor.droppingDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		for(Integer i : dropDistResults)
			System.out.print(i +" ");
		
		System.out.println("");
		
		
		System.out.println("Temporal distance results :");
		temporalDistances = distanceProcessor.temporalDistance(trace1, trace2, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println(temporalDistances);
		
		System.out.println("Temporal distance: " + temporalDistances.get(trace1.size()).get(trace2.size()));
		temporalDistances.clear();
		
		System.out.println("---");
		
		trace1.clear();
		trace2.clear();
		
		//Second Test !!!
		
		trace1.add(new TedEvent(2, new TedEventType(1, "X")));
		trace1.add(new TedEvent(3, new TedEventType(2, "CS")));
		trace1.add(new TedEvent(4, new TedEventType(3, "It")));
		trace1.add(new TedEvent(6, new TedEventType(4, "E")));
		trace1.add(new TedEvent(8, new TedEventType(3, "It")));
		
		trace2.add(new TedEvent(2, new TedEventType(3, "It")));
		trace2.add(new TedEvent(3, new TedEventType(5, "U")));
		trace2.add(new TedEvent(4, new TedEventType(3, "It")));
		trace2.add(new TedEvent(5, new TedEventType(2, "CS")));
		trace2.add(new TedEvent(6, new TedEventType(3, "It")));
		trace2.add(new TedEvent(8, new TedEventType(2, "CS")));
		
		loadEventListsForTest(trace1, trace2);
		
		System.out.println("Comparison between: ");
		System.out.println("trace1: " + trace1);
		System.out.println("and");
		System.out.println("trace2: " + trace2);
		System.out.println("---");
		System.out.println(mapEventId2OccTrace1);
		System.out.println(mapEventId2OccTrace2);
		
		occDistResults = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println("Occurrence distance results :");
		for(Map.Entry<Integer, Float> e : occDistResults.entrySet()){
			System.out.print(e + " ");
		}
		System.out.println("");
		System.out.println("Dropping distance results :");
		dropDistResults = distanceProcessor.droppingDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		for(Integer i : dropDistResults)
			System.out.print(i +" ");
		
		System.out.println("");
		
		System.out.println("Temporal distance results :");
		temporalDistances = distanceProcessor.temporalDistance(trace1, trace2, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println(temporalDistances);
		
		System.out.println("Temporal distance: " + temporalDistances.get(trace1.size()).get(trace2.size()));
		temporalDistances.clear();
		
		System.out.println("---");
		trace1.clear();
		trace2.clear();
		
		//Third Test !!!
		
		trace1.add(new TedEvent(2, new TedEventType(1, "It")));
		trace1.add(new TedEvent(3, new TedEventType(2, "U")));
		trace1.add(new TedEvent(4, new TedEventType(3, "CS")));
		
		trace2.add(new TedEvent(11, new TedEventType(1, "It")));
		trace2.add(new TedEvent(12, new TedEventType(2, "U")));
		trace2.add(new TedEvent(13, new TedEventType(3, "CS")));
		
		loadEventListsForTest(trace1, trace2);
		
		System.out.println("Comparison between: ");
		System.out.println("trace1: " + trace1);
		System.out.println("and");
		System.out.println("trace2: " + trace2);
		System.out.println("---");
		System.out.println(mapEventId2OccTrace1);
		System.out.println(mapEventId2OccTrace2);
		
		occDistResults = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println("Occurrence distance results :");
		for(Map.Entry<Integer, Float> e : occDistResults.entrySet()){
			System.out.print(e + " ");
		}
		System.out.println("");
		System.out.println("Dropping distance results :");
		dropDistResults = distanceProcessor.droppingDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		for(Integer i : dropDistResults)
			System.out.print(i +" ");
		
		System.out.println("");
		
		System.out.println("Temporal distance results :");
		temporalDistances = distanceProcessor.temporalDistance(trace1, trace2, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println(temporalDistances);
		
		System.out.println("Temporal distance: " + temporalDistances.get(trace1.size()).get(trace2.size()));
		
		temporalDistances.clear();
		
		System.out.println("---");
		trace1.clear();
		trace2.clear();

		
		
		
		
	}
	
	
	
}
