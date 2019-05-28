/**
 * 
 */
package graph.dao;

import graph.Node;
import helper.Interval;

/**
 * @author xiaoying
 *
 */
public class StackEntryMJ {

	Node mValue;
	StackEntryMJ mParent;
	PoolEntryMJ mPoolEntry;

	public StackEntryMJ(int qid, Node val, StackEntryMJ parent) {

		mValue = val;
		mParent = parent;
		mPoolEntry = new PoolEntryMJ(qid, val);
	}

	public Node getValue() {

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
