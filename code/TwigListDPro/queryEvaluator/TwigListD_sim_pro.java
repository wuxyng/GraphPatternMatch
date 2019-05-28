package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.roaringbitmap.RoaringBitmap;

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

/*
 *  *intersect for each PC edge separately
 * PC nodes sorted by id values, DC nodes sorted by begin values.
 * 
 */
public class TwigListD_sim_pro {

	TreeQuery mQuery;
	ArrayList<MatList> mCandLists;
	ArrayList<Pool> mPool;
	BFLIndex mBFL;
	TimeTracker tt;
	double totSolns = 0, sizeOfAnsGraph = 0;
	ArrayList<ArrayList<GraphNode>> mInvLsts, mInvLstsByID;
	int V; // total number of graph nodes

	public TwigListD_sim_pro(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		this.V = mBFL.length();
		mInvLsts = invLsts;
		init();
	}

	public TwigListD_sim_pro(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts,
			ArrayList<ArrayList<GraphNode>> invLstsByID, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		this.V = mBFL.length();
		mInvLsts = invLsts;
		mInvLstsByID = invLstsByID;
		init();
	}

	public QueryEvalStat run() {
	
		tt.Start();
		findMatches();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigListDPro_sim_pro:" + matm + " sec.");

		tt.Start();

		// printSolutions();
		calTotSolns();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		calAnsGraphSize();
		QueryEvalStat stat = new QueryEvalStat(matm, entm, 0, totSolns, sizeOfAnsGraph);

		return stat;

	}

	public ArrayList<MatList> genOccLists() {

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
		double pruntm = tt.Stop() / 1000;
		System.out.println("Time on pruning nodes:" + pruntm + "sec.");
		// printSize();
		findMatches(root);
		//findMatchesNoBits(root);
	}

	private void printSize() {

		for (int i = 0; i < mCandLists.size(); i++) {
			MatList mli = mCandLists.get(i);
			LinkedList<GraphNode> elist = mli.elist();
			System.out.println(i + ":" + elist.size());
		}
	}

	private void findMatches(QNode q) {

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		RoaringBitmap[] tBitsIdxArr = new RoaringBitmap[children.size()];
		int idx = 0;
		for (QNode qi : children) {
			findMatches(qi);
			int axis = qi.E_I.get(0).axis;
			ArrayList<PoolEntry> targets = mPool.get(qi.id).elist();
			if (axis == 0) {
				RoaringBitmap t_bits = new RoaringBitmap();
				for (PoolEntry e : targets)
					t_bits.add(e.getValue().id);
				tBitsIdxArr[idx] = t_bits;

			}
			idx++;
		}
		MatList mli = mCandLists.get(q.id);
		LinkedList<GraphNode> elist = mli.elist();
		for (GraphNode n : elist) {

			PoolEntry actEntry = new PoolEntry(q, n);
			// toList(actEntry);
			toList(actEntry, tBitsIdxArr);
		}
	}

	private void findMatchesNoBits(QNode q) {

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

	private void toList(PoolEntry r, RoaringBitmap[] tBitsIdxArr) {

		if (!r.getQNode().isSink()) {

			addChildMatch(r, tBitsIdxArr);
		}

		Pool qAct = mPool.get(r.getQID());
		qAct.addEntry(r);

	}

	private void toList(PoolEntry r) {

		if (!r.getQNode().isSink()) {

			addChildMatch(r);
		}

		Pool qAct = mPool.get(r.getQID());
		qAct.addEntry(r);

	}

	private void addChildMatch(PoolEntry r, RoaringBitmap[] tBitsIdxArr) {
		int c = 0;
		QNode q = r.getQNode();
		GraphNode tq = r.getValue();
		ArrayList<QNode> children = mQuery.getChildren(q.id);

		for (QNode qi : children) {

			Pool pqi = mPool.get(qi.id);
			int axis = qi.E_I.get(0).axis;
			if (axis == 0) {
				addChildMatch(r, tBitsIdxArr[c], pqi.elist());

			} else

				for (PoolEntry hp : pqi.elist()) {
					GraphNode tp = hp.getValue();
					// added on 2018.5.21, skip the self-linking case
					if (tq.id == tp.id)
						continue;
					// this short-cut does not reduce time
					if (tq.L_interval.mEnd < tp.L_interval.mStart)
						break;
					if (mBFL.reach(tq, tp) == 1) {

						r.addChild(hp);

					}
				}
			c++;
		}

	}

	private boolean addChildMatch(PoolEntry r, RoaringBitmap t_bits, ArrayList<PoolEntry> list) {

		GraphNode s = r.getValue();

		if (s.N_O_SZ == 0)
			return false;

		RoaringBitmap s_bits = new RoaringBitmap();

		for (int c : s.N_O) {

			s_bits.add(c);
		}

		RoaringBitmap rs_and = RoaringBitmap.and(s_bits, t_bits);

		if (rs_and.isEmpty())
			return false;

		for (int ti : rs_and) {
			PoolEntry e = list.get(t_bits.rank(ti) - 1);
			r.addChild(e);
		}

		return true;
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
				// this short-cut does not reduce time
				if (tq.L_interval.mEnd < tp.L_interval.mStart)
					break;
				if (axis == 0) {
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

		RoaringBitmap sbits = null;
		for (QNode qi : children) {
			int axis = qi.E_I.get(0).axis;
			if (axis == 0 && sbits == null) {

				sbits = getParbits(parlist);

			}
			findParMatch(parlist, sbits, qi);
			pruneTDW(qi);
		}

	}

	private void pruneTDW2(QNode q) {

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

	private RoaringBitmap getParbits(LinkedList<GraphNode> parlist) {

		RoaringBitmap sbits = new RoaringBitmap();
		for (GraphNode s : parlist) {
			for (int c : s.N_O) {

				sbits.add(c);
			}

		}
		return sbits;
	}

	private void pruneBUP(QNode q) {

		if (q.isSink())
			return;

		ArrayList<QNode> children = mQuery.getChildren(q.id);
		if (Flags.sortByCard) {

			//Collections.sort(children, QNode.AxisComparator);
			sortByCard(children);
		}

		RoaringBitmap[] tBitsIdxArr = new RoaringBitmap[children.size()];
		int idx = 0;
		for (QNode c : children) {
			pruneBUP(c);
			int axis = c.E_I.get(0).axis;
			LinkedList<GraphNode> targets = mCandLists.get(c.id).elist();
			if (axis == 0) {
				RoaringBitmap t_bits = new RoaringBitmap();
				for (GraphNode e : targets)
					t_bits.add(e.id);
				tBitsIdxArr[idx] = t_bits;

			}
			idx++;
		}

		MatList mli = mCandLists.get(q.id);
		LinkedList<GraphNode> elist = mli.elist();
		for (int i = elist.size() - 1; i >= 0; i--) {

			GraphNode qn = elist.get(i);
			boolean found = findChildMatch(qn, children, tBitsIdxArr);

			if (!found)
				elist.remove(i);
		}

	}

	private void findParMatch(LinkedList<GraphNode> parlist, RoaringBitmap sbits, QNode child) {

		MatList mli = mCandLists.get(child.id);
		LinkedList<GraphNode> elist = mli.elist();
		int axis = child.E_I.get(0).axis;

		for (int i = elist.size() - 1; i >= 0; i--) {

			GraphNode ni = elist.get(i);
			boolean found = false;

			if (axis == 0) {
				found = sbits.contains(ni.id);
			} else

				for (GraphNode par : parlist) {
					if (ni.id == par.id)
						continue;
					if (mBFL.reach(par, ni) == 1) {
						found = true;
					}

					if (found)
						break;
				}
			if (!found)
				elist.remove(i);

		}

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
	
	private void sortByCard(ArrayList<QNode> children) {

		int[] ints = new int[children.size()];
		for (int i = 0; i < children.size(); i++) {
			QNode c = children.get(i);
			ints[i] = mCandLists.get(c.id).elist().size();

		}
		Collections.sort(children, (left, right) -> ints[children.indexOf(left)] - ints[children.indexOf(right)]);
	}


	private boolean findChildMatch(GraphNode qn, ArrayList<QNode> children, RoaringBitmap[] tBitsIdxArr) {
		int c = 0;
		for (QNode qi : children) {

			MatList mli = mCandLists.get(qi.id);
			boolean found = false;
			int axis = qi.E_I.get(0).axis;

			if (axis == 0) {
				found = checkChildMatch(qn, tBitsIdxArr[c]);
			} else

				for (GraphNode ni : mli.elist()) {

					if (qn.id == ni.id)
						continue;
					// add this to short cut the checking
					if (qn.L_interval.mEnd < ni.L_interval.mStart) {
						if (!found) {
							return false;
						}
					}
					if (mBFL.reach(qn, ni) == 1) {
						found = true;
						break;
					}

				}
			if (!found)
				return false;
			c++;
		}

		return true;

	}

	private boolean checkChildMatch(GraphNode s, RoaringBitmap t_bits) {

		if (s.N_O_SZ == 0)
			return false;

		RoaringBitmap s_bits = new RoaringBitmap();

		for (int c : s.N_O) {

			s_bits.add(c);
		}

		return RoaringBitmap.andCardinality(s_bits, t_bits) > 0 ? true : false;

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
			if (!n.isSource() && n.E_I.get(0).axis == 0) {

				// Collections.sort(invLst, GraphNode.NodeIDComparator);
				invLst = mInvLstsByID.get(n.lb);
			}
			MatList mlist = new MatList();
			mlist.addList(invLst);
			mCandLists.add(n.id, mlist);
		}

		tt = new TimeTracker();
	}

	public static void main(String[] args) {

	}

}
