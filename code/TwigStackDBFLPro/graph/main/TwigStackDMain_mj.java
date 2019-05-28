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

import dao.BFLIndex;
import dao.DaoController;
import global.Consts;
import global.Flags;
import graph.GraphNode;
import helper.QueryEvalStat;
import helper.QueryEvalStats;
import helper.TimeTracker;
import query.Query;
import query.QueryParser;
import query.TreeQuery;
import queryEvaluator.TwigStackD_mj;
import queryEvaluator.TwigStackD_post;


/**
 * @author xiaoying
 *
 */
public class TwigStackDMain_mj {

	ArrayList<TreeQuery> queries;
	HashMap<String, Integer> l2iMap;

	String queryFileN, dataFileN, outFileN;
	ArrayList<ArrayList<GraphNode>> invLsts;
	BFLIndex bfl;

	TimeTracker tt;
	QueryEvalStats stats;

	public TwigStackDMain_mj(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		String suffix = ".csv";
		String fn = queryFN.substring(0, queryFN.lastIndexOf('.'));
		outFileN = Consts.OUTDIR + "sum_"+ fn+"_bflmj"+suffix;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigStackD-bfl-mj");
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
		evaluate();
		System.out.println("\nTotal eval time: " + tt.Stop() / 1000 + "sec.");
		//writeStats(Consts.OUTDIR + "Summary.out");
		writeStatsToCSV();
	}

	private void evaluate() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries.size(); Q++) {
				TreeQuery query = queries.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				TwigStackD_mj tsd = new TwigStackD_mj(query, invLsts, bfl);
				QueryEvalStat stat = tsd.run();
				Flags.mt.run();
				stats.add(i, Q, stat);
				tsd.clear();
				// tsd.printTPQSolns();
			}
		}

	}

	private void evaluate_post() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries.size(); Q++) {
				TreeQuery query = queries.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				TwigStackD_post tsd = new TwigStackD_post(query, invLsts, bfl);
				QueryEvalStat stat = tsd.run();
				stats.add(i, Q, stat);
				tsd.clear();
				// tsd.printTPQSolns();
			}
		}

	}

	private void loadData() {
		DaoController dao = new DaoController(dataFileN, stats);
		dao.loadGraphAndIndex();
		invLsts = dao.invLsts;
		l2iMap = dao.l2iMap;
		bfl = dao.bfl;
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

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigStackDMain_mj psMain = new TwigStackDMain_mj(dataFileN, queryFileN);
		psMain.run();
	}

}
