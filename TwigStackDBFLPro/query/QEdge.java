package query;

public class QEdge {

	public int from, to;
	public int axis; // 0:child, 1:descendant

	public QEdge(int f, int t) {

		from = f;
		to = t;
		axis = 1; // descendant by default
	}

	public QEdge(int f, int t, int a) {

		from = f;
		to = t;
		axis = a; 
	}

	
	public String toString() {

		StringBuffer s = new StringBuffer();
		s.append("[");
		s.append(from + "," + axis + "," + to + "]");
		return s.toString();
	}
}
