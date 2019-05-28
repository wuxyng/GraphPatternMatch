package helper;

import java.text.DecimalFormat;
import java.util.ArrayList;

import global.Flags;

public class EvalStats {

	public String dataFN, qryFN, algN;
	public int V, E, numLbs;
	public double loadTime = 0.0, bldTime = 0.0;
	public double evalTime;
	// public double totmem = 0.0;
	ArrayList<QueryEvalStat>[] qryEvalStats;
	public double[] totTimes;

	public EvalStats() {

		qryEvalStats = (ArrayList<QueryEvalStat>[]) new ArrayList[Flags.REPEATS];
		totTimes = new double[Flags.REPEATS];
	}

	public EvalStats(String dataFN, String qryFN, String algN) {

		qryEvalStats = (ArrayList<QueryEvalStat>[]) new ArrayList[Flags.REPEATS];
		totTimes = new double[Flags.REPEATS];
		this.dataFN = dataFN;
		this.qryFN = qryFN;
		this.algN = algN;
	}

	public void setGraphStat(int V, int E, int lbs) {

		this.V = V;
		this.E = E;
		numLbs = lbs;

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
				sb.append("[q" + q + " " + "matchTime:" + f.format(stat.matchTime) + " " + "enumTime:"
						+ f.format(stat.enumTime) + " " + "joinTime:" + f.format(stat.joinTime) + " " + "totTime:"
						+ f.format(stat.totTime) + " " + "numSolns:" + new DecimalFormat(",###").format(stat.numSolns)
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
		sb.append("Total Eval Time:" + f.format(calAvg(totTimes)) + "\r\n");
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
