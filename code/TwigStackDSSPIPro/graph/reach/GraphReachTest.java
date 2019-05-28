package graph.reach;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Pattern;

import graph.Digraph;
import graph.DirectedDFSIter;
import graph.Node;
import graph.SSPIndex;
import graph.dao.DigraphTxtLoader;
import helper.TimeTracker;

public class GraphReachTest {

	DigraphTxtLoader loader;
	SSPIndex mSSPI;
	Node[] nodes;
	
	public GraphReachTest(Digraph g) {
		
		nodes = g.getNodes();
		DirectedDFSIter dfs = new DirectedDFSIter(g);
		mSSPI = dfs.getSSPI();
		
	}
	
	public GraphReachTest(String graFN) {
		TimeTracker tt = new TimeTracker();
		tt.Start();
		System.out.println("loading graph " + graFN);
		DigraphTxtLoader loader = new DigraphTxtLoader(graFN);
		//loader.loadGRA();
		loader.loadVE();
		Digraph g = loader.getGraph();
		System.out.println("total loading time " + tt.Stop()/1000+ "sec.");
		nodes = g.getNodes();
		tt.Start();
		System.out.println("building SSPI index ");
		//ArrayList<Integer> sources = g.getSources();
		//DirectedDFSIter dfs = new DirectedDFSIter(g, sources);
		DirectedDFSIter dfs = new DirectedDFSIter(g);
		dfs.buildSSPI();
		mSSPI = dfs.getSSPI();
		//mSSPI.print();
		System.out.println("total index building time " + tt.Stop()/1000+ "sec.");
	}
	
	public void run(String qryFN) {
		System.out.println("Processing reach queries " + qryFN);
		try {
			TimeTracker tt = new TimeTracker();
			tt.Start();
			BufferedReader in = new BufferedReader(new FileReader(qryFN));
			int[] buf = null;
			int count = 0;
			int checked = 0;
			while ((buf = getNextLine(in)) != null) {
				checked++;
                //if(checked>1950)
               // 	break;
				int u = buf[0], v = buf[1], r = buf[2];
				Node u_n = nodes[u], v_n = nodes[v];
				//boolean canReach = mSSPI.reach(u_n, v_n);
				boolean canReach = mSSPI.reachBySSPI(u_n, v_n);
				int result = canReach ? 1 : 0;
				//if(checked>1900)
				//System.out.println("Testing query: " + u + " to " + v + " reachable =" + result);
				if (r == -1 || r == result) {

					count++;
				}
				
			
			}

			System.out.println("Time for processing queries:" + tt.Stop() / 1000 + "sec.");
			System.out.println("Total num of reachable queries:" + count);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	private int[] getNextLine(BufferedReader in) {

		String line = readNextLine(in);
		if (line != null) {
			// id: ids#
			String[] strArr = splitLine(line);
			int sz = strArr.length;
			int[] buf = new int[sz];
			for (int i = 0; i < sz; ++i) {
				buf[i] = Integer.parseInt(strArr[i]);
			}

			return buf;

		}
		return null;
	}

	private String[] splitLine(String aString) {

		Pattern p = Pattern.compile("[,:#\\s]+"); // ("[\\s]+");
		// Split input with the pattern
		String[] result = p.split(aString);

		return result;

	}

	private static String readNextLine(BufferedReader in) {
		String line = null;

		try {
			// read a non-emtpy line
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return line;
	}

	
	public static void main(String[] args) {
		
		GraphReachTest test = new GraphReachTest
				(".\\input\\xm01e.dag");
				//("E:\\experiments\\datasets\\graphs\\data\\mydag.gra");
				//("E:\\experiments\\datasets\\graphs\\data\\citeseerx.gra");
				//("E:\\experiments\\datasets\\graphs\\data\\sigmod08\\amaze_dag_uniq.gra");
				//("E:\\experiments\\datasets\\graphs\\data\\sigmod08\\human_dag_uniq.gra");
				//("E:\\experiments\\datasets\\graphs\\data\\sigmod09\\citeseer_sub_10720.gra");
				//("E:\\experiments\\datasets\\graphs\\data\\sigmod08\\xmark_dag_uniq.gra");
				test.run
				(".\\input\\xm01e.test");
				//("E:\\experiments\\datasets\\graphs\\qrys\\sigmod08\\xmark.test");
				//("E:\\experiments\\datasets\\graphs\\qrys\\large\\citeseerx.test");
				//("E:\\experiments\\datasets\\graphs\\qrys\\mydag.test");
				//("E:\\experiments\\datasets\\graphs\\qrys\\sigmod09\\citeseer.test");
				//("E:\\experiments\\datasets\\graphs\\qrys\\sigmod08\\human.test");
		//("E:\\experiments\\datasets\\graphs\\qrys\\sigmod08\\amaze.test");
		//("E:\\experiments\\datasets\\graphs\\qrys\\large\\citeseerx.test");
	}

}
