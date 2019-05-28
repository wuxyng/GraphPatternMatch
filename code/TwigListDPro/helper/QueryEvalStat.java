package helper;

public class QueryEvalStat {

	public double sizeOfAnsGraph = 0.0, numSolns = 0.0, totNodesBefore = 0.0, totNodesAfter = 0.0, totSolnNodes = 0.0;
	public double matchTime = 0.0, enumTime = 0.0, joinTime = 0.0, totTime = 0.0;

	public QueryEvalStat() {
	}

	public QueryEvalStat(double mt, double et, double jt, double solns) {

		matchTime = mt;
		enumTime = et;
		joinTime = jt;
		totTime = mt + et + jt;
		numSolns = solns;
	}

	public QueryEvalStat(double mt, double et, double jt, double solns, double sz) {

		matchTime = mt;
		enumTime = et;
		joinTime = jt;
		totTime = mt + et + jt;
		numSolns = solns;
		sizeOfAnsGraph = sz;
	}

	public static void main(String[] args) {

	}

}
