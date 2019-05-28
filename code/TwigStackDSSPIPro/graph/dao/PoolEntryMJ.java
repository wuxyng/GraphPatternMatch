/**
 * 
 */
package graph.dao;

import java.util.ArrayList;

import graph.Node;


/**
 * @author xiaoying
 *
 */
public class PoolEntryMJ {

	int mQID; // the corresponding id of the query node it matches.
	Node mValue;
	
	ArrayList<PoolEntryMJ> mChildren;
	
	boolean mIsInPool;
	
	public PoolEntryMJ(int qid, Node val){
		mQID = qid;
		mValue = val;
		mChildren = new ArrayList<PoolEntryMJ>(1);
		mIsInPool = false;
	}
	
	public int getQID(){
		
		return mQID;
	}
	
	public void addChild(PoolEntryMJ c){
		
		mChildren.add(c);
	}
	
	public void setInPoolMark(){
		
		mIsInPool = true;
	}
	
	public boolean isInPool(){
		
		return mIsInPool;
	}
	
	public Node getValue(){
		
		return mValue;
	}
	
	public ArrayList<PoolEntryMJ> getChildren(){
		
		return mChildren;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
