package query;

import java.util.ArrayList;

public class TreeQuery {

	public QNode mRoot;
    public int[] mAxis;
	public int[] mParents;
	public ArrayList<Integer> mLeaves;

	public int V, E;
	public QNode[] nodes;
	
	ArrayList<Integer>[] mPathIndices;

	public TreeQuery(int V, int E, QNode[] nodes) {

		this.V = V;
		this.E = E;
		this.nodes = nodes;
		extractQueryInfo();
	}

	public TreeQuery(Query q) {
		V = q.V;
		E = q.E;
		this.nodes = q.nodes;
		extractQueryInfo();
	}

	public QNode getParent(int id) {
		int pid = nodes[id].N_I.get(0);
		return nodes[pid];
	}

	public ArrayList<Integer> getChildrenIDs(int id) {

		return nodes[id].N_O;
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

	public QNode getNode(int id) {

		return nodes[id];
	}
	
	
	public ArrayList<Integer>[] getPathIndices() {

		return mPathIndices;
	}

	public int[] getParents() {

		return mParents;
	}

	public void extractQueryInfo() {

		mLeaves = new ArrayList<Integer>();
		for (QNode node : nodes) {
			if (node.N_I_SZ == 0) {
				mRoot = node;
			}

			if (node.N_O_SZ == 0)
				mLeaves.add(node.id);
		}

		mAxis = new int[V];
		for (QNode n : nodes) {
			if (n.N_I_SZ == 0)
				mAxis[n.id] = 1;
			else {

				mAxis[n.id] = n.E_I.get(0).axis;
			}
		}
	}

		
	public void extractQueryInfo_mj() {

		mLeaves = new ArrayList<Integer>();
		for (QNode node : nodes) {
			if (node.N_I_SZ == 0) {
				mRoot = node;
			}

			if (node.N_O_SZ == 0)
				mLeaves.add(node.id);
		}

		mParents = new int[V];
		mParents[mRoot.id] = -1;
		mPathIndices = (ArrayList<Integer>[]) new ArrayList<?>[V];

		extractPathIndices(mRoot.id);

		mAxis = new int[V];
		for (QNode n : nodes) {
			if (n.N_I_SZ == 0)
				mAxis[n.id] = 1;
			else {

				mAxis[n.id] = n.E_I.get(0).axis;
			}
		}
	}

	private ArrayList<Integer> extractPathIndices(int nid) {

		ArrayList<Integer> pathIndices = new ArrayList<Integer>();
		ArrayList<Integer> children = nodes[nid].N_O;

		// if the node is a leaf node of the pattern
		if (children == null) {

			// leaf nodes are used to distinguish among paths in the query

			// add the node in pathIndices
			pathIndices.add(nid);
			mPathIndices[nid] = pathIndices;
			return pathIndices;

		}

		for (int child : children) {

			ArrayList<Integer> subIndices = extractPathIndices(child);
			pathIndices.addAll(subIndices);
			mParents[child] = nid;
		}

		mPathIndices[nid] = pathIndices;
		return pathIndices;

	}

	

	public static void main(String[] args) {

	}

}
