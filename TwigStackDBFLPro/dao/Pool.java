/**
 * 
 */
package dao;

import java.util.ArrayList;

/**
 * @author xiaoying
 *
 */
public class Pool {

	ArrayList<PoolEntry> elist;
	
	
	public Pool(){
		
		elist = new ArrayList<PoolEntry>();
	}
	
	public void addEntry(PoolEntry entry){
		
		elist.add(entry);
		entry.setInPoolMark();
	
	}
	
	public ArrayList<PoolEntry> elist(){
		
		return elist;
	}
	
	public boolean isEmpty(){
		
		return elist.isEmpty();
	}
	
	public void clear(){
		
		elist.clear();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



}
