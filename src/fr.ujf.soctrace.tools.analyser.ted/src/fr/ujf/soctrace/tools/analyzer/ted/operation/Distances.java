package fr.ujf.soctrace.tools.analyzer.ted.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ujf.soctrace.tools.analyzer.ted.model.TedEvent;

public class Distances {
	
	public Map<Integer, Float> OccurrenceDistance(float threshold, Map<Integer, Integer> occEvents1, Map<Integer, Integer> occEvents2){
		
		Map<Integer, Float> mapOccRatio = new HashMap<Integer, Float>();
		Set<Integer> keyset = new HashSet<Integer>(occEvents1.keySet());
		
		keyset.addAll(occEvents2.keySet());
		
		for(Integer key : keyset){
			Integer keyOccEventMap1 = occEvents1.get(key) != null ? occEvents1.get(key): 0;
			Integer keyOccEventMap2 = occEvents2.get(key) != null ? occEvents2.get(key): 0;
			
			
			float occ_ratio = Math.min(keyOccEventMap1, keyOccEventMap2)/(float)
					Math.max(keyOccEventMap1, keyOccEventMap2);
//			System.out.println("key: " + key + ", occ_ratio: " + occ_ratio);
			if(occ_ratio <= threshold)
				mapOccRatio.put(key, new Float(occ_ratio));
		}
		
		return mapOccRatio;
		
	}
	
	public Set<Integer> droppingDistance(float threshold, Map<Integer, Integer> occEvents1, Map<Integer, Integer> occEvents2){
	
		Set<Integer> keyset1 = new HashSet<Integer>(occEvents1.keySet());
		Set<Integer> keyset2 = new HashSet<Integer>(occEvents2.keySet());
		Set<Integer> unionKeyset = new HashSet<Integer>();
		Set<Integer> intersectKeyset = new HashSet<Integer>();
		
		unionKeyset.addAll(keyset1);
		unionKeyset.addAll(keyset2);
		intersectKeyset.addAll(keyset1);
		intersectKeyset.retainAll(keyset2);
		unionKeyset.removeAll(intersectKeyset);
		
		return unionKeyset;		
		
		
	}

	private double getWeight(TedEvent e, Map<Integer, Integer> mapEventIdToOcc){
		Integer occ = mapEventIdToOcc.get(e.getEventType().getId()); 
		if(occ != null && occ != 0)
			return 1/(double)occ;
		return (double)1.5;
	}
	
	public List< ArrayList<Double> > temporalDistance(  List<TedEvent> trace1,
														List<TedEvent> trace2,
														Map<Integer, Integer> mapEventIdToOcc1,
														Map<Integer, Integer> mapEventIdToOcc2){
		
		List< ArrayList<Double> > distances = new ArrayList< ArrayList<Double> >();
		
		//Initialization of the table used by dynamic programming
		for(int i = 0; i < trace1.size() + 1; i++){
			ArrayList<Double> vec = new ArrayList<Double>(); 
			for(int j = 0; j < trace2.size() + 1; j++){
				vec.add(0.);
			}
			distances.add(vec);
		}
		
				
		int i = 1;
		for(TedEvent event: trace1){
			(distances.get(i)).set(0, (distances.get(i-1)).get(0) + getWeight(event, mapEventIdToOcc1));
			i++;
		}
		
		int j = 1;
		for(TedEvent event: trace2){
			distances.get(0).set(j, distances.get(0).get(j-1) + getWeight(event, mapEventIdToOcc2));
			j++;
		}
		
		double v = 0;
		double k = 0;
		
		long ts1 = trace1.get(0).getTimestamp();
		long ts2 = trace2.get(0).getTimestamp();
		
		for(i = 1; i < trace1.size() + 1; i++){
			for(j = 1; j < trace2.size() + 1; j++){				
				if(trace1.get(i-1).getEventType().getId() == trace2.get(j-1).getEventType().getId()){
					v = (getWeight(trace1.get(i-1), mapEventIdToOcc1) + getWeight(trace2.get(j-1), mapEventIdToOcc2))/2.;
					k = v * Math.abs(Math.abs(trace1.get(i-1).getTimestamp() - trace2.get(j-1).getTimestamp()) 
								- Math.abs(ts1 - ts2));
				}
				else{
					k = getWeight(trace1.get(i - 1), mapEventIdToOcc1) 
							+ getWeight(trace2.get(j -1), mapEventIdToOcc2); 
				}
				
				distances.get(i).set(j, Math.min(distances.get(i-1).get(j) + getWeight(trace1.get(i-1), mapEventIdToOcc1), 
						Math.min(distances.get(i).get(j-1) + getWeight(trace2.get(j-1), mapEventIdToOcc2),
								 distances.get(i-1).get(j-1) + k)));
			}			
		}
		return distances;		
	}
	
}
