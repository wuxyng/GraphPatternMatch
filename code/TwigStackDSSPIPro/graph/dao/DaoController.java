/**
 * 
 */
package graph.dao;

import java.util.ArrayList;
import java.util.HashMap;
import graph.Digraph;
import graph.Node;
import graph.SSPIndex;

/**
 * @author xiaoying
 *
 */
public class DaoController {

	public ArrayList<ArrayList<Node>> invLsts;
	public SSPIndex SSPI;  
	
	public static HashMap<String, Integer> l2iMap;
	public static HashMap<Integer, String> i2lMap;
	
	public static Digraph G;
	
	
	
	public DaoController(){
		
	}
	
    public ArrayList<ArrayList<Node>> getInvLsts(){
    	
    	return invLsts;
    }
		
	public SSPIndex getSSPI(){
		
		return SSPI;
	}
	
	public Digraph getDiGraph(){
		
		return G;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
