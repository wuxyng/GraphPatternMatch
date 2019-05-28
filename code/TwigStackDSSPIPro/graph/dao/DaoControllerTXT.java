/**
 * 
 */
package graph.dao;

import java.util.ArrayList;

import global.Flags;
import graph.DirectedDFSIter;
import graph.Node;
import helper.QueryEvalStats;


/**
 * @author xiaoying
 *
 */
public class DaoControllerTXT extends DaoController{

	DigraphTxtLoader loader;
	QueryEvalStats stats = null; 
	
	public DaoControllerTXT(String fn, QueryEvalStats stats){
	
		loader = new DigraphTxtLoader(fn);
		this.stats = stats;
	}
	
	public DaoControllerTXT(String fn){
		
		loader = new DigraphTxtLoader(fn);
		
	}
	
	
	
	public void loadGraph(){
		
		loader.loadVE();
		Flags.mt.run();
		G = loader.getGraph();
		System.out.println("graph loaded: " + "V=" + G.V() + " E="+ G.E());
		stats.setGraphStat(G.V(), G.E(), G.getLabels());
		stats.setLoadTime(loader.getLoadTime());
		l2iMap = loader.getL2IMap();
		i2lMap = loader.getI2LMap();
		System.out.println("Generating encoding and SSPI.");
		genEncodingsAndSSPI();
		//System.out.println(G);
		
		
	}
	
	private void genEncodingsAndSSPI(){
		
		DirectedDFSIter dfs = new DirectedDFSIter(G);
		dfs.buildSSPI();
		Flags.mt.run();
		SSPI = dfs.getSSPI();
		invLsts = dfs.getInvLsts();
		stats.setBuildTime(dfs.getBldTime());
		//printInvLsts();
		//SSPI.print();
	}
	
	public void printInvLsts() {

		for (int key = 0; key < invLsts.size(); key++) {

			System.out.println("******Inverted List for Label: " + key);
			ArrayList<Node> entrys = invLsts.get(key);

			for (int i = 0; i < entrys.size(); i++) {

				System.out.println(entrys.get(i));

			}

			System.out.println("*****Total: " + entrys.size());

		}
	}
	
	public void printSSPI(){
		
	   SSPI.print();
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DaoControllerTXT dao = new DaoControllerTXT("E:\\experiments\\datasets\\data\\graphs\\test1.txt");
		dao.loadGraph();
        dao.printInvLsts();
        dao.printSSPI();
	}

}
