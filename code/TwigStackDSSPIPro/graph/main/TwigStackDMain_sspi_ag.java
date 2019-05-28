/**
 * 
 */
package graph.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import global.Consts;
import global.Flags;
import graph.Node;
import graph.SSPIndex;
import graph.dao.DaoController;
import graph.dao.DaoControllerTXT;
import helper.QueryEvalStat;
import helper.QueryEvalStats;
import helper.TimeTracker;
import query.Query;
import query.QueryParser;
import query.TreeQuery;
import queryEvaluator.TwigStackD_ag;



/**
 * @author xiaoying
 *
 */
public class TwigStackDMain_sspi_ag {

	ArrayList<TreeQuery> queries;
	HashMap<String, Integer> l2iMap;

	String queryFileN, dataFileN;
	ArrayList<ArrayList<Node>> invLsts;
	SSPIndex SSPI;

	TimeTracker tt;
	QueryEvalStats stats;

	public TwigStackDMain_sspi_ag(String dataFN, String queryFN) {

		queryFileN = Consts.INDIR + queryFN;
		dataFileN = Consts.INDIR + dataFN;
		stats = new QueryEvalStats(dataFileN, queryFileN, "TwigStackD-SSPI-ag");
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
		writeStats(Consts.OUTDIR + "Summary.out");
	}

	private void evaluate() {
		for (int i = 0; i < Flags.REPEATS; i++) {
			for (int Q = 0; Q < queries.size(); Q++) {
				TreeQuery query = queries.get(Q);
				System.out.println("\nEvaluating query " + Q + " ...");
				TwigStackD_ag tsd = new TwigStackD_ag(query, invLsts, SSPI);
				QueryEvalStat stat = tsd.run();
				Flags.mt.run();
				stats.add(i, Q, stat);
				tsd.clear();

			}
		}

	}

	private void loadData() {
		DaoControllerTXT dao = new DaoControllerTXT(dataFileN, stats);
		dao.loadGraph();
		invLsts = dao.getInvLsts();
		SSPI = dao.getSSPI();
		l2iMap = DaoController.l2iMap;
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String dataFileN = args[0], queryFileN = args[1]; // the query file
		TwigStackDMain_sspi_ag psMain = new TwigStackDMain_sspi_ag(dataFileN, queryFileN);
		psMain.run();
	}

}
