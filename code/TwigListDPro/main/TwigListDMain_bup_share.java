package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import queryEvaluator.TwigListD_bup_share;
import queryEvaluator.TwigListD_bup_pro;

public class TwigListDMain_bup_share {

	ArrayList<TreeQuery> queries;
	HashMap<String, Integer> l2iMap;
	String queryFileN, dataFileN;
	ArrayList<ArrayList<GraphNode>> invLsts;
	GraphNode[] nodes;
	TimeTracker tt;
	QueryEvalStats stats;

	boolean sortByAxis = false;
	
	public TwigListDMain_bup_share(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigListDPro_bup_pro");
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
	}

	private void loadData() {
		DaoController dao = new DaoController(dataFileN, stats);
		dao.loadGraphAndIndex();
		invLsts = dao.invLstsByID;
		l2iMap = dao.l2iMap;
		nodes = dao.G.getNodes();
	}

	private void readQueries() {

		queries = new ArrayList<TreeQuery>();
		QueryParser queryParser = new QueryParser(queryFileN, l2iMap);
		Query query = null;
		int count = 0;
		int noTrees = 0;
		while ((query = queryParser.readNextQuery()) != null) {
			if (query.isTree()) {
				noTrees++;
				TreeQuery treeQ = new TreeQuery(query);
				treeQ.extractQueryInfo();
				queries.add(treeQ);
				System.out.println("*************Query " + (count++) + "*************");
				System.out.println(query);
			}

		}
		System.out.println("Total tree queries: " + noTrees);

	}

	private void evaluate() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries.size(); Q++) {
				TreeQuery query = queries.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				TwigListD_bup_share tld = new TwigListD_bup_share(query, invLsts, nodes);
				QueryEvalStat stat = tld.run(sortByAxis);
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

	public static void main(String[] args) {
		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigListDMain_bup_share psMain = new TwigListDMain_bup_share(dataFileN, queryFileN);
		psMain.run();

	}

}
