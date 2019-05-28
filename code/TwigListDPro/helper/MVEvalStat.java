package helper;

public class MVEvalStat {

	public int numRelViews = 0; 
	//overhead = coverTime + loadTime + andTime
	public double  evalTime = 0.0, overhead = 0.0;
	public long mvBytes;
	public double totNodes_before = 0.0, totNodes_after = 0.0, totSolnNodes =0.0;
	public MVEvalStat(){}
	
	public MVEvalStat(double ovt, double evt,  long mvb, int numv){
		
		numRelViews = numv;
		evalTime = evt;
		mvBytes = mvb;
		overhead = ovt;
		
	}
	
	
	public static void main(String[] args) {
	

	}

}
