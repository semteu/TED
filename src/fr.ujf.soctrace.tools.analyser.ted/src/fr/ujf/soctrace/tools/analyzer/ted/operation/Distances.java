package fr.ujf.soctrace.tools.analyser.ted.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.ReverbType;

import fr.ujf.soctrace.tools.analyser.ted.model.AbstractTimestampedElement;
import fr.ujf.soctrace.tools.analyser.ted.model.ElementDictionary;

public class Distances {
	
	private Map<Integer, Integer> mapStat1;
	private Map<Integer, Integer> mapStat2;
	
	public Map<Integer, Float> OccurenceDistance(float threshold, ElementDictionary traceStats1, ElementDictionary traceStats2){
		
		mapStat1 = traceStats1.getMapIdToNbOccurence();
		mapStat2 = traceStats2.getMapIdToNbOccurence();
		Map<Integer, Float> mapOccRatio = new HashMap<Integer, Float>();
		
		Set<Integer> keyset = mapStat1.keySet();
		keyset.addAll(mapStat2.keySet());
		
		for(Integer key : keyset){
			float occ_ratio = Math.min(mapStat1.get(key), mapStat2.get(key))/
					Math.max(mapStat1.get(key), mapStat2.get(key));
			
			if(occ_ratio > threshold)
				mapOccRatio.put(key, new Float(occ_ratio));
		}
		
		return mapOccRatio;
		
	}
	
	public Set<Integer> droppingDistance(ElementDictionary traceStats1, ElementDictionary traceStats2){
	
		mapStat1 = traceStats1.getMapIdToNbOccurence();
		mapStat2 = traceStats2.getMapIdToNbOccurence();
		
		Set<Integer> keyset1 = mapStat1.keySet();
		Set<Integer> keyset2 = mapStat2.keySet();
		Set<Integer> unionKeyset = new HashSet<Integer>();
		Set<Integer> intersectKeyset = new HashSet<Integer>();
		
		unionKeyset.addAll(keyset1);
		unionKeyset.addAll(keyset2);
		intersectKeyset.addAll(keyset1);
		intersectKeyset.retainAll(keyset2);
		unionKeyset.removeAll(intersectKeyset);
		
		return unionKeyset;		
		
		
	}
	
	private double getWeight(AbstractTimestampedElement element, ElementDictionary traceDico){
		if(traceDico.getMapElementToId().containsKey(element.getElement())){
			Integer elementId = traceDico.getMapElementToId().get(element.getElement());
			if(traceDico.getMapIdToNbOccurence().get(elementId) != 0){
				return 1/traceDico.getMapIdToNbOccurence().get(elementId);
			}
		}
			
		return (double)(1.5);
	}
	
	public double temporalDistance( List<AbstractTimestampedElement> traceEvents1,
									List<AbstractTimestampedElement> traceEvents2){
		
		ElementDictionary traceDico1 = new ElementDictionary(traceEvents1);
		ElementDictionary traceDico2 = new ElementDictionary(traceEvents2);
				
		List< ArrayList<Double> > dists = new ArrayList< ArrayList<Double> >();
		
		for(int i = 0; i < traceEvents1.size(); i++){
			for (int j = 0; j < traceEvents2.size(); j++){
				dists.get(i).set(j, 0.);
			}
		}
		
		int i = 1;
		for(AbstractTimestampedElement element: traceEvents1){
			dists.get(i).set(0, dists.get(i - 1).get(0) + getWeight(element, traceDico1));
		}
		
		int j = 1;
		for(AbstractTimestampedElement element: traceEvents2){
			dists.get(0).set(j, dists.get(0).get(j - 1) + getWeight(element, traceDico2));
		}
		
		double v = 0;
		double k = 0;
		int t1 = traceEvents1.get(0).getTimestamp(); 
		int t2 = traceEvents2.get(0).getTimestamp();

		for(i = 1; i < traceEvents1.size(); i++){
			for(j = 1; j < traceEvents2.size(); j++){
				if(traceEvents1.get(i-1).getElement().equals(traceEvents2.get(j-1).getElement())){
					if(i != 1){
						v = 1.5 * getWeight(traceEvents2.get(i-1), traceDico1);
						k = v * Math.abs(Math.abs(traceEvents2.get(j-1).getTimestamp() - traceEvents2.get(j-2).getTimestamp()) 
								- Math.abs(t1 - t2));
					}
					else
						k = 0;					
				}
				else{
					k = getWeight(traceEvents1.get(i - 1), traceDico1) 
							+ getWeight(traceEvents2.get(j -1), traceDico2); 
				}
			}
			
			dists.get(i).set(j, Math.min(dists.get(i-1).get(j) + getWeight(traceEvents1.get(i-1), traceDico1), 
					Math.min(dists.get(i).get(j-1) + getWeight(traceEvents2.get(j-1), traceDico2), 
							dists.get(i-1).get(j-1) + k)));			
		}
		return dists.get(traceEvents1.size()).get(traceEvents2.size());		
	}

}
