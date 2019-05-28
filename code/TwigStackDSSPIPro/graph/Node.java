/**
 * 
 */
package graph;

import java.util.Comparator;

import helper.Interval;

/**
 * @author xiaoying
 *
 */
public class Node implements Comparable<Node> {

	public int ID;
	public int label;
	public Interval encoding;
	public int indegree, outdegree;
    //dfs parent id
	public Node par;
	public int vis; // visiting flag
	
	public Node() {

		indegree = 0;
		outdegree = 0;
	}

	public Node(int id, int lb, Interval enc) {
		this.ID = id;
		this.label = lb;
		this.encoding = enc;
		indegree = 0;
		outdegree = 0;
	}

	public Node(int id, int lb) {
		this.ID = id;
		this.label = lb;
		indegree = 0;
		outdegree = 0;
	}

	public boolean isSink(){
	
		return (outdegree==0?true:false);
	}
	
	public String toString() {

		StringBuilder s = new StringBuilder();

		// s.append("["+ ID + "," + label + "," + indegree + "," + outdegree+
		//"]" + Consts.NEWLINE);
		 s.append("["+ ID + "," + label + "," + indegree + "," + outdegree+
					"]");


		//s.append("[" + ID + "," + DaoController.i2lMap.get(label) + "," + encoding + "]" + Consts.NEWLINE);
		return s.toString();

	}

	@Override
	public int compareTo(Node other) {
		
		int val = this.encoding.mStart - other.encoding.mStart;
		
		return val;
	}

	public static Comparator<Node> NodeIDComparator = new Comparator<Node>() {

		@Override
		public int compare(Node n1, Node n2) {

			return n1.ID - n2.ID;

		}


	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
