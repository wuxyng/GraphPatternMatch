
/**
 * input graph format
 * v index label 
 * e uid vid
 */
package graph.dao;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import graph.Digraph;
import graph.Node;
import helper.TimeTracker;

/**
 * @author xiaoying
 *
 */
public class DigraphTxtLoader {

	private String mFileName;
	private Digraph mG;

	private int id; // label id
	private HashMap<String, Integer> l2iMap;
	private HashMap<Integer, String> i2lMap;

	private int V; // number of vertices in this digraph
	private int E; // number of edges in this digraph
	private Bag<Node>[] adj_O, adj_I; // adj[v] = outgoing and incoming
										// adjacency list for vertex v
	private Node[] nodes; // nodes of the graph
	private int numLabels;

	private int max_in, max_out;
	
	private double loadTime = 0.0;
	
	public DigraphTxtLoader(String filename) {

		mFileName = filename;
		l2iMap = new HashMap<String, Integer>();
		i2lMap = new HashMap<Integer, String>();
		numLabels = 0;

	}

	public void loadVE() {

		TimeTracker tt = new TimeTracker();
		tt.Start();
		
		try {
			int[] VE = getVandE();
			V = VE[0];
			E = VE[1];
			adj_O = (Bag<Node>[]) new Bag[V];
			adj_I = (Bag<Node>[]) new Bag[V];
			nodes = new Node[V];

			for (int v = 0; v < V; v++) {
				adj_O[v] = new Bag<Node>();
				adj_I[v] = new Bag<Node>();
			}

			In in = new In(mFileName);
			String line = null;
			while (in.hasNextLine()) {
				line = in.readLine().trim();
				if (line.length() == 0)
					continue;
				String[] parts = line.split("\\s+");
				if (line.charAt(0) == 'v') {

					final int index = Integer.parseInt(parts[1]);
					final String label = parts[2];
					Node n = new Node();
					n.ID = index;
					Integer lid = l2iMap.get(label);
					if (lid == null) {
						n.label = id++;
						i2lMap.put(n.label, label);
						l2iMap.put(label, n.label);
						numLabels++;
					} else {
						n.label = lid;
					}
					nodes[index] = n;

				}
				if (line.charAt(0) == 'e') {

					final int v = Integer.parseInt(parts[1]);
					final int w = Integer.parseInt(parts[2]);
					addEdge(v, w);
				}
			}
			mG = new Digraph(V, E, adj_O, adj_I, nodes);
			mG.setLables(numLabels);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		loadTime = tt.Stop() /1000;
	}
	
	public double getLoadTime(){
		
		return loadTime;
	}


	public HashMap<String, Integer> getL2IMap() {

		return l2iMap;
	}

	public HashMap<Integer, String> getI2LMap() {

		return i2lMap;
	}

	public Digraph getGraph() {

		return mG;
	}

	public void loadGRA() {

		try {
			BufferedReader in = new BufferedReader(new FileReader(mFileName));
			readNextLine(in); // graph_for_greach

			String line = readNextLine(in); // total number of nodes

			if (line == null)
				return;

			V = Integer.parseInt(line);
			E = 0;
			adj_O = (Bag<Node>[]) new Bag[V];
			adj_I = (Bag<Node>[]) new Bag[V];
			nodes = new Node[V];

			for (int v = 0; v < V; v++) {
				adj_O[v] = new Bag<Node>();
				adj_I[v] = new Bag<Node>();
			}

			int[] buf = null;
			NodeList[] N_O = new NodeList[V], N_I = new NodeList[V];

			while ((buf = getNextLine(in)) != null) {

				int u = buf[0], v;
				Node node = new Node();
				node.ID = u;
				node.label = 0;
				nodes[u] = node;
				N_O[u] = new NodeList();
				for (int j = 1; j < buf.length; j++) {
					v = buf[j];
					N_O[u].add(v);
					if (N_I[v] == null)
						N_I[v] = new NodeList();
					N_I[v].add(u);
					E++;

				}

			}

			max_in = max_out = 0;
			
			for (int u = 0; u < V; u++) {
				if (N_O[u] != null) {
					nodes[u].outdegree = N_O[u].size();
					if(nodes[u].outdegree>max_out)
						max_out = nodes[u].outdegree;
					for (int w : N_O[u].getList()) {

						adj_O[u].append(nodes[w]);
					}
				}
				if (N_I[u] != null) {
					nodes[u].indegree = N_I[u].size();
					if(nodes[u].indegree>max_in)
						max_in = nodes[u].indegree;
					for (int w : N_I[u].getList()) {

						adj_I[u].append(nodes[w]);
					}

				}

			}

			mG = new Digraph(V, E, adj_O, adj_I, nodes);
			mG.setLables(1);
            System.out.println("Max_in = " + max_in + ",Max_out=" + max_out );
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class NodeList {

		ArrayList<Integer> nlist;
		//Set<Integer> nlist;

		NodeList() {

			nlist = new ArrayList<Integer>();
			//nlist = new HashSet<Integer>();
		}

		ArrayList<Integer> getList() {
		//Set<Integer> getList() {

			return nlist;
		}

		void add(int n) {
        
			nlist.add(n);
		}

		int size() {

			return nlist.size();
		}
	}

	private int[] getVandE() throws Exception {
		int[] rs = new int[2];
		BufferedReader bin = new BufferedReader(new FileReader(mFileName));

		String line = null;

		while ((line = readNextLine(bin)) != null) {

			if (line.charAt(0) == 'v')
				rs[0]++;

			if (line.charAt(0) == 'e')
				rs[1]++;

		}

		return rs;
	}

	private int[] getNextLine(BufferedReader in) {

		String line = readNextLine(in);
		if (line != null) {
			// id: ids#
			String[] strArr = splitLine(line);
			int sz = strArr.length;
			int[] buf = new int[sz];
			for (int i = 0; i < sz; ++i) {
				buf[i] = Integer.parseInt(strArr[i]);
			}

			return buf;

		}
		return null;
	}

	private String[] splitLine(String aString) {

		Pattern p = Pattern.compile("[,:#\\s]+"); // ("[\\s]+");
		// Split input with the pattern
		String[] result = p.split(aString);

		return result;

	}

	private static String readNextLine(BufferedReader in) {
		String line = null;

		try {
			// read a non-emtpy line
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return line;
	}

	// throw an IndexOutOfBoundsException unless 0 <= v < V
	private void validateVertex(int v) {
		if (v < 0 || v >= V)
			throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V - 1));
	}

	private void addEdge(int v, int w) {
		validateVertex(v);
		validateVertex(w);
		Node nv = nodes[v], nw = nodes[w];
		// adj[v].add(nw);
		adj_O[v].append(nw);
		adj_I[w].append(nv);
		nw.indegree++;
		nv.outdegree++;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
