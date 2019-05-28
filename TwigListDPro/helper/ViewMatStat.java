package helper;

public class ViewMatStat {

	public long mvBytes;
	public long occNodes;
	public double matTime = 0.0;
	
	
	public ViewMatStat(){}
	
	
	public ViewMatStat(long bytes, long nodes, double time){
		
		mvBytes = bytes;
		occNodes = nodes;
		matTime = time;
	}
	
	public static void main(String[] args) {
		
	}

}
