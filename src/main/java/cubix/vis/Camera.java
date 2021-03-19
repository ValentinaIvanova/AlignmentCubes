package cubix.vis;

import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.common.nio.Buffers;

import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;

public class Camera implements Constants {
	
//	public static final int PERSP_ORTHO = 0;
//	public static final int PERSP_TRIPOINT = 1;
	
	// Camera position and view
	private float[] pos = new float[]{-30,30,30}; 
	private float[] lookAt = new float[]{0,0,0}; 
	private float viewAngle = 45f; // degree
	private float[] upDir = new float[]{0,1,0}; 
	// Camera Rotation
	private Rotation rotation = new Rotation(new Vector3D(0,1,0), 0);
	// Clipping 
//	private float clippingHeight;
	private float clipClose = 1;
	private float clipDist = 1000000;
	private float ratio;
	private float[] savedPosition;
	private float[] savedLookAt;
	private float zoomFactor = 1;
	
	public Camera(GL2 gl, GLU glu, float width, float height, int perspectiveMode, float viewAngle)
	{
		this.ratio = width/height;	
		this.viewAngle = viewAngle;

		set(gl, glu);
	}
	
	public void set(GL2 gl, GLU glu)
	{
		
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		
		// Rotate if necessary
		if(rotation.getAngle() != 0){
			Vector3D newPos = rotation.applyTo(new Vector3D(pos[X], pos[Y], pos[Z]));
			pos[X] = (float) newPos.getX();
			pos[Y] = (float) newPos.getY();
			pos[Z] = (float) newPos.getZ();
			rotation = new Rotation(new Vector3D(0,1,0), 0); // apply rotaion only once.
		}
		glu.gluPerspective(viewAngle, ratio, clipClose, clipDist);
		glu.gluLookAt(	pos[X], pos[Y], pos[Z],  
					lookAt[X], lookAt[Y], lookAt[Z],
					upDir[X], upDir[Y], upDir[Z]);		
		
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}

	
	//////////////
	/// MODIFY ///
	//////////////
	
	public void changePerspective(float value)
	{
	    float alpha_new = viewAngle + value;
	    alpha_new = Math.max(1, Math.min(130, alpha_new));
	    float d_new = (float) (Utils.length(pos) * Math.tan(viewAngle * Math.PI / 360) / Math.tan(alpha_new * Math.PI / 360));
	    pos = Utils.mult(Utils.normalize(pos), d_new);
	    viewAngle = alpha_new;
	}
	

	
	
	public void zoom(float value)
	{
	    float fac = 1 + value/10;		
	    pos = Utils.vectorAdd(lookAt, Utils.mult(Utils.dir(lookAt, pos), fac));
	}
	
	
	public void lookAt(float[] pos){
		lookAt(pos[0], pos[1], pos[2]);
	}
	public void lookAt(float x, float y, float z)
	{
		this.lookAt[0] = x;
		this.lookAt[1] = y;
		this.lookAt[2] = z;
	}
	public void shift(float[] offset)
	{
		lookAt = Utils.vectorAdd(lookAt, offset);
		pos = Utils.vectorAdd(pos, offset);
	}
	
	
	///////////////
	/// MAPPING ///
	///////////////
	

	public float[] modelToScreen(GL2 gl, GLU glu, float[] point)
	{
		int    viewport[]  = getViewPortAsInt(gl);
	    float screencoord[] = new float[3];
	    
	    float[] projection = new float[3];
	    if( ! glu.gluProject(point[0], point[1], point[2], getModelViewAsFloat(gl), 0, getProjectionAsFloat(gl), 0, viewport, 0, screencoord, 0 ) )
				//throw new RuntimeException("Could not retrieve model coordinates in screen for " + point);
	    	System.out.println("EXception");
		projection = new float[]{screencoord[0], screencoord[1], screencoord[2]};
		return projection;
	}
	
	public int[] getViewPortAsInt(GL2 gl){
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		return viewport;
	}
	public float[] getModelViewAsFloat(GL2 gl){
		float modelview[]  = new float[16];
	    gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
	    return modelview;
	}
	public float[] getProjectionAsFloat(GL2 gl){
		float projection[] = new float[16];
	    gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
	    return projection;
	}
	
	
	/**
	 * Calulates the ray direction into the world starting at the x,y screen coordinates.
	 * 
	 * @param x - x on screen.
	 * @param y - y on screen.
	 * @param gl - gl context
	 * @param glu - glu context
	 * @param rayStart - stores the beginning of the ray.
	 * @return
	 */
	public float[] getPickRay(float x, float y, GL2 gl, GLU glu, float[] rayStart)
	{
		// Get ray
		int[] viewport = getViewPortAsInt(gl);
		glu.gluUnProject(x, viewport[3] -y-1, clipClose, getModelViewAsFloat(gl), 0, getProjectionAsFloat(gl), 0, viewport, 0, rayStart, 0);

		float[] rayEnd = new float[3];
		glu.gluUnProject(x, viewport[3] -y-1, clipDist, getModelViewAsFloat(gl), 0, getProjectionAsFloat(gl), 0, viewport, 0, rayEnd, 0);
		
		float[] ray = Utils.normalize(Utils.dir(rayStart, rayEnd));
		return ray;
	}
	
	
	
	
	
	/////////////////
	/// GET & SET /// 
	/////////////////

	public float[] getPos() { return pos.clone(); }
	public float[] getLookAt() { return lookAt.clone(); }
	public void setLookAt(float[] lookAt) { this.lookAt = lookAt.clone(); }
	public float[] getUp() { return upDir.clone(); }
	public void setUp(float[] up) { this.upDir = up.clone(); }
	public float getClipClose() { return clipClose;}
	public void setClipClose(float clipClose) { this.clipClose = clipClose; }
	public float getClipDist() { return clipDist; }
	public void setClipDist(float clipDist) { this.clipDist = clipDist; }
	public double getScreenRatio() { return this.ratio; }

	public void setPos(float[] pos) {
		this.pos = pos.clone();
	}
	public void rotate(Rotation rotation) { this.rotation = rotation;}
	public Rotation getRotation() { return rotation;}

	public void scaleTo(float factor) {}
	public float getScale() {return 1;}

	public void savePosition() {
		savedPosition = pos.clone();
		savedLookAt = lookAt.clone();
	}
	
	public float[] getSavedPosition() { return this.savedPosition.clone();}
	public float[] getSavedLookAt() { return this.savedLookAt.clone();}

	public float getViewAngle() { return this.viewAngle; }
	public void setViewAngle(float viewAngle) { 
		this.viewAngle = viewAngle; }

	
}
