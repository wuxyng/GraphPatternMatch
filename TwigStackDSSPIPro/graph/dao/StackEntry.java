/**
 * 
 */
package graph.dao;


import graph.Node;
import query.QNode;

/**
 * @author xiaoying
 *
 */
public class StackEntry {

	Node mValue;
	StackEntry mParent;
	PoolEntry mPoolEntry;

	public StackEntry(QNode q, Node val, StackEntry parent) {

		mValue = val;
		mParent = parent;
		mPoolEntry = new PoolEntry(q, val);
	}

	public Node getValue() {

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
