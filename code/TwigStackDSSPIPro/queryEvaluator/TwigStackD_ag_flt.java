/**
 * 
 */
package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import graph.Node;
import graph.SSPIndex;
import graph.dao.Cursor;
import graph.dao.ILMemCursor;
import graph.dao.Pool;
import graph.dao.PoolEntry;
import graph.dao.StackEntry;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

/**
 * @author xiaoying
 * descendant only; merge-join
 */
public class TwigStackD_ag_flt {

	TreeQuery mQuery;
	ArrayList<Stack<StackEntry>> mStacks;
	ArrayList<Cursor> mCursors;
	ArrayList<Pool> mPool;
	ArrayList<ArrayList<Node>> mInvLsts;
	double totSolns = 0;
	
	SSPIndex mSSPI;

	int V; // total number of graph nodes

	
	TimeTracker tt;

	
	public TwigStackD_ag_flt(TreeQuery query, ArrayList<ArrayList<Node>> invLsts, SSPIndex SSPI) {

		mQuery = query;
		mSSPI = SSPI;
		this.V = mSSPI.length();
		mInvLsts = invLsts;
		init();

	}
	
	public QueryEvalStat run(double flttm) {

		initCursors();
		tt.Start();
		twigStackD();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigStackD_SSPI_AG_flt:" + matm + "sec.");
		tt.Start();
		calTotSolns();
		double entm =  tt.Stop() / 1000; 
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		QueryEvalStat stat =  new QueryEvalStat(matm + flttm, entm, 0, totSolns);
		
		return stat;

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


	public void clear() {

		for (Pool p : mPool)
			p.clear();
	}

	protected void init() {
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
			if (minCur.getCurrent().encoding.mStart > nodeCur.getCurrent().encoding.mStart)
				minQ = node;

		}

		return minQ;
	}

	private void cleanStack(Stack<StackEntry> stack, Node actEntry) {

		while (!stack.empty() && stack.peek().getValue().encoding.mEnd < actEntry.encoding.mStart)
			stack.pop();

	}

	private boolean checkInSync(QNode childq, Node tparent) {

		Cursor qCur = mCursors.get(childq.id);
		qCur.savePosition();

		// "==" happens when the two nodes coincide
		while (qCur.getCurrent().encoding.mStart <= tparent.encoding.mStart)
			qCur.advance();

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
			boolean found = false;
			Pool pool = mPool.get(qi.id);
			for (PoolEntry h : pool.elist()) {
				if (mSSPI.reachBySSPI(tq, h.getValue())) {
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

					if (mSSPI.reachBySSPI(tq, tqi) && !checkMissingDEsBySSPI(qi, tqi))
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

	private boolean sweepPartialSolutions(QNode q, Node tq, HashSet<Integer> missings, Stack<StackEntry> sAct,
			Stack<StackEntry> sPar) {

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		HashMap<Integer, ArrayList<PoolEntry>> candidateSet = new HashMap<Integer, ArrayList<PoolEntry>>();
		for (QNode qi : children) {
			Pool pqi = mPool.get(qi.id);
			ArrayList<PoolEntry> entries = new ArrayList<PoolEntry>(1);
			candidateSet.put(qi.id, entries);
			for (PoolEntry hp : pqi.elist()) {

				if (mSSPI.reachBySSPI(tq, hp.getValue())) {
					entries.add(hp);
					if (missings.contains(qi.id))
						missings.remove(qi.id);

				}
			}
		}

		if (missings.isEmpty()) {
			cleanStack(sAct, tq);
			StackEntry stkEntry = moveStreamToStack(q, tq, sAct, sPar);
			for (QNode qi : children) {
				ArrayList<PoolEntry> entries = candidateSet.get(qi.id);
				for (PoolEntry hp : entries) {
					expand(q, stkEntry.getPoolEntry(), hp);

				}

			}

			if (q.isSink()) {

				// transferSolutions(q, stkEntry, null);
				expand(q, stkEntry.getPoolEntry(), null);
				sAct.pop();

			}
			return true;
		}
		return false;
	}

	private StackEntry moveStreamToStack(QNode qAct, Node nodeAct, Stack<StackEntry> sAct, Stack<StackEntry> sPar) {

		StackEntry entryPar = null;

		if (qAct != mQuery.mRoot && !sPar.isEmpty()) {
			entryPar = sPar.peek();
		}

		StackEntry entry = new StackEntry(qAct, nodeAct, entryPar);
		sAct.push(entry);
		return entry;

	}

	private void expand(QNode qAct, PoolEntry tpAct, PoolEntry hpAct) {

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
				if (!sPar.empty()) {
					for (int i = 0; i < sPar.size(); i++) {
						StackEntry parEntry = sPar.get(i);
						PoolEntry parPoolEntry = parEntry.getPoolEntry();
						expand(qPar, parPoolEntry, tpAct);
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
