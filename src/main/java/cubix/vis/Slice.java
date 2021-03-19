package cubix.vis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import cubix.CubixVis;
import cubix.data.MatrixCube;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;


/**
 * Visual representation of a time slice in matrix form. This class manages
 * the positions and rotation of its associated cublets. However, the cublets
 * themself do not belong to this class. 
 * 
 * Matrices are in row-major notation and a cublet's x-position is determined 
 * by its column index, y position by the row order. 
 * 
 * @param <ROW_TYPE> - Row object type
 * @param <COL_TYPE> - Column object type
 * 
 * @author benjamin.bach@inria.fr
 */
public abstract class Slice<ROW_TYPE, COL_TYPE>  implements Constants
{
	protected static final float Q_COORD_VALUE = .5f;
	

	protected ArrayList< String> rowLabels = new ArrayList<String> (); 
	protected ArrayList<String> columnLabels = new ArrayList<String> ();

	protected MatrixCube matrixCube;
//	protected int cubeIndex;

	
	/// DATA OBJECTS
//	protected ArrayList<ROW_TYPE> rowObjects = new ArrayList<ROW_TYPE>(); 
//	protected ArrayList<COL_TYPE> columnObjects = new ArrayList<COL_TYPE>(); 

	/// CUBLETS
	protected Cell[][] cubletGrid;
	protected HashMap<Cell, Integer> cellRow = new HashMap<Cell, Integer>(); // row major notation
	protected HashMap<Cell, Integer> cellColumn = new HashMap<Cell, Integer>(); // row major notation

	/// LABELS
	protected Object data;
	protected Align labelAlignR = Align.LEFT;
	protected Align labelAlignL = Align.RIGHT;
	protected float[] labelPosR = new float[]{0f,0f,0f};
	protected float[] labelPosL = new float[]{0f,0f,0f};
	protected float[][] cornerVertices = new float[8][4];
	protected float[][] cornerAbsPos = new float[8][4];
	protected int rowCount = 0;
	protected int colCount = 0;
	
	protected int frameListIndex = -1;

	protected int LIST_CORNER_FRAME;
	protected int LIST_FRONT_FRAME;
	protected int LIST_BACK_FRAME;
	protected int LIST_HIGHLIGHT_FRAME;
	protected int LIST_COLUMN;
	protected int LIST_ROW;
	protected ArrayList<Line> lines = new ArrayList<Line>();

	protected float[] colTranslationVector = new float[]{0,0,0};

	private String label = "";
		

	/// CONSTRUCTOR
	public Slice(MatrixCube matrixCube, int rowCount, int colCount, Object data)
	{
		this.matrixCube = matrixCube;
		this.rowCount = rowCount;
		this.data= data;
		this.colCount = colCount;
		cubletGrid = new Cell[rowCount][colCount];
	}
	
	
	protected void _createDisplayList(GLAutoDrawable glDrawable)
	{
		GL2 gl = glDrawable.getGL().getGL2();
		frameListIndex = gl.glGenLists(3);
		
		LIST_FRONT_FRAME = frameListIndex;
		LIST_BACK_FRAME = frameListIndex+1;
		LIST_CORNER_FRAME = frameListIndex+2;
		LIST_HIGHLIGHT_FRAME = frameListIndex+3;
		LIST_COLUMN = frameListIndex+4;
		LIST_ROW = frameListIndex+5;
		
		_createCornerVertices(glDrawable);
		
		
		float w = CubixVis.CELL_UNIT;
		gl.glNewList(LIST_ROW, gl.GL_COMPILE);
			gl.glBegin(GL2.GL_LINES);
	
//				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{.7f,.5f,0f, .1f}, 0);							
//
//				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
//				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y]-w, cornerVertices[0][Z]);
//				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y]-w, cornerVertices[1][Z]);
//				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
//				
//				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
//				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y]-w, cornerVertices[0][Z]);
//				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y]-w, cornerVertices[4][Z]);
//				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
////				
//				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
//				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y]-w, cornerVertices[1][Z]);
//				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y]-w, cornerVertices[5][Z]);		
//				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);

				// CREATE OUTER LINES
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y]-w, cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y]-w, cornerVertices[0][Z]);
				
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y], cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y], cornerVertices[4][Z]);
				gl.glVertex3f(cornerVertices[0][X], cornerVertices[0][Y]-w, cornerVertices[0][Z]);
				gl.glVertex3f(cornerVertices[4][X], cornerVertices[4][Y]-w, cornerVertices[4][Z]);
				
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y], cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y], cornerVertices[5][Z]);
				gl.glVertex3f(cornerVertices[1][X], cornerVertices[1][Y]-w, cornerVertices[1][Z]);
				gl.glVertex3f(cornerVertices[5][X], cornerVertices[5][Y]-w, cornerVertices[5][Z]);
//				
				
			gl.glEnd();
		gl.glEndList();
	
		// CREATE LINES
		
		
			
//		// CREATE COLUMN WIRES
//		float[] pos0 = cornerVertices[0];
//		float[] pos1 = cornerVertices[3];
//		float[] pos2 = cornerVertices[7];
//		float[] pos3 = cornerVertices[4];
//		float[] collOffset = this._getColOffset();
//		for(int col=0 ; col<colCount ; col++)
//		{
//			pos0 = Utils.add(pos0, collOffset);
//			pos1 = Utils.add(pos1, collOffset);
//			pos2 = Utils.add(pos2, collOffset);
//			pos3 = Utils.add(pos3, collOffset);
//	
//			gl.glVertex3f(pos0[X], pos0[Y], pos0[Z]);
//			gl.glVertex3f(pos1[X], pos1[Y], pos1[Z]);
//			gl.glVertex3f(pos1[X], pos1[Y], pos1[Z]);
//			gl.glVertex3f(pos2[X], pos2[Y], pos2[Z]);
//			gl.glVertex3f(pos2[X], pos2[Y], pos2[Z]);
//			gl.glVertex3f(pos3[X], pos3[Y], pos3[Z]);
//			gl.glVertex3f(pos3[X], pos3[Y], pos3[Z]);
//			gl.glVertex3f(pos0[X], pos0[Y], pos0[Z]);
//		}
//			
//			// CREATE ROW WIRES
//			pos0 = cornerVertices[0];
//			pos1 = cornerVertices[1];
//			pos2 = cornerVertices[5];
//			pos3 = cornerVertices[4];
//			collOffset = new float[]{0,-CubixVis.CUBE_CELL_SIZE * matScaleFac, 0};
//			for(int col=0 ; col<colCount ; col++)
//			{
//				pos0 = Utils.add(pos0, collOffset);
//				pos1 = Utils.add(pos1, collOffset);
//				pos2 = Utils.add(pos2, collOffset);
//				pos3 = Utils.add(pos3, collOffset);
//
//				gl.glVertex3f(pos0[X], pos0[Y], pos0[Z]);
//				gl.glVertex3f(pos1[X], pos1[Y], pos1[Z]);
//				gl.glVertex3f(pos1[X], pos1[Y], pos1[Z]);
//				gl.glVertex3f(pos2[X], pos2[Y], pos2[Z]);
//				gl.glVertex3f(pos2[X], pos2[Y], pos2[Z]);
//				gl.glVertex3f(pos3[X], pos3[Y], pos3[Z]);
//				gl.glVertex3f(pos3[X], pos3[Y], pos3[Z]);
//				gl.glVertex3f(pos0[X], pos0[Y], pos0[Z]);
//			}
//			
	
	}
	
	
	protected abstract float[] _getColOffset();
		
	protected abstract void _createCornerVertices(GLAutoDrawable glDrawable);
	
	public void displayFront(GLAutoDrawable glDrawable)
	{
		GL2 gl = glDrawable.getGL().getGL2();
		if(frameListIndex < 0)
		{
			_createDisplayList(glDrawable);
		}
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glCallList(LIST_FRONT_FRAME);
		gl.glEnable(GL2.GL_LIGHTING);
	}

	public void displayBack(GLAutoDrawable glDrawable)
	{
		GL2 gl = glDrawable.getGL().getGL2();
		if(frameListIndex < 0){
			_createDisplayList(glDrawable);
		}
		gl.glDisable(GL2.GL_LIGHTING);
			gl.glCallList(LIST_BACK_FRAME);
		gl.glEnable(GL2.GL_LIGHTING);
	}
	
	public void displayEdges(GLAutoDrawable glDrawable)
	{
		GL2 gl = glDrawable.getGL().getGL2();
		if(frameListIndex < 0){
			_createDisplayList(glDrawable);
		}
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glCallList(LIST_CORNER_FRAME);
		gl.glEnable(GL2.GL_LIGHTING);
	}
	
	
	
	public void highlight(GLAutoDrawable glDrawable, int row, int col)
	{
		GL2 gl = glDrawable.getGL().getGL2();
		
		if(row > -1){
			gl.glPushMatrix();
			gl.glTranslatef(0, - row*CubixVis.CELL_UNIT, 0);
			gl.glCallList(LIST_ROW);
			gl.glPopMatrix();
		}

		if(col > -1){
			float[] v = Utils.mult(colTranslationVector , col);
			gl.glPushMatrix();
			gl.glTranslatef(v[X], v[Y], v[Z]);
			gl.glCallList(LIST_COLUMN);
			gl.glPopMatrix();
		}
	}
			

	/**
	 * Returns the position coordinates of the passed matrix positions (i,j,k) as
	 * it should be, according to the position of this matrix. 
	 * The positions can be floating, to get more fine grained positions.
	 * @param i - row position
	 * @param j - column position
	 * @param k - slice position
	 * @return
	 */
	public abstract float[] getRelGridCoords(float row, float column);

	
	
	/////////////////
	/// GET & SET /// 
	/////////////////
	
//	public int getRowIndex(ROW_TYPE obj){ return rowObjects.indexOf(obj); }
//	public ROW_TYPE getRowObject(int index){ return rowObjects.get(index); }
//	public int getColumnIndex(COL_TYPE obj){ return columnObjects.indexOf(obj); }
//	public COL_TYPE getColumnObject(int index){ return columnObjects.get(index); }
//	public ArrayList<ROW_TYPE> getRowObjects() { return this.rowObjects; }
//	public ArrayList<COL_TYPE> getColumnObjects() { return this.columnObjects; }
//	
//	public void setRowObjects(ArrayList<ROW_TYPE> objs){rowObjects.clear(); this.rowObjects.addAll(objs);}
//	public void setColumnObjects(ArrayList<COL_TYPE> objs){columnObjects.clear(); this.columnObjects.addAll(objs);}
	public Collection<Cell> getCells() { return cellRow.keySet();}
	

	public void setCell(Cell cell, int row, int column)
	{ 
		if(cell == null){
			Log.err(this, "Cell is null.");
		}
		if(row >= rowCount || column >= colCount){
			Log.out(this, "Cell not added.");
			return;
		}
		// TODO VI for now it works as it is
//		Cell cellReplace = cubletGrid[row][column];
//		if(cellRow.containsKey(cell)){
//			if(cellReplace != null){
//				cellRow.put(cellReplace, cellRow.get(cell));
//				cellColumn.put(cellReplace, cellColumn.get(cell));
//			}
//			cubletGrid[cellRow.get(cell)][cellColumn.get(cell)] = cellReplace;
//		}
		cubletGrid[row][column] = cell;		
		cellRow.put(cell, row);
		cellColumn.put(cell, column);
	}

	public String getLabel(){return label; } 
	public void setLabel(String label){this.label = label; } 
	public float[] getRightLabelPos(){return labelPosR; } 
	public void setRightLabelPos(float[] labelPos){this.labelPosR = labelPos; }
	public float[] getLeftLabelPos(){return labelPosL; } 
	public void setLeftLabelPos(float[] labelPos){this.labelPosL = labelPos; }

	public String getColumnLabel(int i) {return columnLabels.get(i); }
	public String getRowLabel(int i) {return rowLabels.get(i); }
	public void setColumnLabels(ArrayList<String> labels){columnLabels.clear(); columnLabels.addAll(labels);}
	public void setRowLabels(ArrayList<String> labels){rowLabels.clear(); rowLabels.addAll(labels);}
	

	public int getRow(Cell c){return cellRow.get(c);}
	public int getColumn(Cell c){return cellColumn.get(c);}

	public boolean containsCell(Cell c){ return cellRow.containsKey(c);}

	public Cell getCell(int row, int col){ 
		Cell c;
		try{
			c = cubletGrid[row][col]; 
		}catch(Exception e){
			return null;
		}
		return c;
	}
	
	public int getRowCount(){return this.rowCount;} 
	public int getColumnCount(){return this.colCount;}


	public void storeCornerPositions(float[] m)
	{
		for(int v=0 ; v < 8 ; v++)
		{
			cornerAbsPos[v] = new float[]{0f,0f,0f,0f};
			for(int row=0 ; row < 4 ; row++)
			{
				for(int col=0 ; col < 4 ; col++)
				{
					cornerAbsPos[v][row] += m[(col*4) + row] * cornerVertices[v][col]; 
				}
			}
		}
	}

	public void setLeftLabelAlign(Align align) {this.labelAlignL = align;}
	public Align getLeftLabelAlign() { return labelAlignL;}
	public void setRightLabelAlign(Align align) {this.labelAlignR = align;}
	public Align getRightLabelAlign() {return labelAlignR; }
	
	
	public float[] getCornerPos(int i) { return cornerAbsPos[i];}

	public MatrixCube getMatrixCube(){return matrixCube;}
	
	public abstract Object getData();
	
	/**
	 * We need to dynamically resize the slices in order to dynamically resize the cube.
	 * @param newRowCount
	 * @param newColCount
	 */
	public void reinitializeSlice(int newRowCount, int newColCount) {
		
		this.rowCount = newRowCount;
		this.colCount = newColCount;
		//this.data = data;
		this.cubletGrid = new Cell[rowCount][colCount];
		//this.cellRow = new HashMap<Cell, Integer>(); 
		//this.cellColumn = new HashMap<Cell, Integer>();
		this.frameListIndex = -1; // resets the cube frame
	}
	
	public void printGrid() {
		int num = 0;
		for(int i=0;i<cubletGrid.length ;i++){
			for(int j=0;j<cubletGrid[i].length ;j++){
				if(cubletGrid[i][j] != null)
//				Log.out(i+"," + j +": ");
					num++;
			}
		}
//		Log.out(this, label + ", num cells: " + num);
	}


	public void resetDisplayLists() {
		this.frameListIndex = -1;
	}
}

