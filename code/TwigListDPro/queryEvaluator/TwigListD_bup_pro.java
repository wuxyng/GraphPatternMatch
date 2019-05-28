package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.roaringbitmap.RoaringBitmap;

import dao.BFLIndex;
import dao.Pool;
import dao.PoolEntry;
import global.Flags;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

/******
 * 
 * optimized on the child query edge matching
 * 
 * @author xiaoying intersect for each PC edge separately PC nodes sorted by id
 *         values, DC nodes sorted by begin values.
 */
public class TwigListD_bup_pro {

	TreeQuery mQuery;
	ArrayList<Pool> mPool;

	BFLIndex mBFL;
	TimeTracker tt;

	GraphNode[] nodes;

	ArrayList<ArrayList<GraphNode>> mInvLsts, mInvLstsByID;
	double totSolns = 0, sizeOfAnsGraph = 0;

	boolean sortByCard = false;

	public TwigListD_bup_pro(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		mInvLsts = invLsts;
		nodes = mBFL.getGraphNodes();
		init();
	}

	public TwigListD_bup_pro(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts,
			ArrayList<ArrayList<GraphNode>> invLstsByID, BFLIndex bfl) {

		mQuery = query;
		mBFL = bfl;
		mInvLsts = invLsts;
		mInvLstsByID = invLstsByID;
		nodes = mBFL.getGraphNodes();
		init();
	}

	public void clear() {

		for (Pool p : mPool)
			p.clear();
	}

	public QueryEvalStat run() {
		
		tt.Start();
		// twigListD();
		traverseBUP();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigListD_bup_pro:" + matm + "sec.");
		tt.Start();

		// printSolutions();
		calTotSolns();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		calAnsGraphSize();
		QueryEvalStat stat = new QueryEvalStat(matm, entm, 0, totSolns, sizeOfAnsGraph);

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

	public void calAnsGraphSize() {

		for (Pool pl : mPool) {
			sizeOfAnsGraph +=pl.elist().size(); //nodes
			for (PoolEntry e : pl.elist()) {
				sizeOfAnsGraph += e.getNumChildEnties();

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
			ArrayList<GraphNode> invLst = mInvLsts.get(q.lb);
			if (!q.isSource() && q.E_I.get(0).axis == 0) {

				// Collections.sort(invLst, GraphNode.NodeIDComparator);
				invLst = mInvLstsByID.get(q.lb);
			}
			Pool pool = new Pool();
			mPool.add(q.id, pool);
			if (q.isSink()) {

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
		if (Flags.sortByCard) {

			//Collections.sort(children, QNode.AxisComparator);
			sortByCard(children);
		}

		RoaringBitmap[] tBitsIdxArr = new RoaringBitmap[children.size()];
		int i = 0;
		for (QNode c : children) {
			traverseBUP(c);
			int axis = c.E_I.get(0).axis;
			ArrayList<PoolEntry> targets = mPool.get(c.id).elist();
			if (axis == 0) {
				RoaringBitmap t_bits = new RoaringBitmap();
				for (PoolEntry e : targets)
					t_bits.add(e.getValue().id);
				tBitsIdxArr[i] = t_bits;

			}
			i++;
		}

		ArrayList<GraphNode> invLst = mInvLsts.get(q.lb);
		if (!q.isSource() && q.E_I.get(0).axis == 0) {

			// Collections.sort(invLst, GraphNode.NodeIDComparator);
			invLst = mInvLstsByID.get(q.lb);
		}

		Pool qAct = mPool.get(q.id);

		for (GraphNode qn : invLst) {

			PoolEntry actEntry = new PoolEntry(q, qn);
			boolean found = checkChildMatch(actEntry, children, tBitsIdxArr);
			if (found)
				qAct.addEntry(actEntry);

		}

	}

	private void sortByCard(ArrayList<QNode> children) {

		int[] ints = new int[children.size()];
		for (int i = 0; i < children.size(); i++) {
			QNode c = children.get(i);
			ints[i] = mPool.get(c.id).elist().size();

		}
		Collections.sort(children, (left, right) -> ints[children.indexOf(left)] - ints[children.indexOf(right)]);
	}

	private void printEntries(ArrayList<PoolEntry> list) {

		for (PoolEntry e : list) {
			System.out.println(e.getValue());

		}
	}

	private boolean checkChildMatch(PoolEntry r, RoaringBitmap t_bits, ArrayList<PoolEntry> list) {

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

	private boolean checkChildMatch(PoolEntry r, RoaringBitmap t_bits, ArrayList<PoolEntry> list, boolean isLeaf) {

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

		if (isLeaf) {
			for (int i : rs_and) {
				GraphNode n = nodes[i];
				PoolEntry e = list.get(n.pos);
				r.addChild(e);
			}
		} else {

			HashMap<Integer, PoolEntry> i2e = new HashMap<Integer, PoolEntry>();
			for (PoolEntry e : list) {

				i2e.put(e.getValue().id, e);
			}

			for (int i : rs_and) {
				GraphNode n = nodes[i];
				PoolEntry e = i2e.get(n.id);
				r.addChild(e);
			}
		}

		return true;
	}

	private boolean checkChildMatch(PoolEntry r, ArrayList<QNode> children, RoaringBitmap[] tBitsIdxArr) {

		GraphNode tq = r.getValue();
		int c = 0;
		for (QNode qi : children) {

			Pool pqi = mPool.get(qi.id);
			boolean found = false;
			int axis = qi.E_I.get(0).axis;

			if (axis == 0) {

				found = checkChildMatch(r, tBitsIdxArr[c], pqi.elist());
			} else
				for (PoolEntry hp : pqi.elist()) {
					GraphNode tp = hp.getValue();

					// added on 2018.5.21, skip the self-linking case
					if (tq.id == tp.id)
						continue;

					// add this to short cut the checking

					if (tq.L_interval.mEnd < tp.L_interval.mStart) {

						break;

					}

					if (mBFL.reach(tq, tp) == 1) {

						r.addChild(hp);

						found = true;
					}

				}

			if (!found)
				return false; // don't need to continue;
			c++;
		}

		return true;
	}

	public static void main(String[] args) {

	}

}
