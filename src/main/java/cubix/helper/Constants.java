package cubix.helper;

public interface Constants {

	
	public static enum Align {LEFT,RIGHT, TOP, BOTTOM, CENTER};
	
	// general positions
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int Q = 3;

	public static final float[] X_AXIS = new float[]{1,0,0};
	public static final float[] Y_AXIS = new float[]{0,1,0};
	public static final float[] Z_AXIS = new float[]{0,0,1};

	
	public static final int R = 0;
	public static final int G = 1;
	public static final int B = 2;
	public static final int A = 3;
	
	
	public static final int N = 0;
	public static final int E = 1;
	public static final int S = 2;
	public static final int W = 3;
	
	public static final float[] FLOAT4_0 = new float[]{0f, 0f, 0f, 0f};
	
	public static final int FADE_OUT = 0;
	public static final int FADE_IN = 1;

}
