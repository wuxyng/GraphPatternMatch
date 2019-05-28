/**
 * 
 */
package dao;

import java.util.ArrayList;


import graph.GraphNode;


/**
 * @author xiaoying
 *
 */
public class PoolEntryMJ {

	int mQID; // the corresponding id of the query node it matches.
	GraphNode mValue;
	
	ArrayList<PoolEntryMJ> mChildren;
	
	boolean mIsInPool;
	
	public PoolEntryMJ(int qid, GraphNode val){
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
	
	public GraphNode getValue(){
		
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
