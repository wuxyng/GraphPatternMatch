package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;

import dao.BFLIndex;
import dao.Pool;
import dao.PoolEntry;
import global.Flags;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

public class TwigListD_bup_bas_flt {

	TreeQuery mQuery;
	ArrayList<Pool> mPool;

	BFLIndex mBFL;
	TimeTracker tt;

	ArrayList<ArrayList<GraphNode>> mInvLsts;
	double totSolns = 0, sizeOfAnsGraph = 0;
	

	public TwigListD_bup_bas_flt(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		mInvLsts = invLsts;
		init();
	}

	public void clear() {

		for (Pool p : mPool)
			p.clear();
	}

	public QueryEvalStat run(double flttm) {
		
		tt.Start();
		// twigListD();
		traverseBUP();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigListD_bup_bas_flt:" + matm + "sec.");
		tt.Start();

		//printSolutions();
		calTotSolns();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		//calAnsGraphSize();
		QueryEvalStat stat = new QueryEvalStat(matm, entm, flttm, totSolns);
	
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

	
	public void calAnsGraphSize(){
		// it may include non-solution entries for leave query nodes
		for(Pool pl:mPool){
			sizeOfAnsGraph +=pl.elist().size(); //nodes
			for (PoolEntry e :pl.elist()){
				sizeOfAnsGraph+=e.getNumChildEnties(); //edges
				
			}
			
		}
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

	private void init() {
		mQuery.extractQueryInfo();
		int size = mQuery.V;
		mPool = new ArrayList<Pool>(size);

		QNode[] nodes = mQuery.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode q = nodes[i];
			Pool pool = new Pool();
			mPool.add(q.id, pool);
			if (q.isSink()) {

				ArrayList<GraphNode> invLst = mInvLsts.get(q.id);
				for (GraphNode n : invLst) {
					PoolEntry e = new PoolEntry(q, n);
					pool.addEntry(e);
				}

			}

		}

		tt = new TimeTracker();

	}

	/****************************
	 * TwigListD alg starts here
	 ****************************/

	private void traverseBUP() {
		QNode root = mQuery.mRoot;
		traverseBUP(root);
	}

	private void traverseBUP(QNode q) {

		if (q.isSink())
			return;

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		
		for (QNode c : children) {
			traverseBUP(c);

		}

		ArrayList<GraphNode> invLst = mInvLsts.get(q.id);
		Pool qAct = mPool.get(q.id);

		for (GraphNode qn : invLst) {
			
			PoolEntry actEntry = new PoolEntry(q, qn);
			boolean found = checkChildMatch(actEntry);
			if (found)
				qAct.addEntry(actEntry);
     	
		}

	}

	private void printEntries(ArrayList<PoolEntry> list) {

		for (PoolEntry e : list) {
			System.out.println(e.getValue());

		}
	}

	private boolean checkChildMatch(PoolEntry r) {

		QNode q = r.getQNode();

		GraphNode tq = r.getValue();
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		if (Flags.sortByCard) {

			//Collections.sort(children, QNode.AxisComparator);
			sortByCard(children);
		}

		
		for (QNode qi : children) {

			Pool pqi = mPool.get(qi.id);
			boolean found = false;
			int axis = qi.E_I.get(0).axis;
			for (PoolEntry hp : pqi.elist()) {
				GraphNode tp = hp.getValue();
				
		
				// added on 2018.5.21, skip the self-linking case
				if (tq.id == tp.id)
					continue;
				// add this to short cut the checking

				if (tq.L_interval.mEnd < tp.L_interval.mStart) {

					break;

				}

				if (axis == 0) {
					//if (tq.findOUT(tp.id)) {
					if (tq.searchOUT(tp.id)) {
						r.addChild(hp);

						found = true;

					}
				} else if (mBFL.reach(tq, tp) == 1) {

					r.addChild(hp);

					found = true;
				}

			}

			if (!found)
				return false; // don't need to continue;
		}

		return true;
	}
	
	private void sortByCard(ArrayList<QNode> children) {

		int[] ints = new int[children.size()];
		for (int i = 0; i < children.size(); i++) {
			QNode c = children.get(i);
			ints[i] = mPool.get(c.id).elist().size();

		}
		Collections.sort(children, (left, right) -> ints[children.indexOf(left)] - ints[children.indexOf(right)]);
	}

	public static void main(String[] args) {

	}

}
