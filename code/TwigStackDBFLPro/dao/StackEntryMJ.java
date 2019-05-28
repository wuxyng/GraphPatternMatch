/**
 * 
 */
package dao;

import graph.GraphNode;

/**
 * @author xiaoying
 *
 */
public class StackEntryMJ {

	GraphNode mValue;
	StackEntryMJ mParent;
	PoolEntryMJ mPoolEntry;

	public StackEntryMJ(int qid, GraphNode val, StackEntryMJ parent) {

		mValue = val;
		mParent = parent;
		mPoolEntry = new PoolEntryMJ(qid, val);
	}

	public GraphNode getValue() {

		return mValue;
	}
	
	public StackEntryMJ getParent() {

		return mParent;
	}

	public PoolEntryMJ getPoolEntry(){
		
		return mPoolEntry;
	}
	/**
	 * 
	 */
	public StackEntryMJ() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

}
