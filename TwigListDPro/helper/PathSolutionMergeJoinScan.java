/**
 * 
 */
package helper;


import java.util.ArrayList;


/**
 * @author xiaoying
 *
 */
public class PathSolutionMergeJoinScan implements Scan{

	private PathSolutionScan mS1, mS2;
	private TPQSolutionListFormat mJoinVal;
	private ArrayList<Integer> mFldList;
	
	public PathSolutionMergeJoinScan(PathSolutionScan s1,
			PathSolutionScan s2, ArrayList<Integer> fldList) {

		init(s1, s2, fldList);

	}
	
	public PathSolutionMergeJoinScan(ArrayList<TPQSolutionListFormat> l1,
			ArrayList<TPQSolutionListFormat> l2,
			ArrayList<Integer> fldList) {
		PathSolutionScan s1 = new PathSolutionScan(l1, fldList);
		PathSolutionScan s2 = new PathSolutionScan(l2, fldList);
		init(s1, s2, fldList);
	}

	
	@Override
	public void open() {
		mS1.open();
		mS2.open();
		
	}

	@Override
	public boolean next() {
		boolean hasmore2 = mS2.next();
		if (hasmore2 && mJoinVal != null
				&& mS2.getSoln().compareTo(mJoinVal, mFldList) == 0)
			return true;

		boolean hasmore1 = mS1.next();
		if (hasmore1 && mJoinVal != null
				&& mS1.getSoln().compareTo(mJoinVal, mFldList) == 0) {
			mS2.restorePosition();
			return true;
		}

		while (hasmore1 && hasmore2) {
			TPQSolutionListFormat soln1 = mS1.getSoln();
			TPQSolutionListFormat soln2 = mS2.getSoln();
			if (soln1.compareTo(soln2, mFldList) < 0)
				hasmore1 = mS1.next();
			else if (soln1.compareTo(soln2, mFldList) > 0)
				hasmore2 = mS2.next();
			else {
				mS2.savePosition();
				mJoinVal = mS2.getSoln();
				return true;
			}
		}
		return false;

	}

	@Override
	public void close() {
		mS1.close();
		mS2.close();
		mJoinVal = null;
	}

	@Override
	public void moveToPos(int pos) {
		// TODO Auto-generated method stub
		
	}

	public int getEntry(int fldid) {
		int val1 = mS1.getEntry(fldid);
		if (val1 != -1)
			return val1;
		else
			return mS2.getEntry(fldid);
	}

	
	
	private void init(PathSolutionScan s1, PathSolutionScan s2,
			ArrayList<Integer> fldList) {

		mS1 = s1;
		mS2 = s2;
		mFldList = fldList;
		mJoinVal = null;
		open();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
}
