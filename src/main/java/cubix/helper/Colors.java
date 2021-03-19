package cubix.helper;


public class Colors {
	
	public final static float[] BLUE = new float[]{13/255f, 37/255f, 115/255f};
	public final static float[] TURQ = new float[]{23/255f, 140/255f, 129/255f};
	public final static float[] GREEN = new float[]{54/255f, 191/255f, 70/255f};
	public final static float[] ORANGE = new float[]{242/255f, 111/255f, 20/255f};
	public final static float[] RED = new float[]{191/255f, 24/255f, 33/255f};
	
	public final static float[] DIFF_FIRST = BLUE;
	public final static float[] DIFF_SECOND = RED;
	
	public static float[][] balancedColors = new float[][]
    {
		new float[]{1f, 1f, 0f}, 	// yellow
		new float[]{.8f, .5f, 0f},		// orange/brown
		new float[]{120/255f, 120/255f, 120/255f},	// gray
		new float[]{74/255f, 166/255f, 159/255f},	// cyan
		new float[]{13/255f, 155/255f, 65/255f},		// green
		new float[]{50/255f, 81/255f, 159/255f},		// marine blue
		new float[]{119/255f, 50/255f, 131/255f},	// lila
		new float[]{234/255f, 132/255f, 180/255f},	// pink
		new float[]{124/255f, 162/255f, 224/255f}	// light blue
	};

	protected static int colorIndex = 0;
	
	public static float[] getNextColor() {
		if(balancedColors.length == colorIndex)
			colorIndex = 0;
		return balancedColors[colorIndex++];
	}

	
}
