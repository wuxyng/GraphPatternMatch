package dao;

import java.util.ArrayList;
import java.util.HashMap;

import global.Flags;
import graph.Digraph;
import graph.GraphNode;
import helper.QueryEvalStats;

public class DaoController {

	public ArrayList<ArrayList<GraphNode>> invLsts;
	public static HashMap<String, Integer> l2iMap;
	public static HashMap<Integer, String> i2lMap;
    public BFLIndex bfl;
	public static Digraph G;
	DigraphLoader loader;
	BFLIndexBuilder bflBuilder;
	
	QueryEvalStats stats = null; 
	
	public DaoController(String fn, QueryEvalStats stats) {

		loader = new DigraphLoader(fn);
        this.stats = stats;
	}


	public void loadGraphAndIndex() {

		loader.loadVE();
		Flags.mt.run();
		G = loader.getGraph();
		System.out.println("graph loaded: " + "V=" + G.V() + " E="+ G.E());
		stats.setGraphStat(G.V(), G.E(), G.getLabels());
		stats.setLoadTime(loader.getLoadTime());
		l2iMap = loader.getL2IMap();
		bflBuilder = new BFLIndexBuilder(G);
		bflBuilder.run();
		Flags.mt.run();
		stats.setBuildTime(bflBuilder.getBldTime());
		invLsts = bflBuilder.getInvLsts();
		GraphNode[] nodes = G.getNodes();
		bfl = new BFLIndex(nodes);
	
	}
	
	

	public static void main(String[] args) {

	}

}
