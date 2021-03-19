package cubix.vis;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

import cubix.CubixVis;
import cubix.data.MatrixCube;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;

public class Cubelet2D implements Constants{

	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	public static final int ALIGN_JUSTIFIED = 2;

	public static final int STATE_CUBE = 0;
	public static final int STATE_TIME_SM = 1;
	public static final int STATE_VERTEX_SM = 2;

	
	
	private int dl_vc = -1;
	private float screenWidth;
	private float screenHeight;
	int width_vc = 45;
	int height_vc = 50;
	
	private float[] pos = new float[2];
	private int dl_graphFace;
	private int dl_timeFace;
	private int dl_cubeFace;
	private int dl_camera;

	private boolean highlightGraphFace = false; 
	private boolean highlightTimeFace = false;
	private boolean highlightCubeFace = false; 
	private CubixVis vis;
	private Camera cam;
	private float[][] corner; 
	
	protected float[] COLOR_SET = new float[]{.9f,.9f,.9f};
	protected float[] COLOR_HIGHLIGHT = new float[]{.5f,.5f,.5f};
	protected float[] COLOR_WHITE = new float[]{1f,1f,1f};
	protected float[] COLOR_SLICE_SELECTED = new float[]{1f,.7f,.5f, .5f};
	private MatrixCube mc;
	private int LIST_CUBE;
	private int dl_timeslice;
	private int dl_vertexslice;
	private float[] dir_t;
	private float[] dir_v;

	public Cubelet2D(float screenWidth, float screenHeight, CubixVis vis)
	{
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.vis = vis;
		cam = vis.getCamera();
		mc = vis.getMatrixCube();
	}
	
	
	public void draw(GL2 gl, GLU glu, float x_vc, float y_vc)
	{
		gl.glDisable(gl.GL_DEPTH_TEST);
		gl.glDisable(gl.GL_CULL_FACE);
		
		x_vc += 50;
		y_vc -= 30;
		this.pos[X] = x_vc;
		this.pos[Y] = y_vc;
		
		if(dl_vc < 0){
			createDisplayLists(gl);
		}

		gl.glPushMatrix();
//		
		 gl.glMatrixMode(gl.GL_MODELVIEW);
	     gl.glLoadIdentity();
	     gl.glMatrixMode(gl.GL_PROJECTION);
	     gl.glLoadIdentity();
	     glu.gluOrtho2D(0, screenWidth, screenHeight, 0 );
		 
	     gl.glDisable(GL2.GL_LIGHTING);
	     
	     gl.glLineWidth(1);
	     gl.glTranslated(x_vc, y_vc, 0);

		
	     	
	     	// DRAW FACES
	     	if(!vis.isVNodeSliceSMView() && !vis.isTimeSMView())
	     	{
	     		float[] c;
	     		if(highlightGraphFace)  c = COLOR_HIGHLIGHT.clone();			
	     		else if(vis.isFrontView() || vis.isCubeView()) c = COLOR_SET.clone();
	     		else c = COLOR_WHITE.clone();
	     		gl.glColor3f(c[R], c[G], c[B]);
	     		gl.glCallList(dl_graphFace);
	     		
	     		if(highlightTimeFace)  c = COLOR_HIGHLIGHT.clone();			
	     		else if(vis.isSourceSideView() || vis.isTargetSideView() || vis.isCubeView()) c = COLOR_SET.clone();
	     		else c = COLOR_WHITE.clone();
	     		gl.glColor3f(c[R], c[G], c[B]);
	     		gl.glCallList(dl_timeFace);
	     		
	     		if(highlightCubeFace)  c = COLOR_HIGHLIGHT.clone();			
	     		else if(vis.isCubeView()) c = COLOR_SET.clone();
	     		else c = COLOR_WHITE.clone();
	     		gl.glColor3f(c[R], c[G], c[B]);
	     		gl.glCallList(dl_cubeFace);
	     		
	     		// DRAW SELECTED SLICES
				ArrayList<Slice> slices = new ArrayList<Slice>();
				
				int mode = 0;
//				if(vis.getSelectedGraphSlices().size() != mc.getTimeSlices().size()){
//					slices.addAll(vis.getSelectedGraphSlices());
//					mode = 0;
//				}
//				if(vis.getSelectedHNodeSlices().size() != mc.getHNodeSlices().size()){
//					slices.addAll(vis.getSelectedHNodeSlices());
//					mode = 1;					
//				}
//				if(vis.getSelectedVNodeSlices().size() != mc.getVNodeSlices().size()){
//					slices.addAll(vis.getSelectedVNodeSlices());
//					mode = 2;					
//				}
				slices.addAll(vis.getRotatedSlices());
				
				
				Slice s;
				for(int i=0 ; i<slices.size() ; i++)
				{
					if(mode == 0)
						s = mc.getTimeSlice(i);
					else if(mode == 1)
						s = mc.getVisibleHNodeSlice(i);
					else 
						s = mc.getVisibleVNodeSlice(i);
						
					float r1, r2;
					float[] v1, v2, v3, v4, v5, v6;
					if(s instanceof TimeSlice){
						r1 = i / (vis.getMatrixCube().getTimeCount());
						r2 = (i+1) / (vis.getMatrixCube().getTimeCount());
						v1 = corner[3];
						v2 = corner[2];
						v3 = corner[6];
						v4 = corner[5];
						v5 = corner[0];
						v6 = corner[1];
					}else{
						r1 = i / (vis.getMatrixCube().getVisibleVNodeSlices().size());
						r2 = (i+1) / (vis.getMatrixCube().getVisibleVNodeSlices().size());
						v1 = corner[2];
						v2 = corner[1];
						v3 = corner[5];
						v4 = corner[4];
						v5 = corner[3];
						v6 = corner[0];
					}
					float[] p1 = Utils.add(v1, Utils.mult(Utils.dir(v1, v2), r1));
					float[] p2 = Utils.add(v1, Utils.mult(Utils.dir(v1, v2), r2));
					float[] p3 = Utils.add(v3, Utils.mult(Utils.dir(v3, v4), r1));
					float[] p4 = Utils.add(v3, Utils.mult(Utils.dir(v3, v4), r2));
					float[] p5 = Utils.add(v5, Utils.mult(Utils.dir(v5, v6), r1));
					float[] p6 = Utils.add(v5, Utils.mult(Utils.dir(v5, v6), r2));
					
				 	gl.glLineWidth(3);
				 	gl.glColor4fv(COLOR_SLICE_SELECTED,0);
				 	gl.glBegin(GL2.GL_QUADS);
				 		gl.glVertex2fv(p1, 0);
				 		gl.glVertex2fv(p2, 0);
				 		gl.glVertex2fv(p4, 0);
				 		gl.glVertex2fv(p3, 0);

				 		gl.glVertex2fv(p1, 0);
				 		gl.glVertex2fv(p2, 0);
				 		gl.glVertex2fv(p6, 0);
				 		gl.glVertex2fv(p5, 0);
					gl.glEnd();
				}
				
				// DRAW WIRE
			 	gl.glLineWidth(3);
			 	gl.glColor3f(0.3f,0.3f,0.3f);
		     	gl.glCallList(dl_vc);
		     	
			    
		     	// DRAW VIEW LABELS
		     	displayString(gl, Utils.add(pos, new float[]{0, -height_vc * 1.3f}), "3D (1)", CubixVis.FONT_12, ALIGN_JUSTIFIED, FLOAT4_0.clone());
		     	displayString(gl, Utils.add(pos, new float[]{width_vc/2, height_vc*1.3f}), "Alignment Slices", CubixVis.FONT_12, ALIGN_LEFT, FLOAT4_0.clone());
		     	displayString(gl, Utils.add(pos, new float[]{width_vc/2, height_vc*1.6f}), "(2,4)", CubixVis.FONT_12, ALIGN_LEFT, FLOAT4_0.clone());
		     	displayString(gl, Utils.add(pos, new float[]{-width_vc/2, height_vc*1.3f}), "Vertex Slices", CubixVis.FONT_12, ALIGN_RIGHT, FLOAT4_0.clone());
		     	displayString(gl, Utils.add(pos, new float[]{-width_vc/2, height_vc*1.6f}), "(3,5)", CubixVis.FONT_12, ALIGN_RIGHT, FLOAT4_0.clone());

//		     	if(cam != null){
//		     		float x_cam = (float) (size_vc/2 + Math.atan((Math.min(0,cam.getPos()[X]) / Math.max(0,cam.getPos()[Z]))) * size_vc) ;
//		     		gl.glTranslated(x_cam, -50, 0);
//		     		gl.glCallList(dl_camera);
//		     	}	     	
				
	     	}
	     	else
	     	{	
	     		gl.glLineWidth(3);

	     		int num;
	     		float d_x, d_y;
	     		Slice s;
	     		MatrixCube cube = vis.getMatrixCube();
	     		if(vis.isTimeSMView())
	     		{
	     			num = Math.min(8,vis.getMatrixCube().getTimeSlices().size());
	     			d_x = -width_vc * .1f;
	     			d_y = height_vc/2 * .1f;
     				gl.glTranslatef(num * d_x, -num * d_y, 0f);
     				for(int t=0 ; t < num ; t++)
	     			{
     					s = cube.getTimeSlice(num -t -1);
     					if(vis.isSelectedSlice(s))
     						gl.glColor3fv(COLOR_SLICE_SELECTED, 0);
     					else
     						gl.glColor4f(1f,1f,1f, 1);
     					gl.glCallList(dl_timeslice);
	     				gl.glTranslatef(-d_x ,d_y, 0f);
	     			}
	    	     	displayString(gl, Utils.add(pos, new float[]{0, height_vc*2.6f}), "Time Slices", CubixVis.FONT_12, ALIGN_JUSTIFIED, FLOAT4_0.clone());
    		   }else 
	     		{
    			   num = Math.min(8,vis.getMatrixCube().getVisibleVNodeSlices().size());
	     			d_x = width_vc * .1f;
	     			d_y = height_vc/2 * .1f;
	         		gl.glTranslatef(num * d_x, -num * d_y, 0f);
	     			for(int t=0 ; t < num ; t++)
	     			{
     					s = cube.getVisibleVNodeSlice(num -t -1);
     					if(vis.isSelectedSlice(s))
     						gl.glColor3fv(COLOR_SLICE_SELECTED, 0);
     					else
     						gl.glColor4f(1f,1f,1f, 1);
	     				gl.glCallList(dl_vertexslice);
	     				gl.glTranslatef(-d_x ,d_y, 0f);
	    	     	}
	    	     	displayString(gl, Utils.add(pos, new float[]{0, height_vc*2.6f}), "Vertex 	Slices", CubixVis.FONT_12, ALIGN_JUSTIFIED, FLOAT4_0.clone());
	     		}
	     	}

//	     gl.glPopMatrix();
//	     gl.glEnable(gl.GL_DEPTH_TEST);
//	     gl.glEnable(gl.GL_CULL_FACE);
		CubixVis.end2D(gl);

	}
	
	
	
	protected void createDisplayLists(GL2 gl)
	{
		corner = new float[7][];
		corner[0] = new float[]{0, -height_vc};
		corner[1] = new float[]{width_vc, -height_vc/2f};
		corner[2] = new float[]{0, 0};
		corner[3] = new float[]{-width_vc, -height_vc/2};
		corner[4] = new float[]{width_vc, height_vc/2};
		corner[5] = new float[]{0, height_vc};
		corner[6] = new float[]{-width_vc, height_vc/2};

		int size_cam = 10;
		float[][] camera = new float[4][];
		camera[0] = new float[]{size_cam, size_cam};
		camera[1] = new float[]{-size_cam, size_cam};
		camera[2] = new float[]{-size_cam, -size_cam};
		camera[3] = new float[]{size_cam, -size_cam};
			
		
		dl_vc = gl.glGenLists(8);
		
		gl.glNewList(dl_vc, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_LINES);
		
			gl.glColor4fv(vis.COLOR_AXIS_TIME, 0);
			gl.glVertex2fv(corner[0], 0); 
			gl.glVertex2fv(corner[1], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);
			gl.glVertex2fv(corner[1], 0); 
			gl.glVertex2fv(corner[2], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_TIME, 0);
			gl.glVertex2fv(corner[2], 0); 
			gl.glVertex2fv(corner[3], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);
			gl.glVertex2fv(corner[3], 0); 	
			gl.glVertex2fv(corner[0], 0); 
			
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[1], 0); 
			gl.glVertex2fv(corner[4], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);
			gl.glVertex2fv(corner[4], 0); 
			gl.glVertex2fv(corner[5], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_TIME, 0);
			gl.glVertex2fv(corner[5], 0); 
			gl.glVertex2fv(corner[6], 0); 
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[6], 0); 
			gl.glVertex2fv(corner[3], 0); 			
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[2], 0); 
			gl.glVertex2fv(corner[5], 0); 
			
		gl.glEnd();
		gl.glEndList();

		dl_graphFace = dl_vc+1;
		gl.glNewList(dl_graphFace, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2fv(corner[1], 0); gl.glVertex2fv(corner[4], 0);  gl.glVertex2fv(corner[5], 0); gl.glVertex2fv(corner[2], 0); 
		gl.glEnd();
		gl.glEndList();
		
		dl_timeFace = dl_vc+2;
		gl.glNewList(dl_timeFace, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2fv(corner[5], 0); gl.glVertex2fv(corner[6], 0); gl.glVertex2fv(corner[3], 0); gl.glVertex2fv(corner[2], 0);
		gl.glEnd();
		gl.glEndList();
		
		dl_cubeFace = dl_vc+3;
		gl.glNewList(dl_cubeFace, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2fv(corner[0], 0); gl.glVertex2fv(corner[1], 0); gl.glVertex2fv(corner[2], 0); gl.glVertex2fv(corner[3], 0);
		gl.glEnd();
		gl.glEndList();

		dl_camera = dl_vc+4;
		gl.glNewList(dl_camera, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);

			gl.glVertex2fv(camera[0], 0);
			gl.glVertex2fv(camera[1], 0);
			gl.glVertex2fv(camera[2], 0);
			gl.glVertex2fv(camera[3], 0);
		gl.glEnd();
		gl.glEndList();
		
		
		dl_timeslice = dl_vc+5;
		gl.glNewList(dl_timeslice, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2fv(corner[1], 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glVertex2fv(corner[4], 0);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINES);
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);
			gl.glVertex2fv(corner[1], 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glColor4fv(vis.COLOR_AXIS_X, 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glVertex2fv(corner[4], 0);
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[4], 0);
			gl.glVertex2fv(corner[1], 0);
		gl.glEnd();
		gl.glEndList();

		dir_t = new float[]{-width_vc, height_vc};
		
		dl_vertexslice = dl_vc+6;
		gl.glNewList(dl_vertexslice, gl.GL_COMPILE);
		gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2fv(corner[3], 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glVertex2fv(corner[6], 0);
		gl.glEnd();
		gl.glBegin(GL2.GL_LINES);
			gl.glColor4fv(vis.COLOR_AXIS_TIME, 0);
			gl.glVertex2fv(corner[3], 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glColor4fv(vis.COLOR_AXIS_TIME, 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glVertex2fv(corner[6], 0);
			gl.glColor4fv(vis.COLOR_AXIS_Y, 0);
			gl.glVertex2fv(corner[2], 0);
			gl.glVertex2fv(corner[5], 0);
			gl.glVertex2fv(corner[3], 0);
			gl.glVertex2fv(corner[6], 0);
		gl.glEnd();
		gl.glEndList();
		
		dir_v = new float[]{width_vc, height_vc};

	}
	
	public void highlightGraphFace(){ highlightGraphFace  = true;}
	public void highlightTimeFace(){ highlightTimeFace = true;}
	public void resetGraphFace(){ highlightGraphFace = false;}
	public void resetTimeFace(){ highlightTimeFace = false;}
	public void highlightAllFaces(){ highlightGraphFace = true; highlightTimeFace = true; highlightCubeFace = true; }
	public void resetAllFaces(){
		highlightGraphFace = false; 
		highlightTimeFace = false; 
		highlightCubeFace = false; 
	}

	public boolean isGraphFace(int x_mouse, int y_mouse)
	{
		if(x_mouse > pos[X] && x_mouse < pos[X] + width_vc
		&& y_mouse > pos[Y] + corner[2][Y] && y_mouse < pos[Y] + corner[5][Y])
		{
			return true;
		}
		return false;
	}
	
	public boolean isTimeFace(int x_mouse, int y_mouse)
	{
		if(x_mouse > pos[X]-width_vc && x_mouse < pos[X]
		&& y_mouse > (pos[Y] + corner[2][Y]) && y_mouse < (pos[Y] + corner[5][Y]))
		{
			return true;
		}
		return false;
	}
	
	public boolean isCubeFace(int x_mouse, int y_mouse)
	{
		if(x_mouse > pos[X]-width_vc && x_mouse < pos[X]+width_vc
		&& y_mouse > pos[Y] + corner[0][Y] && y_mouse < pos[Y] + corner[2][Y])
		{
			return true;
		}
		return false;
	}
	
	 protected void displayString(GL2 gl, float[] pos, String string, int font, int align, float[] color)
	 {
		 	GLUT glut = new GLUT();
	     	gl.glPushMatrix();
	     	gl.glLoadIdentity();
			switch(align){
			case ALIGN_LEFT	:
				break;
			case ALIGN_RIGHT:
				pos[0] -= glut.glutBitmapLength(font, string); 
				break;
			case ALIGN_JUSTIFIED:
				pos[0] -= (glut.glutBitmapLength(font,  string) / 2f); 
				break;
			}
			
			gl.glColor3fv(color, 0);
			gl.glWindowPos3f(pos[X], screenHeight - pos[Y], 0);
			glut.glutBitmapString(font, string);
			gl.glPopMatrix();
	    }
	
	
}
