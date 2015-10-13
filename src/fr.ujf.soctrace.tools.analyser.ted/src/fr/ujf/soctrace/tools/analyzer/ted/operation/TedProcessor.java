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
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedStatus;
import fr.ujf.soctrace.tools.analyzer.ted.model.DataNode;
import fr.ujf.soctrace.tools.analyzer.ted.model.DataNode.NodeType;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedAdapter;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedEvent;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedEventType;

/**
 * @author semteu
 * 6 oct. 2015
 * TedProcessor.java
 */
public class TedProcessor {
	
	private final Text  consoleComponent;
	
	private final Text decisionComponent;

	private TedInput tedInput = null;
	
	/**
	 * This attribut allow to construct a results structure for a TreeViewer
	 */
	private DataNode treeResults = null;
	
	private Distances distanceProcessor = null;
	
	private Map<Integer, String> mapEventId2EventDesc = new HashMap<Integer, String>();
	private Map<Integer, Integer> mapEventId2OccTrace1 = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mapEventId2OccTrace2 = new HashMap<Integer, Integer>();
	
	private Map<Integer, Float> occResults = null;
	private Set<Integer> dropResults = null;
	private List< ArrayList<Double> > tempResults =  null;
		
	private List<TedEvent> eventTrace1 = new ArrayList<TedEvent>();
	
	private List<TedEvent> eventTrace2 = new ArrayList<TedEvent>();
	
	public TedProcessor(TedInput input, final Text textbox, final Text txtDecision, DataNode tree){
		tedInput = input;
		distanceProcessor = new Distances();
		this.consoleComponent = textbox;
		this.decisionComponent = txtDecision;
		treeResults = tree;
	}
	

//	public Text getTxtConsole() {
//		return txtConsole;
//	}

//	public void setTxtConsole(Text txtConsole) {
//		this.txtConsole = txtConsole;
//	}
	
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
					mapEventId2EventDesc.put(event.getEventType().getId(), event.getEventType().getDescription());
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
				if(!mapEventId2EventDesc.containsKey(event.getEventType().getId())){
					mapEventId2EventDesc.put(event.getEventType().getId(), event.getEventType().getDescription());
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
//		run_tests();
//		return TedStatus.RUN_OK;
		boolean statusOccDist = false;
		boolean statusDropDist = false;
		boolean statusTempDist = false;
		
		loadEventLists();
		
		System.out.println("Size trace1: "+ eventTrace1.size());
		System.out.println("Size trace2: "+ eventTrace2.size());
		
		switch (tedInput.operation) {
		
		case OCCURRENCE_DISTANCE:
			statusOccDist = processOccurrenceDistance();
			break;
			
		case DROPPING_DISTANCE:
			statusDropDist = processDroppingDistance();
			break;			
			
		case TEMPORAL_DISTANCE:
			statusTempDist = processTemporalDistance();
			break;
			
		case ANY_DISTANCE:
			sendToConsole("Try any distance ...\n");
			
			//Check occurrence distance
			statusOccDist = processOccurrenceDistance();
			if(statusOccDist)
				break;
			
			//check dropping distance
			statusDropDist = processDroppingDistance();
			if(statusOccDist)
				break;
			
			//check temporal distance
			statusTempDist = processTemporalDistance();
			break;
		
		case ALL_DISTANCE:
			sendToConsole("Try all distances ...");
			
			//Check occurrence distance
			statusOccDist = processOccurrenceDistance();
			
			//check dropping distance
			statusDropDist = processDroppingDistance();
			
			//check temporal distance
			statusTempDist = processTemporalDistance();
			break;
			
		default:
			System.out.println("Operation not yet implemented !");
			break;
		}
		
		System.out.println(statusOccDist + " " + statusDropDist +" " + statusTempDist);
		
		if(statusOccDist && statusDropDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation, crash and Slow stream anomalies");
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromDroppingDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
			
		}
		else if(statusOccDist && statusDropDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation and crash anomalies");
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromDroppingDistance(treeResults);
		}
		else if(statusOccDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation and Slow stream anomalies");
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else if(statusDropDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents crash and Slow stream anomalies");
			makeTreeResultsFromDroppingDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else if(statusOccDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation anomaly");
			makeTreeResultsFromOccurrenceDistance(treeResults);
		}
		else if(statusDropDist){
			sendToDecisionComponent("The diagnosed trace presents crash anomaly");
			makeTreeResultsFromDroppingDistance(treeResults);
		}
		else if(statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents Slow stream anomaly");
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else
			sendToDecisionComponent("The diagnosed trace presents no anomaly");
		
		return TedStatus.RUN_OK;
		
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

	private boolean processOccurrenceDistance(){
		boolean status = false;
		sendToConsole("Processing occurrence distance ...");
		System.out.println("Executing occurrence distance ...");
		occResults = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		System.out.println(occResults);
		
		sendToConsole("Results: " + occResults);
		
		for(Integer key: occResults.keySet()){
//			System.out.println("key: " + key +" distance: " + occResults.get(key));
			status |= (occResults.get(key) <= tedInput.threshold);
		}
		
		return status;
	}
	
	private boolean processDroppingDistance(){
		boolean status = false;
		sendToConsole("Processing dropping distance ...");
		//		System.out.println("Executing dropping distance ...");
		dropResults = distanceProcessor.droppingDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		sendToConsole("Results: " + dropResults);
		System.out.println(dropResults);
		
		if(dropResults.size() > 0)
			status = true;
		
		return status;
	}
	
	private boolean processTemporalDistance(){
		boolean status = false;
		sendToConsole("Processing temporal distance ...");
//		System.out.println("Executing temporal distance ...");
		tempResults = distanceProcessor.temporalDistance(eventTrace1, 
				eventTrace1, mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		sendToConsole("Results: " + tempResults);
		System.out.println(tempResults);
		
		System.out.println("Temporal distance: " + tempResults.get(eventTrace1.size()).
				get(eventTrace2.size()));
		if(tempResults.get(eventTrace1.size()).get(eventTrace2.size()) > tedInput.threshold)
			status = true;
		
		return status;
	}	
	
	
	private void sendToConsole(String message){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				consoleComponent.setText(consoleComponent.getText() + message + "\n");
				
			}
		});
	}
	
	private void sendToDecisionComponent(String message){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				decisionComponent.setText(message);
				
			}
		});
	}
	
	private void makeTreeResultsFromOccurrenceDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Occurrence distance");
		for(Entry<Integer, Float> e: occResults.entrySet()){
			float v1 = mapEventId2OccTrace1.get(e.getKey()) == null ? 0 : mapEventId2OccTrace1.get(e.getKey());
			float v2 = mapEventId2OccTrace2.get(e.getKey()) == null ? 0 : mapEventId2OccTrace2.get(e.getKey());
			String value = mapEventId2EventDesc.get(e.getKey()) + " :  Trace1 (" + v1  + ") <-> Trace2 (" 
						   + v2 + ") >> " + e.getValue();
			DataNode child = new DataNode(value);
			root.addChild(child);
		}
		tree.addChild(root);
	}
	
	private void makeTreeResultsFromDroppingDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Dropping distance");
		String value = "|{";
		for(Integer e: dropResults){
			 value += mapEventId2EventDesc.get(e) + ", ";
			
		}
		value += "}| = " + dropResults.size();
		DataNode child = new DataNode(value);
		root.addChild(child);
		tree.addChild(root);
	}
	
	private void makeTreeResultsFromTemporalDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Temporal distance");
		String value = tempResults.get(eventTrace1.size()).get(eventTrace2.size()).toString();
		DataNode child = new DataNode(value);
		root.addChild(child);
		tree.addChild(root);
	}
	
}
