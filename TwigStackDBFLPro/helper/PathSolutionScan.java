/**
 * 
 */
package helper;

import java.util.ArrayList;


/**
 * @author xiaoying
 * 
 */
public class PathSolutionScan implements Scan {

	private int mCurrentPos = -1;
	private int mSavedposition;
	TPQSolutionListComparator mComp;
	private ArrayList<TPQSolutionListFormat> mSolnList;

	public PathSolutionScan(ArrayList<TPQSolutionListFormat> sList,
			ArrayList<Integer> fldList) {

		mSolnList = sList;
		mComp = new TPQSolutionListComparator(fldList);
	}

	@Override
	public void open() {
		mCurrentPos = -1;
		// sort the solution list
		//Collections.sort(mSolnList, mComp);

	}

	public void init(){
		
		mCurrentPos = 0;
	}
	@Override
	public boolean next() {
		mCurrentPos++;
		if (mCurrentPos < mSolnList.size())
			return true;
		return false;
	}

	public boolean end(){
		
		if (mCurrentPos < mSolnList.size())
			return false;
		return true;
	}
	
	@Override
	public void close() {
		mSavedposition = -1;
		mCurrentPos = -1;
	}

	public int getEntry(int fldname) {

		return mSolnList.get(mCurrentPos).getValue(fldname);

	}

	public TPQSolutionListFormat getSoln() {

		return mSolnList.get(mCurrentPos);
	}

	@Override
	public void moveToPos(int pos) {
		mCurrentPos = pos;
	}

	public void restorePosition() {

		mCurrentPos = mSavedposition;

	}

	public void savePosition() {

		mSavedposition = mCurrentPos;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
