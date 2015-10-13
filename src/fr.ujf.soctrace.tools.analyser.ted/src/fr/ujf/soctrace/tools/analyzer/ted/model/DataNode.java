/**
 * @author semteu
 *
 * 13 oct. 2015
 * DataNode.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * @author semteu
 * 13 oct. 2015
 * DataNode.java
 */

public class DataNode implements ITreeNode {
	
	public enum NodeType{
		OPERATION,
		EVENTTYPE,
		OTHER;
	}

	private NodeType dataType;
	private ITreeNode parent;
	private String description;
	private List<ITreeNode> children = new ArrayList<ITreeNode>();
	
	
	/**
	 * Constructor
	 * @param name folder label
	 */
	public DataNode(NodeType nodeType, String description) {
		this.dataType = nodeType;
		this.description = description;
	}
	
	public DataNode(String description) {
		this.dataType = NodeType.OTHER;
		this.description = description;
	}
	
	@Override
	public String getName() {
		return description;
	}
	
	
	@Override
	public Image getImage() {
		return null;
	}
	
	@Override
	public List<ITreeNode> getChildren() {
		return children;
	}
	
	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public ITreeNode getParent() {
		return parent;
	}
	
	@Override
	public void setParent(ITreeNode parent) {
		this.parent = (DataNode) parent;
		
	}
	
	/**
	 * Add a child node to the folder.
	 * @param child a tree node
	 */
	public void addChild(ITreeNode child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Remove all the children from this folder.
	 */
	public void removeAll() {
		children.clear();
	}
	
	@Override
	public String toString() {
		return "DataNode [type="+ dataType + ", name=" + description + ", parent=" + parent + "]";
	}

}
