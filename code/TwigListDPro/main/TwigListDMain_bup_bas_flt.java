package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import dao.BFLIndex;
import dao.DaoController;
import global.Consts;
import global.Flags;
import graph.Digraph;
import graph.GraphNode;
import helper.QueryEvalStats;
import helper.QueryEvalStat;
import helper.TimeTracker;
import prefilter.FilterBuilder;
import query.QNode;
import query.Query;
import query.QueryParser;
import query.TreeQuery;
import queryEvaluator.TwigListD_bup_bas_flt;



public class TwigListDMain_bup_bas_flt {

	ArrayList<TreeQuery> queries_tree;
	ArrayList<Query> queries;
	HashMap<String, Integer> l2iMap;
	String queryFileN, dataFileN, outFileN;
	ArrayList<ArrayList<GraphNode>> invLsts;
	BFLIndex bfl;
	TimeTracker tt;
	QueryEvalStats stats;
	Digraph g;

	public TwigListDMain_bup_bas_flt(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		String suffix = ".csv";
		String fn = queryFN.substring(0, queryFN.lastIndexOf('.'));
		outFileN = Consts.OUTDIR + "sum_"+ fn+"_bupbasflt"+suffix;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigListDPro_bup_bas_flt");
	}

	public void run() {

		tt = new TimeTracker();

		System.out.println("loading graph ...");
		tt.Start();
		loadData();
		double ltm = tt.Stop() / 1000;
		System.out.println("\nTotal loading and building time: " + ltm + "sec.");

		System.out.println("reading queries ...");
		readQueries();

		System.out.println("\nEvaluating queries from " + queryFileN + " ...");
		tt.Start();
		evaluate();
		System.out.println("\nTotal eval time: " + tt.Stop() / 1000 + "sec.");
		//writeStats(Consts.OUTDIR + "Summary.out");
		writeStatsToCSV();
	}

	private void loadData() {
		DaoController dao = new DaoController(dataFileN, stats);
		dao.loadGraphAndIndex();
		invLsts = dao.invLsts;
		//invLstsByID = dao.invLstsByID;
		l2iMap = dao.l2iMap;
		bfl = dao.bfl;
		g = dao.G;
	}

	private void readQueries() {

		queries_tree = new ArrayList<TreeQuery>();
		queries = new ArrayList<Query>();
		QueryParser queryParser = new QueryParser(queryFileN, l2iMap);
		Query query = null;
		int count = 0;
		int noTrees = 0;
		while ((query = queryParser.readNextQuery()) != null) {
			printSize(query);
			if (query.isTree()) {
				noTrees++;
				TreeQuery treeQ = new TreeQuery(query);
				treeQ.extractQueryInfo();
				queries_tree.add(treeQ);
				queries.add(query);
				System.out.println("*************Query " + (count++) + "*************");
				System.out.println(query);
			}

		}
		System.out.println("Total tree queries: " + noTrees);

	}

	private void evaluate() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries_tree.size(); Q++) {
				Query query = queries.get(Q);
				double totNodes_before =getTotNodes(query);
				//printSize(query);
				TreeQuery query_tree = queries_tree.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				FilterBuilder fb = new FilterBuilder(g, query);
				fb.oneRun();
				double bldTM = fb.getBuildTime();
				double totNodes_after = fb.getTotNodes();
				//fb.sortInvLstsByID();
				ArrayList<ArrayList<GraphNode>> invLsts_reduced = fb.getInvLsts();
				TwigListD_bup_bas_flt tld = new TwigListD_bup_bas_flt(query_tree, invLsts_reduced, bfl);
				QueryEvalStat stat = tld.run(bldTM);
				Flags.mt.run();
				stat.totNodesBefore = totNodes_before;
				stat.totNodesAfter = totNodes_after;
				stats.add(i, Q, stat);
				tld.clear();

			}
		}
	}

	private void writeStats(String outfileN) {

		// write results to summary file
		FileWriter summary;
		try {
			summary = new FileWriter(outfileN, true);
			summary.write("***********************************\r\n");
			summary.write(stats + "\n");
			summary.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void writeStatsToCSV() {
		PrintWriter opw;
		
		try {
			opw = new PrintWriter(new FileOutputStream(outFileN, false));
			stats.printToFile(opw);
			opw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private double getTotNodes(Query qry) {

		double totNodes= 0.0;
	
		QNode[] nodes = qry.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<GraphNode> invLst = invLsts.get(n.lb);
			totNodes+=invLst.size();
			
		}

		return totNodes;
	}


	private void printSize(Query qry) {

		System.out.println("Original inverted list sizes: ");
		QNode[] nodes = qry.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<GraphNode> invLst = invLsts.get(n.lb);
			System.out.println(n.id + ":" + invLst.size());
		}

	}

	
	public static void main(String[] args) {
		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigListDMain_bup_bas_flt psMain = new TwigListDMain_bup_bas_flt(dataFileN, queryFileN);
		psMain.run();

	}

}
