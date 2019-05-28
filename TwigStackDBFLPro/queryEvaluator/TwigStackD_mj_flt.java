/**
 * 
 */
package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import dao.BFLIndex;
import dao.Cursor;
import dao.ILMemCursor;
import dao.PoolMJ;
import dao.PoolEntryMJ;
import dao.StackEntryMJ;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.TPQSolutionHandler;
import helper.TPQSolutionListComparator;
import helper.TPQSolutionListFormat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

/**
 * @author xiaoying
 *
 */
public class TwigStackD_mj_flt {

	TreeQuery mQuery;
	ArrayList<Stack<StackEntryMJ>> mStacks;
	ArrayList<Cursor> mCursors;
	ArrayList<PoolMJ> mPool;
	int[] mParent;

	int V; // total number of graph nodes

	// the following variables are for enumerating query solutions.
	HashMap<Integer, ArrayList<TPQSolutionListFormat>> mPathSolns;
	ArrayList<TPQSolutionListFormat> mTPQSolns;

	BFLIndex mBFL;
	ArrayList<ArrayList<GraphNode>> mInvLsts;
	TimeTracker tt;

	// maps each node to the query paths it belongs
	ArrayList<Integer>[] mPathIndex;

	public TwigStackD_mj_flt(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		this.V = mBFL.length();
		mInvLsts = invLsts;
		init();

	}

	public void clear() {

		for (PoolMJ p : mPool)
			p.clear();
	}

	public QueryEvalStat run(double flttm) {
		double matm = 0.0, entm = 0.0, jntm = 0.0;
		int numSolns = 0;
		boolean success = initCursors();
		if (success) {
			tt.Start();
			twigStackD();
			matm = tt.Stop() / 1000;
			System.out.println("Time on TwigStackD_BFL_MJ_flt:" + matm + "sec.");
			tt.Start();
			// showSolutions();
			boolean hasSolns = enumSolutions();
			entm = tt.Stop() / 1000;
			System.out.println("Time on EnumSolutions:" + entm + "sec.");

			if (hasSolns) {
				tt.Start();
				tpqMultiwayMergeJoin();
				jntm = tt.Stop() / 1000;
				System.out.println("Time on MultiwayMergeJoin:" + jntm + "sec.");
				numSolns = mTPQSolns.size();
				// printTPQSolns();
			} else
				System.out.println("Query has empty solutions!");
		}
		QueryEvalStat stat = new QueryEvalStat(matm + flttm, entm, jntm, numSolns);

		return stat;
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

	public void printTPQSolns() {

		System.out.println("TPQ solutions: ");

		for (TPQSolutionListFormat soln : mTPQSolns) {
			System.out.println("\t" + soln);

		}

		System.out.println("total number of solutions: " + mTPQSolns.size());
	}

	protected void init() {
		mQuery.extractQueryInfo_mj();
		mParent = mQuery.mParents;
		mPathIndex = mQuery.getPathIndices();
		int size = mQuery.V;
		mCursors = new ArrayList<Cursor>(size);
		mStacks = new ArrayList<Stack<StackEntryMJ>>(size);
		mPool = new ArrayList<PoolMJ>(size);
		Collections.fill(mCursors, null);
		for (int i = 0; i < size; i++) {

			mStacks.add(new Stack<StackEntryMJ>());
			mPool.add(new PoolMJ());
		}

		mPathSolns = new HashMap<Integer, ArrayList<TPQSolutionListFormat>>();
		initCursors();
		tt = new TimeTracker();
	}

	/**********************************
	 * 
	 * multiway mergejoin goes here
	 ********************************** 
	 */

	protected void tpqMultiwayMergeJoin() {

		if (mQuery.mLeaves.size() == 1) {

			mTPQSolns = mPathSolns.get(mQuery.mLeaves.get(0));
			return; // single partial path query, no need to merge
		}

		HashMap<Integer, ArrayList<TPQSolutionListFormat>> mergeJoinResults = new HashMap<Integer, ArrayList<TPQSolutionListFormat>>();
		tpqMultiwayMergeJoin(0, mergeJoinResults);

		mTPQSolns = mergeJoinResults.get(0);
		mergeJoinResults.clear();
	}

	private void tpqMultiwayMergeJoin(int q, HashMap<Integer, ArrayList<TPQSolutionListFormat>> mergeJoinResults) {

		ArrayList<Integer> pathIndices = mPathIndex[q];

		if (pathIndices.size() == 1)
			return; // don't need to do merge join for one path solution list

		ArrayList<Integer> children = mQuery.getChildrenIDs(q);

		for (int i = 0; i < children.size(); i++) {

			int child = children.get(i);
			tpqMultiwayMergeJoin(child, mergeJoinResults);
		}

		if (children.size() > 1) {
			// merge join the solution lists of its children.
			LinkedList<ArrayList<TPQSolutionListFormat>> joinQueue = getJoinLists(q, mergeJoinResults);

			ArrayList<TPQSolutionListFormat> lhs = joinQueue.poll();
			ArrayList<Integer> fldList = getAncList(q);
			while (!joinQueue.isEmpty()) {
				ArrayList<TPQSolutionListFormat> rhs = joinQueue.poll();

				lhs = TPQSolutionHandler.mergeJoin(lhs, rhs, fldList, mQuery.V);
				rhs.clear();// dispose the space
			}

			mergeJoinResults.put(q, lhs);
			// lhs.clear();
		} else if (pathIndices.size() > 1) {
			// obtain the solution list from its single child
			mergeJoinResults.put(q, mergeJoinResults.get(children.get(0)));
		}

	}

	private LinkedList<ArrayList<TPQSolutionListFormat>> getJoinLists(int q,
			HashMap<Integer, ArrayList<TPQSolutionListFormat>> mergeJoinResults) {

		LinkedList<ArrayList<TPQSolutionListFormat>> queue = new LinkedList<ArrayList<TPQSolutionListFormat>>();

		ArrayList<Integer> ancListCur = getAncList(q);

		TPQSolutionListComparator comparator = new TPQSolutionListComparator(ancListCur);

		ArrayList<Integer> children = mQuery.getChildrenIDs(q);

		for (int child : children) {

			ArrayList<Integer> pathIndices = mPathIndex[child];
			if (pathIndices.size() == 1) {
				int pathIndex = pathIndices.get(0);
				ArrayList<TPQSolutionListFormat> solnList = mPathSolns.get(pathIndex); // a
				mPathSolns.put(pathIndex, null);
				Collections.sort(solnList, comparator);
				queue.add(solnList);
				continue;

			}

			ArrayList<TPQSolutionListFormat> solnList = mergeJoinResults.remove(child);
			queue.add(solnList);
		}

		return queue;
	}

	// get the ancestors of q in the preorder
	private ArrayList<Integer> getAncList(int q) {

		ArrayList<Integer> ancestors = new ArrayList<Integer>();

		while (q != -1) {

			ancestors.add(q);
			q = mParent[q];
		}

		ArrayList<Integer> ancestorInOrder = new ArrayList<Integer>(ancestors.size());
		for (int i = ancestors.size() - 1; i >= 0; i--) {

			ancestorInOrder.add(ancestors.get(i));
		}

		ancestors.clear();
		return ancestorInOrder;
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
			Stack<StackEntryMJ> sPar = null, sAct = mStacks.get(qAct.id);
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

	private void cleanStack(Stack<StackEntryMJ> stack, GraphNode actEntry) {

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
				PoolMJ pool = mPool.get(qi.id);
				for (PoolEntryMJ h : pool.elist()) {
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
					// while (qiCur.getCurrent().L_interval.mEnd <
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

	private boolean sweepPartialSolutions(QNode q, GraphNode tq, HashSet<Integer> missings, Stack<StackEntryMJ> sAct,
			Stack<StackEntryMJ> sPar) {
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		HashMap<Integer, ArrayList<PoolEntryMJ>> candidateSet = new HashMap<Integer, ArrayList<PoolEntryMJ>>();

		for (QNode qi : children) {
			PoolMJ pqi = mPool.get(qi.id);
			int axis = qi.E_I.get(0).axis;
			ArrayList<PoolEntryMJ> entries = new ArrayList<PoolEntryMJ>(1);
			candidateSet.put(qi.id, entries);
			for (PoolEntryMJ hp : pqi.elist()) {

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
			StackEntryMJ stkEntry = moveStreamToStack(q, tq, sAct, sPar);
			// if (stkEntry == null)
			// return false;
			for (QNode qi : children) {
				ArrayList<PoolEntryMJ> entries = candidateSet.get(qi.id);
				for (PoolEntryMJ hp : entries) {
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

	private StackEntryMJ moveStreamToStack(QNode qAct, GraphNode nodeAct, Stack<StackEntryMJ> sAct,
			Stack<StackEntryMJ> sPar) {

		StackEntryMJ entryPar = null;
		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			entryPar = sPar.peek();
		}

		StackEntryMJ entry = new StackEntryMJ(qAct.id, nodeAct, entryPar);
		sAct.push(entry);
		return entry;
	}

	private StackEntryMJ moveStreamToStack2(QNode qAct, GraphNode nodeAct, Stack<StackEntryMJ> sAct,
			Stack<StackEntryMJ> sPar) {

		StackEntryMJ entryPar = null;
		boolean toAdd = true;
		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			int axis = qAct.E_I.get(0).axis;
			entryPar = sPar.peek();
			GraphNode nodePar = entryPar.getValue();
			if (axis == 0 && !nodePar.searchOUT(nodeAct.id)) {

				toAdd = false;

			}

		}

		StackEntryMJ entry = null;
		if (toAdd) {
			entry = new StackEntryMJ(qAct.id, nodeAct, entryPar);
			sAct.push(entry);
		}

		return entry;

	}

	private void expand2(QNode qAct, StackEntryMJ stkAct, PoolEntryMJ hpAct) {

		PoolEntryMJ tpAct = stkAct.getPoolEntry();

		// modified on 2018.5.21, don't link a node to its self
		if (hpAct != null) {
			if (hpAct.getValue().id != tpAct.getValue().id)
				tpAct.addChild(hpAct);
			else
				return;
		}
		if (!tpAct.isInPool()) {

			PoolMJ pAct = mPool.get(qAct.id);
			pAct.addEntry(tpAct);
			QNode qPar = null;
			Stack<StackEntryMJ> sPar = null;
			if (qAct != mQuery.mRoot) {
				qPar = mQuery.getParent(qAct.id);
				sPar = mStacks.get(qPar.id);
				int axis = qAct.E_I.get(0).axis;
				StackEntryMJ lastEntry = stkAct.getParent();
				// if (!sPar.empty()) {
				if (lastEntry != null) {
					int lastIdx = sPar.indexOf(lastEntry);
					if (axis == 0) {
						if (lastEntry.getValue().searchOUT(tpAct.getValue().id))
							expand2(qPar, lastEntry, tpAct);

					} else
						for (int i = 0; i <= lastIdx; i++) {
							StackEntryMJ parEntry = sPar.get(i);
							expand2(qPar, parEntry, tpAct);
						}

				}
			}
		}

	}

	private void expand(QNode qAct, StackEntryMJ stkAct, PoolEntryMJ hpAct) {

		PoolEntryMJ tpAct = stkAct.getPoolEntry();

		if (hpAct != null)
			tpAct.addChild(hpAct);

		if (!tpAct.isInPool()) {

			PoolMJ pAct = mPool.get(qAct.id);
			pAct.addEntry(tpAct);
			QNode qPar = null;
			Stack<StackEntryMJ> sPar = null;
			if (qAct != mQuery.mRoot) {
				qPar = mQuery.getParent(qAct.id);
				sPar = mStacks.get(qPar.id);
				int axis = qAct.E_I.get(0).axis;
				StackEntryMJ lastEntry = stkAct.getParent();
				if (lastEntry != null) {
					int lastIdx = sPar.indexOf(lastEntry);
					for (int i = 0; i <= lastIdx; i++) {
						StackEntryMJ parEntry = sPar.get(i);

						if ((axis == 1) || (axis == 0 && parEntry.getValue().searchOUT(tpAct.getValue().id)))

							expand(qPar, parEntry, tpAct);
					}

				}

			}
		}

	}

	public boolean enumSolutions() {

		QNode root = mQuery.mRoot;
		PoolMJ rPool = mPool.get(root.id);
		ArrayList<PoolEntryMJ> elist = rPool.elist();
		if (elist.isEmpty())
			return false;
		Stack<PoolEntryMJ> s = new Stack<PoolEntryMJ>();
		for (PoolEntryMJ r : elist) {

			enumSolutions(r, s);

		}
		return true;
	}

	private void enumSolutions(PoolEntryMJ r, Stack<PoolEntryMJ> s) {

		s.push(r);
		ArrayList<PoolEntryMJ> children = r.getChildren();
		if (children.isEmpty()) {
			// leave node

			TPQSolutionListFormat pathSoln = new TPQSolutionListFormat(mQuery.V);

			int leaf = r.getQID();

			for (int i = 0; i < s.size(); i++) {
				PoolEntryMJ entry = s.get(i);
				pathSoln.addValue(entry.getQID(), entry.getValue().id);

			}

			addPathSoln(leaf, pathSoln);

		} else {

			for (PoolEntryMJ c : children) {
				if (r.getValue().id == c.getValue().id)
					continue;
				// check the axis
				QNode qAct = mQuery.getNode(c.getQID());
				int axis = qAct.E_I.get(0).axis;
				if ((axis == 0 && r.getValue().searchOUT(c.getValue().id)) || (axis == 1))

					enumSolutions(c, s);

			}

		}

		s.pop();
	}

	private void addPathSoln(int leaf, TPQSolutionListFormat pathSoln) {

		ArrayList<TPQSolutionListFormat> solnVect = mPathSolns.get(leaf);
		if (solnVect == null) {
			solnVect = new ArrayList<TPQSolutionListFormat>();
			mPathSolns.put(leaf, solnVect);
		}
		solnVect.add(pathSoln);

	}

	public void showSolutions() {

		QNode root = mQuery.mRoot;
		PoolMJ rPool = mPool.get(root.id);
		ArrayList<PoolEntryMJ> elist = rPool.elist();
		for (PoolEntryMJ r : elist) {

			showSolutions(r);
			System.out.println();
		}

	}

	private void showSolutions(PoolEntryMJ r) {
		if (r == null)
			return;
		System.out.print(r.getValue() + " ");
		ArrayList<PoolEntryMJ> children = r.getChildren();
		for (PoolEntryMJ c : children) {

			showSolutions(c);

		}

		System.out.print("-1 ");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
