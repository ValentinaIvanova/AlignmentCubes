package cubix.vis;

import java.util.ArrayList;
import java.util.HashSet;


public class Lasso {
	
	public static int STATE_NONE = 0;
	public static int STATE_ADD_POINT = 1;
	public static int STATE_CALCULATE = 2;
	public static int state = STATE_NONE;

	public static int pointCount = 0;
	public static ArrayList<float[]> points = new ArrayList<float[]>();
	public static ArrayList<float[]> rays = new ArrayList<float[]>();
	public static HashSet<Cell> cells = new HashSet<Cell>();


}
