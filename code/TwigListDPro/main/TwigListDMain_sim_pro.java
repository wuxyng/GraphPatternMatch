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
import graph.GraphNode;
import helper.QueryEvalStats;
import helper.QueryEvalStat;
import helper.TimeTracker;
import query.Query;
import query.QueryParser;
import query.TreeQuery;
import queryEvaluator.TwigListD_sim_pro;



public class TwigListDMain_sim_pro {

	ArrayList<TreeQuery> queries;
	HashMap<String, Integer> l2iMap;
	String queryFileN, dataFileN;
	ArrayList<ArrayList<GraphNode>> invLsts, invLstsByID;
	BFLIndex bfl;
	TimeTracker tt;
	QueryEvalStats stats;

	boolean sortByCard = true;
	
	public TwigListDMain_sim_pro(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigListDPro_sim_pro");
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
		writeStats(Consts.OUTDIR + "Summary.out");
		//writeStatsToCSV(Consts.OUTDIR + "summary.csv");
	}

	private void loadData() {
		DaoController dao = new DaoController(dataFileN, stats);
		dao.loadGraphAndIndex();
		invLsts = dao.invLsts;
		invLstsByID = dao.invLstsByID;
		l2iMap = dao.l2iMap;
		bfl = dao.bfl;
	}

	private void readQueries() {

		queries = new ArrayList<TreeQuery>();
		QueryParser queryParser = new QueryParser(queryFileN, l2iMap);
		Query query = null;
		int count = 0;
		int noTrees = 0;
		double totQSize =0.0;
		while ((query = queryParser.readNextQuery()) != null) {
			if (query.isTree()) {
				noTrees++;
				TreeQuery treeQ = new TreeQuery(query);
				treeQ.extractQueryInfo();
				queries.add(treeQ);
				totQSize += treeQ.E+treeQ.V;
				System.out.println("*************Query " + (count++) + "*************");
				System.out.println(query);
			}

		}
		stats.setQryStat(totQSize/noTrees);
		System.out.println("Total tree queries: " + noTrees);
		System.out.println("Total tree query size: " + totQSize);

	}

	private void evaluate() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries.size(); Q++) {
				TreeQuery query = queries.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				TwigListD_sim_pro tld = new TwigListD_sim_pro(query, invLsts, invLstsByID, bfl);
				QueryEvalStat stat = tld.run();
				Flags.mt.run();
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

	private void writeStatsToCSV(String outfileN) {

		
		PrintWriter opw;
		
		try {
			opw = new PrintWriter(new FileOutputStream(outfileN, false));
			stats.printToFile(opw);
			opw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigListDMain_sim_pro psMain = new TwigListDMain_sim_pro(dataFileN, queryFileN);
		psMain.run();

	}

}
