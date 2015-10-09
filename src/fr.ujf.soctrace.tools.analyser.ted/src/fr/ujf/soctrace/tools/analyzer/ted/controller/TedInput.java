/**
 * @author semteu
 *
 * 2 oct. 2015
 * TedInput.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.controller;

import fr.inria.soctrace.lib.model.Trace;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedAdapter;

/**
 * @author semteu
 * 2 oct. 2015
 * TedInput.java
 * 
 * This class handles the input parameters for TED algorithm
 * 
 */
public class TedInput {
	
	public enum InputStatus{
		
		INPUT_OK,
		INPUT_NO_REFTRACE,
		INPUT_NO_DIAGTRACE,
		INPUT_NO_OPERATIONSELECTED,
		INPUT_BAD_THRESHOLD,
		INPUT_BAD_REFTRACE,
		INPUT_BAD_DIAGTRACE,
		INPUT_DIFF_TRACETYPE;

	}
	
	public enum Operation{
		
		DROPPING_DISTANCE, 
		OCCURRENCE_DISTANCE,
		TEMPORAL_DISTANCE,
		ANY_DISTANCE,
		ALL_DISTANCE;
		
	}
	
	public Trace refTrace;
	
	public Trace diagTrace;
	
	public Float threshold;
	
	public Operation operation;	
	
	public TedAdapter refAdapter;
	
	public TedAdapter diagAdapter;
	
	

}
