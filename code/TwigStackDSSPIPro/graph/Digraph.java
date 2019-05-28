/**
 * node labeled directed graph
 */
package graph;

import java.util.ArrayList;
import java.util.Iterator;

import edu.princeton.cs.algs4.Bag;
import global.Consts;

/**
 * @author xiaoying
 *
 */
public class Digraph {

	int V; // number of vertices in this digraph
	int E; // number of edges in this digraph
	Bag<Node>[] adj_O; // adj[v] = outgoing adjacency list for vertex v
	Bag<Node>[] adj_I; // adj[v] = incoming adjacency list for vertex v
	Node[] nodes; // nodes of the graph
	int numLabels = 0;

	ArrayList<Integer> sources; // source nodes of the graph
	ArrayList<Integer> sinks; // sink nodes of the graph

	public Digraph() {
	}

	public Digraph(int V, int E, Bag<Node>[] adj, Node[] nodes) {

		this.V = V;
		this.E = E;
		this.adj_O = adj;
		this.nodes = nodes;

	}

	public Digraph(int V, int E, Bag<Node>[] adj_O, Bag<Node>[] adj_I, Node[] nodes) {

		this.V = V;
		this.E = E;
		this.adj_O = adj_O;
		this.adj_I = adj_I;
		this.nodes = nodes;

	}

	public void setLables(int lbs) {

		numLabels = lbs;

	}

	public int getLabels() {

		return numLabels;
	}

	public Node[] getNodes() {

		return nodes;
	}

	public Node node(int v) {
		validateVertex(v);
		return nodes[v];

	}

	public ArrayList<Integer> getSources() {

		if (sources == null) {

			sources = new ArrayList<Integer>(5);
			for (Node n : nodes) {
				if (n.indegree == 0) {
					sources.add(n.ID);
					n.par = null;
				}

			}
		}

		return sources;
	}

	public ArrayList<Integer> getSinks() {

		if (sinks == null) {

			sinks = new ArrayList<Integer>(5);
			for (Node n : nodes) {
				if (n.outdegree == 0) {
					sinks.add(n.ID);
				}

			}
		}

		return sinks;
	}

	public Iterable<Node> adj_O(int v) {
		validateVertex(v);
		return adj_O[v];
	}

	public Iterable<Node> adj_I(int v) {
		validateVertex(v);
		return adj_I[v];
	}

	/**
	 * Returns the number of vertices in this digraph.
	 *
	 * @return the number of vertices in this digraph
	 */
	public int V() {
		return V;
	}

	/**
	 * Returns the number of edges in this digraph.
	 *
	 * @return the number of edges in this digraph
	 */
	public int E() {
		return E;
	}

	// throw an IndexOutOfBoundsException unless 0 <= v < V
	private void validateVertex(int v) {
		if (v < 0 || v >= V)
			throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V - 1));
	}

	/**
	 * Returns a string representation of the graph.
	 *
	 * @return the number of vertices <em>V</em>, followed by the number of
	 *         edges <em>E</em>, followed by the <em>V</em> adjacency lists
	 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(V + " vertices, " + E + " edges " + Consts.NEWLINE);
		for (int v = 0; v < V; v++) {
			s.append(String.format("%d: ", v));
			for (Node w : adj_O[v]) {
				s.append(w);
			}
			s.append(Consts.NEWLINE);
		}
		return s.toString();
	}



	public boolean linearSearchOUT(int sid, int tid) {
		
		Iterator<Node> it = adj_O[sid].iterator();
		
		while(it.hasNext()){
			Node n = it.next();
			if(n.ID == tid)
				return true;
		}
		return false;
		
	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
