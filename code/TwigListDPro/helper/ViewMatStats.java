package helper;

import java.text.DecimalFormat;


public class ViewMatStats {

	public String dataFN, viewFN;
	public long totViews, totEmptyViews;
	public long totMVBytes;
	public long totOccNodes;
	public long maxMVBytes;
	public double totMatTime = 0.0;

	public ViewMatStats(String dataFN, String viewFN) {

		this.dataFN = dataFN;
		this.viewFN = viewFN;
	}

	public ViewMatStats(){}
	
	public void incrEmptyViews() {

		totEmptyViews++;
	}

	public void setTotalViews(long totViews) {

		this.totViews = totViews;
		
	}

	public void setTotalViews(long totViews, long emptyViews) {

		this.totViews = totViews;
		this.totEmptyViews = emptyViews;
	}

		
	public void add(long mvBytes, long occNodes, double mvTime){
		ViewMatStat stat = new ViewMatStat(mvBytes, occNodes, mvTime);
		add(stat);
	}
	
	public void add(ViewMatStat stat){
		
		if (stat.mvBytes > maxMVBytes)
			maxMVBytes = stat.mvBytes;
		totMVBytes += stat.mvBytes;
		totOccNodes += stat.occNodes;
		totMatTime += stat.matTime;
		if(stat.occNodes ==0)
			totEmptyViews++;
	}
	
	public void add(ViewMatStats stats){
		
		if (stats.maxMVBytes > maxMVBytes)
			maxMVBytes = stats.maxMVBytes;
		totMVBytes += stats.totMVBytes;
		totOccNodes += stats.totOccNodes;
		totEmptyViews += stats.totEmptyViews;
		totViews += stats.totViews;
	}
	
	public String toString() {

		StringBuffer sb = new StringBuffer();

		sb.append("Dataset:" + dataFN + "\r\n");
		sb.append("Viewset:" + viewFN + "\r\n");
		sb.append("Total views:" + totViews + "\r\n");
		sb.append("Total empty views:" + totEmptyViews + "\r\n");
		sb.append("Total serialized bytes:" + totMVBytes + " bytes\r\n");
		sb.append("Maximal serialized bytes of view:" + maxMVBytes + " bytes\r\n");
		sb.append("Total nodes in occurrence lists:" + totOccNodes + " nodes\r\n");
		DecimalFormat f = new DecimalFormat("##.00");
		sb.append("Total materialization Time:" + f.format(totMatTime) + " sec\r\n");
		return sb.toString();
	}

	public static void main(String[] args) {

	}

}
