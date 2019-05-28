/**
 * 
 */
package helper;
import java.util.ArrayList;
import java.util.Arrays;



/**
 * @author xiaoying
 * 
 */
public class TPQSolutionListFormat implements Comparable<TPQSolutionListFormat> {

	protected int[] solution;
	protected int length;
	
	
	public TPQSolutionListFormat(int max) {

		solution = new int[max];
		Arrays.fill(solution, -1);		
		length = max;
	}

	// copy constructor
	public TPQSolutionListFormat(TPQSolutionListFormat other) {

		this(other.length());
		for (int i = 0; i < length; i++) {
			this.addValue(i, other.getValue(i));
		}

	}

	
	public void clear() {
		for(int i=0; i<length; i++){
		    solution[i] = -1;
		}//this.length = 0;
	}

	public void addValue(int key, int value) {

		solution[key]= value;
	}

	public int length() {

		return length;
	}

	public int getValue(int key) {
		if (key < length)
			return solution[key];
		return -1;
	}

	public int compareTo(TPQSolutionListFormat other) {

		for (int i = 0; i < length; i++) {

			int aValue = this.getValue(i);
			int bValue = other.getValue(i);
			if(aValue == -1)
				continue;
			int diff = aValue - bValue;
			if(diff ==0 )
				continue;
			return diff;
		}

		return 0;
	}

	
	public int compareTo(TPQSolutionListFormat other, ArrayList<Integer> keyList) {

		for (int key:keyList) {

			int aValue = this.getValue(key);
			int bValue = other.getValue(key);
			if(aValue == -1)
				continue;
			int diff = aValue - bValue;
			if(diff ==0 )
				continue;
			return diff;
		}

		return 0;
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i=0; i<length; i++) {
			int value = getValue(i);
			sb.append(value + " ");
		}

		sb.append("]\n");
		return sb.toString();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
