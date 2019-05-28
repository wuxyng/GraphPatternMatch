
package global;

import helper.MemoryTracker;

public class Flags {
	public static boolean DEBUG = false;
	public static boolean sortByCard = true;
	public static int vis_cur = 0;
	public static int REPEATS = 1; // for running the experimental evaluation.
	public static MemoryTracker mt = new MemoryTracker();
}
