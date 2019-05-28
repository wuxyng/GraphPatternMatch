package query;

import java.util.ArrayList;
import java.util.Comparator;

public class QNode {

	public int id; // node id
	public int lb; // label id
	public int N_O_SZ = 0, N_I_SZ = 0;
	public ArrayList<Integer> N_O, N_I;
	public ArrayList<QEdge> E_O, E_I; // outgoing and incoming edges

	public QNode(){};

    public boolean isSink(){
		
		if(N_O_SZ==0)
			return true;
		return false;
	}
    
 
    public boolean isSource(){
    	
    	if(N_I_SZ==0)
			return true;
		return false;
    }
    
    
    /*Comparator for sorting the list by axis */
    public static Comparator<QNode> AxisComparator = new Comparator<QNode>(){

		@Override
		public int compare(QNode n1, QNode n2) {
			
			return n1.E_I.get(0).axis - n2.E_I.get(0).axis;
			
		}
    	
    	
    };
   
}
