/**
 *PathSolutionFormat.java Oct 8, 2009 11:48:47 PM
 *Author: Xiaoying Wu 
 *TODO
 */
package helper;

import java.util.ArrayList;
import java.util.Collections;


/**
 * 
 */
public class PathSolutionFormat implements Comparable<PathSolutionFormat> {

	protected ArrayList<Integer> solution;
	protected int length;

	public PathSolutionFormat(int length) {
		solution = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++)
			solution.add(-1);
		this.length = length;
	}
 	
	
	// copy constructor
	public PathSolutionFormat(PathSolutionFormat other) {

		this(other.length());
		for (int i = 0; i < length; i++) {
			this.addEntry(i, other.getEntry(i));
		}

	}

	public void clear(){
		solution.clear();
		this.length = 0;
	}
	
	public void addEntry(int pos, int entry) {

		solution.set(pos, entry);
	}

	public int length() {

		return length;
	}

	public int getEntry(int pos) {
		if (pos < length)
			return solution.get(pos);
		return Integer.MAX_VALUE;
	}

	public String getKey() {
		// get the composition of the solution fields
		String key = "";

		for (int i = 0; i < length; i++) {

			key = key + " " + getEntry(i);

		}

		return key;

	}

	public int compareTo(PathSolutionFormat other) {
		// TODO Auto-generated method stub
		if (length > other.length())
			return 1;
		else if (length < other.length())
			return -1;
		for (int i = 0; i < length; i++) {
			int aEntry = this.getEntry(i);
			int bEntry = other.getEntry(i);
			if (aEntry > bEntry)
				return 1;
			else if (aEntry < bEntry)
				return -1;
		}

		return 0;
	}

	// compare the two path solution up to the level value
	public int compareTo(PathSolutionFormat other, int level) {
		for (int i = 0; i <= level; i++) {
			int aEntry = this.getEntry(i);
			int bEntry = other.getEntry(i);
			if (aEntry > bEntry)
				return 1;
			else if (aEntry < bEntry)
				return -1;
		}

		return 0;
	}
	
	
	public String toString() {

		return solution.toString();
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<PathSolutionFormat> solnsA = new ArrayList<PathSolutionFormat>(10);

		PathSolutionFormat soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 1);
		soln.addEntry(2, 2);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 2);
		soln.addEntry(2, 3);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 2);
		soln.addEntry(2, 4);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 3);
		soln.addEntry(1, 1);
		soln.addEntry(2, 2);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 3);
		soln.addEntry(2, 7);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 3);
		soln.addEntry(1, 1);
		soln.addEntry(2, 9);
		solnsA.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 4);
		soln.addEntry(1, 2);
		soln.addEntry(2, 3);
		solnsA.add(soln);

		Collections.sort(solnsA);

		System.out.println("List A: ");
		for (int i = 0; i < solnsA.size(); i++) {
			PathSolutionFormat s = solnsA.get(i);
			System.out.println(s);
		}

		ArrayList<PathSolutionFormat> solnsB = new ArrayList<PathSolutionFormat>(10);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 2);
		soln.addEntry(2, 4);
		solnsB.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 2);
		soln.addEntry(2, 5);
		solnsB.add(soln);

		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 2);
		soln.addEntry(2, 8);
		solnsB.add(soln);
		
		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 1);
		soln.addEntry(1, 3);
		soln.addEntry(2, 9);
		solnsB.add(soln);
		
		
		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 2);
		soln.addEntry(1, 1);
		soln.addEntry(2, 2);
		solnsB.add(soln);
		
		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 3);
		soln.addEntry(1, 0);
		soln.addEntry(2, 2);
		solnsB.add(soln);
		
		soln = new PathSolutionFormat(3);
		soln.addEntry(0, 3);
		soln.addEntry(1, 1);
		soln.addEntry(2, 3);
		solnsB.add(soln);

		Collections.sort(solnsB);

		System.out.println("List B: ");
		for (int i = 0; i < solnsB.size(); i++) {
			PathSolutionFormat s = solnsB.get(i);
			System.out.println(s);
		}

	}

}
