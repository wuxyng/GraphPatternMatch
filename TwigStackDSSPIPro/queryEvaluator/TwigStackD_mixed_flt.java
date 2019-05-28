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

import graph.Digraph;
import graph.Node;
import graph.SSPIndex;
import graph.dao.Cursor;
import graph.dao.ILMemCursor;
import graph.dao.PoolMJ;
import graph.dao.PoolEntryMJ;
import graph.dao.StackEntryMJ;
import helper.QueryEvalStat;
import helper.TPQSolutionHandler;
import helper.TPQSolutionListComparator;
import helper.TPQSolutionListFormat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

/**
 * @author xiaoying
 * descendant only; merge-join
 */
public class TwigStackD_mixed_flt {

	TreeQuery mQuery;
	ArrayList<Stack<StackEntryMJ>> mStacks;
	ArrayList<Cursor> mCursors;
	ArrayList<PoolMJ> mPool;
	ArrayList<ArrayList<Node>> mInvLsts;
    Digraph mG;
	
	SSPIndex mSSPI;

	int V; // total number of graph nodes

	// the following variables are for enumerating query solutions.
	HashMap<Integer, ArrayList<TPQSolutionListFormat>> mPathSolns;
	ArrayList<TPQSolutionListFormat> mTPQSolns;
	HashMap<Integer, ArrayList<Integer>> mChildren;
	Integer[] mParent;
	ArrayList<Integer> mLeaves;

	TimeTracker tt;

	// maps each node to the query paths it belongs
	HashMap<Integer, ArrayList<Integer>> mPathIndexMap;

	public TwigStackD_mixed_flt(TreeQuery query, ArrayList<ArrayList<Node>> invLsts, SSPIndex SSPI, Digraph G) {

		mQuery = query;
		mSSPI = SSPI;
		this.V = mSSPI.length();
		mInvLsts = invLsts;
		mG = G;
		init();

	}
	
	public QueryEvalStat run(double flttm) {

		initCursors();
		tt.Start();
		twigStackD();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigStackD_SSPI_mixed_flt:" + matm + "sec.");
		tt.Start();
		boolean hasSolns = enumSolutions();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on EnumSolutions:" + entm + "sec.");
		double jntm = 0.0;
		int numSolns = 0;

		if (hasSolns) {
			tt.Start();
			tpqMultiwayMergeJoin();
			jntm = tt.Stop() / 1000;
			System.out.println("Time on MultiwayMergeJoin:" + jntm + "sec.");
			numSolns = mTPQSolns.size();

			printTPQSolns();
		} else
			System.out.println("Query has empty solutions!");

		QueryEvalStat stat = new QueryEvalStat(matm + flttm, entm, jntm, numSolns);

		return stat;

	}


	public void printTPQSolns() {

		System.out.println("TPQ solutions: ");
/*
		for (TPQSolutionListFormat soln : mTPQSolns) {
			System.out.println("\t" + soln);

		}
*/
		System.out.println("total number of solutions: " + mTPQSolns.size());
	}

	public void clear() {

		for (PoolMJ p : mPool)
			p.clear();
	}

	protected void init() {
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
		extractPatternInfo();
		tt = new TimeTracker();
	}
	
	private void initCursors() {
		QNode[] nodes = mQuery.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<Node> invLst = mInvLsts.get(n.id);
			ILMemCursor cursor = new ILMemCursor(invLst);
			cursor.open();
			mCursors.add(n.id, cursor);
		}
	}


	private void extractPatternInfo() {

		mPathIndexMap = new HashMap<Integer, ArrayList<Integer>>();
		mChildren = new HashMap<Integer, ArrayList<Integer>>();
		mParent = new Integer[mQuery.V];
		mParent[0] = -1;
		for (int i = 0; i < mQuery.V; i++) {

			ArrayList<QNode> childNodes = mQuery.getChildren(i);
			ArrayList<Integer> children = new ArrayList<Integer>(childNodes.size());
			for (QNode child : childNodes)
				children.add(child.id);

			mChildren.put(i, children);
			// mParents.put(i, PatternHandler.get_parent(mPattern, i));
		}
		mLeaves = extractPatternInfo(0);
		// PatternHandler.get_leaves(mPattern);

	}

	private ArrayList<Integer> extractPatternInfo(int nid) {

		// the list that keeps the ids of the leaf nodes that belong to the
		// paths that this node participates
		ArrayList<Integer> pathIndices = new ArrayList<Integer>();
		ArrayList<Integer> children = mChildren.get(nid);

		for (int child : children) {

			ArrayList<Integer> subIndices = extractPatternInfo(child);
			pathIndices.addAll(subIndices);
			mParent[child] = nid;
		}

		// if the node is a leaf node of the pattern
		if (children.size() == 0) {

			// leaf nodes are used to distinguish among paths in the query

			ArrayList<TPQSolutionListFormat> solnList = new ArrayList<TPQSolutionListFormat>();
			mPathSolns.put(nid, solnList);
			// add the node in pathIndices
			pathIndices.add(nid);

		}

		mPathIndexMap.put(nid, pathIndices);

		return pathIndices;
	}

	/**********************************
	 * 
	 * multiway mergejoin goes here
	 ********************************** 
	 */

	protected void tpqMultiwayMergeJoin() {

		if (mLeaves.size() == 1) {

			mTPQSolns = mPathSolns.get(mLeaves.get(0));
			return; // single partial path query, no need to merge
		}

		HashMap<Integer, ArrayList<TPQSolutionListFormat>> mergeJoinResults = new HashMap<Integer, ArrayList<TPQSolutionListFormat>>();
		tpqMultiwayMergeJoin(0, mergeJoinResults);

		mTPQSolns = mergeJoinResults.get(0);
		mergeJoinResults.clear();
	}

	private void tpqMultiwayMergeJoin(int q, HashMap<Integer, ArrayList<TPQSolutionListFormat>> mergeJoinResults) {

		ArrayList<Integer> pathIndices = mPathIndexMap.get(q);

		if (pathIndices.size() == 1)
			return; // don't need to do merge join for one path solution list

		ArrayList<Integer> children = mChildren.get(q);

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

		ArrayList<Integer> children = mChildren.get(q);

		for (int child : children) {

			ArrayList<Integer> pathIndices = mPathIndexMap.get(child);
			if (pathIndices.size() == 1) {
				int pathIndex = pathIndices.get(0);
				ArrayList<TPQSolutionListFormat> solnList = mPathSolns.get(pathIndex); // a
																						// non-sorted
																						// path
																						// solutions.
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
			Node actEntry = curAct.getCurrent();

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
			if (minCur.getCurrent().encoding.mStart > nodeCur.getCurrent().encoding.mStart)
				minQ = node;

		}

		return minQ;
	}

	private void cleanStack(Stack<StackEntryMJ> stack, Node actEntry) {

		while (!stack.empty() && stack.peek().getValue().encoding.mEnd < actEntry.encoding.mStart)
			stack.pop();

	}

	private boolean checkInSync(QNode childq, Node tparent) {

		Cursor qCur = mCursors.get(childq.id);
		int axis = childq.E_I.get(0).axis;
		qCur.savePosition();

		// "==" happens when the two nodes coincide
		while (qCur.getCurrent().encoding.mStart <= tparent.encoding.mStart)
			qCur.advance();

		if (axis == 0) {
			boolean found = false;
			while (qCur.getCurrent().encoding.mStart < tparent.encoding.mEnd) {

				if (mG.linearSearchOUT(tparent.ID, qCur.getCurrent().ID)) {
					found = true;
					break;
				}
				qCur.advance();
			}

			qCur.restorePosition();
			return found;

		}
		
		
		Node tq = qCur.getCurrent();
		qCur.restorePosition();

		if (tq.encoding.mStart < tparent.encoding.mEnd) {
			return true;
		} else {
			return false;
		}

	}

	private boolean checkMissingDEsBySSPI(QNode q, Node tr) {

		Cursor qCur = mCursors.get(q.id);
		Node tq = qCur.getCurrent();

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		for (QNode qi : children) {
			int axis = qi.E_I.get(0).axis;
			boolean found = false;
			PoolMJ poolMJ = mPool.get(qi.id);
			for (PoolEntryMJ h : poolMJ.elist()) {
				if ((axis == 0 && mG.linearSearchOUT(tq.ID, h.getValue().ID)) || (axis == 1 && mSSPI.reachBySSPI(tq, h.getValue()))) {
					found = true;
					break;
				}
				
			}
			if (!found) {

				Cursor qiCur = mCursors.get(qi.id);
				qiCur.savePosition();

				while (qiCur.getCurrent().encoding.mStart <= tr.encoding.mStart)
					qiCur.advance();

				// Change tq to tr, modified on 2018.06.01
				// while (qiCur.getCurrent().encoding.mEnd < tq.encoding.mEnd &&
				// !found) {
				while (qiCur.getCurrent().encoding.mEnd <= tr.encoding.mEnd && !found) {
					Node tqi = qiCur.getCurrent();
					if (tqi.ID == tq.ID) {

						qiCur.advance();
						continue;
					}

					if (((axis == 0 && mG.linearSearchOUT(tq.ID, tqi.ID)) || (axis == 1 && mSSPI.reachBySSPI(tq, tqi))) 
							&& !checkMissingDEsBySSPI(qi, tqi))
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

		return false;

	}

	private HashSet<Integer> getMissings(QNode q, Node tq) {

		HashSet<Integer> missings = new HashSet<Integer>();
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		for (QNode qi : children) {

			Cursor qiCur = mCursors.get(qi.id);
			if (checkInSync(qi, tq)) {
				boolean allInSync = false;
				qiCur.savePosition();
				while (qiCur.getCurrent().encoding.mStart < tq.encoding.mEnd) {
					if (checkMissingDEsBySSPI(qi, tq))
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

	private boolean sweepPartialSolutions(QNode q, Node tq, HashSet<Integer> missings, Stack<StackEntryMJ> sAct,
			Stack<StackEntryMJ> sPar) {

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		HashMap<Integer, ArrayList<PoolEntryMJ>> candidateSet = new HashMap<Integer, ArrayList<PoolEntryMJ>>();
		for (QNode qi : children) {
			PoolMJ pqi = mPool.get(qi.id);
			int axis = qi.E_I.get(0).axis;
			ArrayList<PoolEntryMJ> entries = new ArrayList<PoolEntryMJ>(1);
			candidateSet.put(qi.id, entries);
			for (PoolEntryMJ hp : pqi.elist()) {
				Node tp = hp.getValue();
				if (tq.ID == tp.ID)
					continue;
				if ((axis == 0 && mG.linearSearchOUT(tq.ID, tp.ID)) || 
					(axis == 1 && mSSPI.reachBySSPI(tq, tp))){
					entries.add(hp);
					if (missings.contains(qi.id))
						missings.remove(qi.id);

				}
			}
		}

		if (missings.isEmpty()) {
			cleanStack(sAct, tq);
			StackEntryMJ stkEntry = moveStreamToStack(q, tq, sAct, sPar);
			for (QNode qi : children) {
				ArrayList<PoolEntryMJ> entries = candidateSet.get(qi.id);
				for (PoolEntryMJ hp : entries) {
					//expand(q, stkEntry.getPoolEntry(), hp);
					expand(q, stkEntry, hp);
				}

			}

			if (q.isSink()) {

				// transferSolutions(q, stkEntry, null);
				//expand(q, stkEntry.getPoolEntry(), null);
				expand(q, stkEntry, null);
				sAct.pop();

			}
			return true;
		}
		return false;
	}

	private StackEntryMJ moveStreamToStack(QNode qAct, Node nodeAct, Stack<StackEntryMJ> sAct, Stack<StackEntryMJ> sPar) {

		StackEntryMJ entryPar = null;

		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			entryPar = sPar.peek();
		}

		StackEntryMJ entry = new StackEntryMJ(qAct.id, nodeAct, entryPar);
		sAct.push(entry);
		return entry;

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
						if ((axis == 1) ||
							(axis == 0 && mG.linearSearchOUT(parEntry.getValue().ID,tpAct.getValue().ID)))
							expand(qPar, parEntry, tpAct);
					}

				}

			}
		}

	}

	

	private void expand(QNode qAct, PoolEntryMJ tpAct, PoolEntryMJ hpAct) {

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
				if (!sPar.empty()) {
					for (int i = 0; i < sPar.size(); i++) {
						StackEntryMJ parEntry = sPar.get(i);
						PoolEntryMJ parPoolEntry = parEntry.getPoolEntry();
						expand(qPar, parPoolEntry, tpAct);
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
				pathSoln.addValue(entry.getQID(), entry.getValue().ID);

			}

			addPathSoln(leaf, pathSoln);

		} else {

			for (PoolEntryMJ c : children) {
				if (r.getValue().ID == c.getValue().ID)
					continue;
				// check the axis
				QNode qAct = mQuery.getNode(c.getQID());
				int axis = qAct.E_I.get(0).axis;
				if ((axis == 0 && mG.linearSearchOUT(r.getValue().ID,c.getValue().ID)) || (axis == 1))

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
