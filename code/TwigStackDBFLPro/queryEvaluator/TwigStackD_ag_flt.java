/**
 * 
 */
package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import dao.BFLIndex;
import dao.Cursor;
import dao.ILMemCursor;
import dao.Pool;
import dao.PoolEntry;
import dao.StackEntry;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

/**
 * @author xiaoying
 *
 */
public class TwigStackD_ag_flt {

	TreeQuery mQuery;
	ArrayList<Stack<StackEntry>> mStacks;
	ArrayList<Cursor> mCursors;
	ArrayList<Pool> mPool;
	double totSolns = 0;
	int V; // total number of graph nodes
	BFLIndex mBFL;
	ArrayList<ArrayList<GraphNode>> mInvLsts;
	TimeTracker tt;

	public TwigStackD_ag_flt(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		mInvLsts = invLsts;
		this.V = mBFL.length();
		init();

	}

	public QueryEvalStat run(double flttm) {
		double matm = 0.0, entm = 0.0, jntm = 0.0;
		boolean success = initCursors();
		if (success) {
			tt.Start();
			twigStackD();
			matm = tt.Stop() / 1000;
			System.out.println("Time on TwigStackD_bfl_ag_flt:" + matm + "sec.");
			tt.Start();
			// printSolutions();
			calTotSolns();
			entm = tt.Stop() / 1000;
			System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		}
		QueryEvalStat stat = new QueryEvalStat(matm + flttm, entm, 0, totSolns);

		return stat;

	}

	public void clear() {

		for (Pool p : mPool)
			p.clear();
	}

	private boolean initCursors() {
		QNode[] nodes = mQuery.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<GraphNode> invLst = mInvLsts.get(n.id);
			if (invLst.size() == 0)
				return false;
			ILMemCursor cursor = new ILMemCursor(invLst);
			cursor.open();
			mCursors.add(n.id, cursor);
		}
		return true;
	}

	public void calTotSolns() {

		QNode root = mQuery.mRoot;
		Pool rPool = mPool.get(root.id);
		ArrayList<PoolEntry> elist = rPool.elist();
		for (PoolEntry r : elist) {

			totSolns += r.size();

		}

		System.out.println("total number of solution tuples: " + totSolns);
	}

	public void printSolutions() {

		QNode root = mQuery.mRoot;
		Pool rPool = mPool.get(root.id);
		ArrayList<PoolEntry> elist = rPool.elist();
		if (elist.isEmpty())
			return;

		for (PoolEntry r : elist) {

			System.out.println(r);

		}

	}

	protected void init() {
		mQuery.extractQueryInfo();
		int size = mQuery.V;
		mCursors = new ArrayList<Cursor>(size);
		mStacks = new ArrayList<Stack<StackEntry>>(size);
		mPool = new ArrayList<Pool>(size);
		Collections.fill(mCursors, null);
		for (int i = 0; i < size; i++) {

			mStacks.add(new Stack<StackEntry>());
			mPool.add(new Pool());
		}

		tt = new TimeTracker();
	}

	/****************************
	 * TwigStackD alg starts here
	 ****************************/

	public void twigStackD() {

		while (!end()) {

			QNode qAct = getMinSource();
			Cursor curAct = mCursors.get(qAct.id);
			GraphNode actEntry = curAct.getCurrent();
			if (actEntry == Cursor.MAXENTRY)
				break;

			QNode qPar = null;
			Stack<StackEntry> sPar = null, sAct = mStacks.get(qAct.id);
			if (qAct != mQuery.mRoot) {
				qPar = mQuery.getParent(qAct.id);
				sPar = mStacks.get(qPar.id);
				cleanStack(sPar, actEntry);
			}

			HashSet<Integer> missings = new HashSet<Integer>();
			missings = getMissings(qAct, actEntry);
			sweepPartialSolutions(qAct, actEntry, missings, sAct, sPar);
			curAct.advance();
		}

	}

	private boolean end() {

		QNode root = mQuery.mRoot;
		if (mStacks.get(root.id).isEmpty() && mCursors.get(root.id).eof())
			return true;

		return false;
	}

	private QNode getMinSource() {

		Cursor minCur, nodeCur;
		QNode[] nodes = mQuery.nodes;
		QNode minQ = nodes[0];

		for (int i = 1; i < nodes.length; i++) {
			QNode node = nodes[i];
			minCur = mCursors.get(minQ.id);
			nodeCur = mCursors.get(node.id);
			if (minCur.getCurrent().L_interval.mStart > nodeCur.getCurrent().L_interval.mStart)
				minQ = node;

		}

		return minQ;
	}

	private void cleanStack(Stack<StackEntry> stack, GraphNode actEntry) {

		// while (!stack.empty() && stack.peek().getValue().L_interval.mEnd <=
		// actEntry.L_interval.mStart)
		while (!stack.empty() && stack.peek().getValue().L_interval.mEnd < actEntry.L_interval.mStart)

			stack.pop();

	}

	private boolean checkInSync(QNode childq, GraphNode tparent) {

		Cursor qCur = mCursors.get(childq.id);
		int axis = childq.E_I.get(0).axis;
		qCur.savePosition();

		// while (qCur.getCurrent().L_interval.mStart <
		// tparent.L_interval.mStart)
		while (qCur.getCurrent().L_interval.mStart <= tparent.L_interval.mStart)
			qCur.advance();

		if (axis == 0) {
			boolean found = false;
			while (qCur.getCurrent().L_interval.mStart < tparent.L_interval.mEnd) {

				if (tparent.searchOUT(qCur.getCurrent().id)) {
					found = true;
					break;
				}
				qCur.advance();
			}

			qCur.restorePosition();
			return found;

		}

		GraphNode tq = qCur.getCurrent();
		qCur.restorePosition();

		if (tq.L_interval.mStart < tparent.L_interval.mEnd) {
			return true;
		} else {
			return false;
		}

	}

	private boolean checkMissingDEs(QNode q, GraphNode tr) {

		Cursor qCur = mCursors.get(q.id);
		GraphNode tq = qCur.getCurrent();

		if (!q.isSink()) {

			ArrayList<QNode> children = mQuery.getChildren(q.id);
			for (QNode qi : children) {
				int axis = qi.E_I.get(0).axis;
				boolean found = false;
				Pool pool = mPool.get(qi.id);
				for (PoolEntry h : pool.elist()) {
					// if (tq.id!= h.getValue().id && mBFL.reach(tq,
					// h.getValue()) == 1) {

					if ((axis == 0 && tq.searchOUT(h.getValue().id))
							|| (axis == 1 && mBFL.reach(tq, h.getValue()) == 1)) {
						found = true;
						break;
					}
				}
				if (!found) {

					Cursor qiCur = mCursors.get(qi.id);
					qiCur.savePosition();

					// while (qiCur.getCurrent().L_interval.mStart <
					// tr.L_interval.mStart)
					while (qiCur.getCurrent().L_interval.mStart <= tr.L_interval.mStart)
						qiCur.advance();
					// Change tq to tr, modified on 2018.06.01
					// while (qiCur.getCurrent().L_interval.mEnd <=
					// tq.L_interval.mEnd && !found) {
					while (qiCur.getCurrent().L_interval.mEnd <= tr.L_interval.mEnd && !found) {

						GraphNode tqi = qiCur.getCurrent();
						if (tqi.id == tq.id) {

							qiCur.advance();
							continue;
						}

						if (((axis == 0 && tq.searchOUT(tqi.id)) || (axis == 1 && mBFL.reach(tq, tqi) == 1))
								&& !checkMissingDEs(qi, tqi))
							// if (tq.id != tqi.id && mBFL.reach(tq, tqi) == 1
							// && !checkMissingDEs(qi, tqi))
							// if (mBFL.reach(tq, tqi) == 1 &&
							// !checkMissingDEs(qi, tqi))
							found = true;
						else
							qiCur.advance();
					}

					qiCur.restorePosition();

				}

				if (!found) {

					return true;
				}

			}
		}

		return false;

	}

	private HashSet<Integer> getMissings(QNode q, GraphNode tq) {

		HashSet<Integer> missings = new HashSet<Integer>();
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		for (QNode qi : children) {

			Cursor qiCur = mCursors.get(qi.id);
			if (checkInSync(qi, tq)) {
				boolean allInSync = false;
				qiCur.savePosition();
				while (qiCur.getCurrent().L_interval.mStart < tq.L_interval.mEnd) {
					if (checkMissingDEs(qi, tq))
						qiCur.advance();
					else {

						allInSync = true;
						break;
					}

				}
				qiCur.restorePosition();
				if (!allInSync)
					missings.add(qi.id);

			} else
				missings.add(qi.id);

		}

		return missings;
	}

	private boolean sweepPartialSolutions(QNode q, GraphNode tq, HashSet<Integer> missings, Stack<StackEntry> sAct,
			Stack<StackEntry> sPar) {
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		HashMap<Integer, ArrayList<PoolEntry>> candidateSet = new HashMap<Integer, ArrayList<PoolEntry>>();

		for (QNode qi : children) {
			Pool pqi = mPool.get(qi.id);
			int axis = qi.E_I.get(0).axis;
			ArrayList<PoolEntry> entries = new ArrayList<PoolEntry>(1);
			candidateSet.put(qi.id, entries);
			for (PoolEntry hp : pqi.elist()) {

				GraphNode tp = hp.getValue();
				if (tq.id == tp.id)
					continue;
				if ((axis == 0 && tq.searchOUT(tp.id)) || (axis == 1 && mBFL.reach(tq, tp) == 1)) {
					// if (mBFL.reach(tq, tp) == 1) {
					// if (tq.id!= hp.getValue().id && mBFL.reach(tq,
					// hp.getValue()) == 1) {
					entries.add(hp);
					if (missings.contains(qi.id))
						missings.remove(qi.id);

				}

			}
		}

		if (missings.isEmpty()) {
			cleanStack(sAct, tq);
			StackEntry stkEntry = moveStreamToStack(q, tq, sAct, sPar);
			// if (stkEntry == null)
			// return false;
			for (QNode qi : children) {
				ArrayList<PoolEntry> entries = candidateSet.get(qi.id);
				for (PoolEntry hp : entries) {
					// expand(q, stkEntry.getPoolEntry(), hp);
					expand(q, stkEntry, hp);

				}

			}

			if (q.isSink()) {

				// transferSolutions(q, stkEntry, null);
				// expand(q, stkEntry.getPoolEntry(), null);
				expand(q, stkEntry, null);
				sAct.pop();

			}
			return true;
		}
		return false;
	}

	private StackEntry moveStreamToStack(QNode qAct, GraphNode nodeAct, Stack<StackEntry> sAct,
			Stack<StackEntry> sPar) {

		StackEntry entryPar = null;
		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			entryPar = sPar.peek();
		}

		StackEntry entry = new StackEntry(qAct, nodeAct, entryPar);
		sAct.push(entry);
		return entry;
	}

	private StackEntry moveStreamToStack2(QNode qAct, GraphNode nodeAct, Stack<StackEntry> sAct,
			Stack<StackEntry> sPar) {

		StackEntry entryPar = null;
		boolean toAdd = true;
		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			int axis = qAct.E_I.get(0).axis;
			entryPar = sPar.peek();
			GraphNode nodePar = entryPar.getValue();
			if (axis == 0 && !nodePar.searchOUT(nodeAct.id)) {

				toAdd = false;

			}

		}

		StackEntry entry = null;
		if (toAdd) {
			entry = new StackEntry(qAct, nodeAct, entryPar);
			sAct.push(entry);
		}

		return entry;

	}

	private void expand2(QNode qAct, StackEntry stkAct, PoolEntry hpAct) {

		PoolEntry tpAct = stkAct.getPoolEntry();

		// modified on 2018.5.21, don't link a node to its self
		if (hpAct != null) {
			if (hpAct.getValue().id != tpAct.getValue().id)
				tpAct.addChild(hpAct);
			else
				return;
		}
		if (!tpAct.isInPool()) {

			Pool pAct = mPool.get(qAct.id);
			pAct.addEntry(tpAct);
			QNode qPar = null;
			Stack<StackEntry> sPar = null;
			if (qAct != mQuery.mRoot) {
				qPar = mQuery.getParent(qAct.id);
				sPar = mStacks.get(qPar.id);
				int axis = qAct.E_I.get(0).axis;
				StackEntry lastEntry = stkAct.getParent();
				// if (!sPar.empty()) {
				if (lastEntry != null) {
					int lastIdx = sPar.indexOf(lastEntry);
					if (axis == 0) {
						if (lastEntry.getValue().searchOUT(tpAct.getValue().id))
							expand2(qPar, lastEntry, tpAct);

					} else
						for (int i = 0; i <= lastIdx; i++) {
							StackEntry parEntry = sPar.get(i);
							expand2(qPar, parEntry, tpAct);
						}

				}
			}
		}

	}

	private void expand(QNode qAct, StackEntry stkAct, PoolEntry hpAct) {

		PoolEntry tpAct = stkAct.getPoolEntry();

		if (hpAct != null)
			tpAct.addChild(hpAct);

		if (!tpAct.isInPool()) {

			Pool pAct = mPool.get(qAct.id);
			pAct.addEntry(tpAct);
			QNode qPar = null;
			Stack<StackEntry> sPar = null;
			if (qAct != mQuery.mRoot) {
				qPar = mQuery.getParent(qAct.id);
				sPar = mStacks.get(qPar.id);
				int axis = qAct.E_I.get(0).axis;
				StackEntry lastEntry = stkAct.getParent();
				if (lastEntry != null) {
					int lastIdx = sPar.indexOf(lastEntry);
					for (int i = 0; i <= lastIdx; i++) {
						StackEntry parEntry = sPar.get(i);

						if ((axis == 1) || (axis == 0 && parEntry.getValue().searchOUT(tpAct.getValue().id)))
							expand(qPar, parEntry, tpAct);
					}

				}

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
