/**
 * 
 */
package global;

import helper.Interval;

/**
 * @author xiaoying
 *
 */
public class DataEntry extends Interval{

	public int mLevel;
	public int mPos;
	
	public DataEntry(int s, int e) {
		
		super(s, e);
		mPos = 0;
		mLevel = 0;
	}
	
	public DataEntry(int p, int s, int e){
		
		super(s, e);
		mPos = p;
		mLevel = 0;
	}

	
    public DataEntry(int p, int s, int e, int l){
		
		super(s, e);
		mPos = p;
		mLevel = l;
	}
    
    public boolean lessThan(DataEntry entry){
    	
    	return this.mStart<entry.mStart;
    	
    }

    public String toString(){
		
		StringBuffer s = new StringBuffer();
		s.append("[");
		s.append("POS: "+mPos + ",");
		s.append("START: "+mStart + ",");
		s.append("END: "+mEnd + ",");
		s.append("LEVEL: "+mLevel + "]\n");
		return s.toString();
	}
    
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
