/**
 * 
 */
package helper;

import java.util.Comparator;

import global.DataEntry;


/**
 * @author xiaoying
 *
 */
public class DataEntryComparator implements Comparator<DataEntry>{

	@Override
	public int compare(DataEntry o1, DataEntry o2) {
		
		int rs = o1.mStart - o2.mStart;
		return rs;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	

}
