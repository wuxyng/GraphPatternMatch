/**
 * 
 */
package graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import graph.Digraph;
import graph.Node;
import helper.Interval;
import helper.TimeTracker;

/**
 * @author xiaoying
 *
 */
public class DirectedDFSIter {

	private Digraph mG;
	private boolean[] marked; // marked[v] = true if v is reachable
	// from source (or sources)
	
	private Iterator<Node>[] adj_O_it, adj_I_it;

	private int mDFSCounter;
	private SSPIndex mSSPI;
	private ArrayList<ArrayList<Node>> mInvLsts;
	private double bldTime = 0.0; 
	
	public DirectedDFSIter(Digraph G) {

		init(G);

	}

	
	public void buildSSPI() {
		TimeTracker tt = new TimeTracker();
		tt.Start();
		for (int v : mG.getSources()) {
			
			if (!marked[v])
				dfs_out(v);
		}
		
		bldTime = tt.Stop() / 1000;
		//for (int v : mG.getSinks()) {

		//	if (marked[v])
		//		dfs_in(v);
		//}

	}

	public double getBldTime() {

		return bldTime;
	}
	
	public SSPIndex getSSPI() {

		return mSSPI;
	}

	public ArrayList<ArrayList<Node>> getInvLsts() {

		return mInvLsts;
	}

	private void init(Digraph G) {

		mG = G;
		marked = new boolean[G.V()];
		// to be able to iterate over each adjacency list, keeping track of
		// which
		// vertex in each adjacency list needs to be explored next
		adj_O_it = (Iterator<Node>[]) new Iterator[G.V()];
		adj_I_it = (Iterator<Node>[]) new Iterator[G.V()];
		for (int v = 0; v < G.V(); v++) {
			adj_O_it[v] = G.adj_O(v).iterator();
			adj_I_it[v] = G.adj_I(v).iterator();
		}
		mSSPI = new SSPIndex(G.V());
		mInvLsts = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < mG.getLabels(); i++) {
			mInvLsts.add(new ArrayList<Node>());

		}
	}

	private void dfs_out(int s) {

		Node n = mG.node(s);
		marked[s] = true;

		// depth-first search using an explicit stack
		Stack<Node> stack = new Stack<Node>();
		stack.push(n);
		previsit(n);
		while (!stack.isEmpty()) {
			Node v = stack.peek();
			if (adj_O_it[v.ID].hasNext()) {
				Node w = adj_O_it[v.ID].next();
		
				// StdOut.printf("check %d\n", w);

				if (!marked[w.ID]) {
					// discovered vertex w for the first time
					marked[w.ID] = true;
					w.par = v;
					// edgeTo[w] = v;
					stack.push(w);
					previsit(w);
					// StdOut.printf("dfs(%d)\n", w);

				} else {

					// addSSPIEntry(w, v);

					mSSPI.add(w, v);
				}
			} else {
				// StdOut.printf("%d done\n", v);
				stack.pop();
				postvisit(v);

			}
		}

	}

	private void dfs_in(int s) {

		Node n = mG.node(s);
		marked[s] = false;

		// depth-first search using an explicit stack
		Stack<Node> stack = new Stack<Node>();
		stack.push(n);

		while (!stack.isEmpty()) {
			Node v = stack.peek();

			if (adj_I_it[v.ID].hasNext()) {

				Node w = adj_I_it[v.ID].next();
				// StdOut.printf("check %d\n", w);
				// w is a parent of v
				if (marked[w.ID]) {
					// discovered vertex w for the first time
					marked[w.ID] = false;
					// edgeTo[w] = v;
					stack.push(w);
					// StdOut.printf("dfs(%d)\n", w);

				}

				continue;

			}

		
			Node p = v.par;
			if (p != null && !mSSPI.isEmpty(p))
				mSSPI.merge(p, v);

			// StdOut.printf("%d done\n", v);
			stack.pop();

		}

	}

	private void addSSPIEntry(Node n, Node p) {

		mSSPI.add(n, p);

	}

	private void previsit(Node n) {
		mDFSCounter++;
		n.encoding = new Interval(mDFSCounter, -1);
		ArrayList<Node> invLst = mInvLsts.get(n.label);
		invLst.add(n);

	}

	private void postvisit(Node n) {

		mDFSCounter++;
		n.encoding.mEnd = mDFSCounter;
	}

	private boolean marked(int v) {
		return marked[v];
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
