/**
 * 
 */
package helper;

import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * @author xiaoying
 *
 */
public class TPQSolutionHandler {

	
	/*
	 * solutions in both l1 and l2 have been sorted based on fldList
	 */
	public static ArrayList<TPQSolutionListFormat> mergeJoin(ArrayList<TPQSolutionListFormat> l1,
			ArrayList<TPQSolutionListFormat> l2, ArrayList<Integer> fldList, int len) {

		ArrayList<TPQSolutionListFormat> result = new ArrayList<TPQSolutionListFormat>();
		PathSolutionScan s1 = new PathSolutionScan(l1, fldList);
		PathSolutionScan s2 = new PathSolutionScan(l2, fldList);

		PathSolutionMergeJoinScan mjs = new PathSolutionMergeJoinScan(s1, s2, fldList);
		while (mjs.next()) {

			result.add(genSoln(mjs, len));
		}

		mjs.close();

		return result;
	}

	private static TPQSolutionListFormat genSoln(PathSolutionMergeJoinScan mjs, int len) {
		TPQSolutionListFormat soln = new TPQSolutionListFormat(len);
		for (int fldid = 0; fldid < len; fldid++) {
			soln.addValue(fldid, mjs.getEntry(fldid));

		}

		return soln;

	}
	
	
	
	private static void genSoln(PrintWriter pw, PathSolutionMergeJoinScan mjs, int len) {
			
		for (int fldid = 0; fldid < len; fldid++) {
			pw.print(mjs.getEntry(fldid) + " ");
		}
		pw.print("\n");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
