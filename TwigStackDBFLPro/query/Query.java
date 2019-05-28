package query;

import java.util.ArrayList;

import global.Consts;

public class Query {

	public int Qid;
	public int V, E;
	public QNode[] nodes;

	private static int idseq=0;
	
	public ArrayList<QNode> sources, sinks; // source and sink nodes of the graph

	public Query(int V, int E, QNode[] nodes) {

		this.V = V;
		this.E = E;
		this.nodes = nodes;
		Qid = idseq++;
	}
	
	public Query(int i, int V, int E, QNode[] nodes) {

		this.V = V;
		this.E = E;
		this.nodes = nodes;
		Qid = i;
	}

	public QNode[] getNodes() {

		return nodes;
	}
	
	public ArrayList<QNode> getChildren(int id) {

		ArrayList<QNode> children = new ArrayList<QNode>();
		if (!nodes[id].isSink()) {

			ArrayList<Integer> ids = nodes[id].N_O;

			for (int i : ids) {

				children.add(nodes[i]);
			}
		}

		return children;
	}
	
	public ArrayList<QNode> getParents(int id){
		
		ArrayList<QNode> parents = new ArrayList<QNode>();
		
		if (!nodes[id].isSource()) {

			ArrayList<Integer> ids = nodes[id].N_I;

			for (int i : ids) {

				parents.add(nodes[i]);
			}
		}

		
		
		return parents;
	}
	
	public QNode getNode(int id){
		
		return nodes[id];
	}

	public QNode getParent(int id) {
		int pid = nodes[id].N_I.get(0);
		return nodes[pid];
	}

	public ArrayList<Integer> getChildrenIDs(int id) {

		return nodes[id].N_O;
	}
	
	public boolean isTree(){
		
		for(QNode n: nodes){
			
			if(n.N_I_SZ>1)
				return false;
		}
		
		if(E!= V-1)
			return false;
		return true;
	}

	
	public ArrayList<QNode> getSources() {
		if (sources == null) {

			sources = new ArrayList<QNode>(5);
			for (QNode node : nodes) {
				if (node.N_I_SZ == 0)
					sources.add(node);

			}

		}

		return sources;
	}

	public ArrayList<QNode> getSinks() {
		if (sinks == null) {

			sinks = new ArrayList<QNode>(5);
			for (QNode node : nodes) {
				if (node.N_O_SZ == 0)
					sinks.add(node);

			}

		}

		return sinks;
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

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(V + " vertices, " + E + " edges " + Consts.NEWLINE);
		for (int v = 0; v < V; v++) {
			s.append(String.format("%d: ", v));
			QNode n_v = nodes[v];
			if(n_v.N_O_SZ>0)
			for (int w : n_v.N_O) {
				s.append(w + " ");
			}
			s.append(Consts.NEWLINE);
		}
		return s.toString();
	}
	
	public static void main(String[] args) {

	}

}
