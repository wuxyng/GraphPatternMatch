/**
 * 
 */
package graph.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import global.Consts;
import global.Flags;
import graph.Digraph;
import graph.Node;
import graph.SSPIndex;
import graph.dao.DaoController;
import graph.dao.DaoControllerTXT;
import helper.QueryEvalStat;
import helper.QueryEvalStats;
import helper.TimeTracker;
import prefilter.FilterBuilder;
import query.QNode;
import query.Query;
import query.QueryParser;
import query.TreeQuery;
import queryEvaluator.TwigStackD_mixed_ag_flt;


/**
 * @author xiaoying
 *
 */
public class TwigStackDMain_sspi_mixed_ag_flt {

	ArrayList<TreeQuery> queries_tree;
	ArrayList<Query> queries;
	HashMap<String, Integer> l2iMap;

	String queryFileN, dataFileN, outFileN;
	ArrayList<ArrayList<Node>> invLsts;
	SSPIndex SSPI;

	TimeTracker tt;
	QueryEvalStats stats;
	Digraph g;
	

	public TwigStackDMain_sspi_mixed_ag_flt(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		String suffix = ".csv";
		String fn = queryFN.substring(0, queryFN.lastIndexOf('.'));
		outFileN = Consts.OUTDIR + "sum_"+ fn+"_sspimixedagflt"+suffix;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigStackD-SSPI-mixed-ag-FLT");
	}

	public void run() {

		tt = new TimeTracker();

		System.out.println("loading graph ...");
		tt.Start();
		loadData();
		System.out.println("\nTotal loading time: " + tt.Stop() / 1000 + "sec.");

		System.out.println("reading queries ...");
		readQueries();

		System.out.println("\nEvaluating queries from " + queryFileN + " ...");
		tt.Start();
		SSPI.openFile();
		evaluate();
		SSPI.closeFile();
		System.out.println("\nTotal eval time: " + tt.Stop() / 1000 + "sec.");
		//writeStats(Consts.OUTDIR + "Summary.out");
		writeStatsToCSV();
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
				ArrayList<ArrayList<Node>> invLsts_reduced = fb.getInvLsts();
				TwigStackD_mixed_ag_flt tsd = new TwigStackD_mixed_ag_flt(query_tree, invLsts_reduced, SSPI, g);

				QueryEvalStat stat = tsd.run(bldTM);
				Flags.mt.run();
				stats.add(i, Q, stat);
				stat.totNodes_before = totNodes_before;
				stat.totNodes_after = totNodes_after;
				tsd.clear();

			}
		}

	}

	private void loadData() {
		DaoControllerTXT dao = new DaoControllerTXT(dataFileN, stats);
		dao.loadGraph();
		invLsts = dao.getInvLsts();
		SSPI = dao.getSSPI();
		g =dao.getDiGraph();
		l2iMap = DaoController.l2iMap;
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
			ArrayList<Node> invLst = invLsts.get(n.lb);
			totNodes+=invLst.size();
			
		}

		return totNodes;
	}
	
	private void printSize(Query qry) {

		System.out.println("Original inverted list sizes: ");
		QNode[] nodes = qry.nodes;
		for (int i = 0; i < nodes.length; i++) {
			QNode n = nodes[i];
			ArrayList<Node> invLst = invLsts.get(n.lb);
			System.out.println(n.id + ":" + invLst.size());
		}

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigStackDMain_sspi_mixed_ag_flt psMain = new TwigStackDMain_sspi_mixed_ag_flt(dataFileN, queryFileN);
		psMain.run();
	}

}
