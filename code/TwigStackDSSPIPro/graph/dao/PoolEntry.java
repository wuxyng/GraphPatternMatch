/**
 * 
 */
package graph.dao;

import java.util.ArrayList;
import java.util.HashMap;

import graph.Node;
import query.QNode;

/**
 * @author xiaoying
 *
 */
public class PoolEntry implements Comparable<PoolEntry> {

	QNode mQNode; // the corresponding query node it matches
	Node mValue;
	HashMap<Integer, ArrayList<PoolEntry>> mSubEntries;

	double size; // total number of solution tuples for the subquery rooted at
					// mQNode

	boolean mIsInPool;

	public PoolEntry(QNode q, Node val) {
		mQNode = q;
		mValue = val;
		mIsInPool = false;
		initSubEntries();
	}

	public boolean isSink() {

		return mQNode.N_O_SZ == 0;
	}

	public int getQID() {

		return mQNode.id;
	}

	public void setInPoolMark() {

		mIsInPool = true;
	}

	public boolean isInPool() {

		return mIsInPool;
	}

	public QNode getQNode() {

		return mQNode;
	}

	public ArrayList<PoolEntry> getSubEntries(int qid) {

		return mSubEntries.get(qid);
	}

	public void addChild(PoolEntry c) {

		ArrayList<PoolEntry> subs = mSubEntries.get(c.getQID());
		subs.add(c);
	}

	public Node getValue() {

		return mValue;
	}

	public double size() {

		if (size == 0)
			getTotSolns(this);

		return size;
	}

	private void getTotSolns(PoolEntry e) {

		if (e.isSink()) {
			e.size = 1;
			return;
		}

		QNode qn = e.mQNode;
		double tot = 1;
		for (int c : qn.N_O) {
			ArrayList<PoolEntry> elist = e.mSubEntries.get(c);

			double tot_c = 0;
			for (int i = 0; i < elist.size(); i++) {
				PoolEntry sub = elist.get(i);
				if (sub.getValue().ID == e.getValue().ID)
					continue;
				//getTotSolns(sub);
				tot_c += sub.size();
			}
			tot *= tot_c;

		}
		e.size = tot;

	}

	@Override
	public int compareTo(PoolEntry other) {
		int rs = this.mValue.encoding.mStart - other.mValue.encoding.mStart;

		return rs;
	}

	public String toString() {

		StringBuilder s = new StringBuilder();
		toString(this, s);

		return s.toString();
	}

	private void toString(PoolEntry e, StringBuilder s) {

		if (e.isSink()) {

			s.append(e.mValue.ID);
			return;

		}

		s.append(e.mValue.ID);

		QNode qn = e.mQNode;
		for (int c : qn.N_O) {
			s.append("{");
			ArrayList<PoolEntry> elist = e.mSubEntries.get(c);

			for (int i = 0; i < elist.size(); i++) {
				PoolEntry sub = elist.get(i);
				if (sub.getValue().ID == e.getValue().ID)
					continue;
				toString(sub, s);
				if (i < elist.size() - 1)
					s.append(",");
			}
			s.append("}");
		}
	}

	private void initSubEntries() {

		if (mQNode.N_O_SZ > 0) {
			int sz = mQNode.N_O_SZ;
			mSubEntries = new HashMap<Integer, ArrayList<PoolEntry>>(sz);
			for (int cid : mQNode.N_O) {

				ArrayList<PoolEntry> subs = new ArrayList<PoolEntry>(1);
				mSubEntries.put(cid, subs);
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
