package helper;

public class QueryEvalStat {

	public double numSolns = 0.0, totNodes_before = 0.0, totNodes_after = 0.0, totSolnNodes =0.0;
	public double matchTime = 0.0, enumTime =0.0, joinTime = 0.0, totTime = 0.0;
	
	
	public QueryEvalStat(){}
	
	public QueryEvalStat(double mt, double et, double jt, double solns){
		
		matchTime = mt;
		enumTime  = et;
		joinTime  = jt;
		totTime   = mt+et+jt;
		numSolns  = solns;
	}
	
  
	public static void main(String[] args) {
	

	}

}
