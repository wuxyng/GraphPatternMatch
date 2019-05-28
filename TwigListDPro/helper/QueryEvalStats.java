package helper;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import global.Flags;

public class QueryEvalStats {

	public String dataFN, qryFN, algN;
	public int V, E, numLbs;
	public double loadTime = 0.0, bldTime = 0.0;
	public double evalTime;
    public double avgQSize =0.0;
	// public double totmem = 0.0;
	ArrayList<QueryEvalStat>[] qryEvalStats;
	public double[] totTimes,totMatchTimes, totJoinTimes;

	public QueryEvalStats() {

		qryEvalStats = (ArrayList<QueryEvalStat>[]) new ArrayList[Flags.REPEATS];
		totTimes = new double[Flags.REPEATS];
		totMatchTimes = new double[Flags.REPEATS];
		totJoinTimes = new double[Flags.REPEATS];
	}

	public QueryEvalStats(String dataFN, String qryFN, String algN) {

		qryEvalStats = (ArrayList<QueryEvalStat>[]) new ArrayList[Flags.REPEATS];
		totTimes = new double[Flags.REPEATS];
		totMatchTimes = new double[Flags.REPEATS];
		totJoinTimes = new double[Flags.REPEATS];
		this.dataFN = dataFN;
		this.qryFN = qryFN;
		this.algN = algN;
	}

	public void setGraphStat(int V, int E, int lbs) {

		this.V = V;
		this.E = E;
		numLbs = lbs;

	}

	public void setQryStat(double size){
		
		avgQSize = size;
	}
	
	public void setLoadTime(double lt) {

		loadTime = lt;

	}

	public void setBuildTime(double bt) {

		bldTime = bt;
	}

	public void add(int iter, int qid, double mt, double et, double jt, double solns) {

		QueryEvalStat qst = new QueryEvalStat(mt, et, jt, solns);
		// add(qst);
		add(iter, qid, qst);
	}

	public void add(int iter, int qid, QueryEvalStat qst) {
		if (qryEvalStats[iter] == null)
			qryEvalStats[iter] = new ArrayList<QueryEvalStat>();

		qryEvalStats[iter].add(qid, qst);
		
	}

	
	public void printToFile(PrintWriter opw){
	     
		DecimalFormat f = new DecimalFormat("##.00");
		
		int numQs = qryEvalStats[0].size();
		double[] evalTimes, matchTimes, enumTimes, joinTimes;
		evalTimes = new double[numQs];
		matchTimes = new double[numQs];
		enumTimes =  new double[numQs];
		joinTimes = new double[numQs];
		
		double totNodesBefore =0.0, totNodesAfter = 0.0, totSolnNodes =0.0, totSizeOfAG = 0.0;
		double totSolns =0.0; 
		
		opw.append("id" +  " " + "matchTime" + " " + "enumTime"
				       +  " " + "joinTime"  + " " + "totTime"
				       +  " " + "numSoln"   + " " + "solnNodes" 
				                            + " " + "numNodesBefore" 				
				                            + " " + "numNodesAfter" 
				                            + " " + "answerGraphSize" 		     					 
				+ "\r\n");
		for (int i = 0; i < Flags.REPEATS; i++) {
			ArrayList<QueryEvalStat> qryEvalStatList = qryEvalStats[i];
			for (int q = 0; q < numQs; q++) {
				QueryEvalStat stat = qryEvalStatList.get(q);
				matchTimes[q] += stat.matchTime;
				enumTimes[q]  += stat.enumTime;
				joinTimes[q]  += stat.joinTime;
				evalTimes[q]  += stat.totTime;
				totTimes[i] += evalTimes[q];
				totMatchTimes[i] += matchTimes[q];
				totJoinTimes[i] += joinTimes[q];
				if(i==0){
					totNodesBefore +=stat.totNodesBefore;
					totNodesAfter  +=stat.totNodesAfter; 
					totSolnNodes    +=stat.totSolnNodes;
					totSizeOfAG     +=stat.sizeOfAnsGraph;
					totSolns        +=stat.numSolns;
					
				}
				
				
				opw.append(q + " " +  f.format(stat.matchTime) + " " 
						           + f.format(stat.enumTime)   + " " 
						           + f.format(stat.joinTime)   + " " 
						           + f.format(stat.totTime)    + " "
						           + new DecimalFormat(",###").format(stat.numSolns) + " "
						           + new DecimalFormat(",###").format(stat.totSolnNodes) + " "
						           + new DecimalFormat(",###").format(stat.totNodesBefore) + " "
						           + new DecimalFormat(",###").format(stat.totNodesAfter) + " "
						           + new DecimalFormat(",###").format(stat.sizeOfAnsGraph)
						     					 
						+ "\r\n");
			}
		}
		
		opw.append("Average running time: \r\n");
		for (int q = 0; q < numQs; q++) {
			
			opw.append("[q" + q + " " + "matchTime:" + f.format(matchTimes[q]/Flags.REPEATS) + " " + "enumTime:"
					+ f.format(enumTimes[q]/Flags.REPEATS) + " " + "joinTime:" + f.format(joinTimes[q]/Flags.REPEATS) + " " + "totTime:"
					+ f.format(evalTimes[q]/Flags.REPEATS)
					+ "]\r\n");	
		}

		opw.append("Data loading Time:" + f.format(loadTime) + "\r\n");
		opw.append("Index building Time:" + f.format(bldTime) + "\r\n");
		double avgExeTime = calAvg(totTimes);
		double avgMatTime = calAvg(totMatchTimes);
		double avgJoinTime = calAvg(totJoinTimes);
		opw.append("Average Eval Time per run:" + f.format(avgExeTime) + "\r\n");
		opw.append("Average Eval Time per query:" + f.format(avgExeTime/numQs) + "\r\n");
		opw.append("Average Matching Time per run:" + f.format(avgMatTime) + "\r\n");
		opw.append("Average Matching Time per query:" + f.format(avgMatTime/numQs) + "\r\n");
		opw.append("Average Join Time per run:" + f.format(avgJoinTime) + "\r\n");
		opw.append("Average Join Time per query:" + f.format(avgJoinTime/numQs) + "\r\n");
		opw.append("Average Nodes before per query:" + f.format(totNodesBefore/numQs) + "\r\n");
		opw.append("Average Nodes after per query:" + f.format(totNodesAfter/numQs) + "\r\n");
		opw.append("Average Soln nodes per query:" + f.format(totSolnNodes/numQs) + "\r\n");
		opw.append("Average Size of query answer graph:" + f.format(totSizeOfAG/numQs) + "\r\n");
		opw.append("Average number of solution tuples per query:" + f.format(totSolns/numQs) + "\r\n");
		opw.append("Average Size of solution tuples per query:" + f.format(avgQSize*totSolns/numQs) + "\r\n");
		opw.append("Max Used Memory:" + f.format(Flags.mt.getMaxUsedMem()) + " MB\r\n");

	}
	
	public String toString() {

		StringBuffer sb = new StringBuffer();

		sb.append("Dataset:" + dataFN + "\r\n");
		sb.append("V:" + V + " " + "E:" + E + " " + "lbs:" + numLbs + "\r\n");
		sb.append("Queryset:" + qryFN + "\r\n");
		sb.append("Algorithm:" + algN + "\r\n");
		DecimalFormat f = new DecimalFormat("##.00");
		int numQs = qryEvalStats[0].size();
		double[] evalTimes, matchTimes, enumTimes, joinTimes;
		evalTimes = new double[numQs];
		matchTimes = new double[numQs];
		enumTimes =  new double[numQs];
		joinTimes = new double[numQs];
		
		double totNodesBefore =0.0, totNodesAfter = 0.0, totSolnNodes =0.0, totSizeOfAG = 0.0;
		double totSolns =0.0; 
		for (int i = 0; i < Flags.REPEATS; i++) {
			ArrayList<QueryEvalStat> qryEvalStatList = qryEvalStats[i];
			sb.append("Run " + (i + 1) + ":\r\n");
			for (int q = 0; q < numQs; q++) {
				QueryEvalStat stat = qryEvalStatList.get(q);
				matchTimes[q] += stat.matchTime;
				enumTimes[q]  += stat.enumTime;
				joinTimes[q]  += stat.joinTime;
				evalTimes[q]  += stat.totTime;
				totTimes[i] += evalTimes[q];
				totMatchTimes[i] += matchTimes[q];
				totJoinTimes[i] += joinTimes[q];
				if(i==0){
					totNodesBefore +=stat.totNodesBefore;
					totNodesAfter  +=stat.totNodesAfter; 
					totSolnNodes    +=stat.totSolnNodes;
					totSizeOfAG     +=stat.sizeOfAnsGraph;
					totSolns        +=stat.numSolns;
					
				}
				sb.append("[q" + q + " " + "matchTime:" + f.format(stat.matchTime) + " " + "enumTime:"
						+ f.format(stat.enumTime) + " " + "joinTime:" + f.format(stat.joinTime) + " " + "totTime:"
						+ f.format(stat.totTime) + " " + "numSolns:" + new DecimalFormat(",###").format(stat.numSolns)
						                         + " " + "solnNodes:" + new DecimalFormat(",###").format(stat.totSolnNodes)
						                         + " " + "numNodesBefore:" + new DecimalFormat(",###").format(stat.totNodesBefore)
						                         + " " + "numNodesAfter:" + new DecimalFormat(",###").format(stat.totNodesAfter)
						                         + " " + "answerGraphSize:" + new DecimalFormat(",###").format(stat.sizeOfAnsGraph)
						     					 
						+ "]\r\n");
			}
		}
		
		sb.append("Average running time: \r\n");
		for (int q = 0; q < numQs; q++) {
			
			sb.append("[q" + q + " " + "matchTime:" + f.format(matchTimes[q]/Flags.REPEATS) + " " + "enumTime:"
					+ f.format(enumTimes[q]/Flags.REPEATS) + " " + "joinTime:" + f.format(joinTimes[q]/Flags.REPEATS) + " " + "totTime:"
					+ f.format(evalTimes[q]/Flags.REPEATS)
					+ "]\r\n");	
		}

		sb.append("Data loading Time:" + f.format(loadTime) + "\r\n");
		sb.append("Index building Time:" + f.format(bldTime) + "\r\n");
		double avgExeTime = calAvg(totTimes);
		double avgMatTime = calAvg(totMatchTimes);
		double avgJoinTime = calAvg(totJoinTimes);
		sb.append("Average Eval Time per run:" + f.format(avgExeTime) + "\r\n");
		sb.append("Average Eval Time per query:" + f.format(avgExeTime/numQs) + "\r\n");
		sb.append("Average Matching Time per run:" + f.format(avgMatTime) + "\r\n");
		sb.append("Average Matching Time per query:" + f.format(avgMatTime/numQs) + "\r\n");
		sb.append("Average Join Time per run:" + f.format(avgJoinTime) + "\r\n");
		sb.append("Average Join Time per query:" + f.format(avgJoinTime/numQs) + "\r\n");
		sb.append("Average Nodes before per query:" + f.format(totNodesBefore/numQs) + "\r\n");
		sb.append("Average Nodes after per query:" + f.format(totNodesAfter/numQs) + "\r\n");
		sb.append("Average Soln nodes per query:" + f.format(totSolnNodes/numQs) + "\r\n");
		sb.append("Average Size of query answer graph:" + f.format(totSizeOfAG/numQs) + "\r\n");
		sb.append("Average number of solution tuples per query:" + f.format(totSolns/numQs) + "\r\n");
		sb.append("Average Size of solution tuples per query:" + f.format(avgQSize*totSolns/numQs) + "\r\n");
		sb.append("Max Used Memory:" + f.format(Flags.mt.getMaxUsedMem()) + " MB\r\n");

		return sb.toString();
	}

	
	private double calAvg(double[] statArr) {

		double sum = 0L;
		for (int i = 0; i < Flags.REPEATS; i++) {
			double curr = statArr[i];
			sum += curr;
			
		}

		return sum / Flags.REPEATS;
	}
	public static void main(String[] args) {

	}

}
