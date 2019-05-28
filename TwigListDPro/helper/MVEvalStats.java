package helper;

import java.text.DecimalFormat;
import java.util.ArrayList;

import global.Flags;

public class MVEvalStats {

	public String dataFN, qryFN, algN;
	public double evalTime;
	// public double totmem = 0.0;
	ArrayList<MVEvalStat>[] qryEvalStats;
	public double[] totTimes, totOverheads;
	public long[]  totMVBytes;
	public int[]   totViews;

    public MVEvalStats(String dataFN, String qryFN, String algN) {

		qryEvalStats = (ArrayList<MVEvalStat>[]) new ArrayList[Flags.REPEATS];
		totTimes = new double[Flags.REPEATS];
		totMVBytes = new long[Flags.REPEATS];
		totViews  = new int[Flags.REPEATS];
		totOverheads = new double[Flags.REPEATS];
		this.dataFN = dataFN;
		if(qryFN == null)
			qryFN = "";
		this.qryFN = qryFN;
		this.algN = algN;
	}


    //(double cvt, double ldt, double adt, double evt,  long mvb, int numv)
	public void add(int iter, int qid, double ovt,  double evt,  long mvb, int numv) {

		MVEvalStat qst = new MVEvalStat(ovt, evt, mvb, numv);
		// add(qst);
		add(iter, qid, qst);
	}
	
	public void add(int iter, int qid, MVEvalStat qst) {
		if (qryEvalStats[iter] == null)
			qryEvalStats[iter] = new ArrayList<MVEvalStat>();

		qryEvalStats[iter].add(qid, qst);
		
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();

		sb.append("Dataset:" + dataFN + "\r\n");
		sb.append("Queryset:" + qryFN + "\r\n");
		sb.append("Algorithm:" + algN + "\r\n");
		DecimalFormat f = new DecimalFormat("##.00");
		int numQs = qryEvalStats[0].size();
		double[] evalTimes, totTime, overHeads;
		long[] mvBytes = new long[numQs];
		int[] views = new int[numQs];
		evalTimes = new double[numQs];
		totTime  = new double[numQs];
		overHeads = new double[numQs];
		//double cvt, double ldt, double adt, double evt,  int numv
		double totNodes_before =0.0, totNodes_after = 0.0, totSolnNodes =0.0, totMVBytes = 0.0, totViews = 0.0;

		
		for (int i = 0; i < Flags.REPEATS; i++) {
			ArrayList<MVEvalStat> qryEvalStatList = qryEvalStats[i];
			sb.append("Run " + (i + 1) + ":\r\n");
			for (int q = 0; q < numQs; q++) {
				MVEvalStat stat = qryEvalStatList.get(q);
			
				evalTimes[q]  += stat.evalTime;
				overHeads[q]  += stat.overhead;
				totTime[q]  += stat.evalTime + stat.overhead;
				mvBytes[q]  += stat.mvBytes;
				views[q]    += stat.numRelViews;
				totTimes[i] += totTime[q];
				totOverheads[i] += overHeads[q];
				if(i==0){
					
					totNodes_before +=stat.totNodes_before;
					totNodes_after  +=stat.totNodes_after; 
					totSolnNodes    +=stat.totSolnNodes;
					totMVBytes += stat.mvBytes;
					totViews   += stat.numRelViews;
				}
				sb.append("[q" + q + " " + "overheads:" + f.format(stat.overhead)   +
						             " " + "evalTime:" + f.format(stat.evalTime)   +
						             " " + "totTime:"  + f.format(stat.overhead + stat.evalTime)    +
						             " " + "solnNodes:" + new DecimalFormat(",###").format(stat.totSolnNodes) +
			                         " " + "numNodes before:" + new DecimalFormat(",###").format(stat.totNodes_before) +
			                         " " + "numNodes after:" + new DecimalFormat(",###").format(stat.totNodes_after)   +
						             " " + "numViews:" + new DecimalFormat(",###").format(stat.numRelViews) +
						             " " + "mvBytes:"  + stat.mvBytes + "]\r\n");
			}
		}
		
		sb.append("Average running stats: \r\n");
		for (int q = 0; q < numQs; q++) {
			
			sb.append("[q" + q + " " + "overheads:"  + f.format(overHeads[q]/Flags.REPEATS)   + 
					             " " + "evalTime:"  + f.format(evalTimes[q]/Flags.REPEATS)   + 
					             " " + "totTime:"   + f.format(totTime[q]/Flags.REPEATS)    +
					             " " + "numViews:"   + f.format(views[q]/Flags.REPEATS)    +
					             " " + "totBytes:"   + f.format(mvBytes[q]/Flags.REPEATS)  
					+ "]\r\n");	
		}

		
		double avgExeTime = calAvg(totTimes);
		double avgOverheads = calAvg(totOverheads);
		sb.append("Average Eval Time per run:" + f.format(avgExeTime) + "\r\n");
		sb.append("Average Eval Time per query:" + f.format(avgExeTime/numQs) + "\r\n");
		sb.append("Average Overheads per run:" + f.format(avgOverheads) + "\r\n");
		sb.append("Average Overheads per query:" + f.format(avgOverheads/numQs) + "\r\n");
		sb.append("Average Nodes before per query:" + f.format(totNodes_before/numQs) + "\r\n");
		sb.append("Average Nodes after per query:" + f.format(totNodes_after/numQs) + "\r\n");
		sb.append("Average Soln nodes per query:" + f.format(totSolnNodes/numQs) + "\r\n");
		sb.append("Average Mat bytes per query:" + f.format(totMVBytes/numQs) + "\r\n");
		sb.append("Average relevant views per query:" + f.format(totViews/numQs) + "\r\n");
		
		
		return sb.toString();
	}

	private double calAvg(int[] statArr) {

		int sum = 0;
		for (int i = 0; i < Flags.REPEATS; i++) {
			int curr = statArr[i];
			sum += curr;
			
		}

		return sum / Flags.REPEATS;
	}
	
	
	private double calAvg(double[] statArr) {

		double sum = 0L;
		for (int i = 0; i < Flags.REPEATS; i++) {
			double curr = statArr[i];
			sum += curr;
			
		}

		return sum / Flags.REPEATS;
	}
	
	private double calAvg(long[] statArr) {

		long sum = 0L;
		for (int i = 0; i < Flags.REPEATS; i++) {
			long curr = statArr[i];
			sum += curr;
			
		}

		return sum / Flags.REPEATS;
	}
	
	public static void main(String[] args) {

	}

}
