package dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.roaringbitmap.RoaringBitmap;

import global.Consts;
import graph.GraphNode;
import helper.Pair;
import helper.RoaringList;
import helper.TimeTracker;

public class SetReachSearch {

	GraphNode[] nodes;
	

	public SetReachSearch() {

	}

	public SetReachSearch(GraphNode[] nodes) {

		this.nodes = nodes;

	}

	public SetReachSearch(ArrayList<GraphNode> nodesList) {

		nodes = (GraphNode[]) nodesList.toArray();
	}

	
	public void search_BUP(RoaringBitmap source_filter, ArrayList<PoolEntry> targets, RoaringBitmap[][] reach_yes, RoaringBitmap[][] reach_no) {

		TimeTracker tt = new TimeTracker();
		tt.Start();
		
		System.out.println("Input source card: " + source_filter.getCardinality() + " target card:" + targets.size());
		for (int si : source_filter) {
			GraphNode s = nodes[si];
			RoaringBitmap[] bits_yes = reach_yes[s.id], bits_no = reach_no[s.id];
			for (int j = 0; j < targets.size(); j++) {
				GraphNode t = targets.get(j).mValue;
			
				int ti = t.id;
				if (ti == si)
					continue; // skip this case for doing pattern matching
				int i = t.id/Consts.BLOCK, r = t.id%Consts.BLOCK;
				if (bits_yes[i] != null && bits_yes[i].contains(r)) {
			
				// System.out.println(si + " => " + ti);
					continue;
				}

				if (bits_no[i] != null && bits_no[i].contains(r)) {
					continue;
				}

				chkReach_iter(s, t, i, r, reach_yes, reach_no);
			}

		}

		System.out.println("Time on search_BUP: " + tt.Stop()/1000 + "s");
		
	}

	private boolean chkReach_iter(GraphNode s, GraphNode t, int i, int r, RoaringBitmap[][] reach_yes, RoaringBitmap[][] reach_no) {

		
		int rs = chkReach_base(s, t);
		if (rs == 1) {
			setReachFlag(reach_yes, s.id, i, r);
			return true;
		} else if (rs == 0) {
			setReachFlag(reach_no, s.id, i, r);
			return false;
		}
		// otherwise undetermined
		Stack<Pair<GraphNode, Integer>> stack = new Stack<Pair<GraphNode, Integer>>();
		stack.push(new Pair(s, 0));

		while (!stack.isEmpty()) {
			Pair<GraphNode, Integer> entry = stack.peek();
			GraphNode u = entry.getFirst();
			int idx = entry.getSecond();

			ArrayList<Integer> children = u.N_O;

			if (idx < children.size()) {
				GraphNode v = nodes[children.get(idx)];
				idx++;
				entry.setSecond(idx);
				stack.set(stack.size() - 1, entry);
				RoaringBitmap[] bits_yes = reach_yes[v.id], bits_no = reach_no[v.id];
				if (bits_yes[i] != null && bits_yes[i].contains(r)) {
					setReachFlag(stack, i, r, reach_yes);
					return true;
				}

				if (bits_no[i] != null && bits_no[i].contains(r)) {

					continue;
				}

				rs = chkReach_base(v, t);
				if (rs == 1) {
					if (v.id != t.id)
						setReachFlag(reach_yes, v.id, i, r);
					setReachFlag(stack, i, r, reach_yes);
					return true;
				}
				if (rs == 0)
					setReachFlag(reach_no, v.id, i, r);
				// cannot return false when rs == 0, since it is possible
				// that another child node v of u can reach t.
				if (rs == -1) { // undetermined, need to check v's children

					stack.push(new Pair(v, 0));
				}

				continue;
			}

			stack.pop();
			// u does not reach t
			setReachFlag(reach_no, u.id, i, r);
		}

		// have visited all the children of U
		setReachFlag(reach_no, s.id, i, r);
		return false;
	}

	private void setReachFlag(RoaringBitmap[][] flags, int s, int i, int r){
		
		RoaringBitmap flag = flags[s][i];
		if(flag == null)
			flag = new RoaringBitmap();
		flag.add(r);
	}
	
	private void setReachFlag(Stack<Pair<GraphNode, Integer>> stack, int i, int r, RoaringBitmap[][] reach_yes) {

		Iterator<helper.Pair<GraphNode, Integer>> it = stack.iterator();

		while (it.hasNext()) {

			helper.Pair<GraphNode, Integer> entry = it.next();
			setReachFlag(reach_yes, entry.getFirst().id, i, r);
			// System.out.println("Setting node " + entry.getValue0().id + " to
			// index " + ti + " to true.");
		}

	}

	
	
	////////////////////////////////
	
	public RoaringList search(ArrayList<Integer> sources, ArrayList<Integer> targets) {

		int count = 0;
		RoaringList reach_yes = new RoaringList(nodes.length);
		RoaringList reach_no = new RoaringList(nodes.length);
		for (int si : sources) {

			GraphNode s = nodes[si];
			RoaringBitmap bits_yes = reach_yes.get(s.id), bits_no = reach_no.get(s.id);
			for (int j = 0; j < targets.size(); j++) {
				int ti = targets.get(j);
				if (ti == si)
					continue; // skip this case for doing pattern matching
				GraphNode t = nodes[ti];

				if (bits_yes != null && bits_yes.contains(j)) {
                
					count++;
					// System.out.println(si + " => " + ti);
					continue;
				}

				if (bits_no != null && bits_no.contains(j)) {
					 
					continue;
				}

				if (chkReach_iter(s, t, j, reach_yes, reach_no)) {
					count++;
				}
			}

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	// For each source node, check if some of the targets are in the adjacency
	// list of that node
	public RoaringList searchADJ(ArrayList<Integer> sources, ArrayList<Integer> targets) {

		int count = 0;
		RoaringBitmap t_bits = new RoaringBitmap();

		for (int t : targets)
			t_bits.add(t);

		RoaringList reach_yes = new RoaringList(nodes.length);

		for (int si : sources) {

			GraphNode s = nodes[si];
			if (s.N_O_SZ == 0)
				continue;
			RoaringBitmap s_bits = new RoaringBitmap();
			for (int c : s.N_O) {

				s_bits.add(c);
			}

			RoaringBitmap rs_and = RoaringBitmap.and(s_bits, t_bits);
			count += rs_and.getCardinality();
			reach_yes.set(s.id, rs_and);

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	public RoaringList searchADJ_BUP(ArrayList<GraphNode> sources, ArrayList<PoolEntry> targets) {

		int count = 0;
		RoaringBitmap t_bits = new RoaringBitmap();

		for (PoolEntry e : targets)
			t_bits.add(e.mValue.id);

		RoaringList reach_yes = new RoaringList(nodes.length);

		for (GraphNode s : sources) {

			RoaringBitmap s_bits = new RoaringBitmap();
			for (int c : s.N_O) {

				s_bits.add(c);
			}

			RoaringBitmap rs_and = RoaringBitmap.and(s_bits, t_bits);
			count += rs_and.getCardinality();
			reach_yes.set(s.id, rs_and);

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	public RoaringList searchADJ_BUP(RoaringBitmap source_filter, ArrayList<PoolEntry> targets) {

		int count = 0;
		RoaringBitmap t_bits = new RoaringBitmap();

		for (PoolEntry e : targets)
			t_bits.add(e.mValue.id);

		RoaringList reach_yes = new RoaringList(nodes.length);

		for (int si : source_filter) {
			GraphNode s = nodes[si];
			if (s.N_O_SZ == 0)
				continue;
			RoaringBitmap s_bits = new RoaringBitmap();

			for (int c : s.N_O) {

				s_bits.add(c);
			}

			RoaringBitmap rs_and = RoaringBitmap.and(s_bits, t_bits);
			count += rs_and.getCardinality();
			reach_yes.set(s.id, rs_and);

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	public RoaringList searchADJ_BUP(RoaringBitmap source_filter, RoaringBitmap t_bits) {

		int count = 0;
		RoaringList reach_yes = new RoaringList(nodes.length);

		for (int si : source_filter) {
			GraphNode s = nodes[si];
			if (s.N_O_SZ == 0)
				continue;
			RoaringBitmap s_bits = new RoaringBitmap();

			for (int c : s.N_O) {

				s_bits.add(c);
			}

			RoaringBitmap rs_and = RoaringBitmap.and(s_bits, t_bits);
			count += rs_and.getCardinality();
			reach_yes.set(s.id, rs_and);

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	// used in the BUP algorithm
	public RoaringList search_BUP(ArrayList<GraphNode> sources, ArrayList<PoolEntry> targets) {

		int count = 0;
		RoaringList reach_yes = new RoaringList(nodes.length);
		RoaringList reach_no = new RoaringList(nodes.length);
		for (GraphNode s : sources) {
			int si = s.id;
			RoaringBitmap bits_yes = reach_yes.get(s.id), bits_no = reach_no.get(s.id);
			for (int j = 0; j < targets.size(); j++) {
				GraphNode t = targets.get(j).mValue;
				int ti = t.id;
				if (ti == si)
					continue; // skip this case for doing pattern matching

				if (bits_yes != null && bits_yes.contains(j)) {

					count++;
					// System.out.println(si + " => " + ti);
					continue;
				}

				if (bits_no != null && bits_no.contains(j)) {
					continue;
				}

				if (chkReach_iter(s, t, j, reach_yes, reach_no)) {
					count++;
				}
			}

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	public RoaringList search_BUP(RoaringBitmap source_filter, ArrayList<PoolEntry> targets) {

		TimeTracker tt = new TimeTracker();
		tt.Start();
		
		RoaringList reach_yes = new RoaringList(nodes.length);
		RoaringList reach_no = new RoaringList(nodes.length);
		
		System.out.println("Input source card: " + source_filter.getCardinality() + " target card:" + targets.size());
		for (int si : source_filter) {
			GraphNode s = nodes[si];
			RoaringBitmap bits_yes = reach_yes.get(s.id), bits_no = reach_no.get(s.id);
			for (int j = 0; j < targets.size(); j++) {
				GraphNode t = targets.get(j).mValue;
			
				int ti = t.id;
				if (ti == si)
					continue; // skip this case for doing pattern matching
			
				if (bits_yes != null && bits_yes.contains(j)) {
			
				// System.out.println(si + " => " + ti);
					continue;
				}

				if (bits_no != null && bits_no.contains(j)) {
					continue;
				}

				chkReach_iter(s, t, j, reach_yes, reach_no);
			}

		}

		System.out.println("Time on search_BUP: " + tt.Stop()/1000 + "s");
		return reach_yes;
	}

	public RoaringList search(RoaringBitmap source_filter, ArrayList<Integer> targets) {

		int count = 0;
		RoaringList reach_yes = new RoaringList(nodes.length);
		RoaringList reach_no = new RoaringList(nodes.length);

		for (int si : source_filter) {

			GraphNode s = nodes[si];
			RoaringBitmap bits_yes = reach_yes.get(s.id), bits_no = reach_no.get(s.id);
			for (int j = 0; j < targets.size(); j++) {
				int ti = targets.get(j);
				if (ti == si)
					continue; // skip this case for doing pattern matching
				GraphNode t = nodes[ti];

				if (bits_yes != null && bits_yes.contains(j)) {

					count++;
					// System.out.println(si + " => " + ti);
					continue;
				}

				if (bits_no != null && bits_no.contains(j)) {
					continue;
				}

				if (chkReach_iter(s, t, j, reach_yes, reach_no)) {
					count++;
				}
			}

		}

		// System.out.println("Total number of connected pairs: " + count);
		return reach_yes;
	}

	public void searchBas(ArrayList<Integer> sources, ArrayList<Integer> targets) {

		BFLIndex bfl = new BFLIndex(nodes);
		int count = 0;
		for (int si : sources) {

			GraphNode s = nodes[si];
			for (int ti : targets) {

				GraphNode t = nodes[ti];
				int result = bfl.reach(s, t);
				if (result == 1) {
					// System.out.println(si + " => " + ti);
					count++;
				}
			}

		}

		// System.out.println("Total number of connected pairs: " + count);

	}

	public void searchBas_BUP(ArrayList<GraphNode> sources, ArrayList<PoolEntry> targets) {

		BFLIndex bfl = new BFLIndex(nodes);
		int count = 0;
		for (GraphNode s : sources) {

			for (PoolEntry e : targets) {

				GraphNode t = e.getValue();
				int result = bfl.reach(s, t);
				if (result == 1) {
					// System.out.println(si + " => " + ti);
					count++;
				}
			}

		}

		// System.out.println("Total number of connected pairs: " + count);

	}

	
	private boolean chkReach_iter(GraphNode s, GraphNode t, int ti, RoaringList reach_yes, RoaringList reach_no) {

		int rs = chkReach_base(s, t);
		if (rs == 1) {
			reach_yes.add(s.id, ti);
			return true;
		} else if (rs == 0) {
			reach_no.add(s.id, ti);
			return false;
		}
		// otherwise undetermined
		Stack<Pair<GraphNode, Integer>> stack = new Stack<Pair<GraphNode, Integer>>();
		stack.push(new Pair(s, 0));

		while (!stack.isEmpty()) {
			Pair<GraphNode, Integer> entry = stack.peek();
			GraphNode u = entry.getFirst();
			int idx = entry.getSecond();

			ArrayList<Integer> children = u.N_O;

			if (idx < children.size()) {
				GraphNode v = nodes[children.get(idx)];
				idx++;
				entry.setSecond(idx);
				stack.set(stack.size() - 1, entry);
				
				RoaringBitmap bits_yes = reach_yes.get(v.id), bits_no = reach_no.get(v.id);
				if (bits_yes != null && bits_yes.contains(ti)) {
					setReachFlag(stack, ti, reach_yes);
					return true;
				}

				if (bits_no != null && bits_no.contains(ti)) {

					continue;
				}

				rs = chkReach_base(v, t);
				if (rs == 1) {
					if (v.id != t.id)
						reach_yes.add(v.id, ti);
					setReachFlag(stack, ti, reach_yes);
					return true;
				}
				//if (rs == 0)
				//	reach_no.add(v.id, ti);
				// cannot return false when rs == 0, since it is possible
				// that another child node v of u can reach t.
				if (rs == -1) { // undetermined, need to check v's children

					stack.push(new Pair(v, 0));
				}

				continue;
			}

			stack.pop();
			// u does not reach t
			reach_no.add(u.id, ti);
		}

		// have visited all the children of U
		reach_no.add(s.id, ti);
		return false;
	}

	private void setReachFlag(Stack<Pair<GraphNode, Integer>> stack, int ti, RoaringList reach_yes) {

		Iterator<helper.Pair<GraphNode, Integer>> it = stack.iterator();

		while (it.hasNext()) {

			helper.Pair<GraphNode, Integer> entry = it.next();
			reach_yes.add(entry.getFirst().id, ti);
			// System.out.println("Setting node " + entry.getValue0().id + " to
			// index " + ti + " to true.");
		}

	}

	private int chkReach_base(GraphNode u, GraphNode v) {

		// 0: false, 1: true
		// from u to v, u starts earlier than v
		if (u.L_interval.mEnd < v.L_interval.mEnd) {
			return 0;
			// must <=
		} else if (u.L_interval.mStart <= v.L_interval.mStart) {
			return 1;
		}

		if (v.N_I_SZ == 0) {
			return 0;
		}
		if (u.N_O_SZ == 0) {
			return 0;
		}
		if (v.N_O_SZ == 0) {

			int u_h_in = ((GraphNode) v).h_out & 0x0FFFFFFFF;
			if ((u.L_out[u_h_in >>> 5] & (1 << (((GraphNode) v).h_out & 31))) == 0)
				return 0;

		} else {
			for (int i = 0; i < Consts.K; i++) {
				if ((u.L_out[i] & v.L_out[i]) != v.L_out[i]) {
					return 0;
				}
			}
		}

		if (u.N_I_SZ == 0) {
			int u_h_in = ((GraphNode) u).h_in & 0x0FFFFFFFF;
			if ((v.L_in[u_h_in >>> 5] & (1 << (((GraphNode) u).h_in & 31))) == 0)
				return 0;

		} else {
			for (int i = 0; i < Consts.K; i++) {
				if ((u.L_in[i] & v.L_in[i]) != u.L_in[i]) {
					return 0;
				}
			}
		}

		return -1; // undetermined

	}

	public static void main(String[] args) {

	}

}
