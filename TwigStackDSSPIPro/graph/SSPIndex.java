/**
 * 
 */
package graph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import graph.Node;

/**
 * @author xiaoying
 *
 */
public class SSPIndex {

	private LinkedList<Node>[] SSPI;
	private int V;
	PrintWriter opw;

	public SSPIndex(int len) {
	

		SSPI = (LinkedList<Node>[]) new LinkedList<?>[len];
		V = len;
	}

	public void openFile(){
		
		try {
			opw = new PrintWriter(new FileOutputStream("xm01e.text", false));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeFile(){
		opw.close();
		
	}
	public int length() {

		return V;
	}

	// add p as a pred of n
	public void add(Node n, Node p) {

		LinkedList<Node> entryList = SSPI[n.ID];
		if (entryList == null) {

			entryList = new LinkedList<Node>();
			SSPI[n.ID] = entryList;

		}
		// entryList.add(p.encoding);
		add(entryList, p);

	}

	private void add(LinkedList<Node> entryList, Node newNode) {

		int pos = entryList.size() - 1;
		while (pos >= 0) {
			if (entryList.get(pos).encoding.mStart > newNode.encoding.mStart)
				pos--;
			else if (entryList.get(pos).encoding.mStart == newNode.encoding.mStart)
				return;
			else
				break;
		}

		entryList.add(++pos, newNode);
	}

	public boolean isEmpty(int id) {

		LinkedList<Node> entryList = SSPI[id];
		if (entryList == null || entryList.isEmpty())
			return true;

		return false;
	}

	public boolean isEmpty(Node n) {

		return isEmpty(n.ID);
	}

	public boolean isNull(int id) {

		LinkedList<Node> entryList = SSPI[id];
		if (entryList == null)
			return true;
		return false;

	}

	public boolean isNull(Node n) {

		return isNull(n.ID);
	}

	public Node getFirst(int id) {

		LinkedList<Node> entryList = SSPI[id];
		Node entry = null;
		if (entryList != null)
			entry = entryList.getFirst();
		return entry;
	}

	public Node getFirst(Node n) {

		return getFirst(n.ID);
	}

	public LinkedList<Node> getList(Node n) {

		LinkedList<Node> entryList = SSPI[n.ID];
		return entryList;
	}

	public LinkedList<Node> getList(int id) {

		LinkedList<Node> entryList = SSPI[id];
		return entryList;
	}


	public void merge(Node fn, Node tn) {

		merge(fn.ID, tn.ID);
	}

	public void merge(int from, int to) {

		LinkedList<Node> fromList = SSPI[from], toList = SSPI[to];

		if (toList == null) {

			SSPI[to] = fromList;
			return;
		}

		if (fromList != null) {
			toList = merge(fromList, toList);
			SSPI[to] = toList; // this step is needed.

		}
	}

	private LinkedList<Node> merge(LinkedList<Node> fromList, LinkedList<Node> toList) {

		int f = 0, t = 0;
		Node fn = null, tn = null;
		while (f < fromList.size()) {

			if (t >= toList.size()) {
				toList.addAll(fromList.subList(f, fromList.size()));
				break;
			}
			fn = fromList.get(f);
			tn = toList.get(t);

			if (fn.compareTo(tn) == 0) {

				f++;
				t++;
			} else if (fn.compareTo(tn) < 0) {
				toList.add(t, fn);
				t++;
				f++;
			} else {

				t++;
			}

		}

		return toList;
	}

	public boolean reach(Node tq, Node h) {

		int cp = tq.encoding.compareTo(h.encoding);
		if (cp == 0) {
			// tq is an ancestor of a
			return true;
		} else if (cp == -1) {
			// tq is to the left of tq
			return false;
		} else {

			if (SSPI[h.ID] != null)
				for (Node a : SSPI[h.ID]) {

					if (reach(tq, a))
						return true;
				}

		}

		return false;
	}

	public boolean reachBySSPI(Node tq, Node h) {

		boolean[] visited = new boolean[V];
        boolean rs = reachExt(tq, h, visited);
        //opw.println(tq.ID + " " + h.ID + " " + (rs?1:0)); 		
        		
		//return reach(tq, h, visited);
		return rs;
	}

	// for doing the predecessor inheriting, need dfs_in when building SSPI
	public boolean reach(Node tq, Node h, boolean[] visited) {

		if (!visited[h.ID]) {
			visited[h.ID] = true;
			int cp = tq.encoding.compareTo(h.encoding);
			if (cp == 0) {
				// tq is an ancestor of a
				return true;
			} else if (cp == -1) {
				// tq is to the left of tq
				return false;
			} else {

				if (SSPI[h.ID] != null) {
					for (Node a : SSPI[h.ID]) {

						if (reach(tq, a, visited))
							return true;
					}
				}

				// h has no predecessors, or all its predecessors cannot be
				// reached

			}
		}

		return false;
	}

	// no inherit, no dfs_in is needed
	public boolean reachExt(Node tq, Node h, boolean[] visited) {

		if (h == null || visited[h.ID]) {
			return false;
		}

		visited[h.ID] = true;

		int cp = tq.encoding.compareTo(h.encoding);
		if (cp == 0) {
			// tq is an ancestor of a
			return true;
		} else if (cp == -1 || cp== -2) {
			// tq is to the left or descendant of h 
			return false;
		}

		if (isEmpty(h)) {
			// go up to h's parent
			return reachExt(tq, h.par, visited);
		}

		// check h's predecessors in SSPI

		Iterator<Node> it = SSPI[h.ID].iterator();
		while (it.hasNext()) {
			Node hp = it.next();

			if (reachExt(tq, hp, visited))
				return true;
		}

		// h has no predecessors, or all its predecessors cannot be reached

		return reachExt(tq, h.par, visited);
	}

	
	public void print() {

		// HashMap<Integer, LinkedList<Node>> SSPI
		System.out.println("*****************SSPI*************");

		for (int i = 0; i < SSPI.length; i++) {

			LinkedList<Node> preds = SSPI[i];
			if (preds != null) {
				System.out.println("node " + i + "  preds: ");
				for (Node n : preds) {

					System.out.println("\t" + n);
				}
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
