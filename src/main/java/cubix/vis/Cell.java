package cubix.vis;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;

import cubix.data.CEdge;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;


import java.util.ArrayList;
import java.util.Collection;

public class Cell implements Constants{

	protected int renderMode = 0;
	public static boolean SHOW_CUBE_CENTERS = false;

	
	// FACE NAMES
	public static final int FACE_TIME_RIGHT_1 = 0;
	public static final int FACE_TIME_RIGHT_2 = 1;
	public static final int FACE_TIME_LEFT_1 = 2;
	public static final int FACE_TIME_LEFT_2 = 3;
	public static final int FACE_GRAPH_TOP_1 = 4;
	public static final int FACE_GRAPH_TOP_2 = 5;
	public static final int FACE_GRAPH_BACK_1 = 6;
	public static final int FACE_GRAPH_BACK_2 = 7;
	public static final int FACE_GRAPH_FRONT_1 = 8;
	public static final int FACE_GRAPH_FRONT_2 = 9;
	public static final int FACE_GRAPH_BOTTOM_1 = 10;
	public static final int FACE_GRAPH_BOTTOM_2 = 11;


	
	// GEOMETRY
	private float[] relativeGraphSlicePos = new float[]{0f,0f,0f};
	private float[] relativeVNodeSlicePos = new float[]{0f,0f,0f};
	private float[] relativeHNodeSlicePos = new float[]{0f,0f,0f};
	private float[] absolutePos = new float[]{0f,0f,0f};
	private float hWidth;
	private float hDepth;
	private float hHeight;
	protected static Vector3D[] vertices = new Vector3D[8];
	Vector3D[] verticesRelPos = new Vector3D[8];
//	private FloatBuffer vertexBuffer;
//	private IntBuffer vertexOrder;
//	private FloatBuffer normals;
	protected static int[][] faces = new int[12][4];
	// Rotation
//	private Rotation nextRotation = new Rotation(new Vector3D(0,1,0), 0);
//	private Rotation rotationAbsolute = Utils.getNullRotation();
	private static int displayListIndex = -1;
		
	
	// VIS ATTRIBUTES
//	protected float[] originalColor = new float[4];
//	protected float[] currentColor = new float[4];
	
	
	// DATA
	CEdge owner = null; 
	private ArrayList<Integer> hiddenFaces = new ArrayList<Integer>();
	
	
	
	// STATES
	private boolean highlight;
	private TimeSlice graphSlice;
	private HNodeSlice hNodeSlice;
	private VNodeSlice vNodeSlice;
	private float transparency = 1;
	private int renderState = 3;
	private float scale = 1f;

	
	public Cell(float height, float width, float depth)
	{
		
		this.hHeight = height/2f;
		this.hWidth = width/2f;
		this.hDepth = depth/2f;
		
		// CREATE RELATIVE VERTIX POSITIONS
		vertices[0] = new Vector3D(-hWidth, -hHeight, -hDepth);
		vertices[1] = new Vector3D(-hWidth, -hHeight, +hDepth);
		vertices[2] = new Vector3D(-hWidth, +hHeight, -hDepth);
		vertices[3] = new Vector3D(-hWidth, +hHeight, +hDepth);
		vertices[4] = new Vector3D(+hWidth, -hHeight, -hDepth);
		vertices[5] = new Vector3D(+hWidth, -hHeight, +hDepth);
		vertices[6] = new Vector3D(+hWidth, +hHeight, -hDepth);
		vertices[7] = new Vector3D(+hWidth, +hHeight, +hDepth);

		
		// CREATE FACES and define buffer for face vertices
		faces[FACE_TIME_RIGHT_1] = new int[]{4,6,7,5}; // 1
		faces[FACE_GRAPH_BACK_1] = new int[]{0,2,6,4}; // 2
		faces[FACE_TIME_LEFT_1] = new int[]{0,1,3,2};	// 3
		faces[FACE_GRAPH_FRONT_1] = new int[]{1,5,7,3}; // 4
		faces[FACE_GRAPH_TOP_1] = new int[]{2,3,7,6}; // 5
		faces[FACE_GRAPH_BOTTOM_1] = new int[]{0,4,5,1}; // 6

	}
	
	public void _createDisplayList(GL2 gl)
	{
		// CREATE DISPLAYLISTS
		displayListIndex = gl.glGenLists(1); 
		gl.glNewList(displayListIndex, gl.GL_COMPILE);
			createGeometry(gl, new float[]{}, 1f,1f,1f );
		gl.glEndList();
	
	}
	
	
	
	
	public float[] getNormal(Vector3D a, Vector3D b, Vector3D c)
	{
		float[] p = Utils.toFArray(b.subtract(a));
		float[] q = Utils.toFArray(c.subtract(a));
		return Utils.normalize(Utils.cross(p, q));
	}
	
	
	public void display(GL2 gl)
	{
		if(displayListIndex < 0)
			_createDisplayList(gl);
		
		gl.glCallList(displayListIndex);
	}

	public void createGeometry(GL2 gl, float[] offset, float scaleX, float scaleY, float scaleZ)
	{
		drawFace(FACE_GRAPH_FRONT_1, gl, offset, scaleX, scaleY, scaleZ);
		drawFace(FACE_GRAPH_BACK_1, gl, offset,  scaleX, scaleY, scaleZ);
		drawFace(FACE_GRAPH_TOP_1,  gl, offset,  scaleX, scaleY, scaleZ);
		drawFace(FACE_GRAPH_BOTTOM_1, gl, offset,scaleX, scaleY, scaleZ);
		drawFace(FACE_TIME_RIGHT_1, gl, offset,  scaleX, scaleY, scaleZ);
		drawFace(FACE_TIME_LEFT_1, gl, offset,   scaleX, scaleY, scaleZ);
	}
	
	public void drawFace(int face, GL2 gl, float[] offset, float scaleX, float scaleY, float scaleZ)
	{
		float[] pos; 
		
		gl.glNormal3fv(getNormal(	vertices[faces[face][0]], 
									vertices[faces[face][1]], 
									vertices[faces[face][2]]),0);

		gl.glBegin(gl.GL_QUADS);
			for(int i=0 ; i < 4 ; i++){
				pos = Utils.toFArray(vertices[faces[face][i]]);
				pos[X] *= scaleX; 
				pos[Y] *= scaleY; 
				pos[Z] *= scaleZ; 
				pos = Utils.add(offset, pos);
				gl.glVertex3fv(pos, 0);
			}
		gl.glEnd();
	}

	
	public void setGraphPos(float[] newPos)
	{
		relativeGraphSlicePos = newPos.clone();
	}
	
	public void setVNodePos(float[] newPos)
	{
		relativeVNodeSlicePos = newPos.clone();
	} 
	
	public void setHNodePos(float[] newPos)
	{
		relativeHNodeSlicePos = newPos.clone();
	} 

	
	protected void rotateVertices(Rotation rot, boolean inverse)
	{
//		Vector3D vec;
//		float[] initPos = cubletPos.clone();
//		cubletPos = Utils.add(cubletPos, Utils.invert(initPos));
//		if(inverse){
//			for(int i=0 ; i<vertices.length ; i++) vertices[i] = rot.applyInverseTo(vertices[i]);
//		}else{
//			for(int i=0 ; i<vertices.length ; i++) vertices[i] = rot.applyTo(vertices[i]);
//		}
//		cubletPos = Utils.add(cubletPos, initPos);
	}
	
	
	/////////////////
	/// GET & SET ///
	/////////////////
	
//	public void setColor(float[] c) {this.currentColor = c.clone(); this.originalColor = c.clone();}
//	public float[] getColor() { return currentColor;}
//	public void setQueryColor(float[] c) {this.currentColor = c.clone();}
//	public float[] getQueryColor() { return currentColor; }
//	public void restoreColor() {currentColor = originalColor.clone();}

	
	public float[] getAbsolutePos(){return this.absolutePos;}
	public void setAbsolutePos(float[] pos){ this.absolutePos = pos; }
	
	public void setOwner(CEdge owner){ this.owner = owner; }
	public CEdge getData() {return owner; } 
	public boolean hasOwner(){return owner != null ;}
//	public void setGraphColor(float[] color) {this.graphColor = color; this.graphFaceColor = color; }
//	public void setTimeColor(float[] color) {this.timeColor = color; this.timeFaceColor = color;}
	public float[] getRelTimeSlicePos() {return this.relativeGraphSlicePos.clone();}
	public float[] getRelVNodeSlicePos() {return this.relativeVNodeSlicePos.clone();}
	public float[] getRelHNodeSlicePos() {return this.relativeHNodeSlicePos.clone();}
	
	public void setHighlight(boolean highlight) { this.highlight = highlight;}
	public boolean isHighlight(){return this.highlight;}
	
//	@Override public void rotate(Rotation rotation) { 
//		rotateVertices(rotation, false);	
//		rotationAbsolute = rotation.applyTo(rotationAbsolute);
//	}
//	@Override public Rotation getRotation() { return rotationAbsolute;}
//	public void setHiddenFaces(ArrayList<Integer> hiddenFaces){this.hiddenFaces = hiddenFaces;}
	public void clearHiddenFaces(){this.hiddenFaces.clear();}
	public void addHiddenFace(int face) {this.hiddenFaces.add(face);}
	public Collection<Integer> getHiddenFaces() {return hiddenFaces;}
//	@Override public void scaleTo(float value) { scaleFactor = value; }
//	@Override public float getScale(){return this.scaleFactor;}
	public void setRenderMode(int renderMode) {this.renderMode = renderMode; }
	public int getRenderMode() {return renderMode; } 
	
	
	public void setGraphSlice(TimeSlice s){this.graphSlice = s;}
	public void setHNodeSlice(HNodeSlice s){this.hNodeSlice = s;}
	public void setVNodeSlice(VNodeSlice s){this.vNodeSlice = s;}
	
	public TimeSlice getTimeSlice(){return graphSlice;}
	public HNodeSlice getHNodeSlice(){return hNodeSlice;}
	public VNodeSlice getVNodeSlice(){return vNodeSlice;}

	public float getTransparency() { return this.transparency ;}
	public void setTranslucency(float transparency) { this.transparency = transparency;}

	public void setSelectionMode(int renderState) {this.renderState  = renderState;}
	public int getSelectionState() {return this.renderState;}

	public float getScale() {
		return scale;
	}
	public void setScale(float s){this.scale = s;}

	public void resetDisplayLists() {
		displayListIndex = -1;
	}

	
}
