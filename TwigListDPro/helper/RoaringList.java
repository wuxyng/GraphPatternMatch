package helper;

import org.roaringbitmap.RoaringBitmap;

public class RoaringList {

	int length;
	RoaringBitmap[] rlist;

	RoaringBitmap state;

	public RoaringList(int len) {
		length = len;
		rlist = new RoaringBitmap[len];
		// init();
		state = new RoaringBitmap();

	}

	public RoaringBitmap getState() {

		return state;
	}

	public RoaringBitmap get(int pos) {

		return rlist[pos];
	}

	public void or(int pos, RoaringBitmap other) {

		if (rlist[pos] == null) {
			rlist[pos] = other;
		} else
			rlist[pos].or(other);
		state.add(pos);
	}

	public void and(int pos, RoaringBitmap other) {
		/*if (rlist[pos] == null) {
			rlist[pos] = other;
		} else
		*/
		rlist[pos].and(other);
		if (rlist[pos].isEmpty())
			state.flip(pos);
	}

	public void set(int pos, RoaringBitmap newBits) {

		rlist[pos] = newBits;
		state.add(pos);
		if (newBits.isEmpty())
			state.flip(pos);

	}

	public void add(int pos, int val) {

		if (rlist[pos] == null) {
			rlist[pos] = new RoaringBitmap();
		}

		rlist[pos].add(val);
		state.add(pos);
	}

	public void add(int pos, int start, int end){
	
		if (rlist[pos] == null) {
			rlist[pos] = new RoaringBitmap();
		}

		rlist[pos].add(start, end);
		state.add(pos);
		
	}
	
	public boolean isEmpty() {

		return state.isEmpty();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
