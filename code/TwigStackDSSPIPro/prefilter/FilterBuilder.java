package prefilter;
/*
 * assume unique labels in the input query
 * 
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;


import global.Consts;
import global.Flags;
import graph.Digraph;
import graph.Node;
import graph.dao.DigraphTxtLoader;
import helper.TimeTracker;
import query.QNode;
import query.Query;
import query.QueryParser;

/*
 * assume that all the query nodes have distinct labels
 * 
 */

public class FilterBuilder {

	private Digraph mG;
	private Query mQ;
	private ArrayList<ArrayList<Node>> mInvLsts_reduced;
	
	//private Node[] mGNodes;
	private QNode[] mQNodes;
	private BitSet[] mQBits_dn, mQBits_up;
	private boolean[][] mSat_dn, mSat_up;
	private BitSet[] mGBit_dn, mGBit_up;
	private Iterator<Node>[] in_it, out_it;
	private ArrayList<Query> queries;


	private double bldTime = 0.0;
	private double totNodes_after = 0.0;

	public FilterBuilder(Digraph g, Query q) {
		mG = g;
		mQ = q;
	}

	public FilterBuilder(String dataFN, String queryFN) {

		String queryFileN, dataFileN;
		HashMap<String, Integer> l2iMap;

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		DigraphTxtLoader loader = new DigraphTxtLoader(dataFileN);

		System.out.println("loading data graph ...");
		// load data graph
		loader.loadVE();
		mG = loader.getGraph();
		l2iMap = loader.getL2IMap();
		System.out.println("done. ...");

		System.out.println("loading queries ...");
		// load queries
		QueryParser queryParser = new QueryParser(queryFileN, l2iMap);
		Query query = null;
		queries = new ArrayList<Query>();
		while ((query = queryParser.readNextQuery()) != null) {
			queries.add(query);
		}

		System.out.println("done. ...");
	}

	public ArrayList<ArrayList<Node>> getInvLsts() {

		return mInvLsts_reduced;
	}

	public void run() {

		for (int i = 0; i < queries.size(); i++) {

			mQ = queries.get(i);
			System.out.println("Query:" + mQ);
			oneRun();
			printInvLsts();
		}

	}

	public double getBuildTime() {

		return bldTime;
	}
	
	public double getTotNodes(){
		
		return totNodes_after;
	}

	public void oneRun() {

		init();
		
		System.out.println("generating QBits...");
		genQBits();

		TimeTracker tt = new TimeTracker();
		tt.Start();
		Flags.vis_cur++;

		System.out.println("generating downward gBits...");
		for (int v : mG.getSources()) {
			Node node = mG.node(v);
			dfs_out(node);
		}


		Flags.vis_cur++;

		System.out.println("generating upward gBits...");
		
		for (int v : mG.getSinks()) {
			Node node = mG.node(v);
			dfs_in(node);

		}
		
		sortInvLsts();
		bldTime = tt.Stop() / 1000;
		Flags.mt.run();
		System.out.println("Time for building prefilter index:" + bldTime + "sec.");
		
		printSize();
		//printInvLsts(); 
	}

	private void init() {

		//mGNodes = mG.getNodes();
		mQNodes = mQ.getNodes();
		mGBit_dn = new BitSet[mG.V()];
		mGBit_up = new BitSet[mG.V()];

		in_it = (Iterator<Node>[]) new Iterator[mG.V()];
		out_it = (Iterator<Node>[]) new Iterator[mG.V()];

		int qlen = mQNodes.length;
		mSat_dn = new boolean[mG.V()][qlen];
		mSat_up = new boolean[mG.V()][qlen];
		for (int v = 0; v < mG.V(); v++) {
			in_it[v] = mG.adj_I(v).iterator();
			out_it[v] = mG.adj_O(v).iterator();
			mGBit_dn[v] = new BitSet(qlen);
			mGBit_up[v] = new BitSet(qlen);
			for (int q = 0; q < qlen; q++) {
				mSat_dn[v][q] = false;
				mSat_up[v][q] = false;
			}
		}
		mQBits_dn = new BitSet[qlen];
		mQBits_up = new BitSet[qlen];

		for (int i = 0; i < qlen; i++) {
			mQBits_dn[i] = new BitSet(qlen);
			mQBits_up[i] = new BitSet(qlen);
		}

		mInvLsts_reduced = new ArrayList<ArrayList<Node>>(qlen);
		for (QNode q : mQNodes) {
			mInvLsts_reduced.add(q.id, new ArrayList<Node>());
		}
	}

	private void genQBits() {

		int qlen = mQNodes.length;
		boolean[] flags = new boolean[qlen];
		ArrayList<QNode> sources = mQ.getSources();

		for (QNode source : sources) {

			genQBits_dn(source, flags);
		}

		Arrays.fill(flags, false);

		ArrayList<QNode> sinks = mQ.getSinks();
		for (QNode sink : sinks) {

			genQBits_up(sink, flags);
		}
	}

	private void genQBits_up(QNode q, boolean[] flags) {

		int qid = q.id;
		BitSet QBits = mQBits_up[qid];

		ArrayList<QNode> parents = mQ.getParents(qid);
		for (QNode parent : parents) {
			if (!flags[parent.id])
				genQBits_up(parent, flags);
			BitSet QBits_p = mQBits_up[parent.id];
			QBits.or(QBits_p);
		}
		flags[qid] = true;
		QBits.set(qid);

		//System.out.println("UP" + " qid: " + qid + " bits: " + QBits);
	}

	private void genQBits_dn(QNode q, boolean[] flags) {
		int qid = q.id;
		BitSet QBits = mQBits_dn[qid];
		ArrayList<QNode> children = mQ.getChildren(qid);

		for (QNode child : children) {
			if (!flags[child.id])
				genQBits_dn(child, flags);
			BitSet QBits_c = mQBits_dn[child.id];
			QBits.or(QBits_c);
		}
		flags[qid] = true;
		QBits.set(qid);
		//System.out.println("DN" + " qid: " + qid + " bits: " + QBits);
	}

	private void dfs_in(Node u) {

		Stack<Node> stack = new Stack<Node>();
		stack.push(u);
		u.vis = Flags.vis_cur;
		while (!stack.isEmpty()) {
			Node v = stack.peek();
			BitSet gBits_v = mGBit_up[v.ID];
			if (in_it[v.ID].hasNext()) {
				Node w = in_it[v.ID].next();
				if (w.vis != Flags.vis_cur) {
					w.vis = Flags.vis_cur;
					stack.push(w);
				}
				continue;
			}

			for (Node w : mG.adj_I(v.ID)) {

				BitSet gBits_w = mGBit_up[w.ID];
				gBits_v.or(gBits_w);

			}

			// must traverse the query nodes in the bottom-up way, 2018.11.11
			for(int i= mQNodes.length-1; i>=0;i--){
				QNode q_v = mQNodes[i];
				if (q_v.lb != v.label)
					continue;
				gBits_v.set(q_v.id);
				BitSet qBits_v = mQBits_up[q_v.id];
				if (qBits_v.equals(bitAND(gBits_v, qBits_v))) {
					//must first satisfy the downward constraint,can the upward constraint be checked
					if (mSat_dn[v.ID][q_v.id]){
						//mSat_up[v.id][q_v.id] = true;
						mInvLsts_reduced.get(q_v.id).add(v);
						
					}
				}
				else{
					// otherwise clear the bit, 2018.11.10
					gBits_v.clear(q_v.id);
				}
			}

			// System.out.println("UP" + " vid: " + v.id + " bits: "+ gBits_v);
			stack.pop();

		}
	}
	private void dfs_out(Node u) {

		Stack<Node> stack = new Stack<Node>();
		stack.push(u);
		u.vis = Flags.vis_cur;

		while (!stack.isEmpty()) {
			Node v = stack.peek();
			BitSet gBits_v = mGBit_dn[v.ID];
		
			if (out_it[v.ID].hasNext()) {
				Node w = out_it[v.ID].next();
				if (w.vis != Flags.vis_cur) {
					w.vis = Flags.vis_cur;
					stack.push(w);

				}
				continue;

			}

			for (Node w : mG.adj_O(v.ID)) {

				BitSet gBits_w = mGBit_dn[w.ID];
				gBits_v.or(gBits_w);

			}

			for (QNode q_v : mQNodes) {
				if (q_v.lb != v.label)
					continue;
				gBits_v.set(q_v.id);
				BitSet qBits_v = mQBits_dn[q_v.id];
				if (qBits_v.equals(bitAND(gBits_v, qBits_v))) {
					mSat_dn[v.ID][q_v.id] = true;
				}
				else{
					//otherwise clear the bit, 2018.11.10
					gBits_v.clear(q_v.id);
				}

			}

			// System.out.println("UP" + " vid: " + v.id + " bits: "+ gBits_v);
			stack.pop();
		}

	}

	
   private BitSet bitAND(BitSet gBits, BitSet qBits) {

		BitSet temp = (BitSet) gBits.clone();
		temp.or(gBits);

		temp.and(qBits);
		return temp;
	}

	private void sortInvLsts(){
		
		for (int i = 0; i < mInvLsts_reduced.size(); i++) {

			ArrayList<Node> elist = mInvLsts_reduced.get(i);
			Collections.sort(elist);
			totNodes_after+=elist.size();
		}
	}
	
	private void printInvLsts() {

		for (int i = 0; i < mInvLsts_reduced.size(); i++) {
			ArrayList<Node> lst = mInvLsts_reduced.get(i);
		
			System.out.println("Inverted list for lid " + i + ":");
			for (Node n : lst)
				System.out.println(n);

		}

	}

	private void printSize() {

		for (int i = 0; i < mInvLsts_reduced.size(); i++) {

			ArrayList<Node> elist = mInvLsts_reduced.get(i);
			System.out.println(i + ":" + elist.size());
		}
	}

	private BitSet bitComplementAND(BitSet qBits) {

		BitSet temp = (BitSet) qBits.clone();
		temp.flip(0, qBits.size());
		return temp;
	}

	public static void main(String[] args) {
		String dataFileN = "gtest1.txt", queryFileN = "qtest1.txt"; // the query
																	// file
		FilterBuilder fb = new FilterBuilder(dataFileN, queryFileN);
		fb.run();

	}

}
