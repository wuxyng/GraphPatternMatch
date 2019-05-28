package queryEvaluator;

import java.util.ArrayList;
import java.util.Collections;

import org.roaringbitmap.RoaringBitmap;

import dao.Pool;
import dao.PoolEntry;
import dao.SetReachSearch;
import global.Consts;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.RoaringList;
import helper.TimeTracker;
import query.QNode;
import query.TreeQuery;

public class TwigListD_bup_share {

	TreeQuery mQuery;
	ArrayList<Pool> mPool;
	SetReachSearch mSRS;
	ArrayList<ArrayList<GraphNode>> mInvLsts;
	GraphNode[] mNodes;
	TimeTracker tt;
	double totSolns = 0;
	
	boolean sortByAxis = false;
	
	public TwigListD_bup_share(TreeQuery query, ArrayList<ArrayList<GraphNode>> invLsts, GraphNode[] nodes){
		
		mQuery = query;
		mInvLsts = invLsts;
		mNodes = nodes;
		mSRS   = new SetReachSearch(nodes);
		init();
	}
	
	public QueryEvalStat run(boolean sort) {
		sortByAxis = sort;

		tt.Start();
		traverseBUP();
		double matm = tt.Stop() / 1000;
		System.out.println("Time on TwigListD_bup_pro:" + matm + "sec.");
		tt.Start();

		//printSolutions();
		calTotSolns();
		double entm = tt.Stop() / 1000;
		System.out.println("Time on calculating number of solutions:" + entm + "sec.");
		QueryEvalStat stat = new QueryEvalStat(matm, entm, 0, totSolns);

		return stat;
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

	
	/****************************
	 * TwigListD alg starts here
	 ****************************/

	private void traverseBUP() {
		QNode root = mQuery.mRoot;
		traverseBUP(root);
	}
	
	private void traverseBUP(QNode q) {
		
		if (q.isSink()){
			return;
		}

		ArrayList<QNode> children = mQuery.getChildren(q.id);

	
		
		for (QNode c : children) {
			traverseBUP(c);
		}
		
		ArrayList<GraphNode> sources = mInvLsts.get(q.lb);
		RoaringBitmap s_bits = new RoaringBitmap();
		
		for(GraphNode s:sources){
			s_bits.add(s.id);
		}
		
	
		populatePool(q, s_bits);
		
			
	}
	

	
	private void populatePool(QNode q, RoaringBitmap s_bits){
		TimeTracker tt = new TimeTracker();
		tt.Start();
		Pool pl = mPool.get(q.id);
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		if(sortByAxis)
			 Collections.sort(children, QNode.AxisComparator);
		
		RoaringBitmap[] tbitsIdxArr = new RoaringBitmap[children.size()];
		
		RoaringList[] roarArr = new RoaringList[children.size()];
		int i = 0;
		for (QNode c : children) {
			Pool pqi = mPool.get(c.id);
			int axis = c.E_I.get(0).axis;
			ArrayList<PoolEntry> targets = pqi.elist();
			
			RoaringList rl = null;
			if(axis == 0){
				RoaringBitmap t_bits= new RoaringBitmap();
				for(PoolEntry e:targets)
					t_bits.add(e.getValue().id);
				tbitsIdxArr[i]= t_bits;
				rl = mSRS.searchADJ_BUP(s_bits, t_bits);
			}
			else
				rl = mSRS.search_BUP(s_bits, targets);
			roarArr[i++] = rl;
			s_bits.and(rl.getState());
			
		}
		
		System.out.println("Time on set Reach: " + tt.Stop()/1000 + "s");
		
		for(int si:s_bits){
			GraphNode s = mNodes[si];
			PoolEntry actEntry = new PoolEntry(q, s);
		    i=0;
			for (QNode qi : children) {
				int axis = qi.E_I.get(0).axis;
				ArrayList<PoolEntry> targets= mPool.get(qi.id).elist();
				RoaringList rl = roarArr[i];
				RoaringBitmap t_bits = rl.get(si);
				RoaringBitmap t_bitsIdx = tbitsIdxArr[i];
				for(int ti:t_bits){
					PoolEntry e=null;
					if(axis == 0)
					   e = targets.get(t_bitsIdx.rank(ti)-1);
					else
					   e = targets.get(ti);
					actEntry.addChild(e);
					
				}
				i++;
				
			}
			pl.addEntry(actEntry);
		}
		
	}
	
	private RoaringList[] genRoarlist(ArrayList<QNode> children, RoaringBitmap s_bits){
		
		RoaringList[] roarArr = new RoaringList[children.size()];
		int i = 0;
		for (QNode c : children) {
			Pool pqi = mPool.get(c.id);
			int axis = c.E_I.get(0).axis;
			ArrayList<PoolEntry> targets = pqi.elist();
			
			RoaringList rl = null;
			if(axis == 0)
				rl = mSRS.searchADJ_BUP(s_bits, targets);
			else
				rl = mSRS.search_BUP(s_bits, targets);
			roarArr[i++] = rl;
			s_bits.and(rl.getState());
		
			
		}
		
		return roarArr;
	}
	
	private void roar2pool(QNode q, RoaringList[] roarArr, RoaringBitmap s_bits ){
		Pool pl = mPool.get(q.id);
		ArrayList<QNode> children = mQuery.getChildren(q.id);
		
		for(int si:s_bits){
			GraphNode s = mNodes[si];
			PoolEntry actEntry = new PoolEntry(q, s);
			int i=0;
			for (QNode qi : children) {
				int axis = qi.E_I.get(0).axis;
				ArrayList<PoolEntry> targets= mPool.get(qi.id).elist();
				RoaringList rl = roarArr[i++];
				RoaringBitmap t_bits = rl.get(si);
				for(int ti:t_bits){
					PoolEntry e = targets.get(ti);
					actEntry.addChild(e);
					
				}
				
			}
			pl.addEntry(actEntry);
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

				ArrayList<GraphNode> invLst = mInvLsts.get(q.lb);
				for (GraphNode n : invLst) {
					PoolEntry e = new PoolEntry(q, n);
					pool.addEntry(e);
				}

			}

		}

		tt = new TimeTracker();

	}

	
	public static void main(String[] args) {
		
	}

}
