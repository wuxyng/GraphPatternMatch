/**
 * 
 */
package dao;

import graph.GraphNode;
import query.QNode;

/**
 * @author xiaoying
 *
 */
public class StackEntry {

	GraphNode mValue;
	StackEntry mParent;
	PoolEntry mPoolEntry;

	public StackEntry(QNode q, GraphNode val, StackEntry parent) {

		mValue = val;
		mParent = parent;
		mPoolEntry = new PoolEntry(q, val);
	}

	public GraphNode getValue() {

		return mValue;
	}
	
	public StackEntry getParent() {

		return mParent;
	}

	public PoolEntry getPoolEntry(){
		
		return mPoolEntry;
	}
	/**
	 * 
	 */
	public StackEntry() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

}
