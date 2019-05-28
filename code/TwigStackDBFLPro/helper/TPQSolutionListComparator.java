/**
 * 
 */
package helper;

import java.util.ArrayList;
import java.util.Comparator;


/**
 * @author xiaoying
 * 
 */
public class TPQSolutionListComparator implements
		Comparator<TPQSolutionListFormat> {

	ArrayList<Integer> keyList;

	public TPQSolutionListComparator(ArrayList<Integer> list) {

		keyList = list;
	}

	@Override
	public int compare(TPQSolutionListFormat soln1, TPQSolutionListFormat soln2) {
		
		return soln1.compareTo(soln2, keyList);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
