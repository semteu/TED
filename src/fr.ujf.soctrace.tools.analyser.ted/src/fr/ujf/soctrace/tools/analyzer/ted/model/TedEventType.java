/**
 * @author semteu
 *
 * 5 oct. 2015
 * TedEventType.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

/**
 * @author semteu
 * 5 oct. 2015
 * TedEventType.java
 */
public class TedEventType {
	
	/**
	 * ID used to identify a Ted event type
	 */
	
	private int id;
	
	/**
	 * String that describes a Ted event type
	 */
	
	private String description;
	
	public TedEventType(int id, String description){
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString(){
		return "TedEventType [id: "+ id + ", description: " + description +"]";
	}
	

}
