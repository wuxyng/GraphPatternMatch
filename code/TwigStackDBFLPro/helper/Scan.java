/**
 * 
 */
package helper;


/**
 * @author xiaoying
 *
 */
public interface Scan {

	 /**
	    * Positions the scan before its first record.
	    */
	   public void     open();
	   
	   /**
	    * Moves the scan to the next record.
	    * @return false if there is no next record
	    */
	   public boolean  next();
	   
	   /**
	    * Closes the scan and its subscans, if any. 
	    */
	   public void     close();
	  // public int      getInt(int fldid);
	   public void moveToPos(int pos);
	   
}
