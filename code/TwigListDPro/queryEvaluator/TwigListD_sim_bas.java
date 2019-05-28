package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import dao.BFLIndex;
import dao.MatList;
import dao.Pool;
import dao.PoolEntry;
import global.Flags;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

public class TwigListD_sim_bas {

	TreeQuery mQuery;
	ArrayList<MatList> mCandLists;
	ArrayList<Pool> mPool;
	BFLIndex mBFL;
	TimeTracker tt;
	double totSolns = 0, sizeOfAnsGraph = 0;
	double pruntm = 0.0;
	ArrayList<ArrayList<GraphNode>> mInvLsts;
	int V; // total number of graph nodes

	public TwigListD_sim_bas(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		this.V = mBFL.length();
		mInvLsts = invLsts;
		init();
	}

	public QueryEvalStat run() {
	
		tt.Start();
		findMatches();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigListDPro_sim_bas:" + matm + " sec.");

		tt.Start();

		// printSolutions();
		calTotSolns();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		calAnsGraphSize();
		QueryEvalStat stat = new QueryEvalStat(matm, entm, pruntm, totSolns, sizeOfAnsGraph);

		return stat;

	}
	
	public ArrayList<MatList> genOccLists(){
		
		QNode root = mQuery.mRoot;
		pruneBUP(root);
		pruneTDW(root);
		
		return mCandLists;
	}

	public void clear() {

		for (Pool p : mPool)
			p.clear();
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
	
	public void calAnsGraphSize(){
		
		for(Pool pl:mPool){
			sizeOfAnsGraph +=pl.elist().size(); //nodes
			for (PoolEntry e :pl.elist()){
				sizeOfAnsGraph+=e.getNumChildEnties(); //edges
				
			}
			
		}
	}

	private void findMatches() {

		QNode root = mQuery.mRoot;
		TimeTracker tt = new TimeTracker();
		tt.Start();
		pruneBUP(root);
		pruneTDW(root);
		pruntm = tt.Stop() / 1000;
		System.out.println("Time on pruning nodes:" + pruntm + "sec.");
		//printSize();
		findMatches(root);
	}

	private void printSize() {

		for (int i = 0; i < mCandLists.size(); i++) {
			MatList mli = mCandLists.get(i);
			LinkedList<GraphNode> elist = mli.elist();
			System.out.println(i+ ":" + elist.size());
		}
	}

	private void findMatches(QNode q) {

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		for (QNode qi : children) {
			findMatches(qi);
		}
		MatList mli = mCandLists.get(q.id);
		LinkedList<GraphNode> elist = mli.elist();
		for (GraphNode n : elist) {

			PoolEntry actEntry = new PoolEntry(q, n);
			toList(actEntry);
		}
	}

	private void toList(PoolEntry r) {

		if (!r.getQNode().isSink()) {

			addChildMatch(r);
		}

		Pool qAct = mPool.get(r.getQID());
		qAct.addEntry(r);

	}

	private void addChildMatch(PoolEntry r) {

		QNode q = r.getQNode();

		GraphNode tq = r.getValue();
		ArrayList<QNode> children = mQuery.getChildren(q.id);

		for (QNode qi : children) {
			Pool pqi = mPool.get(qi.id);
			int axis = qi.E_I.get(0).axis;
			for (PoolEntry hp : pqi.elist()) {
				GraphNode tp = hp.getValue();
				// added on 2018.5.21, skip the self-linking case
				if (tq.id == tp.id)
					continue;
				
				if (tq.L_interval.mEnd < tp.L_interval.mStart)
				    break;
				if (axis == 0) {
					//if (tq.findOUT(tp.id)) {
					if (tq.searchOUT(tp.id)) {	
						r.addChild(hp);

					}
				} else if (mBFL.reach(tq, tp) == 1) {

					r.addChild(hp);

				}
			}

		}

	}

	private void pruneTDW(QNode q) {

		if (q.isSink())
			return;

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		MatList mli = mCandLists.get(q.id);
		LinkedList<GraphNode> parlist = mli.elist();

		for (QNode qi : children) {

			findParMatch(parlist, qi);
			pruneTDW(qi);
		}

	}

	private void pruneBUP(QNode q) {

		if (q.isSink())
			return;

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		if (Flags.sortByCard) {

			//Collections.sort(children, QNode.AxisComparator);
			sortByCard(children);
		}
		
		for (QNode c : children) {
			pruneBUP(c);

		}

		MatList mli = mCandLists.get(q.id);
		LinkedList<GraphNode> elist = mli.elist();
		for (int i = elist.size() - 1; i >= 0; i--) {

			GraphNode qn = elist.get(i);
			boolean found = findChildMatch(qn, children);

			if (!found)
				elist.remove(i);
		}

	}
	
	private void sortByCard(ArrayList<QNode> children) {

		int[] ints = new int[children.size()];
		for (int i = 0; i < children.size(); i++) {
			QNode c = children.get(i);
			ints[i] = mCandLists.get(c.id).elist().size();

		}
		Collections.sort(children, (left, right) -> ints[children.indexOf(left)] - ints[children.indexOf(right)]);
	}

	private void findParMatch(LinkedList<GraphNode> parlist, QNode child) {

		MatList mli = mCandLists.get(child.id);
		LinkedList<GraphNode> elist = mli.elist();
		int axis = child.E_I.get(0).axis;
		for (int i = elist.size() - 1; i >= 0; i--) {

			GraphNode ni = elist.get(i);
			boolean found = false;
			for (GraphNode par : parlist) {
				if (ni.id == par.id)
					continue;
				if (axis == 0) {
					//if (par.findOUT(ni.id)) {
					if (par.searchOUT(ni.id)) {	
						found = true;
					}
				} else if (mBFL.reach(par, ni) == 1) {
					found = true;
				}

				if (found)
					break;
			}
			if (!found)
				elist.remove(i);

		}

	}

	private boolean findChildMatch(GraphNode qn, ArrayList<QNode> children) {

		for (QNode qi : children) {

			MatList mli = mCandLists.get(qi.id);
			boolean found = false;

			for (GraphNode ni : mli.elist()) {
				int axis = qi.E_I.get(0).axis;
				if (qn.id == ni.id)
					continue;
				// add this to short cut the checking
				if (qn.L_interval.mEnd < ni.L_interval.mStart) {
					if (!found) {
						return false;
					}
				}
				if (axis == 0) {
					//if (qn.findOUT(ni.id)) {
					if (qn.searchOUT(ni.id)) {	
						found = true;
					}
				} else if (mBFL.reach(qn, ni) == 1) {
					found = true;
				}
				if (found)
					break;

			}
			if (!found)
				return false;

		}

		return true;

	}

	private void init() {
		mQuery.extractQueryInfo();
		int size = mQuery.V;

		mCandLists = new ArrayList<MatList>(size);
		mPool = new ArrayList<Pool>(size);
		for (int i = 0; i < size; i++) {
			mPool.add(new Pool());
		}

		QNode[] nodes = mQuery.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<GraphNode> invLst = mInvLsts.get(n.lb);
			MatList mlist = new MatList();
			mlist.addList(invLst);
			mCandLists.add(n.id, mlist);
		}

		tt = new TimeTracker();
	}

	public static void main(String[] args) {

	}

}
