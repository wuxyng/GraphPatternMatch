/**
 * 
 */
package graph.dao;

import java.util.ArrayList;

/**
 * @author xiaoying
 *
 */
public class PoolMJ {

	ArrayList<PoolEntryMJ> elist;
	
	
	public PoolMJ(){
		
		elist = new ArrayList<PoolEntryMJ>();
	}
	
	public void addEntry(PoolEntryMJ entry){
		
		elist.add(entry);
		entry.setInPoolMark();
	}
	
	public ArrayList<PoolEntryMJ> elist(){
		
		return elist;
	}
	
	public boolean isEmpty(){
		
		return elist.isEmpty();
	}
	
	public void clear() {

		elist.clear();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
