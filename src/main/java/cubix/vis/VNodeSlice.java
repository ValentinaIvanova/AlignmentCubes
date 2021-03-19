package cubix.vis;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import cubix.CubixVis;
import cubix.CubixVisInteractive;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.MatrixCube;
import cubix.helper.Utils;

public class VNodeSlice extends Slice<CNode, CTime> {

	public VNodeSlice(MatrixCube matrixCube, int rowCount, int colCount, Object data) 
	{
		super(matrixCube, rowCount, colCount, data);
		colTranslationVector = new float[]{0, 0, CubixVis.CELL_UNIT};

	}

	@Override
	public float[] getRelGridCoords(float row, float column) 
	{
		float x = 0f;
		float y = -( row - ((rowCount-1) / 2.0f)) * CubixVis.CELL_UNIT;
		float z = (column - (colCount-1) / 2.0f) * CubixVis.CELL_UNIT;
		
		float[] newPosRel = new float[]{x,y,z, Q_COORD_VALUE};

		return newPosRel;
	}
	
	@Override
	protected void _createCornerVertices(GLAutoDrawable glDrawable) 
	{
		GL2 gl = glDrawable.getGL().getGL2();
		float size = CubixVis.CELL_UNIT  / 2f;

		cornerVertices[0] = Utils.add(getRelGridCoords(0,0), new float[]{-size, +size, -size, Q_COORD_VALUE});
		cornerVertices[1] = Utils.add(getRelGridCoords(0,colCount-1), new float[]{-size, size, size, Q_COORD_VALUE});
		cornerVertices[2] = Utils.add(getRelGridCoords(rowCount-1, colCount-1), new float[]{-size, -size, size, Q_COORD_VALUE});
		cornerVertices[3] = Utils.add(getRelGridCoords(rowCount-1, 0), new float[]{-size, -size, -size, Q_COORD_VALUE});
		cornerVertices[4] = Utils.add(getRelGridCoords(0,0), new float[]{size, +size, -size, Q_COORD_VALUE});
		cornerVertices[5] = Utils.add(getRelGridCoords(0,colCount-1), new float[]{size, size, size, Q_COORD_VALUE});
		cornerVertices[6] = Utils.add(getRelGridCoords(rowCount-1, colCount-1), new float[]{size, -size, size, Q_COORD_VALUE});
		cornerVertices[7] = Utils.add(getRelGridCoords(rowCount-1, 0), new float[]{size, -size, -size, Q_COORD_VALUE});
	
		gl.glNewList(LIST_FRONT_FRAME, GL2.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);
		
				gl.glColor4fv(CubixVis.COLOR_AXIS_Y, 0);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
	
				gl.glColor3fv(CubixVis.COLOR_AXIS_TIME, 0);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
				gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
	        
			gl.glEnd();
		gl.glEndList();
				
		gl.glNewList(LIST_BACK_FRAME, gl.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);

				gl.glColor4fv(CubixVis.COLOR_AXIS_Y, 0);
				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
				gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
				gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);


				gl.glColor4fv(CubixVis.COLOR_AXIS_TIME, 0);
				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
				gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
				gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
	    
			gl.glEnd();
		gl.glEndList();
				
		gl.glNewList(LIST_CORNER_FRAME, gl.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);
					
				gl.glColor4fv(CubixVis.COLOR_AXIS_X, 0);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
				gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
				gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
				gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
			
			gl.glEnd();
		gl.glEndList();
		
		/// HIGHLIGHTING DISPLAYLIST
		gl.glNewList(LIST_HIGHLIGHT_FRAME, gl.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);
			
			gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
			gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
			gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
			gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
			gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
			gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
			gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
			gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
			gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
			gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
			gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
			gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
			gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
			gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
			gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
			gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
			gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
			gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
			gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
			gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
			gl.glVertex3f(cornerVertices[2][X], cornerVertices[2][Y], cornerVertices[2][Z]);
			gl.glVertex3f(cornerVertices[6][X], cornerVertices[6][Y], cornerVertices[6][Z]);
			gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
			gl.glVertex3f(cornerVertices[7][X], cornerVertices[7][Y], cornerVertices[7][Z]);
			gl.glEnd();
		gl.glEndList();
		
		float w = CubixVis.CELL_UNIT;
		gl.glNewList(LIST_COLUMN, gl.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);
	
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]+w);
	
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]+w);
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]+w);
				
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]+w);
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
				
				gl.glVertex3f(cornerVertices[3][X], cornerVertices[3][Y], cornerVertices[3][Z]);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);

			gl.glEnd();
		gl.glEndList();

	}	
	
	
	@Override
	protected float[] _getColOffset()
	{
		float[] offset = new float[3];
		offset[X] = 0;
		offset[Y] = 0;
		offset[Z] = CubixVis.CELL_UNIT;
		return offset;
	}
	
	@Override
	public CNode getData(){return (CNode) data;}

}
