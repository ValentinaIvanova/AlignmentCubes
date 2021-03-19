package cubix.helper;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.opengl.math.VectorUtil;
//import com.jogamp.graph.math.VectorUtil;

public class Utils extends VectorUtil{

	
	/////////////
	/// COLOR ///
	/////////////
	
	/**
	 * Returns the rotation that each object has
	 * when not rotated. This rotation can be used
	 * to revert an object in its initial state. 
	 * @return
	 */
	public static Rotation getNullRotation(){
		Vector3D v = new Vector3D(0,1,0);
		Rotation r = new Rotation(v, 0); 
		return r;
	} 
	
	
	/////////////////
	/// ROTATIONS /// 
	/////////////////
	
	
	public static Rotation getYDiffRotation(Rotation r, Rotation s, boolean cw){
		float angle = (float) (s.getAngle() - r.getAngle());
		if(cw){
			if(angle < 0)
				angle = (float) (Math.PI*2 - angle);
			return new Rotation(new Vector3D(0,-1,0), angle);
		}
		if(angle < 0)
			angle = (float) (Math.PI*2 - angle);
		return new Rotation(new Vector3D(0,1,0), angle);
		
	}
	
	


	public static Rotation getDefaultRotationGraphMatrix() {
		return new Rotation(new Vector3D(0,1,0),0);
	}
	public static Rotation getDefaultRotationTimeColMatrix() {
		return new Rotation(new Vector3D(0,1,0),Math.PI/2);
	}




	/////////////////////////
	/// VECTOR OPERATIONS ///
	/////////////////////////
	
	public static void print(String name, float[] vec){
		System.out.print(name + ": (");
		for(int i=0 ; i < vec.length; i++)
			System.out.print(vec[i] + ", ");
		System.out.println(")");
	}
	
	
	public static void print(String name, int[] vec){
		System.out.print("[VectorUtils] " + name + ": (");
		for(int i=0 ; i < vec.length; i++)
			System.out.print(vec[i] + ", ");
		System.out.println(")");
	}
	public static float[] invert(float[] u) {
		float[] r = new float[u.length];
		for(int i=0 ; i<u.length ; i++){
			r[i] = u[i] * -1;
		}
		return r;
	}
	public static float[] sub(float[] u, float[] v) {
		float[] w;
		int l;
		if(u.length < v.length){
			w = v;
			l = v.length;
		}else{
			w = u;
			l = u.length;
		}
		float[] r = new float[l];
		for(int i=0 ; i<l ; i++){
			try{
				r[i] = u[i] - v[i];
			}catch(ArrayIndexOutOfBoundsException ex){
//				Log.err("sub: ArrayIndexOutOfBoundsException");
				r[i] = w[i];
			}
		}
		return r;
	}
	public static float[] add(float[] u, float[] v) 
	{
		float[] w;
		int l;
		if(u.length < v.length){
			w = v;
			l = v.length;
		}else{
			w = u;
			l = u.length;
		}
		float[] r = new float[l];
		for(int i=0 ; i<l ; i++){
			try{
				r[i] = u[i] + v[i];
			}catch(ArrayIndexOutOfBoundsException ex){
//				Log.err("add: ArrayIndexOutOfBoundsException");
				r[i] = w[i];
			}
		}
		return r;
	}
	public static float[] dir(float[] u, float[] v) 
	{	
		int l = Math.min(u.length, v.length);
		float[] r = new float[l];
		for(int i=0 ; i<l ; i++){
			try{
				r[i] = v[i] - u[i];
			}
			catch(ArrayIndexOutOfBoundsException ex){
//				Log.err("dir: ArrayIndexOutOfBoundsException");
			}
		}
		return r;
	}
	public static float[] mult(float[] u, float s) {
		float[] r = new float[u.length];
		for(int i=0 ; i<u.length ; i++){
			r[i] = u[i] * s;
		}
		return r;
	}
	
	public static double[] crossD(float[] u, float[] v) {
		double[] c = new double[u.length];
		c[0] = u[1] * v[2] - u[2] * v[1];
		c[1] = u[2] * v[0] - u[0] * v[2];
		c[2] = u[0] * v[1] - u[1] * v[0];
		return c;
	}
	public static float length(float[] v) {
		float sum = 0;
		float a = 0;
		for(int i=0 ; i<v.length ; i++){
			sum += v[i]*v[i];
		}
		a = (float) Math.sqrt(sum);
		return a;
	}


	public static float[] slice(float[] v, int i, int j) {
		float[] w = new float[j-i+1];
		for(int k=0 ; k < w.length ; k++)
			w[k] = v[i+k];
		return w;
	}


	public static float[] toFArray(Vector3D v){
		float[] arr = new float[3];
		arr[0] = (float) v.getX();
		arr[1] = (float) v.getY();
		arr[2] = (float) v.getZ();
		return arr;
	}

	public static float[] normalize(float[] v){
		return mult(v, 1f / length(v));
	}
	
	public static Vector3D toVector3D(float[] v){
		return new Vector3D(v[0], v[1], v[2]);
	}


	public static Rotation getIverse(Rotation rotation) {
		return new Rotation(rotation.getAxis(), -rotation.getAngle());
	}

	
	public static org.apache.commons.math3.geometry.euclidean.threed.Vector3D getVector3D(float[] f){
		return new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(f[0], f[1], f[2]);
	}
	public static float[] getFloat(org.apache.commons.math3.geometry.euclidean.threed.Vector3D v){
		return new float[]{(float) v.getX(), (float) v.getY(), (float) v.getZ()};
	}


	public static float[] trim(float[] pos, float d) {
		return mult(normalize(pos), d);
	}


	public static float getDeg(float alpha_rad) { return (float) (alpha_rad * 180 / Math.PI);}
	public static float getRad(float alpha_deg) { return (float) (alpha_deg *  Math.PI / 180);}


	/** Returns the angle between two vectors; */
	public static double getAngleRad(float[] d_0, float[] d_1) 
	{
		d_0 = Utils.normalize(d_0);
		d_1 = Utils.normalize(d_1);
//		float[] cross = cross(d_0, d_1);		
//		float aRad = Utils.length(cross) / (Utils.length(d_0) * Utils.length(d_1));
//		return (float) Math.asin(Math.min(1, Math.max(-1, aRad)));
		double dot = dot(d_0, d_1);		
		return Math.acos(dot);
	}


	public static float[] rotate2D(float[] v, double alphaRad) {
		float[] w = new float[2];
		float sin = (float) Math.sin(alphaRad);
		float cos = (float) Math.cos(alphaRad);
		w[0] = v[0] * cos - v[1] * sin; 
		w[1] = v[0] * sin + v[1] * cos;
		return w;
	}


	public static float dist(float[] u, float[] v) {
		return Utils.length(Utils.dir(u, v));
	}
	
	

	
	
}
