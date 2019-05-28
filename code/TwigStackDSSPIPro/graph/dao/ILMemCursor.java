/**
 * 
 */
package graph.dao;

import java.util.ArrayList;

import global.Consts;
import graph.Node;
import helper.Interval;

/**
 * @author xiaoying
 *
 */
public class ILMemCursor implements Cursor {

	ArrayList<Node> mInvLst;
	int mCursor, mSize;
	int actState = 0;
	Node actEntry;
	
	public static int mNumNodesAccessed;

	// add on 2017.1.15
	
	int mSavedposition;
	int mSavedState;
	Node mSavedEntry;
	
	public ILMemCursor(ArrayList<Node> invLst) {

		mInvLst = invLst;
		mSize = invLst.size();
	}

	@Override
	public Node getCurrent() {

		if (actState == Consts.EOF)
			return MAXENTRY;

		if (actEntry != null)
			return actEntry;

		actEntry = mInvLst.get(mCursor);
		mNumNodesAccessed++;
		return actEntry;
	}

	@Override
	public void close() {
		actState = Consts.CLOSED;
		mInvLst = null;

	}

	@Override
	public void advance() {
		if (actState == Consts.EOF)
			return;
		actEntry = null;

		// position cursor to the next relevant node

		mCursor++;

		if (mCursor == mSize) { // can not find relevant node

			actState = Consts.EOF;
		}

	}

	@Override
	public boolean eof() {
		return (actState == Consts.EOF);
	}

	@Override
	public void open() {
		if (actState == Consts.OPEN)
			return;

		mCursor = 0;
		actEntry = null;
		if (mCursor == -1) { // can not find relevant node

			actState = Consts.EOF;
		} else {
			// initialize the cursor parameters
			actState = Consts.OPEN;

		}
	}
	
	
	public int getInvSize() {

		return mSize;
	}

	// add on 2017.1.15
	public void restorePosition() {

		mCursor = mSavedposition;
		actEntry = mSavedEntry;
		actState = mSavedState;
	}

	public void savePosition() {

		mSavedposition = mCursor;
		mSavedState = actState;
		mSavedEntry = actEntry;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
