package dao;

import java.util.ArrayList;
import java.util.LinkedList;

import graph.GraphNode;

public class MatList {

	LinkedList<GraphNode> elist;

	public MatList() {

		elist = new LinkedList<GraphNode>();
	}
	
	public LinkedList<GraphNode> elist(){
		
		return elist;
	}

	public void addEntry(GraphNode entry) {

		elist.add(entry);

	}
	
	public void addList(ArrayList<GraphNode> lst){
		
	
		for(GraphNode e:lst){
			
			addEntry(e);
		}
		
	}

	public static void main(String[] args) {

	}

}
