package global;

import helper.MemoryTracker;

public class Flags {
	public static final boolean DEBUG = false;
	public static int vis_cur = 0;
	public static int REPEATS = 1; // for running the experimental evaluation.
	public static MemoryTracker mt = new MemoryTracker();
}
