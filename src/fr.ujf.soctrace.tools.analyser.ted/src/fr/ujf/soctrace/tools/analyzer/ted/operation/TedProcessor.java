/**
 * @author semteu
 *
 * 6 oct. 2015
 * TedProcessor.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.ujf.soctrace.tools.analyzer.ted.chart.StackedBarChartLoader;
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
	
//	private final Text  consoleComponent;
	
	private ChartComposite chartView;
	
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
	
	public TedProcessor( TedInput input, ChartComposite chartView, 
						 final Text txtDecision, DataNode tree){
		tedInput = input;
		distanceProcessor = new Distances();
		this.chartView = chartView;
		this.decisionComponent = txtDecision;
		treeResults = tree;
	}
	

	/*
	 * This method construct the list of events in a trace ordered by timestamp
	 */
	private int loadEventLists(IProgressMonitor monitor){
		mapEventId2OccTrace1.clear();
		mapEventId2OccTrace2.clear();
		
		TedAdapter adapter1 = tedInput.refAdapter;
		TedAdapter adapter2 = tedInput.diagAdapter;
		TedEvent event =  null;

		try{
			while(adapter1.hasNext()){
				
				if(monitor != null && monitor.isCanceled())
					return 1;
							
				event = adapter1.getNext();
				if(tedInput.eventCategory == EventCategory.PUNCTUAL_EVENT ||
						event.getEvent().getCategory() == tedInput.eventCategory){
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
			}
			while(adapter2.hasNext()){
				
				if(monitor != null && monitor.isCanceled())
					return 1;
				
				event = adapter2.getNext();
				if(tedInput.eventCategory == EventCategory.PUNCTUAL_EVENT ||
						event.getEvent().getCategory() == tedInput.eventCategory){
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
		}
		catch(SoCTraceException e){
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private void loadEventListsForTest(List<TedEvent> trace1, List<TedEvent> trace2){
		mapEventId2OccTrace1.clear();
		mapEventId2OccTrace2.clear();
		DeltaManager dm = new DeltaManager();
		dm.start();
		for(TedEvent e: trace1){
			Integer value = mapEventId2OccTrace1.get(e.getEventType().getId());
			if(value != null){
				mapEventId2OccTrace1.put(e.getEventType().getId(), value + 1);
			}
			else{
				mapEventId2OccTrace1.put(e.getEventType().getId(), 1);
			}
		}
		dm.end("Loading ref. trace: ");
		
		dm.start();
		for(TedEvent e: trace2){
			Integer value = mapEventId2OccTrace2.get(e.getEventType().getId());
			if(value != null){
				mapEventId2OccTrace2.put(e.getEventType().getId(), value + 1);
			}
			else{
				mapEventId2OccTrace2.put(e.getEventType().getId(), 1);
			}
		}
		dm.end("Loading susp. trace: ");
	}
	
	/**
	 * Method used to process occurrence distance, dropping distance or temporal distance 
	 * for a job launched from the TED Plug-in 
	 *   
	 * @param monitor
	 * @return
	 */
		
	public TedStatus run(IProgressMonitor monitor){
		
//		System.out.println("ProcessorRunning ...");
//		run_tests();
//		return TedStatus.RUN_OK;
		boolean statusOccDist = false;
		boolean statusDropDist = false;
		boolean statusTempDist = false;
		
		int loadingStatus = loadEventLists(monitor);
		
//		System.out.println("Size trace1: "+ eventTrace1.size());
//		System.out.println("Size trace2: "+ eventTrace2.size());
		if(monitor != null && monitor.isCanceled() || loadingStatus != 0)
			return TedStatus.RUN_CANCEL;
		
		
//		Selection of the distance processing
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
			
			//Check occurrence distance
			statusOccDist = processOccurrenceDistance();
			
			if(monitor != null && monitor.isCanceled())
				return TedStatus.RUN_CANCEL;
			
			//check dropping distance
			statusDropDist = processDroppingDistance();
			
			if(monitor != null && monitor.isCanceled())
				return TedStatus.RUN_CANCEL;
			
			//check temporal distance
			statusTempDist = processTemporalDistance();
			break;
			
		default:
			System.out.println("Operation not yet implemented !");
			break;
		}
		
		// Output construction for UI 
		if(statusOccDist && statusDropDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation, crash and Slow stream anomalies", 1);
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromDroppingDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
			
		}
		else if(statusOccDist && statusDropDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation and crash anomalies", 1);
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromDroppingDistance(treeResults);
		}
		else if(statusOccDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation and Slow stream anomalies", 1);
			makeTreeResultsFromOccurrenceDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else if(statusDropDist && statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents crash and Slow stream anomalies", 1);
			makeTreeResultsFromDroppingDistance(treeResults);
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else if(statusOccDist){
			sendToDecisionComponent("The diagnosed trace presents A/V/S Desynchronisation anomaly", 1);
			makeTreeResultsFromOccurrenceDistance(treeResults);
			plotOccurenceDistanceResults();
		}
		else if(statusDropDist){
			sendToDecisionComponent("The diagnosed trace presents crash anomaly", 1);
			makeTreeResultsFromDroppingDistance(treeResults);
			plotDroppingDistanceResults();
		}
		else if(statusTempDist){
			sendToDecisionComponent("The diagnosed trace presents Slow stream anomaly", 1);
			makeTreeResultsFromTemporalDistance(treeResults);
		}
		else
			sendToDecisionComponent("The diagnosed trace presents no anomaly", 0);
		
		
		if(occResults != null)
			occResults.clear();
		
		if(dropResults != null)
			dropResults.clear();
		
		if(tempResults != null){
			for(ArrayList<Double> array: tempResults)
				array.clear();
			tempResults.clear();
		}
		
		return TedStatus.RUN_OK;
		
	}
	
	
	/**
	 * Test method for distances 
	 */
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

	/**
	 * Method to launch occurrence distance processing
	 * 
	 * @return a boolean to indicate if the occurrence distance is null or not
	 */
	private boolean processOccurrenceDistance(){
		boolean status = false;

//		System.out.println("Executing occurrence distance ...");
		DeltaManager dm = new DeltaManager();
		dm.start();
		occResults = distanceProcessor.OccurrenceDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		dm.endMessage("occ. distance: ");
//		System.out.println(occResults);
		
		for(Integer key: occResults.keySet()){
//			System.out.println("key: " + key +" distance: " + occResults.get(key));
			status |= (occResults.get(key) <= tedInput.threshold);
		}
		
		return status;
	}
	
	/**
	 * Method to launch dropping distance processing
	 * 
	 * @return a boolean to indicate if the dropping distance is null or not
	 */
	private boolean processDroppingDistance(){
		boolean status = false;

		DeltaManager dm = new DeltaManager();
		dm.start();
//		System.out.println("Executing dropping distance ...");
		dropResults = distanceProcessor.droppingDistance(tedInput.threshold, 
				mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		dm.endMessage("drop. distance: ");
//		System.out.println(dropResults);
		
		if(dropResults.size() > 0)
			status = true;
		
		return status;
	}
	
	/**
	 * Method to launch temporal distance processing
	 * 
	 * @return a boolean to indicate if the temporal distance is null or not
	 */
	private boolean processTemporalDistance(){
		boolean status = false;
		DeltaManager dm = new DeltaManager();
		dm.start();
//		System.out.println("Executing temporal distance ...");
		tempResults = distanceProcessor.temporalDistance(eventTrace1, 
				eventTrace2, mapEventId2OccTrace1, mapEventId2OccTrace2);
		
		dm.end("temp. distance");
//		System.out.println(tempResults);
		
		if(tempResults.get(eventTrace1.size()).get(eventTrace2.size()) > tedInput.threshold)
			status = true;
		
		return status;
	}	
	
	/**
	 * Sending a message to decision text component
	 * 
	 * @param message
	 */
	private void sendToDecisionComponent(final String message, final int status){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				FontData[] fontData = decisionComponent.getFont().getFontData();
				fontData[0].setHeight(9);
				fontData[0].setStyle(1);
				
				if(status == 1){	
					decisionComponent.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
				else{
					decisionComponent.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
				}
				
				decisionComponent.setFont( new Font(Display.getCurrent(), fontData[0]));

				decisionComponent.setText(message);
				
			}
		});
	}
	
	/**
	 * This method allows to construct tree results from occurrence distance processing
	 * in order to fill the treeView of TED UI
	 * 
	 * @param tree
	 */
	private void makeTreeResultsFromOccurrenceDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Occurrence distance");
		
		List<Entry<Integer, Float>> sortedOccDistanceEventList = getDistanceEventListSortedByDistance(occResults);
		
		for(Entry<Integer, Float> e: sortedOccDistanceEventList){
			float v1 = mapEventId2OccTrace1.get(e.getKey()) == null ? 0 : mapEventId2OccTrace1.get(e.getKey());
			float v2 = mapEventId2OccTrace2.get(e.getKey()) == null ? 0 : mapEventId2OccTrace2.get(e.getKey());
			String value = "(e"+ e.getKey() +") " + mapEventId2EventDesc.get(e.getKey()) + " :  ref. trace (" + v1  + ") <-> susp. trace (" 
						   + v2 + ") >> " + e.getValue();
			DataNode child = new DataNode(value);
			root.addChild(child);
		}
		tree.addChild(root);
	}
	
	/**
	 * This method plots event proportion between trace according to occurrence distance
	 * The plot is done on JFreeChart
	 * 
	 */
	private void plotOccurenceDistanceResults(){
		//Plot stacked bar chart
		List<Entry<Integer, Float>> sortedOccDistanceEventList = getDistanceEventListSortedByDistance(occResults);
		StackedBarChartLoader chartBuilder = new StackedBarChartLoader(mapEventId2OccTrace1, 
																	   mapEventId2OccTrace2, sortedOccDistanceEventList, 
																	   mapEventId2EventDesc, tedInput.refTrace.getAlias(), 
																	   tedInput.diagTrace.getAlias());
		JFreeChart chart = chartBuilder.makeStackedChart();
		plotChart(chart);
	}
	
	
	/**
	 * This method plots event proportion between trace according to occurrence distance
	 * The plot is done on JFreeChart
	 * 
	 */
	private void plotDroppingDistanceResults(){
		//Plot stacked bar chart
		
		StackedBarChartLoader chartBuilder = new StackedBarChartLoader(mapEventId2OccTrace1, 
																	   mapEventId2OccTrace2, dropResults, 
																	   mapEventId2EventDesc, tedInput.refTrace.getAlias(), 
																	   tedInput.diagTrace.getAlias());
		JFreeChart chart = chartBuilder.makeStackedChart();
		plotChart(chart);
	}
	

	
	/**
	 * @return Entry<EventId, Distance> list sorted by distance
	 */
	private List<Entry<Integer, Float>> getDistanceEventListSortedByDistance(Map<Integer, Float> eventDistances) {
		
		List<Entry<Integer, Float>> entryList = new ArrayList<Entry<Integer, Float>>(eventDistances.entrySet());
		Collections.sort(entryList, new Comparator<Entry<Integer,Float>>(){
			@Override
			public int compare(Entry<Integer, Float> entry1,
					Entry<Integer, Float> entry2) {
				
				return entry1.getValue().compareTo(entry2.getValue());
			}
			
		});
		return entryList;
	}

	/**
	 * This method allows to construct tree results from dropping distance processing
	 * in order to fill the treeView of TED UI
	 * 
	 * @param tree
	 */
	private void makeTreeResultsFromDroppingDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Dropping distance");
		
		Set<Integer> keyset1 = new HashSet<Integer>(mapEventId2OccTrace1.keySet());
		Set<Integer> keyset2 = new HashSet<Integer>(mapEventId2OccTrace2.keySet());
		Set<Integer> set1 = new HashSet<Integer>();
		Set<Integer> set2 = new HashSet<Integer>();
		
		set1.addAll(dropResults);
		set1.retainAll(keyset1);
		
		set2.addAll(dropResults);
		set2.retainAll(keyset2);
				
		String value = "Events in trace1 not in trace2: ";
		for(Integer eid: set1){
			value += mapEventId2EventDesc.get(eid)+ ", ";
		}
		DataNode child = new DataNode(value);
		root.addChild(child);
		
		value = "Events in trace2 not in trace1: ";
		for(Integer eid: set2){
			value += mapEventId2EventDesc.get(eid)+ ", ";
		}
		child = new DataNode(value);
		root.addChild(child);
		
		value = "distance = " + dropResults.size();
		child = new DataNode(value);
		root.addChild(child);
		
		tree.addChild(root);
	}
	
	/**
	 * This method allows to construct tree results from temporal distance processing
	 * in order to fill the treeView of TED UI
	 * 
	 * @param tree
	 */
	private void makeTreeResultsFromTemporalDistance(DataNode tree){
		DataNode root = new DataNode(NodeType.OPERATION, "Temporal distance");
		String value = tempResults.get(eventTrace1.size()).get(eventTrace2.size()).toString();
		DataNode child = new DataNode(value);
		root.addChild(child);
		tree.addChild(root);
	}
	
	/**
	 * The method plot a constructed jfreechart on the Chart composite of TED UI
	 * @param chart
	 */
	private void plotChart(final JFreeChart chart){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				chartView.setChart(chart);
				chartView.forceRedraw();
				
			}
		});
	}
	
}
