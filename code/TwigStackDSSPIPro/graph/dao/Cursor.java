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
public interface Cursor {

	public abstract Node getCurrent();
	public abstract void close();
	public abstract void advance();
	public abstract boolean eof();
	public abstract void open();
	// add on 2017.01.17
	public abstract void restorePosition();
	public abstract void savePosition();
	//public static Interval MAXENTRY = new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static Node MAXENTRY = new Node(-1, -1, new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE));  
}
