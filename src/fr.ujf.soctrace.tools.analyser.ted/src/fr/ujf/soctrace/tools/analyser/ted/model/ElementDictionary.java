package fr.ujf.soctrace.tools.analyser.ted.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementDictionary {
	
	private Map<AbstractTimestampedElement, Integer> mapElementToId;
	private Map<Integer, AbstractTimestampedElement> mapIdToElement;
	private Map<Integer, Integer> mapIdToNbOccurence;
	
	private void ConstructStructures(){
		mapElementToId = new HashMap<AbstractTimestampedElement, Integer>();
		mapIdToElement =  new HashMap<Integer, AbstractTimestampedElement>();
		mapIdToNbOccurence = new HashMap<Integer, Integer>();
	}
	public ElementDictionary(){
		ConstructStructures();
	}
	
	public ElementDictionary(List<AbstractTimestampedElement> elements){
		ConstructStructures();
		for(AbstractTimestampedElement element: elements){
			add(element);
		}
	}
	
	public void add(AbstractTimestampedElement tsElt){
		int key;
		int value;
		if(mapElementToId.containsKey(tsElt)){
			key = mapElementToId.get(tsElt);
			value = mapIdToNbOccurence.get(key) + 1;
			mapIdToNbOccurence.put(key, value);
		}
		else{
			key = mapElementToId.size();
			value = 1;
			mapElementToId.put(tsElt, key);
			mapIdToElement.put(key, tsElt);
			mapIdToNbOccurence.put(key, value);
		}
	}

	public Map<AbstractTimestampedElement, Integer> getMapElementToId() {
		return mapElementToId;
	}

	public void setMapElementToId(
			Map<AbstractTimestampedElement, Integer> mapElementToId) {
		this.mapElementToId = mapElementToId;
	}

	public Map<Integer, AbstractTimestampedElement> getMapIdToElement() {
		return mapIdToElement;
	}

	public void setMapIdToElement(
			Map<Integer, AbstractTimestampedElement> mapIdToElement) {
		this.mapIdToElement = mapIdToElement;
	}

	public Map<Integer, Integer> getMapIdToNbOccurence() {
		return mapIdToNbOccurence;
	}

	public void setMapIdToNbOccurence(Map<Integer, Integer> mapIdToNbOccurence) {
		this.mapIdToNbOccurence = mapIdToNbOccurence;
	}
	
	
}
