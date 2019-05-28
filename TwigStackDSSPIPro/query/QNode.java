package query;

import java.util.ArrayList;

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
   
}
