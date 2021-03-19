package cubix;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.Timer;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gicentre.utils.colour.ColourTable;

//import com.jogamp.graph.math.VectorUtil;
import com.jogamp.opengl.util.gl2.GLUT;

import cubix.CubixVis.AlignmentMode;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.HierarchicalCNode;
import cubix.data.MatrixCube;
import cubix.data.TimeGraph;
import cubix.helper.Constants;
import cubix.helper.ConvexHull;
import cubix.helper.Log;
import cubix.helper.Map;
import cubix.helper.NodeLabelComparator;
import cubix.helper.Utils;
import cubix.helper.histogram.Histogram;
import cubix.transitions.Transition;
import cubix.transitions.TransitionListener;
import cubix.transitions.TransitionManager;
import cubix.view.CView;
import cubix.view.CubeView;
import cubix.view.FrontView;
import cubix.view.SourceSideView;
import cubix.view.TargetSideView;
import cubix.view.ViewManager;
import cubix.vis.Camera;
import cubix.vis.Cell;
import cubix.vis.Cubelet2D;
import cubix.vis.HNodeSlice;
import cubix.vis.Lasso;
import cubix.vis.Slice;
import cubix.vis.TimeSlice;
import cubix.vis.VNodeSlice;
import cubix.vis.slider.DoubleRangeSlider;

public class CubixVis extends GLJPanel implements 	GLEventListener, 
													TransitionListener,
													Constants
													{
		
		protected static final long serialVersionUID = -5819379505394706209L;

		
		// GENERAL COLORS
		public static final int CLIPPING_HEIGHT = 10;
		public static final float[] COLOR_CELL_HOVER = new float[]{.6f,.2f,.3f, 1f};
		public static final float[] COLOR_CELL_SELECTION = new float[]{.3f,.2f,.1f, 1f};
		public static final float[] COLOR_HOVER_LIGHT = new float[]{.5f,.5f,.5f, 1f};
		public static final float[] COLOR_LABELS = new float[] {.15f,.15f,.15f, 1f}; //{.25f,.25f,.25f, 1f};
		public static final float[] COLOR_LABELS_HIGHLIGHT = new float[]{0f,.0f,0f, 1f};
		public static final float[] COLOR_AXIS_X = new float[]{1,0,0,1f};
		public static final float[] COLOR_AXIS_Y = new float[]{0,0.5f,0,1f};
		public static final float[] COLOR_AXIS_TIME = new float[]{0,0,1,1f};
		public static final float[] COLOR_TIME_START = new float[]{75f/255f,75f/255f, .75f, .5f};
		public static final float[] COLOR_TIME_END 	 = new float[]{.75f,.4f ,.04f, .5f};
		public static final float[] COLOR_CELL = new float[]{.2f,.2f, .2f, 1f};
		
		protected ArrayList<float[]> selectionColors = new ArrayList<float[]>();
		
		// VISUALIZATION CONSTANTS
		public static final float ROTATION_KEY_DIFF_DEGREE = 5;
		public static float CELL_UNIT = 1f; // size of one connection cube.
		protected static final float SETTINGS_MARGIN_LEFT = 20;	
		protected static final float SETTINGS_MARGIN_TOP = 25;
		protected static final int SETTINGS_LINE_SPACE = 15;
		public static final float ROTATION_MAP_FAC = .1f;
		public static boolean SHOW_INFOS = true;
		public static boolean SHOW_TIMEMATRIX_CENTERS = false;
		public static boolean SHOW_GRAPHMATRIX_CENTERS = true;
		public static boolean SHOW_COORINATE_SYSTEM = true;
		protected float width = 0;
		protected float height = 0; 
		public static int FONT_12 = 0;
		public static int FONT_18 = 0;
		
		// LIGHT
		protected static final float LIGHT_VALUE_AMBIENT_TIME = .2f;  // old 0.5
		protected static final float LIGHT_VALUE_DIFFUSE_TIME = .9f; // old 1
		protected static final float LIGHT_VALUE_AMBIENT_ALL = .5f;  // old 0.5
		protected static final float LIGHT_VALUE_DIFFUSE_ALL = 1f; // old 1
		protected static final float SHINE_ALL_DIRECTIONS = 0; // 0 = distant parallel light source.
		protected static final float FRAME_DIST_MIN = 0.5f;
		
		// STATES
		protected float[] dragDirScreen = new float[]{0,0};
		protected boolean doPicking = false;
		protected int xPick;
		protected int yPick;
		protected int xMouse;
		protected int yMouse;
		protected JPopupMenu contextMenu;
		public static enum SliceMode {TIME, HNODE, VNODE};
		private SliceMode sliceMode = SliceMode.HNODE;
		protected static int activeMatrixIndex = -1;
		protected Cell hoveredCell = null;
		protected Cell selectedCell = null;
		protected Slice<?, ?> hoveredSlice;
		protected float[] prevHit;
		protected CView currentView;
		protected HashSet<Cell> queriedCells = new HashSet<Cell>();
		protected HashSet<Slice<?,?>> queriedSlices = new HashSet<Slice<?,?>>();
		protected ArrayList<Cell> selectedCells = new ArrayList<Cell>();
		protected HashSet<Cell> cellsInWeightRange = new HashSet<Cell>();
		protected int cubeletHitFace;
		public static float WEIGHT_MIN = 1000000;
		public static float WEIGHT_MAX = 0;
		public static float ACCUMULATED_WEIGHT_MIN = 1000000;
		public static float ACCUMULATED_WEIGHT_MAX = 0;		
		/** SINGLE_VALUE - the similarity value between two nodes is shown; 
		 * SUM_MAPPINGS - the number of mappings in which their children participate **/
		protected enum AlignmentMode{SIMILARITY_VALUES, SUM_MAPPINGS};
		protected AlignmentMode alignmentMode = AlignmentMode.SIMILARITY_VALUES; // VI when change the default mode, change also the default radio button selection
		protected enum ShapeEncoding{WEIGHT, CONE, NONE};
		protected ShapeEncoding shapeEncoding = ShapeEncoding.WEIGHT;
		protected enum ColorEncoding{TIME, WEIGHT, WEIGHT_DIV, NONE};
		protected ColorEncoding colorEncoding = ColorEncoding.TIME; // VI it was WEIGHT by default
		private boolean PRINT_MODE = false;
		private boolean weightAdaption = false;
		/** Indicates whether the red-blue cube frame is visible **/
		protected boolean frameVisibility = true;
		private static boolean doDepthTest = true;
		public enum Query {CELLS, VECTORS, SLICES}
		protected Query queryMode = Query.CELLS;
		protected boolean mouseClickedLeft = false;
		protected enum DLMode{CELL, SLICE, CUBE};
		protected DLMode dlMode = DLMode.CELL;
		protected boolean shiftDown = false;
		protected boolean altDown = false;
		protected JCheckBox selfEdgeVisibilityCheckBox;
		protected JCheckBox nonSelfEdgeVisibilityCheckBox;
		public static boolean SHOW_FRAMES = false;
		

		
	    // OPEN GL
		protected GLU glu;
		protected Camera camera;
		protected boolean ready = false;
		protected boolean takeScreenShot;
		protected int matrixWireDL = -1;

		
		// MANAGEMENT CLASSES
		protected TransitionManager tm;
		protected ViewManager vm;
		protected JPanel controlPanel;
//		protected File logFile;
//		protected CSVWriter log;



		// DATA AND DATA STRUCTURES
		protected TimeGraph<CNode,CEdge,CTime> tGraph = null;
		protected MatrixCube matrixCube;
		protected HashSet<Slice<?,?>> rotatedSlices = new HashSet<Slice<?,?>>();
		protected HashSet<CTime> timeSliderTimes = new HashSet<CTime>();
		protected HashSet<CTime> selectedTimes = new HashSet<CTime>();
		protected HashSet<CNode> selectedHNodes = new HashSet<CNode>();
		protected HashSet<CNode> selectedVNodes = new HashSet<CNode>();
		protected HashMap<Slice<?,?>, float[]> coloredSlices = new HashMap<Slice<?,?>, float[]>();

		protected ArrayList<Float> weightValues = new ArrayList<Float>();
		protected ArrayList<Float> timeValues = new ArrayList<Float>();
		protected ArrayList<Slice<?, ?>> renderedSlices = new ArrayList<Slice<?,?>>();
		/** Contains visible cells. If a cell is visible, momentaly is defined in 
		 * the _displayCell method. Should be done elsewhere.*/
		private HashSet<Cell> visibleCells = new HashSet<Cell>();
		private float[] yLabelOffsetFactor;
		private float[] labelRotation;
		private float[] zLabelOffsetFactor;

		
		// VISUAL VALUES
		protected HashMap<Slice, float[]> slicePos = new HashMap<Slice, float[]>();
		protected HashMap<Slice, Float> sliceScale = new HashMap<Slice, Float>();
		protected HashMap<Slice, Float> sliceRotation = new HashMap<Slice, Float>();
		protected HashMap<Slice, float[]> labelTransparency = new HashMap<Slice, float[]>();
		private HashMap<String, float[]> northLabelPos = new HashMap<String, float[]>();
 		protected HashMap<Cell, float[]> cellColor = new HashMap<Cell, float[]>();
		protected HashMap<Integer, float[]> timeColors = new HashMap<Integer, float[]>();
		//protected HashMap<Integer, float[]> alignmentColors = new HashMap<Integer, float[]>();
		protected HashMap<String, float[]> alignmentColors = new HashMap<String, float[]>();
		protected float[][] colorPalette = new float[][] {{166f,206f,227f},{31f,120f,180f},
				{178f,223f,138f},{51f,160f,44f},{251f,154f,153f},
				{227f,26f,28f},{253f,191f,111f},{255f,127f,0f},{202f,178f,214f},
				{106f,61f,154f},{255f,255f,36f},{177f,89f,40f},
				{0f,128f,128f},{0f,0f,128f},{95f,2f,31f}};
//		protected HashMap<Integer, float[]> valueColors = new HashMap<Integer, float[]>();
		protected ColourTable edgeWeightColorScale1;
		protected ColourTable biColorScale;
		protected static final int FACE_FRONT = 0;
		protected static final int FACE_SIDE = 1;
		protected static final int FACE_TOP = 2;
		private static final float FRAME_WIDTH = 0.1f;
		private float[] currentCellColor = new float[]{-1f,-1f,-1f};
		private float currentCellTransparency = -1;
		protected boolean logScale = false; 	// Whether weight is scaled log or linear.
		protected boolean divergingScale = false; 	// Whether weight is scaled log or linear.


		
		// UI ELEMENTS
		protected JButton cellColorEncodingButton;
		protected JSlider logScaleSlider;
		protected Cubelet2D cubelet;
		protected DoubleRangeSlider weightRangeSlider = null;
		protected DoubleRangeSlider timeRangeSlider = null;
		protected DoubleRangeSlider opacityRangeSlider = null;
		protected JSlider transitionSpeedSlider;
		protected JCheckBox inverseFilterCheckBox = null;



		// LABELS
		protected float SCALE_LABELS = .13f;
		private static final float SKEW_FACTOR = .007f;
		private static final float ROTATION_FACTOR = .8f;
		private static final float HOVER_THICKNESS_FACTOR = 2f;
		protected HashMap<float[], Slice> labelBounds = new HashMap<float[], Slice>();
		// the labels are indented depending on their depth in the ontology hierarchy
		public static final float LABEL_LEVEL_INDENT = 1.0f; 

		// ANIMATIONS / TRANSITIONS
		private Timer frameRateCounter;
		private int frameCount= 0;
		protected float framesPerSecond;
		protected float[] frameRateArray = new float[10];
		private long viewStartTime;	 
		private int animationFrameCount;
		private long animationStartTime;


		private int sliceDisplayListIndex = -1;
		// Stores display lists for slices.
		protected HashMap<Slice, Integer> sliceDisplayListMap = new HashMap<Slice, Integer>();




		private int cubeDLIndex;


		protected boolean switchToSliceDLMode = false;
		protected boolean switchToCubeDLMode = false;


		private CView newView;


		private HashMap<Slice, Align> eastAlign = new HashMap<Slice, Align>();
		private HashMap<Slice, Align> westAlign = new HashMap<Slice, Align>();
		private HashMap<Slice, float[]> eastOffset = new HashMap<Slice, float[]>();
		private HashMap<Slice, float[]> westOffset = new HashMap<Slice, float[]>();
		private HashMap<Slice, float[]> northOffset = new HashMap<Slice, float[]>();
		private HashMap<Slice, float[]> southOffset = new HashMap<Slice, float[]>();

		protected boolean rotationChange = false;
		protected boolean orderChange = false;
		protected boolean sliceLabelRotationDirChange = false;
		protected boolean sliceOrderChange = false;


		private boolean invertYOffset = false;


		private int camCase;


		private boolean invertSliceAlign;


		private float rightYLabelPos = 0;
		private float leftYLabelPos = 0;


		private ColourTable edgeWeightColorScale2;


		protected ColourTable currentEdgeWeightColorScale;		
		
		
		public static String SOURCE_ONTO = "";
		public static String TARGET_ONTO = "";	
		public static Long SOURCE_ONTO_NUM_CLASSES = 0L;
		public static Long TARGET_ONTO_NUM_CLASSES = 0L;
		public static int MAPPINGS_COUNT = 0;
		
		///////////////////
		/// CONSTRUCTOR	///
		///////////////////
		
		protected CubixVis()
		{
			// Setup OpenGL Version 2
			super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
			
			setLayout(new BorderLayout());
			
			GLUT glut = new GLUT();
			FONT_12  = glut.BITMAP_HELVETICA_12;
			FONT_18  = glut.BITMAP_HELVETICA_18;
			
			weightRangeSlider = new DoubleRangeSlider(0, 1	, 0, 1);
			weightRangeSlider.setEnabled(true);
			
			// Create log file			
			Calendar c = Calendar.getInstance();
//			logFile = new File("log_" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR) + "-" + c.get(Calendar.MINUTE) + ".csv");
//			try {
//				log = new CSVWriter(new FileWriter(logFile));
//			} catch (IOException e) {
//				Log.err(this, "No log file created.");
//				e.printStackTrace();
//			}
//			log.writeNext(new String[]{"VIEW", "DURATION", "TRANSITION SPEED", "WEIGHT_ADAPTION", "COLOR_ENCODING", "SHAPE_ENCODING", "LOW_WEIGHT", "HIGH_WEIGHT"});
		
			selectionColors.add(new float[]{251/255f, 128/255, 114/255f});
			selectionColors.add(new float[]{41/255f, 211/255f, 199/255f});
			selectionColors.add(new float[]{190/255f, 186/255f, 218/255f});
			selectionColors.add(new float[]{128/255f, 177/255f, 211/255f});
			selectionColors.add(new float[]{253/255f, 180/255f, 98/255f});
			selectionColors.add(new float[]{179/255f, 222/255f, 105/255f});
			selectionColors.add(new float[]{252/255f, 205/255f, 229/255f});
			selectionColors.add(new float[]{188/255f, 128/255f, 189/255f});
			selectionColors.add(new float[]{204/255f, 235/255f, 197/255f});
			selectionColors.add(new float[]{255/255f, 237/255f, 111/255f});
		}		
		
		public void setDimensions(int width, int height)
		{
			this.width = width;
			this.height = height;
		}
		
		
		

	/////////////////
	/// LOAD DATA ///
	/////////////////
		
		public void createCube(TimeGraph<CNode,CEdge,CTime> timeGraph) 
		{
			System.out.println("[CubeVis] visualizeGraph()");
			
			if(timeGraph == null){
				System.out.println("[CubeVis] Graph is NULL;");
				return;
			} 
			
			// Set Time Graph
			this.tGraph = timeGraph;
			
			System.out.println("[CubeVis] Visualize graph:");
			System.out.println("[CubeVis] \tNodes: " + tGraph.getVertexNumber() );
			System.out.println("[CubeVis] \tEdges: " + tGraph.getEdgeNumber() );
			System.out.println("[CubeVis] \tTimes: " + tGraph.getTimeSliceNumber());
				
				
			// CREATE TIMELINE MATRIX CUBE. 
			// Matrices and Cubtes are initialized internally
			//matrixCube = new MatrixCube(tGraph, CELL_UNIT);
			// VI TODO VI choose between the two constructors depending on the data loaded	
			matrixCube = new MatrixCube(tGraph, CELL_UNIT, true);
		
			// DEFINE TIME COLORS 
			for(int time=0 ;time < matrixCube.getTimeCount(); time++)
			{
				float frac;
				ColourTable cTable = new ColourTable();
				cTable.addContinuousColourRule(0/1, (int)(COLOR_TIME_START[R]* 255), (int)(COLOR_TIME_START[G]* 255), (int)(COLOR_TIME_START[B]* 255));
				cTable.addContinuousColourRule(1/1, (int)(COLOR_TIME_END[R]* 255), (int)(COLOR_TIME_END[G]* 255), (int)(COLOR_TIME_END[B]* 255));
				frac = time / (matrixCube.getTimeCount() + 0.0f);
				timeColors.put(time, _getColor(cTable, frac));
			}
			
			// VI DEFINE ALIGNMENT COLORS - same purpose as above but different color scheme
			for(int time=0 ;time < matrixCube.getTimeCount(); time++)
			{
				float[] color = new float[]{0f,0f,0f,1f};
				color[R] = colorPalette[time][0] /255f;
				color[G] = colorPalette[time][1] /255f;
				color[B] = colorPalette[time][2] /255f;
				alignmentColors.put(matrixCube.getTimeSlice(time).getLabel(), color);
			}
			
			// Create Value colors 
			edgeWeightColorScale1 = new ColourTable();
			edgeWeightColorScale2 = new ColourTable();
			biColorScale = new ColourTable();

//			float brighness = 1f;
//			edgeWeightColorScale.addContinuousColourRule(0/1, (int)(255*brighness),(int)(255*brighness), (int)(217*brighness));
//			edgeWeightColorScale.addContinuousColourRule(0.3f/1, (int)(199*brighness), (int)(233*brighness), (int)(180*brighness));
//			edgeWeightColorScale.addContinuousColourRule(0.5f/1, 29, 145, 192);
//			edgeWeightColorScale.addContinuousColourRule(1/1, 8, 29, 88);
			
			
//			edgeWeightColorScale.addContinuousColourRule(1/1, (int)(255*brighness),(int)(255*brighness), (int)(217*brighness));
//			edgeWeightColorScale.addContinuousColourRule(0.33f/1, (int)(199*brighness), (int)(233*brighness), (int)(180*brighness));
//			edgeWeightColorScale.addContinuousColourRule(0.66f/1, 29, 145, 192);
//			edgeWeightColorScale.addContinuousColourRule(0/1, 100, 100, 100);

//			edgeWeightColorScale.addContinuousColourRule(1f/1, (int)(199*brighness), (int)(233*brighness), (int)(180*brighness));
//			edgeWeightColorScale.addContinuousColourRule(1f/1, (int)(255*brighness),(int)(50*brighness), (int)(217*brighness));
			
//			edgeWeightColorScale.addContinuousColourRule(1/1, (int)(200*brighness),(int)(200*brighness), (int)(100*brighness));
//			edgeWeightColorScale.addContinuousColourRule(.3f/1, (int)(255*brighness),(int)(50*brighness), (int)(217*brighness));
//			edgeWeightColorScale.addContinuousColourRule(0/1, 0, 0, 150);

			float b = 1f;
//			edgeWeightColorScale.addContinuousColourRule(0/1, (int)(107*b),(int)(77*b), (int)(0*b));
//			edgeWeightColorScale.addContinuousColourRule(.25f/1, (int)(140*b), (int)(78*b), (int)(24*b));
//			edgeWeightColorScale.addContinuousColourRule(.5f/1, (int)(184*b), (int)(69*b), (int)(37*b));
//			edgeWeightColorScale.addContinuousColourRule(.75f/1, (int)(224*b), (int)(65*b), (int)(70*b));
//			edgeWeightColorScale.addContinuousColourRule(.5f/1, (int)(255), (int)(75), (int)(120));
//			edgeWeightColorScale.addContinuousColourRule(1/1, (int)(255), (int)(75), (int)(120));

			// Latest used light blue-blue
			edgeWeightColorScale1.addContinuousColourRule(0/1, 150,150, 150);
			edgeWeightColorScale1.addContinuousColourRule(1f/1, 00, 00, 100);

			edgeWeightColorScale2.addContinuousColourRule(0f/1,(int)(30*b), (int)(30*b), (int)(255*b));
			edgeWeightColorScale2.addContinuousColourRule(0.5f/1, (int)(150*b), (int)(150*b), (int)(150*b));
			edgeWeightColorScale2.addContinuousColourRule(1f/1, (int)(150*b), (int)(30*b), (int)(30*b));

			biColorScale.addContinuousColourRule(0/1, 145,191,219);
			biColorScale.addContinuousColourRule(0.5f/1, 255,255,191);
			biColorScale.addContinuousColourRule(1/1, 252,141,89);
			
			
			currentEdgeWeightColorScale = edgeWeightColorScale1;
			
			calculateSlicesPositions();
				
			weightRangeSlider.getModel().setMinimum(WEIGHT_MIN);
			weightRangeSlider.getModel().setMaximum(WEIGHT_MAX);
			weightRangeSlider.setLowValue(WEIGHT_MIN);
			weightRangeSlider.setHighValue(WEIGHT_MAX);

			cellsInWeightRange.addAll(matrixCube.getCells());
			
			// Set focus to window for key input
			this.requestFocus();
			this.requestFocusInWindow();
			
	    	ready = true;
	    	
	    	vm = ViewManager.getInstance();
	    	vm.init(this);

			
			tm = new TransitionManager(this);
			

			currentView = vm.getView(0);

			// VI moved ina method below
			//for(Slice slice : matrixCube.getTimeSlices()){
			//	slice.setRightLabelPos(currentView.getLabelPosR(slice));
			//	slice.setLeftLabelPos(currentView.getLabelPosL(slice));
			//	labelTransparency.put(slice, currentView.getLabelTrans(slice));
			//}
			//for(Slice slice : matrixCube.getVisibleHNodeSlices()){
			//	slice.setRightLabelPos(currentView.getLabelPosR(slice));
			//	slice.setLeftLabelPos(currentView.getLabelPosL(slice));
			//}
			//for(Slice slice : matrixCube.getVisibleVNodeSlices()){
			//	slice.setRightLabelPos(currentView.getLabelPosR(slice));
			//	slice.setLeftLabelPos(currentView.getLabelPosL(slice));
			//}

			selectedCells.addAll(matrixCube.getCells());
	
			setSliceMode(SliceMode.TIME);
			reorder(tGraph.getEdges());
			
			updateLabelsPosition();
			
			// Calculate weight distribution
			for(Cell ce : matrixCube.getCells()){
				if(ce.getData().getWeight() == 0)
//					Log.out(this, "weight = 0");
				weightValues.add(ce.getData().getWeight());
			}
			
		}

		private void calculateSlicesPositions() {
			//int tIndex = 0; // VI needed?
			TimeSlice s;
			float[] color;
			for(CTime t: tGraph.getTimes())
			{
				s = matrixCube.getTimeSlice(t);
				//tIndex = matrixCube.getTimeIndex(t); // VI needed?
				if (!slicePos.containsKey(s)){
					//System.out.println("TimeSlice ADDED");
					sliceScale.put(s, 1f);
					sliceRotation.put(s, 0f);
					slicePos.put(s, new float[]{0f,0f,0f});
					labelTransparency.put(s, FLOAT4_0.clone());
				}
				//color = timeColors.get(tIndex);
				//color = alignmentColors.get(tIndex);
				color = alignmentColors.get(s.getLabel());
				for(Cell c : s.getCells())
				{
					//timeValues.add((float) tIndex); // VI needed?
					cellColor.put(c, color);
				}
			}
			
			HNodeSlice sh;
			VNodeSlice sv;
			for(CNode n : tGraph.getVertices())
			{
				if (n instanceof HierarchicalCNode) {
					
					HierarchicalCNode hn = (HierarchicalCNode) n;
					if (hn.isVisible()) {
						if (!hn.belongsToSourceOnto()) {
							// NODE V-SLICES
							sv = matrixCube.getVNodeSlice(hn);
							if (!slicePos.containsKey(sv)){
								//System.out.println("VNodeSlice ADDED");
								sliceScale.put(sv, 1f);
								sliceRotation.put(sv, 0f);
								slicePos.put(sv, new float[]{CELL_UNIT * (matrixCube.getVisibleColumnIndex(hn)-(matrixCube.getVisibleColumnCount()-1)/2f), 0f, 0f}); 
								labelTransparency.put(sv, FLOAT4_0.clone());
							}
						} else {
							// NODE H-SLICES
							sh = matrixCube.getHNodeSlice(hn);
							if (!slicePos.containsKey(sh)){
								//System.out.println("HNodeSlice ADDED");
								sliceScale.put(sh, 1f);
								sliceRotation.put(sh, 0f);
								slicePos.put(sh, new float[]{0f, CELL_UNIT * ((matrixCube.getVisibleColumnCount()-1)/2f - matrixCube.getVisibleRowIndex(hn)), 0f}); 
								labelTransparency.put(sh, FLOAT4_0.clone());
							}
						}
					}
				} 
			}
		}

		/** Reorders the cube taking into account the passed times steps. **/
		public boolean reorder(Collection<CEdge> validEdges)
		{
			if(rotatedSlices.size() > 0 
			|| isTimeSMView() || isVNodeSliceSMView()){
				return false;
			}
			
			// VI bypasses the method below for optimization purposes
			//matrixCube.reorderNodes(validEdges);
			
			ArrayList<CNode> arr = new ArrayList<>();
			arr.addAll(tGraph.getVertices());
			matrixCube.setNodeOrder(arr);
			
			updateViewsAndSlices();
			
			requestFocus(); 
			display();
			return true;
		}
		
		public boolean reorderTime(Collection<CEdge> validEdges)
		{
			if(rotatedSlices.size() > 0 
			|| isTimeSMView() || isVNodeSliceSMView()){
				return false;
			}
					
			matrixCube.reorderTimes(validEdges);
					
			updateViewsAndSlices();
					
			requestFocus(); 
			display();
			return true;
		}
		
		public void reorderAlignments(String[] order){
			
			matrixCube.setAlignmentsOrder(order);
			
			//for(Slice<?,?> s : matrixCube.getTimeSlices()){
			//	s.resetDisplayLists();
			//}
			calculateSlicesPositions();
			renderedSlices.clear();
			if(isVSliceMode())
			{
			    renderedSlices.addAll(matrixCube.getVisibleVNodeSlices());
			    Collections.reverse(renderedSlices);
			}else if(isHSliceMode()){
			    renderedSlices.addAll(matrixCube.getVisibleHNodeSlices());
			}else{
				renderedSlices.addAll(matrixCube.getTimeSlices());
			}
			updateViewsAndSlices();
			updateLabelsPosition();

			requestFocus(); 
			display();
		}
		
		public boolean reorderByName()
		{
			if(rotatedSlices.size() > 0 
			|| isTimeSMView() || isVNodeSliceSMView()){
					return false;
			}
				
			ArrayList<CNode> nodes = new ArrayList<CNode>();
			nodes.addAll(tGraph.getVertices());
			Collections.sort(nodes, new NodeLabelComparator(tGraph));
		
			matrixCube.setNodeOrder(nodes);
		
//			int t=0;
//			for(TimeSlice s : matrixCube.getTimeSlices()){
//				slicePos.put(s, new float[]{0f,0f, CubixVis.CELL_UNIT * (t - (matrixCube.getTimeCount()-1)/2f)});
//				t++;
//			}
//			t=0;
//			for(VNodeSlice s : matrixCube.getVNodeSlices()){
//				slicePos.put(s, new float[]{CELL_UNIT * (t-(matrixCube.getColumnCount()-1)/2f), 0f, 0f}); 
//				t++;
//			}
//			t=0;
//			for(HNodeSlice s : matrixCube.getHNodeSlices()){
//				slicePos.put(s, new float[]{0f, CELL_UNIT * ((matrixCube.getColumnCount()-1)/2f - t), 0f}); 
//				t++;
//			}
			
			// VI replaces the code below 
			updateViewsAndSlices();
			
/*			for(CView v : ViewManager.getInstance().getViews()){
				v.init(this);
			}

			for(Slice s : renderedSlices){
				slicePos.put(s, currentView.getSlicePosition(s));
			}*/
			requestFocus();
			display();
			return true;
		}
		
		public void collapseNode(HierarchicalCNode nodeToCollapse){
				
			if (!nodeToCollapse.isExpanded())
				return;
			
			nodeToCollapse.setExpanded(false);
			collapseChildNodes(nodeToCollapse);
			
			ArrayList<CNode> nodes = new ArrayList<CNode>();
			nodes.addAll(tGraph.getVertices());
			
			matrixCube.setNodeOrder(nodes);
			updateViewsAndSlices();
			updateLabelsPosition();
			
			requestFocus(); 
			display();			
		}
		
		private void collapseChildNodes(HierarchicalCNode node) {
			
			ArrayList<HierarchicalCNode> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				
				HierarchicalCNode n = children.get(i);
				n.setVisible(false);
				n.setExpanded(false);
				collapseChildNodes(n);
			}
		}

		public void expandNode(HierarchicalCNode nodeToExpand){

			ArrayList<HierarchicalCNode> children = nodeToExpand.getChildren();
			if (children.isEmpty())
				return;
			//for (int i = 0; i < children.size(); i++) {
				
				//System.out.println("CHILDREN: " + children.get(i).getLabel());
			//}
			
			nodeToExpand.setExpanded(true);
			
			ArrayList<CNode> nodes = new ArrayList<CNode>();
			nodes.addAll(tGraph.getVertices());
			//System.out.println(nodes.size());
			nodes.removeAll(nodeToExpand.getChildren());
			//System.out.println(nodes.size());
			for (int i = 0; i < children.size(); i++) {
				HierarchicalCNode n = children.get(i);
				n.setVisible(true);
				nodes.add(nodes.indexOf(nodeToExpand)+1+i, n);
			}
			
			matrixCube.setNodeOrder(nodes);
			
			selectedCells.clear();
			selectedCells.addAll(matrixCube.getCells());
			
			// the next three lines mean that when we expand a node we reset the weightRangeSlider
			cellsInWeightRange.clear();
			cellsInWeightRange.addAll(matrixCube.getCells());
			
			updateWeightRangeSlider();
			
			// TODO VI change the histogram - see CubixVisInteractive 430-440
			// the histogram does not update even when its paint method is called
			// or the paint for the slider is called, but it is updated when changing alignment modes!
			//weightRangeSlider.paintComponents(getGraphics());
			// TODO VI weightRangeSlider.paintComponents(getGraphics()); what about collapse - reset the slider ? but what about the cells
			
			//setCellsColor();
			calculateSlicesPositions();
			updateViewsAndSlices();
			updateLabelsPosition();
			
			requestFocus(); 
			display();
		}
		
		private void setCellsColor() {
			int tIndex = 0;
			TimeSlice s;
			float[] color;
			for(CTime t: tGraph.getTimes())
			{
				s = matrixCube.getTimeSlice(t);
				tIndex = matrixCube.getTimeIndex(t);
				// color = timeColors.get(tIndex);
				color = alignmentColors.get(tIndex);
				for(Cell c : s.getCells())
				{
					cellColor.put(c, color);
				}
			}			
		}

		protected void updateWeightRangeSlider() {
			float min = 1000000;
			float max = 0;
			
			if (alignmentMode == AlignmentMode.SUM_MAPPINGS){
				min = ACCUMULATED_WEIGHT_MIN;
				max = ACCUMULATED_WEIGHT_MAX;
			} else if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) {
				min = WEIGHT_MIN;
				max = WEIGHT_MAX;
			}
			
			float w = 0;
			ArrayList<Float> values = new ArrayList<Float>();
			for(Cell c : matrixCube.getCells()){
				if (alignmentMode == AlignmentMode.SUM_MAPPINGS)
					w = c.getData().getAccumulatedWeight();
				else if (alignmentMode == AlignmentMode.SIMILARITY_VALUES)
					w = c.getData().getWeight();
				
				if(w != 0) 		
				{
					min = Math.min(min,w);
					max = Math.max(max,w);
					values.add(w);
				}
			}
			weightRangeSlider.setMinimum(min);
			weightRangeSlider.setMaximum(max);
			weightRangeSlider.setLowValue(min);
			weightRangeSlider.setHighValue(max);
			Histogram h = weightRangeSlider.getHistogram();
			//h.setValues(min, max, values, Cubix.dataSetName.contains("Collab"));
			h.setValues(min, max, values, false);
		}		

		private void updateLabelsPosition() {
			for(Slice slice : matrixCube.getTimeSlices()){
				//slice.resetDisplayLists();
				slice.setRightLabelPos(currentView.getLabelPosR(slice));
				slice.setLeftLabelPos(currentView.getLabelPosL(slice));
				labelTransparency.put(slice, currentView.getLabelTrans(slice));
			}
			for(Slice slice : matrixCube.getVisibleHNodeSlices()){
				//slice.resetDisplayLists();
				slice.setRightLabelPos(currentView.getLabelPosR(slice));
				slice.setLeftLabelPos(currentView.getLabelPosL(slice));
			}
			for(Slice slice : matrixCube.getVisibleVNodeSlices()){
				//slice.resetDisplayLists();
				slice.setRightLabelPos(currentView.getLabelPosR(slice));
				slice.setLeftLabelPos(currentView.getLabelPosL(slice));
			}			
		}

		private void updateViewsAndSlices() {
			for(CView v : ViewManager.getInstance().getViews()){
				v.init(this);
			}

			//float[] p;
			for(Slice s : renderedSlices){
				//p = currentView.getSlicePosition(s);
				slicePos.put(s, currentView.getSlicePosition(s));
			}
		}
		
		//////////////////////////////////
		/// OPEN GL & DRAWING ROUTINES ///
		//////////////////////////////////
		
		public void init(GLAutoDrawable glDrawable) 
	    {
	    	System.out.println("[CubeVis] init() ");
	    	
	    	// INIT GL
	    	GL2 gl = glDrawable.getGL().getGL2();
	    
			glu = GLU.createGLU(gl);
	    	gl.glShadeModel(GL2.GL_FLAT);  // Flat shading
	        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	        gl.glEnable(GL2.GL_CULL_FACE);
	        gl.glFrontFace(GL2.GL_CCW);
	        gl.glEnable(GL2.GL_COLOR_MATERIAL_FACE);
	        gl.glEnable(GL2.GL_BLEND); 		// activates transparency
	        gl.glClearColor(1,1,1,1); 		// White background
	        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	       
	    	
	        // Camera
	        camera = new Camera(gl, glu, width, height, CLIPPING_HEIGHT, 40);
	      
	        gl.glEnable(GL2.GL_NORMALIZE);
	        
	        initLight(gl);
		
	        // init name stack
	        gl.glInitNames(); 
	        
			cubelet = new Cubelet2D(width, height, this);
      
//			frameRateCounter = new Timer(1000, new ActionListener(){
//				int i=0;
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					if(i==frameRateArray.length) 
//						i=0;
//					framesPerSecond = frameCount;
//					frameCount=0;
//					Log.out(this, "fps: " + framesPerSecond);
//					if(framesPerSecond > 0)
//						frameRateArray[i++] = framesPerSecond;
//				}});
//			frameRateCounter.start();
			
			viewStartTime = System.currentTimeMillis();
			
			if(matrixCube != null){				
				// Recreate displayLists
				for(Cell c : matrixCube.getCells()){
					c.resetDisplayLists();
				}
				for(Slice<?,?> s : matrixCube.getTimeSlices()){
					s.resetDisplayLists();
				}
			}

	    }
	 
	    
	    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			   
	    public void display(GLAutoDrawable glDrawable) 
	    {
	    	if(matrixCube == null)
	    		return; 
	    	
	    	frameCount++;
	    	animationFrameCount++;
	    	
	    	// OPEN GL 
	    	GL2 gl = glDrawable.getGL().getGL2();
	    	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

	    	gl.glLoadIdentity();

			doDepthTest = 
					isCubeView()
					|| camera.getViewAngle() > 1;
//					|| ((isFrontView() || isSideView()) && camera.getViewAngle() < 1 && opacityRangeSlider.getHighValue() == 1);// && this.colorEncoding == ColorEncoding.WEIGHT );
					
	    	if(doDepthTest) gl.glEnable(GL2.GL_DEPTH_TEST);
	    	else gl.glDisable(GL2.GL_DEPTH_TEST);
	    	
	    	// init
	    	labelBounds.clear();
	    	
	        if(!ready){
		        return;
	        }
	        

			// CAMERA
			glu = new GLU();
			camera.set(gl, glu);
			
			// VI sets up the ambient and diffuse lighting based on the color encoding mode
	        if (colorEncoding == ColorEncoding.TIME){
		        float[] lightColorAmbient = {LIGHT_VALUE_AMBIENT_TIME,LIGHT_VALUE_AMBIENT_TIME, LIGHT_VALUE_AMBIENT_TIME};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightColorAmbient, 0);
		        float[] lightColorDiffuse = {LIGHT_VALUE_DIFFUSE_TIME, LIGHT_VALUE_DIFFUSE_TIME, LIGHT_VALUE_DIFFUSE_TIME};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColorDiffuse, 0);
	        } else {
		        float[] lightColorAmbient = {LIGHT_VALUE_AMBIENT_ALL,LIGHT_VALUE_AMBIENT_ALL, LIGHT_VALUE_AMBIENT_ALL};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightColorAmbient, 0);
		        float[] lightColorDiffuse = {LIGHT_VALUE_DIFFUSE_ALL, LIGHT_VALUE_DIFFUSE_ALL, LIGHT_VALUE_DIFFUSE_ALL};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColorDiffuse, 0);
	        }
			
			// update light position when camera moves
			float[] cam = Utils.dir(camera.getPos(), camera.getLookAt());
			float[] cam_1 = new float[]{cam[X], 0, cam[Z]};
			float[] l = Utils.cross(Y_AXIS, cam_1);
			float[] lightPos = new float[]{l[X], Utils.length(l), l[Z]};
			lightPos = Utils.add(camera.getPos(), Utils.mult(Utils.dir(camera.getPos(), lightPos), .4f));
	        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);


	        // PICK
	        if(doPicking || Lasso.state != Lasso.STATE_NONE){
	        	_performPicking(gl);
	        }
	        
	        updateLabelPositionsWithCamera();
	        
	        // If transitions starts, this routine creates displaylists for all slices of the current render list.
	        if(switchToSliceDLMode)
	        {
	        	dlMode = DLMode.SLICE;
	        	sliceDisplayListIndex = -1;
	        	sliceDisplayListIndex = gl.glGenLists(renderedSlices.size()); 
	        	int i = sliceDisplayListIndex;
	        	// CREATE SLICE DISPLAY LISTS
	        	for(Slice<?,?> s : renderedSlices){
		    		createSliceDisplayList(gl, s, i++);
		    	}
	        	switchToSliceDLMode = false;
	        }else if(switchToCubeDLMode ){
	        	dlMode = DLMode.CUBE;
	        	cubeDLIndex = -1;
	        	cubeDLIndex = gl.glGenLists(1);
	        	createCubeDisplayList(gl);
	        	switchToCubeDLMode = false;
	        }

	        
	        if(dlMode == DLMode.CUBE){
	        	gl.glCallList(cubeDLIndex);
	        }
	        else
	        {
	        	// visibleCells does not need to be refreshed when 
	        	// in aniamtion (DLMode.SLICE, DLMode.CUBE) mode.
	        	if(dlMode == DLMode.CELL){
	        		visibleCells.clear();
	        	}
	        	
	        float[] pos;
	    	float angle;
	    	for(Slice<?,?> slice : renderedSlices)
	    	{
	    		gl.glPushMatrix();
				//if (slice instanceof VNodeSlice){
				//	System.out.println("STOP");
				//}
				// Position
				pos = slicePos.get(slice);
				gl.glTranslatef(pos[X], pos[Y], pos[Z]);
				
				// Rotation
				if (sliceRotation.get(slice) == null){
					System.out.println(slice.getLabel());
				}
				angle = sliceRotation.get(slice);
				if(angle != 0){
					if(slice instanceof HNodeSlice)
						gl.glRotated(angle, 0, 0, 1);
					else
						gl.glRotated(angle, 0, 1, 0);
				} 
				
				// Slice frame
				boolean drawFront = false;
				boolean drawBack = false;
				if(frameVisibility || SHOW_FRAMES)
				{
					if(isTimeSMView() 
							|| isVNodeSliceSMView()
							|| rotatedSlices.contains(slice)){
						drawFront = true;	
					}
					else{
						drawBack = renderedSlices.indexOf(slice) == 0;
						drawFront = renderedSlices.indexOf(slice) == renderedSlices.size()-1;
					}
					
					// Wire width
					gl.glLineWidth(FRAME_WIDTH);
					if(drawBack) slice.displayBack(glDrawable);
					
					
					float[] m = new float[16]; 
					gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);
					slice.storeCornerPositions(m);
				}	
				
				
				// DRAW/SHOW CELLS
				if(dlMode == DLMode.CELL){
					displayCellDLMode(gl, slice);
				}else{
				  	gl.glCallList(sliceDisplayListMap.get(slice));
				}
				
				if(frameVisibility){
					slice.displayEdges(glDrawable);
					
					if(drawFront) slice.displayFront(glDrawable);
				}


				float[] labelColor = COLOR_LABELS.clone();
				int strokeWidth = 1;
				if(hoveredSlice == slice || _isAnyCellHovered(slice)) 
				{
					labelColor = COLOR_LABELS_HIGHLIGHT.clone();
				}

				if(!tm.isRunning()){
					_displayLabels2(gl, slice);
				}
				
				
				// DISPLAY ROW AND COLUM LABELS
				float alpha = 0;
				float trans = 1;
				float scale_label = SCALE_LABELS;
				
				if(isTimeSMView() || isVNodeSliceSMView())
				{
					trans = .3f;	
					scale_label = SCALE_LABELS;
					alpha = 0;
				}
				else if(rotatedSlices.contains(slice) 
						|| isFrontView() && isTSliceMode() 
						|| isSourceSideView() && isVSliceMode()
						|| isTargetSideView() && isHSliceMode())
				{
					alpha = 0;				
				}else{
					alpha = _getLabelRotationAngle();
				}	
				
			
				if((slice == hoveredSlice && isCubeView())
					|| selectedTimes.contains(slice.getData())
					|| selectedHNodes.contains(slice.getData())
					|| selectedVNodes.contains(slice.getData()))
				{
					labelColor = COLOR_LABELS_HIGHLIGHT.clone();
					strokeWidth *= HOVER_THICKNESS_FACTOR;	  
					
				}
			

				// Highlight row and/or column is slice is hovered and not in Cubeview
				if(!tm.isRunning())
				{
					int row=-1, col=-1;
					if(isFrontView() || isTimeSMView()){
						if(hoveredSlice != null){
							if(hoveredSlice instanceof VNodeSlice){
								col = matrixCube.getVisibleColumnIndex((CNode) hoveredSlice.getData());
							}else if(hoveredSlice instanceof HNodeSlice){
								row = matrixCube.getVisibleRowIndex((CNode) hoveredSlice.getData());
							}
						}else if ((hoveredCell != null) ) {			//&& visibleCells.contains(hoveredCell)
							row = hoveredCell.getTimeSlice().getRow(hoveredCell);
							col = hoveredCell.getTimeSlice().getColumn(hoveredCell);
						}
					}else if(isSourceSideView() || isVNodeSliceSMView()){
						if(hoveredSlice != null){
							if(hoveredSlice instanceof TimeSlice){
								col = matrixCube.getTimeIndex((CTime) hoveredSlice.getData());
							}else if(hoveredSlice instanceof HNodeSlice){
								row = matrixCube.getVisibleRowIndex((CNode) hoveredSlice.getData());
							}
						}else if ((hoveredCell != null)) {			//  && visibleCells.contains(hoveredCell)
							row = hoveredCell.getVNodeSlice().getRow(hoveredCell);
							col = hoveredCell.getVNodeSlice().getColumn(hoveredCell);
						}
					}else if(isTargetSideView() || isHNodeSliceSMView()){ // TODO VI possible sources of errors
						if(hoveredSlice != null){
							if(hoveredSlice instanceof TimeSlice){
								col = matrixCube.getTimeIndex((CTime) hoveredSlice.getData());
							}else if(hoveredSlice instanceof VNodeSlice){
								row = matrixCube.getVisibleColumnIndex((CNode) hoveredSlice.getData()); // TODO VI possible sources of errors
							}
						}else if ((hoveredCell != null) ) {				// && visibleCells.contains(hoveredCell)
							row = hoveredCell.getHNodeSlice().getRow(hoveredCell);
							col = hoveredCell.getHNodeSlice().getColumn(hoveredCell);
						}
					}
					// if any row or column is hovered, draw corresponding frame
					if(this.rotatedSlices.size() > 0)
						col = -1;
					if(row>-1 || col>-1)
					{
						gl.glLineWidth(.1f);
						gl.glColor3f(.6f,.6f,.6f);
						gl.glDisable(GL2.GL_LIGHTING);
						slice.highlight(glDrawable, row, col);
						gl.glEnable(GL2.GL_LIGHTING);
					}
				}	
								
				int i = matrixCube.getTimeSlices().indexOf(slice);
				if(i > -1 && (timeRangeSlider.getLowValue() > i || timeRangeSlider.getHighValue() <= i))
				{
					trans = trans * .4f;
				}else if(isTimeSMView() || isVNodeSliceSMView()){
					trans = 1f;
				}
				
				
				if(!tm.isRunning()){
					try{
						// Draw slice labels
						float[] posCorr;
						pos = slice.getRightLabelPos();
						if(isCubeView()){
							pos[Y] = rightYLabelPos;
						}
						posCorr = pos.clone();
						if(isCubeView()){
							if(sliceLabelRotationDirChange){
								if(invertYOffset){
									posCorr[Y] -= renderedSlices.indexOf(slice) * (alpha) * SKEW_FACTOR;
								}else{
									posCorr[Y] += renderedSlices.indexOf(slice) * (alpha) * SKEW_FACTOR;
								}
							}else{
								if(invertYOffset){
									posCorr[Y] -= (renderedSlices.size() - renderedSlices.indexOf(slice)) * (alpha) * SKEW_FACTOR;
								}else{
									posCorr[Y] += (renderedSlices.size() - renderedSlices.indexOf(slice)) * (alpha) * SKEW_FACTOR;
								}
							}
							if(sliceLabelRotationDirChange){
								alpha = -alpha;
							}
						}
						Align a = slice.getRightLabelAlign();
						if(invertSliceAlign){
							if(a == Align.LEFT) a = Align.RIGHT;
							else a = Align.LEFT;
						}
							
		    			if(hoveredSlice != slice && isCubeView()) scale_label *= _calculateLabelScaleFactor(posCorr);
						labelBounds.put(_display3DString(gl, posCorr, slice.getLabel(), strokeWidth, a, Align.BOTTOM, trans, labelColor, alpha * ROTATION_FACTOR, scale_label), slice);
						drawLabelLine(gl, pos, posCorr);
					
						
						if(sliceLabelRotationDirChange && isCubeView())
							alpha = -alpha;
						
						pos = slice.getLeftLabelPos();
						if(isCubeView()){
							pos[Y] = leftYLabelPos;
						}
						posCorr = pos.clone();
						if(isCubeView()){
							if(sliceLabelRotationDirChange && isCubeView()){
								if(invertYOffset){
									posCorr[Y] += (renderedSlices.size() - renderedSlices.indexOf(slice)) * (alpha) * .01;
								}else{
									posCorr[Y] -= (renderedSlices.size() - renderedSlices.indexOf(slice)) * (alpha) * .01;
								}
							}else{
								if(invertYOffset && isCubeView()){
									posCorr[Y] += renderedSlices.indexOf(slice) * (alpha) * .01;
								}else{
									posCorr[Y] -= renderedSlices.indexOf(slice) * (alpha) * .01;
								}
							}
							if(sliceLabelRotationDirChange && isCubeView())
								alpha = -alpha;
						}
						
						a = slice.getLeftLabelAlign();
						if(invertSliceAlign){
							if(a == Align.LEFT) a = Align.RIGHT;
							else a = Align.LEFT;
						}
	
						if(hoveredSlice != slice && isCubeView()) scale_label *= _calculateLabelScaleFactor(posCorr);
						labelBounds.put(_display3DString(gl, posCorr, slice.getLabel(), strokeWidth, a, Align.BOTTOM, trans, labelColor, alpha *ROTATION_FACTOR, scale_label), slice);
						drawLabelLine(gl, posCorr, pos);
					}catch(Exception e){
	//					e.printStackTrace();
					}
				}	
			
				gl.glPopMatrix();
	    	}
	        }
	        
	        drawLassoPolygon(gl);
	        
	        // Show settings on left upper screen corner
	      	displayStatistics(gl);   
		   
	       
	        // DRAW LABEL OF HOVERED CELL
	        if((hoveredCell != null) && visibleCells.contains(hoveredCell))
	        {
	        	
	        	float[] p = camera.modelToScreen(gl, glu, hoveredCell.getAbsolutePos());
	        	p = Utils.add(p, new float[]{110,80});
	        	begin2D(gl, glu, (int) width, (int) height);
		    	GLUT glut = new GLUT();
		    	gl.glColor4f(.2f, .2f, .2f, .9f);

		    	
		    	int num = 0;
		    	float weight = 0;
		    	float accWeight = 0;
		    	float min = 1000000;
		    	float max = 0;
		    	float calcMin = 1000000;
		    	float calcMax = 0;
		    	ArrayList<CEdge> minEdges = new ArrayList<>();
		    	ArrayList<CEdge> maxEdges = new ArrayList<>();
		    	if(isFrontView()){
		    		CNode s = tGraph.getSource(hoveredCell.getData());
		    		CNode t = tGraph.getTarget(hoveredCell.getData());
		    		for(CEdge e : tGraph.getEdges(s, t)){
		    			//System.out.println(e.getID());
		    			if(visibleCells .contains(matrixCube.getCell(e))){
		    				if (alignmentMode == AlignmentMode.SIMILARITY_VALUES){
		    					accWeight += e.getWeight();
		    					weight = e.getWeight();
		    				}
		    				else if (alignmentMode == AlignmentMode.SUM_MAPPINGS){
		    					accWeight += e.getAccumulatedWeight();
		    					weight = e.getAccumulatedWeight();
		    				}
		    				
		    				if(weight != 0) 		
		    				{
		    					if (weight < calcMin) {
		    						calcMin = weight;
		    						minEdges.clear();
		    						minEdges.add(e);
		    					} else if (weight == calcMin) {
		    						minEdges.add(e);
		    					}
		    					
		    					//min = Math.min(min,weight);
		    					if (weight > calcMax) {
		    						calcMax = weight;
		    						maxEdges.clear();
		    						maxEdges.add(e);
		    					} else if (weight == calcMax) {
		    						maxEdges.add(e);
		    					}
		    					//max = Math.max(max,weight);
		    				}
		    				num++;
		    			}
		    		}
		    		num /= 2;
		    		accWeight /= 2; // Since edges are directed by nevertheless symmetric by definiton in this context.
		    		//System.out.println("accweight: " + accWeight);
		    		//System.out.println("num: " + num);
		    		//System.out.println("max: " + max + "calcMax: " + calcMax);
		    		//System.out.println("min: " + min + "calcMin: " + calcMin);
		    	}
		    	else if(isSourceSideView()){
//		    		CNode n = tGraph.getSource(hoveredCell.getData());
//		    		CTime t = hoveredCell.getTimeSlice().getData();
//		    		for(CEdge e : tGraph.getGraph(t).getOutEdges(n)){
//		    			System.out.println(e.getID());
//		    			if(visibleCells .contains(matrixCube.getCell(e))){
//		    				if (alignmentMode == AlignmentMode.SIMILARITY_VALUES){
//		    					accWeight += e.getWeight();
//		    					weight = e.getWeight();
//		    				}
//		    				else if (alignmentMode == AlignmentMode.SUM_MAPPINGS){
//		    					accWeight += e.getAccumulatedWeight();
//		    					weight = e.getAccumulatedWeight();
//		    				}
//		    				
//		    				if(weight != 0) 		
//		    				{
//		    					if (weight < calcMin) {
//		    						calcMin = weight;
//		    						minEdges.clear();
//		    						minEdges.add(e);
//		    					} else if (weight == calcMin) {
//		    						minEdges.add(e);
//		    					}
//		    					
//		    					//min = Math.min(min,weight);
//		    					if (weight > calcMax) {
//		    						calcMax = weight;
//		    						maxEdges.clear();
//		    						maxEdges.add(e);
//		    					} else if (weight == calcMax) {
//		    						maxEdges.add(e);
//		    					}
//		    					//max = Math.max(max,weight);
//		    				}
//		    				num++;
//		    			}
//
//		    		}
//
//		    		//num /= 2;
//		    		//accWeight /= 2; // Since edges are directed by nevertheless symmetric by definiton in this context.
//		    		System.out.println("accweight: " + accWeight);
//		    		System.out.println("num: " + num);
//		    		System.out.println("max: " + max + "calcMax: " + calcMax);
//		    		System.out.println("min: " + min + "calcMin: " + calcMin);
		    	}
//		    	else if(isTargetSideView()){ 		    	// TODO VI possible sources of errors
//		    		w = 0;
//		    		CNode n = tGraph.getTarget(hoveredCell.getData());
//		    		CTime t = hoveredCell.getTimeSlice().getData();
//		    		for(CEdge e : tGraph.getGraph(t).getInEdges(n)){ // TODO VI possible sources of errors
//		    			if(visibleCells .contains(matrixCube.getCell(e))){
//			    			w += e.getWeight();
//			    			num++;
//			    		}
//		    		}
//		    	}
//		    	glut.glutBitmapString(CubixVis.FONT_12, "Edges:" + num);
//				gl.glWindowPos3f(p[0]+20, p[1]+10, 0);
//			    glut.glutBitmapString(CubixVis.FONT_12, "Weight: "+ String.format("%.5f",w));
//		    	end2D(gl);
		    	
		    	//gl.glWindowPos3f(p[0]+20, p[1]+10, 0);
		    	if (isFrontView()){
			    	ArrayList<CTime> times = tGraph.getTimes();
			    	String minString = "";
			    	String maxString = "";
					for (int i = 0; i < times.size(); i++){
						
						for (int j = 0; j < minEdges.size(); j++) {
							if(tGraph.hasEdge(minEdges.get(j), times.get(i))){
								if(!minString.contains(times.get(i).getLabel())) {
									minString = minString.concat(times.get(i).getLabel()).concat(";");
								}
							}
						}
						for (int j = 0; j < maxEdges.size(); j++) {
							if(tGraph.hasEdge(maxEdges.get(j), times.get(i))){
								if (!maxString.contains(times.get(i).getLabel())) {
									maxString = maxString.concat(times.get(i).getLabel()).concat(";");
								}
							}
						}
			    	}
			    	gl.glRectd(p[0]-85, p[1]-70, p[0]+20+200+400, p[1]+25);
			    	gl.glColor4f(.9f, .9f, .9f, 1);		
			    	gl.glWindowPos3f(p[0]-70, p[1]-60, 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "Min: " + calcMin);
		    		glut.glutBitmapString(CubixVis.FONT_18, " from: " + minString);
		    		gl.glWindowPos3f(p[0]-70, p[1]-40, 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "Max: " + calcMax);
		    		glut.glutBitmapString(CubixVis.FONT_18, " from: " + maxString);
		    		gl.glWindowPos3f(p[0]-70, p[1]-20, 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "# of mappings:" + num);
		    		gl.glWindowPos3f(p[0]-70, p[1], 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "aggregated weight: " + accWeight);
		    	} else if (!isSourceSideView()) {
			    	CEdge cellOwner = hoveredCell.getData();
			    	HierarchicalCNode sourceN = (HierarchicalCNode) tGraph.getSource(cellOwner);
			    	HierarchicalCNode targetN = (HierarchicalCNode) tGraph.getTarget(cellOwner);
			    	ArrayList<CTime> times = tGraph.getTimes();
			    	String time = "";
					for (int i = 0; i < times.size(); i++){
			    		if (tGraph.hasEdge(cellOwner, times.get(i)))
			    			time = times.get(i).getLabel();
			    	}
			    	gl.glRectd(p[0]-85, p[1]-70, p[0]+20+200, p[1]+25);
			    	gl.glColor4f(.9f, .9f, .9f, 1);		
			    	gl.glWindowPos3f(p[0]-70, p[1]-60, 0);		    		glut.glutBitmapString(CubixVis.FONT_18, "X-axis: " + targetN.getLabel());
		    		gl.glWindowPos3f(p[0]-70, p[1]-40, 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "Y-axis: " + sourceN.getLabel());
		    		gl.glWindowPos3f(p[0]-70, p[1]-20, 0);
		    		glut.glutBitmapString(CubixVis.FONT_18, "matcher: " + time);
		    		gl.glWindowPos3f(p[0]-70, p[1], 0);
					if (alignmentMode == AlignmentMode.SUM_MAPPINGS){
			    		glut.glutBitmapString(CubixVis.FONT_18, "# of mappings: " + cellOwner.getAccumulatedWeight());
					}
			    	else {
			    		glut.glutBitmapString(CubixVis.FONT_18, "sim. value: " + cellOwner.getWeight());
			    	}
		    	}

			    end2D(gl);
	        }
	        
	        
	        
	        cubelet.draw(gl, glu, 70, height - 70);
	        	
	        gl.glFlush();
	    }
	    
	    

		/** Iterates the slices and draws each cell in its own displayList */
	    protected void displayCellDLMode(GL2 gl, Slice slice)
	    {
	    	// DRAW CELLS INSIDE SLICES
				Cell c;
				if(isVSliceMode()){
					for(int r=slice.getRowCount()-1 ; r>=0 ; r--)
					{
						for(int q=0 ; q < slice.getColumnCount() ; q++)
						{
							c = slice.getCell(r, q);
							if(c == null) continue;
							_displayCell(gl, c, c.getRelVNodeSlicePos());
						}
					}
				}else if(isHSliceMode()){
					for(int r=slice.getRowCount()-1 ; r >= 0 ; r--)
					{
						for(int q=0 ; q < slice.getColumnCount() ; q++)
						{
							c = slice.getCell(r, q);
							if(c == null) continue;
							_displayCell(gl, c, c.getRelHNodeSlicePos());
						}
					}
				}else{
					for(int r=slice.getRowCount()-1 ; r >= 0 ; r--)
					{							
						for(int q=slice.getColumnCount()-1 ; q >= 0 ; q--)
						{
							c = slice.getCell(r, q);
							if(c == null){ continue;}
							
							_displayCell(gl, c, c.getRelTimeSlicePos());
						}
					}
				}
	    }
	  
	    protected void createSliceDisplayList(GL2 gl, Slice<?,?> s, int dlIndex)
	    {
	    	Cell c;
	    	float[] color;
	    	float transparency;
	    	float[] cPos = new float[]{};
		    	
	    	float scale;
	    	sliceDisplayListMap.put(s, dlIndex);

	    	gl.glNewList(dlIndex, GL2.GL_COMPILE);
	    		for(Object o : s.getCells())
	    		{
	    			if(!visibleCells.contains(o)) continue;
	    			c = (Cell)o;

	    			// Color
	    			color = cellColor.get(c);
	    			if(selectedCell == c){
						color = COLOR_CELL_SELECTION;
					}else if(coloredSlices.containsKey(c.getTimeSlice())){
						color = coloredSlices.get(c.getTimeSlice());
						Utils.print("[CubixVis]", color);
					}else if(coloredSlices.containsKey(c.getHNodeSlice())){
						color = coloredSlices.get(c.getHNodeSlice());
						Utils.print("[CubixVis]", color);
					}else if(coloredSlices.containsKey(c.getVNodeSlice())){
						color = coloredSlices.get(c.getVNodeSlice());
						Utils.print("[CubixVis]", color);
					}else if(colorEncoding == ColorEncoding.WEIGHT || colorEncoding == ColorEncoding.WEIGHT_DIV){
						if(this.weightAdaption)
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1));
						else
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 0.1, 1));
					}else if(colorEncoding == ColorEncoding.NONE){
						color = COLOR_CELL;
					}
	
	    			transparency = c.getTransparency();
		    		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{color[0], color[1], color[2], transparency}, 0);
					gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{color[0]+.1f, color[1]+.1f, color[2]+.1f, transparency}, 0);							

					// Scale
					
					// Position
					if(s instanceof TimeSlice) cPos = c.getRelTimeSlicePos();
					if(s instanceof VNodeSlice) cPos = c.getRelVNodeSlicePos();
					if(s instanceof HNodeSlice) cPos = c.getRelHNodeSlicePos();
						
					scale = c.getScale();
					if(shapeEncoding == ShapeEncoding.CONE)
						c.createGeometry(gl, cPos, scale, scale, .95f);
					else 
						c.createGeometry(gl, cPos, scale, scale, scale);
					
	    		}
	    	gl.glEndList();
	    }
	    
	    protected void createCubeDisplayList(GL2 gl)
	    {
//	    	Log.out(this, "createCubeDisplayList");
	
    		Cell c;
    		float transparency;
    		float scale;
    		float[] color;
    		gl.glNewList(cubeDLIndex, GL2.GL_COMPILE);
	    	for(Slice<?,?> s : renderedSlices)
    		{
	    		for(Object o : s.getCells())
    			{
	    			if(!visibleCells.contains(o)) continue;
	    			c = (Cell)o;
    				// Color
	    			
	    			color = cellColor.get(c);
	    			if(selectedCell == c){
						color = COLOR_CELL_SELECTION;
					}else if(coloredSlices.containsKey(c.getTimeSlice())){
						color = coloredSlices.get(c.getTimeSlice());
						Utils.print("[CubixVis]", color);
					}else if(coloredSlices.containsKey(c.getHNodeSlice())){
						color = coloredSlices.get(c.getHNodeSlice());
						Utils.print("[CubixVis]", color);
					}else if(coloredSlices.containsKey(c.getVNodeSlice())){
						color = coloredSlices.get(c.getVNodeSlice());
						Utils.print("[CubixVis]", color);
					}else if(colorEncoding == ColorEncoding.WEIGHT || colorEncoding == ColorEncoding.WEIGHT_DIV){
						if(this.weightAdaption)
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1));
						else
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 0.1, 1));
					}else if(colorEncoding == ColorEncoding.NONE){
						color = COLOR_CELL;
					}
    				transparency = c.getTransparency();
    				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{color[0], color[1], color[2], transparency}, 0);
    				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{color[0]+.1f, color[1]+.1f, color[2]+.1f, transparency}, 0);							
    				
    				// Scale
    				scale = c.getScale();
    				
    				// Position
    				scale = c.getScale();
					if(shapeEncoding == ShapeEncoding.CONE)
						c.createGeometry(gl, c.getAbsolutePos(), scale, scale, .95f);
					else 
						c.createGeometry(gl, c.getAbsolutePos(), scale, scale, scale);
    			}
    		}
    		gl.glEndList();

	    }
	    
	    
	    
	    
	    
	    protected void _calculateLabelOffsets(GL2 gl)
	    {
	    	// Get cube corner positions
	    	float[][][] points = new float[4][2][3]; 
	    	int N = 0,S = 1,R = 2,L = 3;
	 
	    	int l = renderedSlices.size()-1;
	    	points[N][0] = renderedSlices.get(0).getCornerPos(0);
	    	points[N][1] = renderedSlices.get(0).getCornerPos(1);
	    	points[S][0] = renderedSlices.get(l).getCornerPos(7);
	    	points[S][1] = renderedSlices.get(l).getCornerPos(6);
	    	points[R][0] = renderedSlices.get(0).getCornerPos(1);
	    	points[R][1] = renderedSlices.get(l).getCornerPos(5);
	    	points[L][0] = renderedSlices.get(0).getCornerPos(3);
	    	points[L][1] = renderedSlices.get(l).getCornerPos(7);

	    	// Calculate slope and
	    	// Calculate label rotation
	    	float[][] vec = new float[4][2];
	    	float slope;
	    	yLabelOffsetFactor = new float[4];
	    	zLabelOffsetFactor = new float[4];
			labelRotation = new float[4];
		    int i=0;
		    float yMax, zMax;
		    float length, h; 
	    	for(float[][] pp : points){
	    		vec[i] = Utils.dir(camera.modelToScreen(gl, glu, pp[0]), camera.modelToScreen(gl, glu, pp[1]));
	    		slope = vec[i][1] / vec[i][0]; 
	    		
	    		
//	    		if(slope[i] <= 1){
//	    			yMax = Utils.length(Utils.dir(points[i][0], points[i][1])) / 3f;
//	    			yLabelOffsetFactor[i] = yMax - slope[i] * yMax;
//	    			yLabelOffsetFactor[i] = Math.max(0, Math.min(yLabelOffsetFactor[i], yMax));
//	    			zLabelOffsetFactor[i] = 0;
//	    		}else{
//	    			zMax = Utils.length(Utils.dir(points[i][0], points[i][1])) / 3f;
//	    			zLabelOffsetFactor[i] =	zMax - (vec[i][0] / vec[i][1]) * zMax;
//	    			zLabelOffsetFactor[i] = -Math.max(0, Math.min(zLabelOffsetFactor[i], zMax));
//		    		yLabelOffsetFactor[i] = 0;
//	    		}

	    		length = Utils.length(Utils.dir(points[i][0], points[i][1]));
    			yMax = length / 3f;
    			yLabelOffsetFactor[i] = yMax - slope * yMax;
    			yLabelOffsetFactor[i] = Math.max(0, Math.min(yLabelOffsetFactor[i], yMax));
    			zLabelOffsetFactor[i] = 0;

    			h = Utils.dir(camera.modelToScreen(gl, glu, pp[0]), camera.modelToScreen(gl, glu, pp[1]))[1];
    			int fontsize = 15;
//    			if(h < fontsize* length){
//    				yLabelOffsetFactor[i] += (fontsize*length - h); 
//    				labelRotation[i] = 0;
//    			}else 
    				if(slope < 1){
    				labelRotation[i] = (float) Map.map(Utils.getDeg((float) Math.atan(slope)), 0, 30, 90, 0);
    				labelRotation[i] = Math.max(0, Math.min(90, labelRotation[i]));
    				if(i == N) labelRotation[i] = -labelRotation[i];
    				if(i == S) labelRotation[i] = -labelRotation[i];
    				if(i == R) labelRotation[i] = labelRotation[i];
    				if(i == L) labelRotation[i] = labelRotation[i];
    			}

    			i++;
	    	}
	    	
	   	
	    	// Calculate label y-offset
	    }
	    
	    
	    protected void _displayCell(GL2 gl, Cell c, float[] pos)
	    {
	    	
	    	//if (c.getData().getID().contains("Organization"))
	    	//	System.out.println("Event found ...");
	    		
	    	if(!inverseFilterCheckBox.isSelected() && !cellsInWeightRange.contains(c))
				return;
	    	else if(inverseFilterCheckBox.isSelected() && cellsInWeightRange.contains(c))
	    		return; 
	    	
	    	if(!isInVisibleCategory(c))
	    		return;
	    	
	    	if(Lasso.cells.size() > 0  && !Lasso.cells.contains(c))
	    		return;
	    	
	    	float transparency = (float) this.opacityRangeSlider.getHighValue();
	    	if(hoveredSlice == null && transparency == 0)
				return;
	    	
	    	// VI we don't show cells for nodes when they have no children to participate in mappings
	    	if ((alignmentMode == AlignmentMode.SUM_MAPPINGS) && (c.getData().getAccumulatedWeight() == 0))
	    		return;

	    	// VI we don't show cells for nodes when they have no mapping between them
	    	if ((alignmentMode == AlignmentMode.SIMILARITY_VALUES) && (c.getData().getWeight() == 0))
	    		return;

	    	boolean inTime = timeSliderTimes.contains(c.getTimeSlice().getData());
	    	if(!inTime && selectedTimes.size() > 0)
	    		inTime = selectedTimes.contains(c.getTimeSlice().getData());

	    	boolean inHNode = selectedHNodes.size() == 0;
	    	if(!inHNode) inHNode = selectedHNodes.contains(c.getHNodeSlice().getData());
	    	
	    	boolean inVNode = selectedVNodes.size() == 0;
	    	if(!inVNode) inVNode = selectedVNodes.contains(c.getVNodeSlice().getData());
	    		
	    	if(hoveredSlice != null)
	    	{
	    		if(hoveredSlice instanceof TimeSlice)
	    		{
	    			if(hoveredSlice.containsCell(c) || isSourceSideView() || isTargetSideView() || isTimeSMView() || isVNodeSliceSMView()){
	    				inTime = true;
	    			}else{
	    				if( (selectedTimes.contains(hoveredSlice.getData())
	    					|| timeSliderTimes.contains(hoveredSlice.getData()))){
	    						inTime = false;
	    				}
	    			}
	    		}else if(hoveredSlice instanceof HNodeSlice)
	    		{
	    			if(hoveredSlice.containsCell(c) || !isCubeView()){
	    				inHNode = true;
	    			}else{
	    				inHNode = selectedHNodes.size() > 0 && selectedHNodes.contains(c.getHNodeSlice().getData());
	    			}
	    		}else{
	    			if(hoveredSlice.containsCell(c) || isFrontView() || isTimeSMView() || isVNodeSliceSMView()){
	    				inVNode = true;
	    			}else{
	    				inVNode = selectedVNodes.size() > 0 && selectedVNodes.contains(c.getVNodeSlice().getData());
	    			}
	    		}
	    	}
	    	else{
				inHNode = selectedHNodes.size() == 0 || selectedHNodes.contains(c.getHNodeSlice().getData());
				inVNode = selectedVNodes.size() == 0 || selectedVNodes.contains(c.getVNodeSlice().getData());
				int t = matrixCube.getTimeIndex(c.getTimeSlice().getData());
				if(selectedCell == null)
					inTime = (selectedTimes.size() > 0 && selectedTimes.contains(c.getTimeSlice().getData())) || (t >= timeRangeSlider.getLowValue() && t < timeRangeSlider.getHighValue());
				else{
					inTime = selectedTimes.size() == 0 || selectedTimes.contains(c.getTimeSlice().getData());
					if(isFrontView() && queryMode == Query.SLICES){
						inHNode = false;
						inVNode = false;
					}else if(isSourceSideView() && queryMode == Query.SLICES){
						inTime = false;
						inHNode = false;
					}else if(isTargetSideView() && queryMode == Query.SLICES){
						inTime = false;
						inVNode = false;
					}
				}
	    	}
	    		    	
	    	switch(queryMode){
	    	case CELLS : if(!(inTime && inHNode && inVNode)) transparency = (float) opacityRangeSlider.getLowValue(); break;
	    	case VECTORS : if(!((inTime && inHNode) || (inTime && inVNode) || (inVNode && inHNode))) transparency = (float) opacityRangeSlider.getLowValue(); break;
	    	case SLICES : if(!(inTime || inHNode || inVNode)) transparency = (float) opacityRangeSlider.getLowValue(); break;
	    	}
	    		
	    	if(transparency == 0){
	    		return;
	    	}

	    	c.setTranslucency(transparency);
	    	visibleCells.add(c);
	    		
	    	gl.glPushMatrix();		

	    	if(isFrontView() && camera.getViewAngle() == 0.01f && (colorEncoding == ColorEncoding.WEIGHT  || colorEncoding == ColorEncoding.WEIGHT_DIV)) // && this.colorEncoding == ColorEncoding.WEIGHT )
	    	{
	    		// VI add accumulated weight; 
	    		if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
	    			pos[Z] = (float) (Map.map(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 10,-10) - (-5 + matrixCube.getTimeIndex(c.getTimeSlice().getData())));
	    		else
	    			pos[Z] = (float) (Map.map(c.getData().getAccumulatedWeight(), ACCUMULATED_WEIGHT_MIN, ACCUMULATED_WEIGHT_MAX, 10,-10) - (-5 + matrixCube.getTimeIndex(c.getTimeSlice().getData())));
	    	}
	
	    	gl.glTranslatef(pos[X], pos[Y], pos[Z]);
			// SCALE
			float cellScale = 1;
			if(shapeEncoding != ShapeEncoding.NONE){
				if(weightAdaption){
//					if(c.getData().isWeight2Valid()){
//						cellScale = (float) Map.map(Math.max(c.getData().getWeight(), c.getData().getWeight2()), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0,1);
//					}else 
					if(logScale){
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							cellScale = (float) Map.mapLog(c.getData().getWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1);
						else
							cellScale = (float) Map.mapLog(c.getData().getAccumulatedWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1);
					}
					else {
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							cellScale = (float) Map.map(c.getData().getWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1);
						else
							cellScale = (float) Map.map(c.getData().getAccumulatedWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1);
					}
				}else{
//					if(c.getData().isWeight2Valid()){
//						cellScale = (float) Map.map(Math.max(c.getData().getWeight(), c.getData().getWeight2()), WEIGHT_MIN, WEIGHT_MAX, 0,1);
//					}else
					if(logScale){
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							cellScale = (float) Map.mapLog(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 0.1, 1);
						else
							cellScale = (float) Map.mapLog(c.getData().getAccumulatedWeight(), ACCUMULATED_WEIGHT_MIN, ACCUMULATED_WEIGHT_MAX, 0.1, 1);
					}
					else if(divergingScale){
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							cellScale = (float) Map.map(Math.abs(c.getData().getWeight()), 0, WEIGHT_MAX, 0.1, 1);
						else
							cellScale = (float) Map.map(Math.abs(c.getData().getAccumulatedWeight()), 0, ACCUMULATED_WEIGHT_MAX, 0.1, 1);
					}
					else {
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							cellScale = (float) Map.map(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 0.1, 1);
						else
							cellScale = (float) Map.map(c.getData().getAccumulatedWeight(), ACCUMULATED_WEIGHT_MIN, ACCUMULATED_WEIGHT_MAX, 0.1, 1);
					}
				}
			}
//			cellScale *= .95f;
			c.setScale(cellScale);
			if(shapeEncoding == ShapeEncoding.CONE)
				gl.glScalef(cellScale, cellScale, .95f);						
			else 
				gl.glScalef(cellScale, cellScale, cellScale);						
			
			// Save absolute cell positions for ray picking:
			float[] matrix = new float[16]; 
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, matrix, 0);
			float[] absPos = new float[3];
			absPos[X] = matrix[12];
			absPos[Y] = matrix[13];
			absPos[Z] = matrix[14];
			c.setAbsolutePos(absPos);

			// CELL COLOR
			float[] color = cellColor.get(c);
			if(hoveredCell == c){
				color = COLOR_CELL_HOVER;
			}else if(selectedCell == c){
				color = COLOR_CELL_SELECTION;
			}else if(coloredSlices.containsKey(c.getTimeSlice())){
				color = coloredSlices.get(c.getTimeSlice());
				Utils.print("[CubixVis]", color);
			}else if(coloredSlices.containsKey(c.getHNodeSlice())){
				color = coloredSlices.get(c.getHNodeSlice());
				Utils.print("[CubixVis]", color);
			}else if(coloredSlices.containsKey(c.getVNodeSlice())){
				color = coloredSlices.get(c.getVNodeSlice());
				Utils.print("[CubixVis]", color);
			}else if(colorEncoding == ColorEncoding.WEIGHT || colorEncoding == ColorEncoding.WEIGHT_DIV){
//				if(!c.getData().isWeight2Valid()){
					if(weightAdaption){
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1, 1));
						else 
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getAccumulatedWeight(), ACCUMULATED_WEIGHT_MIN, ACCUMULATED_WEIGHT_MAX, 0.1, 1));
					}
					else{
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) 
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getWeight(), WEIGHT_MIN, WEIGHT_MAX, 0.1, 1));
						else
							color = _getColor(currentEdgeWeightColorScale,  (float)Map.map(c.getData().getAccumulatedWeight(), ACCUMULATED_WEIGHT_MIN, ACCUMULATED_WEIGHT_MAX, 0.1, 1));
					}
//				}else{
//					color = new float[]{0,.1f,0};
//					if(weightAdaption){
//						color = _getBiColor(c.getData(), weightRangeSlider.getLowValue(), weightRangeSlider.getHighValue(), 0.1f, 1);
//					}else{
//						color = _getBiColor(c.getData(), WEIGHT_MIN, WEIGHT_MAX, 0.1f, 1);
//					}
//				}
			}else if(colorEncoding == ColorEncoding.NONE){
				color = COLOR_CELL;
			}
			
			if(Utils.length(Utils.dir(currentCellColor,color)) != 0 || currentCellTransparency != transparency){
				currentCellColor  = color.clone();
				currentCellTransparency  = transparency;
				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{color[0], color[1], color[2], transparency}, 0);
				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{color[0]+.1f, color[1]+.1f, color[2]+.1f, transparency}, 0);							
			}
			
			c.display(gl);
	
			gl.glPopMatrix();
	    }
	    
	    
	    protected void drawLassoPolygon(GL2 gl)
	    {
	    	if(Lasso.points.size() > 2)
	    	{
//	    		Log.out(this, "\t draw lasso " + Lasso.points.size());
	    		gl.glDisable(GL2.GL_LIGHTING);
	    		gl.glColor4f(.7f, .7f, .7f, .3f);
	    		gl.glBegin(GL2.GL_TRIANGLE_FAN);
	    		gl.glVertex3fv(Lasso.points.get(0),0);
	    		for(int i=0 ; i < Lasso.points.size()-2 ; i++)
	    		{
	    			gl.glVertex3fv(Lasso.points.get(i+1),0);
	    			gl.glVertex3fv(Lasso.points.get(i+2),0);
	    		}		 
	    		gl.glEnd();
	    		gl.glEnable(GL2.GL_LIGHTING);
	    	}
	    }
	    
	    
	    protected void _displayLabels2(GL2 gl, Slice<?,?> s)
	    {
	    	float[] pos;
	    	int row, col;
	    	float[] offset = new float[]{0,0,0}; 
	    	float[] translucency_label = labelTransparency.get(s);
	    	float[] labelColor;
	    	float thickness;
	    	Slice<?,?> s2 = null;	
	    	Align wAlign, hAlign;
	    	
	    	//NORTH
	    	if(translucency_label[N] > 0)
	    	{
	    		offset = northOffset.get(s);
	    		if(offset == null || !isCubeView()) 	
	    			offset = currentView.getNorthLabelOffset(isTSliceMode());
	    		
	    		for(col = 0 ; col < s.getColumnCount() ; col++)
	    		{
	    			if(s instanceof TimeSlice){
	    				s2 = matrixCube.getVisibleVNodeSlice(col);  
	    			}else if(s instanceof VNodeSlice){
	    				s2 = matrixCube.getTimeSlice(col);
	    			}else if(s instanceof HNodeSlice){
	    				s2 = matrixCube.getTimeSlice(col);
	    			}
	    	
	    			gl.glPushMatrix();
	    			pos = s.getRelGridCoords(0, col).clone();
	    			gl.glTranslatef(pos[X], pos[Y], pos[Z]);
	    			gl.glPopMatrix();
	    		
	    			labelColor = COLOR_LABELS.clone();
	    			thickness = 1;
	    			
	    			// VI adjust the labels to represent the is-a hierarchy; 
	    			HierarchicalCNode node = null;
	    			if (s2.getData() instanceof CNode)
	    				node = (HierarchicalCNode) s2.getData();
	    			if (node != null) {
		    			int nodeDepth = node.getNodeDepth();
		    			pos[Y] += (nodeDepth * LABEL_LEVEL_INDENT);  
	    			}	    			
	    			
	    			hAlign = Align.CENTER;
	    			wAlign = Align.LEFT;
	    			if(isCubeView() && isTSliceMode()) 
	    				wAlign = Align.RIGHT; 
	    			
	    			
	    			pos = Utils.add(pos, offset);
	    			float[] posCorr = pos.clone();

	    			float alpha = -90f / ROTATION_FACTOR;
	    			if(isCubeView()){
	    				alpha = _getLabelRotationAngle();
	    				if(orderChange)
	    					posCorr[Y] += (s.getColumnCount() - col) * alpha * SKEW_FACTOR;
	    				else
	    					posCorr[Y] += col * alpha * SKEW_FACTOR;
		    				
	    				hAlign = Align.BOTTOM;
	    				if(rotationChange){
	    					alpha = -alpha;
	    					wAlign = Align.LEFT;
	    				}	    
	    			}
	    			
	    			float scale = SCALE_LABELS;
	    			if(hoveredSlice == s2 
	    			|| (s2.containsCell(hoveredCell) && visibleCells.contains(hoveredCell))
	    			|| selectedVNodes.contains(s2.getData())
	    			|| selectedTimes.contains(s2.getData()))
	    			{
	    				labelColor = COLOR_LABELS_HIGHLIGHT.clone();
	    				thickness *= HOVER_THICKNESS_FACTOR;	    
	    			}else if(isCubeView()){
	    				scale *= _calculateLabelScaleFactor(posCorr);
	    			}else if((isTimeSMView() || isVNodeSliceSMView()) && Utils.dist(camera.getLookAt(), camera.getPos()) > 65000){
	    				continue;
	    			}
	    				    			
	    			String label = s2.getLabel();
	    			if (node != null) {
		    			if (node.getChildren().size() > 0){
		    				if (node.isExpanded()){
			    				label = s2.getLabel() + " >";
		    				} else {
			    				label = s2.getLabel() + " >>";
		    				}
		    			} else {
		    				label = s2.getLabel();
		    			}	    				
	    			}
	    			labelBounds.put(_display3DString(gl, posCorr, label, thickness, wAlign, hAlign, translucency_label[N], labelColor, -alpha * ROTATION_FACTOR, scale),s2);
//	    			labelBounds.put(_display3DString(gl, posCorr, s2.getLabel(), thickness, wAlign, hAlign, translucency_label[N], labelColor, alpha, scale),s2);
	    			drawLabelLine(gl, posCorr, pos);
	    		}
	    	}
	    	

	    	// EAST
	    	if(translucency_label[E] > 0)
	    	{
	    		offset = this.eastOffset.get(s);
	    		if(offset == null)
	    			offset = currentView.getEastLabelOffset(isTSliceMode());
	    		for(row = 0 ; row < s.getRowCount() ; row++)
	    		{
	    			gl.glPushMatrix();
	    			pos = s.getRelGridCoords(row, s.getColumnCount()-1).clone();
	    			gl.glTranslatef(pos[X], pos[Y], pos[Z]);
	    			gl.glPopMatrix();
	    			
	    			labelColor = COLOR_LABELS.clone();
	    			thickness = 1;
	    			if(s instanceof TimeSlice || s instanceof VNodeSlice){
	    				s2 = matrixCube.getVisibleHNodeSlice(row);
	    			} 
	    			else if (s instanceof HNodeSlice){
	    				s2 = matrixCube.getVisibleVNodeSlice(row);
	    			}
	    			
	    			// VI adjust the labels to represent the is-a hierarchy; 
	    			HierarchicalCNode node = (HierarchicalCNode) s2.getData();
	    			int nodeDepth = node.getNodeDepth();
	    			if (isSourceSideView() || isVNodeSliceSMView()){
		    			pos[Z] += (nodeDepth * LABEL_LEVEL_INDENT);  
	    			} else {
		    			pos[X] += (nodeDepth * LABEL_LEVEL_INDENT);  
	    			}
	    			
	    			float scale = SCALE_LABELS;
	    			if(hoveredSlice == s2 
	    			|| (s2.containsCell(hoveredCell) && visibleCells.contains(hoveredCell))
	    	    	|| selectedHNodes.contains(s2.getData()))
	    			{
	    				labelColor = COLOR_LABELS_HIGHLIGHT.clone();
	    				thickness *= HOVER_THICKNESS_FACTOR;	    					
	    			}else if(isCubeView()){
	    				scale *= _calculateLabelScaleFactor(pos);
	    			}else if((isTimeSMView() || isVNodeSliceSMView()) && Utils.dist(camera.getLookAt(), camera.getPos()) > 65000){
	    				continue;
	    			}

	    			wAlign = this.eastAlign.get(s);
	    			if(wAlign == null) 	
	    				wAlign = Align.LEFT;
	    			pos = Utils.add(pos, offset);
	    			float[] posCorr = pos.clone();
	    			
	    			String label = s2.getLabel();
	    			if (node.getChildren().size() > 0){
	    				if (node.isExpanded()){
		    				label = s2.getLabel() + " >";
	    				} else {
		    				label = s2.getLabel() + " >>";
	    				}
	    			} else {
	    				label = s2.getLabel();
	    			}
					labelBounds.put(_display3DString(gl, posCorr, label, thickness, wAlign, Align.CENTER, translucency_label[E], labelColor, 0, scale),s2);

	    		}
	    	}

	    	
	     	// SOUTH
	    	if(translucency_label[S]> 0)
	    	{
	    		offset = southOffset.get(s);
	    		if(offset == null)
	    			offset = currentView.getSouthLabelOffset(isTSliceMode());
	    		for(col = 0 ; col < s.getColumnCount() ; col++)
	    		{
	    			gl.glPushMatrix();
	    			pos = s.getRelGridCoords(s.getRowCount()-1, col).clone();
	    			gl.glTranslatef(pos[X], pos[Y], pos[Z]);
	    			gl.glPopMatrix();
	    			
	    			labelColor = COLOR_LABELS.clone();
	    			thickness = 1;
	    			if(s instanceof TimeSlice){
	    				s2 = matrixCube.getVisibleVNodeSlice(col); 
	    			}else if(s instanceof VNodeSlice){
	    				s2 = matrixCube.getTimeSlice(col);
	    			}else if(s instanceof HNodeSlice){
	    				s2 = matrixCube.getTimeSlice(col);
	    			}

	    			// VI adjust the labels to represent the is-a hierarchy;  
	    			HierarchicalCNode node = null;
	    			if (s2.getData() instanceof CNode)
	    				node = (HierarchicalCNode) s2.getData();
	    			if (node != null) {
		    			int nodeDepth = node.getNodeDepth();
		    			pos[Y] += -(nodeDepth * LABEL_LEVEL_INDENT);  
	    			}

	    			hAlign = Align.CENTER;
	    			wAlign = Align.LEFT;
	    			if(isCubeView() && isHSliceMode()) 
	    				wAlign = Align.RIGHT; 
	    			

	    			pos = Utils.add(pos, offset);
	    			float[] posCorr = pos.clone();
	    			float alpha = 90f / ROTATION_FACTOR;
	    			if(isCubeView()){
	    				alpha = _getLabelRotationAngle();
	    				if(orderChange){
	    					posCorr[Y] -= col * (alpha) * SKEW_FACTOR;
	    				}else{
	    					posCorr[Y] -= (s.getColumnCount() - col) * (alpha) * SKEW_FACTOR;
	    				}
	    				if(rotationChange){
	    					alpha = -alpha;
	    					wAlign = Align.RIGHT;
	    				}
	    				hAlign = Align.TOP;
	    			}

	    			float scale = SCALE_LABELS;
	    			if(hoveredSlice == s2
	    					|| (s2.containsCell(hoveredCell) && visibleCells.contains(hoveredCell))
	    					|| selectedVNodes.contains(s2.getData())
	    					|| selectedTimes.contains(s2.getData()))
	    			{
	    				labelColor = COLOR_LABELS_HIGHLIGHT.clone();
	    				thickness *= HOVER_THICKNESS_FACTOR;	    					
	    			}else if(isCubeView()){
	    				scale *= _calculateLabelScaleFactor(posCorr);
	    			}else if((isTimeSMView() || isVNodeSliceSMView()) && Utils.dist(camera.getLookAt(), camera.getPos()) > 65000){
	    				continue;
	    			}
	    			
	    			String label = s2.getLabel();
	    			if (node != null) {
		    			if (node.getChildren().size() > 0){
		    				if (node.isExpanded()){
			    				label = s2.getLabel() + " >";
		    				} else {
			    				label = s2.getLabel() + " >>";
		    				}
		    			} else {
		    				label = s2.getLabel();
		    			}	    				
	    			}	    			
	    			labelBounds.put(_display3DString(gl, posCorr, label, thickness, wAlign, hAlign,  translucency_label[S], labelColor, -alpha * ROTATION_FACTOR, scale) ,s2);
//	    			labelBounds.put(_display3DString(gl, posCorr, s2.getLabel(), thickness, wAlign, hAlign,  translucency_label[S], labelColor, alpha, scale) ,s2);
	    			drawLabelLine(gl, posCorr, pos);
	    		}
	    	}
	    	
	    	// WEST
	    	if(translucency_label[W] > 0)
	    	{
	    		offset = this.westOffset.get(s);
	    		if(offset == null)
	    			offset = currentView.getWestLabelOffset(isTSliceMode());
	    		for(row = 0 ; row < s.getRowCount() ; row++)
	    		{
	    			gl.glPushMatrix();
	    			pos = s.getRelGridCoords(row, 0).clone();
	    			gl.glTranslatef(pos[X], pos[Y], pos[Z]);
	    			gl.glPopMatrix();
	    			
	    			labelColor = COLOR_LABELS.clone();
	    			thickness = 1;
	    			
	    			if(s instanceof TimeSlice || s instanceof VNodeSlice){
	    				s2 = matrixCube.getVisibleHNodeSlice(row);
	    			}
	    			else if (s instanceof HNodeSlice){
	    				s2 = matrixCube.getVisibleVNodeSlice(row);
	    			}
	    			
	    			// VI adjust the labels to represent the is-a hierarchy; 
	    			HierarchicalCNode node = (HierarchicalCNode) s2.getData();
	    			int nodeDepth = node.getNodeDepth();
	    			if (isSourceSideView() || isVNodeSliceSMView()){
		    			pos[Z] -= (nodeDepth * LABEL_LEVEL_INDENT) - 1.5;  
	    			} else {
		    			pos[X] -= (nodeDepth * LABEL_LEVEL_INDENT) - 1.5;  
	    			} 
	    			
	    			float scale = SCALE_LABELS;
	    			if(hoveredSlice == s2 
	    			|| (s2.containsCell(hoveredCell) && visibleCells.contains(hoveredCell))
	    			|| selectedHNodes.contains(s2.getData()))
	    	    	{
	    				labelColor = COLOR_LABELS_HIGHLIGHT.clone();
	    				thickness *= HOVER_THICKNESS_FACTOR;	    					
	    			}else if(isCubeView()){
	    				scale *= _calculateLabelScaleFactor(pos);
	    			}else if((isTimeSMView() || isVNodeSliceSMView()) && Utils.dist(camera.getLookAt(), camera.getPos()) > 65000){
	    				continue;
	    			}
	    			
	    			
	    			wAlign = this.westAlign.get(s);
	    			if(wAlign == null)
	    				wAlign = Align.RIGHT;
	    			
	    			pos = Utils.add(pos, offset);
	    			float[] posCorr = pos.clone();
	    			
	    			String label = s2.getLabel();
	    			if (node.getChildren().size() > 0){
	    				if (node.isExpanded()){
		    				label = s2.getLabel() + " >";
	    				} else {
		    				label = s2.getLabel() + " >>";
	    				}
	    			} else {
	    				label = s2.getLabel();
	    			}
	    			labelBounds.put(_display3DString(gl, posCorr, label, thickness, wAlign, Align.CENTER,  translucency_label[W], labelColor, 0, scale),s2);
	    	    }
	    	}
	    }
	    
	    
	    /** Returns a factor that scales the label depending on camera distance.*/
	    protected float _calculateLabelScaleFactor(float[] pos)
	    {
			float labelScaleFac = Math.abs(Utils.length(Utils.dir(camera.getPos(), pos)));
			return (float) Math.max(.3f, Math.min(1, Map.map(labelScaleFac, 60, 200, 1, .3f)));
	    }
	    
	    protected float _getLabelRotationAngle()
	    {
	    	float alpha_min = 30;
	    	float beta = 0;
	    	float[] u = Utils.dir(camera.getPos(), camera.getLookAt());
		    float[] v = u.clone();
		    v[Y] = 0;
		    float alpha = (float) Utils.getAngleRad(u, v);
		    
		    if(Utils.getDeg(alpha) < alpha_min)
			{
				beta = (float) Map.map(Utils.getDeg(alpha), 0, alpha_min, 90, 0);
			}
		    if(isVSliceMode())
		    	beta = -beta;

		    return beta;
		}
	    
	    
//	    protected float[][] display2DString(GL2 gl, float[] pos_3d, String string, int thickness, int hAlign, float transparency, float[] color, float scale)
//	    {
//	    	int font = CubixVis.FONT_12;
//	    	float[][] bounds = new float[4][2];
//
//	    	float[] pos2d = camera.modelToScreen(gl, glu, pos_3d);
//
//	    	begin2D(gl, glu, WIDTH, -HEIGHT);
//	    	GLUT glut = new GLUT();
//	     	gl.glPushMatrix();
//	     	gl.glLoadIdentity();
//			switch(hAlign){
//			case ALIGN_LEFT	:
//				break;
//			case ALIGN_RIGHT:
//				pos2d[0] -= glut.glutBitmapLength(font, string); 
//				break;
//			case ALIGN_JUSTIFIED:
//				pos2d[0] -= (glut.glutBitmapLength(font,  string) / 2f); 
//				break;
//			}
//			gl.glColor3fv(color, 0);
//			gl.glWindowPos3f(pos2d[X], pos2d[Y], 0);
//			glut.glutBitmapString(font, string);
//			
//			gl.glPopMatrix();
//	    	end2D(gl);
//	    	return bounds;
//	    }
	    
	    
	    protected float[] _display3DString(GL2 gl, float[] pos_3d, String string, float thickness, Align hAlign, Align vAlign, float transparency, float[] color, float alpha, float scale)
	    {
	    	GLUT glut = new GLUT();

	    	float[] pos_2d = camera.modelToScreen(gl, glu, pos_3d);

	    	begin2D(gl, glu, (int) width, (int) height);
	    	gl.glTranslatef(pos_2d[X], pos_2d[Y], pos_2d[Z]);
	    	gl.glScalef(scale, scale, scale);
	    	
			gl.glRotated(alpha, 0, 0, 1);
	    	
		    // Align
		    float offset_x = 0;
		    switch(hAlign)
		    {
		    case RIGHT:
		    	offset_x = glut.glutStrokeLength(GLUT.STROKE_ROMAN, string); 
		    	break;
		    case CENTER:
		    	offset_x = (glut.glutStrokeLength(GLUT.STROKE_ROMAN,  string) / 2f); 
		    	break;
		    }
		    
		    float offset_y = 0;
			switch(vAlign)
		    {
		    case TOP:
		    	offset_y = -100; 
		    	break;
		    case CENTER:
		    	offset_y = -50;
		    	break;
		    }
		    
		    gl.glTranslatef(-offset_x, offset_y, 0f);
		    
		    // Store bounds
//		    float[] bounds_2d = new float[4];
//			float[] matrix = new float[16]; 
//			float w = glut.glutStrokeLength(GLUT.STROKE_ROMAN,  string)/6;
//			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, matrix, 0);
//			bounds_2d[W] = matrix[12];
//			bounds_2d[N] = matrix[13] + 15;
//			bounds_2d[E] = bounds_2d[W] + w;
//			bounds_2d[S] = bounds_2d[N] - 35;
//				
//			// calculate rotation
//			float[] p0 = new float[]{bounds_2d[W], bounds_2d[S]};
//			float[] p1 = new float[]{bounds_2d[W], bounds_2d[N]};
//			float[] p2 = new float[]{bounds_2d[E], bounds_2d[N]};
//			float[] p3 = new float[]{bounds_2d[E], bounds_2d[S]};
//			float[][] points_2d = new float[4][2];
//			points_2d[0] = p0;
//			points_2d[1] = Utils.add(p0, Utils.rotate2D(Utils.dir(p0, p1), Math.toRadians(alpha)));
//			points_2d[2] = Utils.add(p0, Utils.rotate2D(Utils.dir(p0, p2), Math.toRadians(alpha)));
//			points_2d[3] = Utils.add(p0, Utils.rotate2D(Utils.dir(p0, p3), Math.toRadians(alpha)));
			
			 // Draw letters
		    char c;
		    gl.glLineWidth(thickness);
		 
		    if(PRINT_MODE){
		    	color = new float[]{.1f,.1f,.1f};
		    }
		    gl.glColor4f(color[R], color[G], color[B], transparency);
		    for(int i=0 ; i < string.length() ; i++)
		    {
		    	c = string.charAt(i);
		    	glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, c);
		    }

		    float[] matrix = new float[16]; 
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, matrix, 0);
		    float[] p = new float[2];
		    p[0] = matrix[12];
		    p[1] = height - matrix[13];
	
			end2D(gl);

			return p;
//		 
//		    float[][] points_3d = new float[4][3];
//			int[] viewport = camera.getViewPortAsInt(gl);
////			gl.glBegin(gl.GL_QUADS);
//			for(int i=0 ; i<4;i++)
//			{
//				glu.gluUnProject(points_2d[i][X], points_2d[i][Y]-1, camera.getClipClose(), camera.getModelViewAsFloat(gl), 0, camera.getProjectionAsFloat(gl), 0, viewport, 0, points_3d[i], 0);	
////				gl.glVertex3fv(points_3d[i], 0);
////				if(string.equals("2008") || string.equals("2006"))
////					Utils.print(string +": " , points_3d[i]);
//			}
////			gl.glEnd();			
//		    return points_3d;
	    }
	    
	    protected void drawLabelLine(GL2 gl, float[] start, float[] end){
	    	drawLine(gl, start, end, .1f, new float[]{0,0,0,.1f});
	    }
	    
	    protected void drawLine(GL2 gl, float[] start, float[] end, float width, float[] color){
	    	gl.glDisable(GL2.GL_LIGHTING);
	    	gl.glColor4fv(color,0);
	    	gl.glLineWidth(width);
	    	gl.glBegin(GL2.GL_LINES);
	    		gl.glVertex3fv(start, 0);
	    		gl.glVertex3fv(end, 0);
	    	gl.glEnd();
	    	gl.glEnable(GL2.GL_LIGHTING);
	    }

		
	    
	    protected float getThreasholdedTransparency(float weight){
	    	return (float) Math.max(Map.map(weight, WEIGHT_MIN, WEIGHT_MAX, 0, 1),0);
	    }
		
		/////////////
		/// LABEL ///
		/////////////	

		
	    public void displayStatistics(GL2 gl)
	    {
	    	GLUT glut = new GLUT();
	    	int[] viewport = camera.getViewPortAsInt(gl);
	    	int line = 0;

//	    	drawSettingLine("VIEWS( + command keys to change):", line, gl, glut);
//	    	drawSettingLine("__________", line, gl, glut);
//	    	int a=1;
//	    	for(CView v : vm.getViews())
//	    		drawSettingLine(a++ +") " + v.getName(), ++line, gl, glut);
//	    	drawSettingLine("", ++line, gl, glut);

//	    	drawSettingLine("__________", line, gl, glut);
//	    	drawSettingLine("Minimal displayed weight: " + String.format("%.2f", WEIGHT_MIN), ++line, gl, glut);
//	    	drawSettingLine("Maximal displayed weight: " + String.format("%.2f", WEIGHT_MAX), ++line, gl, glut);
//	    	drawSettingLine("", ++line, gl, glut);
//	    	drawSettingLine("Slice mode: " + sliceMode, ++line, gl, glut);
//	    	drawSettingLine("", ++line, gl, glut);

	    	float[] pos = new float[2];
	    	drawSettingLine("CUBE:", ++line, gl, glut);
	    	
	    	pos = drawSettingLine("Red-axis: Ontology " + CubixVis.TARGET_ONTO, ++line, gl, glut);
	    	drawSettingLine("Red-axis: # of Concepts: " + CubixVis.TARGET_ONTO_NUM_CLASSES, ++line, gl, glut);
	    	
	    	pos = drawSettingLine("Green-axis: Ontology " + CubixVis.SOURCE_ONTO, ++line, gl, glut);
	    	
	    	drawSettingLine("Green-axis: # of Concepts: " + CubixVis.SOURCE_ONTO_NUM_CLASSES, ++line, gl, glut);
	    	
	    	drawSettingLine("Blue-axis: Alignments: " + tGraph.getTimeSliceNumber(), ++line, gl, glut);
	    	drawSettingLine("Total # Of Mappings: " + CubixVis.MAPPINGS_COUNT, ++line, gl, glut);

	    	//drawSettingLine("Times: " + tGraph.getTimeSliceNumber(), ++line, gl, glut);        	
	    	//drawSettingLine("Cube Density: " + (tGraph.getTimeSliceNumber() * tGraph.getVertexNumber() * tGraph.getVertexNumber()) / (tGraph.getEdgeNumber() * 100f), ++line, gl, glut);        	
	    	//drawSettingLine("", ++line, gl, glut);
	    	//drawSettingLine("VISUAL PARAMETERS:", ++line, gl, glut);
	    	//drawSettingLine("fps: " + framesPerSecond, ++line, gl, glut);        	
	    		    	

	    	
//	    	drawSettingLine("QUERIES:", ++line, gl, glut);
	    	
	    	// DRAW COLORS: 
//	    	int yOffset = 300;
//	    	float[] c;
//	    	for(Slice<?,?> s : queriedSlices)
//	    	{
//	    		c = s.getCublets().get(0).getColor();
//	    		draw2DRect(gl, (int) SETTINGS_MARGIN_LEFT, yOffset, 15, 15, c);
//		        gl.glEnable(gl.GL_DEPTH_TEST);
//		        yOffset += 30; 
//	    	}
//	    	gl.glClearColor(1f, 1f, 1f, 1f);
	    	
	    }
	    
	    protected void write2D(GL2 gl, GLUT glut, String str, int x, int y)
	    {
	    	gl.glWindowPos3f(x, y, 0);
	    	glut.glutBitmapString(glut.BITMAP_HELVETICA_12, str);
	    }
	    

	    protected float[] drawSettingLine(String string, int line, GL2 gl, GLUT glut)
	    {
	    	float[] pos = new float[2];
	    	int[] viewport = camera.getViewPortAsInt(gl);
	    	pos[0] = SETTINGS_MARGIN_LEFT;
	    	pos[1] = viewport[3] - (SETTINGS_MARGIN_TOP + (line*SETTINGS_LINE_SPACE));
	    	gl.glColor3f(.3f,.3f,.3f);
	    	gl.glWindowPos3f(pos[0], pos[1], 0);
	    	glut.glutBitmapString(glut.BITMAP_HELVETICA_12, string);
	    	
	    	return pos;
	    }

	    
	    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) 
	    {
	    	System.out.println("reshape() called: x = "+x+", y = "+y+", width = "+width+", height = "+height);
	        final GL2 gl = gLDrawable.getGL().getGL2();
	 
	        if (height <= 0) // avoid a divide by zero error!
	        {
	            height = 1;
	        }

	        gl.glViewport(0, 0, width, height);
	        gl.glLoadIdentity();
	    }
	    
		public void dispose(GLAutoDrawable arg0) 
		{
			System.out.println("dispose() called");
		}

	    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) 
	    {
	    	System.out.println("displayChanged called");
	    	
	    }
	    
		protected boolean isVisible(Cell c) {
			
	    	CEdge e = c.getData();
	    	CNode source = tGraph.getSource(e);
			CNode target = tGraph.getTarget(e);
			if((source instanceof HierarchicalCNode) 
					&& (target instanceof HierarchicalCNode)
	    			&& ((HierarchicalCNode) source).isVisible() 
	    			&& ((HierarchicalCNode) target).isVisible()){
	    		return true;
	    	}

	    	return false;
		}	    
	    
	    protected boolean isInVisibleCategory(Cell c)
	    {
	    	CEdge e = c.getData();
	    	boolean show = false;
	    	
	    	if(tGraph.getSource(e) == tGraph.getTarget(e)){
	    		show = selfEdgeVisibilityCheckBox.isSelected();
	    	}else{
	    		show = nonSelfEdgeVisibilityCheckBox.isSelected();
	    	}

	    	return show;
	    }
	    
	    
	    
	    ///////////////////
	    /// INTERACTIOM ///
	    ///////////////////
	    
		protected void _closeUp(Slice s)
		{
			float[] v0 = s.getCornerPos(0);
			float[] v1 = s.getCornerPos(1);
			float[] v2 = s.getCornerPos(3);
			float[] ortho = Utils.cross(Utils.dir(v1, v0), Utils.dir(v0, v2));

			
			float dist = s.getRowCount()/5f;
//			Log.out(this, "dist " + dist);
			
			float[] cPos = Utils.add(slicePos.get(s), Utils.mult(Utils.normalize(ortho), dist));
			
			camera.savePosition();
			tm.startTransition(tm.getCameraTransition(camera, cPos, slicePos.get(s), 5000));
		
		}
		
		protected void updateSelfEdgeVisibility()
		{
			float min = WEIGHT_MAX;
			float max = WEIGHT_MIN;
			float w;
			ArrayList<Float> values = new ArrayList<Float>();
			for(Cell c : matrixCube.getCells()){
				if(isInVisibleCategory(c))
				{
					w = c.getData().getWeight();
					min = Math.min(min,w);
					max = Math.max(max,w);
					values.add(w);
				}
			}
			this.weightRangeSlider.setMinimum(min);
			this.weightRangeSlider.setMaximum(max);
			this.weightRangeSlider.setLowValue(min);
			this.weightRangeSlider.setHighValue(max);
			Histogram h = weightRangeSlider.getHistogram();
			//h.setValues(min, max, values, Cubix.dataSetName.contains("Collab"));
			h.setValues(min, max, values, false);
			
			display();
		}
		
		
	    protected void _performPicking(GL2 gl)
	    {
	    	hoveredCell = null;
	    	hoveredSlice = null;
	    	prevHit = null;
	    	doPicking = false;

	    	float[] rayStart = new float[3];
	        float[] ray = camera.getPickRay(xPick, yPick, gl, glu, rayStart);	
	        ray = Utils.mult(ray, -1);
	        
	        // CHECK LASSO SELECTION 
	        if(Lasso.state == Lasso.STATE_ADD_POINT)
	        {
				Lasso.pointCount++;
				if(Lasso.pointCount > 2)
				{
					Lasso.pointCount = 0;
					Lasso.points.add(rayStart.clone());
					Lasso.rays.add(ray.clone());
				}
				return;
	        }
	        else if(Lasso.state == Lasso.STATE_CALCULATE)
	        {
	        	if(Lasso.cells.size() > 0)
	        	{
	        		Lasso.cells.clear();
	        	}
	        	else{
	        		for(Cell c : visibleCells)
	        		{
	        			if(!cellsInWeightRange.contains(c)) 
	        				continue;
	        		
	        			if(ConvexHull.isInside(c.getAbsolutePos(), Lasso.points, Lasso.rays ,gl))
	        			{
	        				Lasso.cells.add(c);
	        			}
	        		}
	        		Lasso.points.clear();
	        		Lasso.state = Lasso.STATE_NONE;
	        	}
	        	return;
	        }
	        
	        // TEST IF CELL IS HOVERED
	        hoveredCell = testForCellHit(selectedCells, rayStart, ray);
	    }
	    
	    public Cell testForCellHit(Collection<Cell> cublets, float[] rayStart, float[] ray)
	    {
			float[] b;
			float dist;
			float[] cross;
			Cell closestCell = null;
			for(Cell c : cublets)
	    	{
				if(!cellsInWeightRange.contains(c)) 
					continue;
				
	    		b = Utils.dir(rayStart, c.getAbsolutePos());
	    		cross = Utils.cross(ray, b);
	    		dist = Utils.length(cross) / Utils.length(ray);
	    		if(((isTimeSMView() || isVNodeSliceSMView()) && dist < c.getScale())
	    		|| dist < c.getScale() / 2f)
	    		{
	    			if(closestCell == null)
	    			{
	    				closestCell = c;	
	    			}
	    			else if(Utils.norm(Utils.sub(rayStart, c.getAbsolutePos())) 
	    						> Utils.norm(Utils.sub(rayStart, closestCell.getRelTimeSlicePos())))
	    			{
	    				if(selectedCells.contains(c)){
	    					closestCell = c;
	    				}		
	    			}
	    		}
	    	}
			return closestCell;
		}
	    
	    
	    protected void _testSlices(Collection<? extends Slice> slices, float[] rayStart, float[] ray)
	    {
	    	if(slices == null)
	    		return;
	    	
	    	cubeletHitFace = -1;
			for(Slice<?,?> s : slices)
	    	{
				try{
					if(_testPlane(s, 0,3,7,4, rayStart, ray)){
						cubeletHitFace = FACE_SIDE;
						return;
					}
					if(_testPlane(s, 3,2,1,0, rayStart, ray)) 
						cubeletHitFace = FACE_FRONT;
				}catch(Exception e){
//					Log.err(this, "plane error");
				}
			}
		}
	    protected boolean _testPlane(Slice s, int i, int j, int k, int l, float[] rayStart, float[] ray)
	    {
	    	float[] c1 = s.getCornerPos(i);
	    	float[] c2 = s.getCornerPos(j);
	    	float[] c3 = s.getCornerPos(k);
	    	float[] c4 = s.getCornerPos(l);
			
	    	Vector3D v1 = new Vector3D(c1[X], c1[Y], c1[Z]);
			Vector3D v2 = new Vector3D(c2[X], c2[Y], c2[Z]);
			Vector3D v3 = new Vector3D(c3[X], c3[Y], c3[Z]);
			Vector3D v4 = new Vector3D(c4[X], c4[Y], c4[Z]);
			Plane face = new Plane(v1, v2, v3); 
			

			float[] rayEnd = Utils.add(rayStart, ray);
	    	Line r = new Line(	new Vector3D((double)rayStart[X],(double)rayStart[Y], (double)rayStart[Z]), 
	    			 			new Vector3D((double)rayEnd[X], (double)rayEnd[Y], (double)rayEnd[Z]));

			Vector3D isec = face.intersection(r);
			if(isec != null){
				float d1 = (float) isec.distance(v1);
				float d2 = (float) isec.distance(v2);
				float d3 = (float) isec.distance(v3);
				float d4 = (float) isec.distance(v4);
				float a = (float) v1.distance(v2);
				float b = (float) v1.distance(v4);
				float c = (float) v1.distance(v3);
				
				float sum = d1+d2+d3+d4;
				if(sum < a + b + c){
					if(prevHit != null){
	    				if(Utils.length(Utils.dir(rayStart, Utils.getFloat(isec))) < Utils.length(Utils.dir(rayStart, prevHit))){
	    					prevHit = Utils.getFloat(isec);
	    					hoveredSlice = s;
	    					return true;
	    				}
	    			}else{
	    				hoveredSlice = s;
    					return true;
	    			}
				}
			}
			return false;
	    }
	    
	    
	    protected void setWeightAdaption(boolean b)
	    {
	    	this.weightAdaption = b;
//	    	log.writeNext(new String[]{
//	    			System.currentTimeMillis() + "", 
//	    			currentView.name, 
//	    			"", 
//	    			"" + b});
//	    	try {
//				log.flush();
//			} catch (IOException e) { e.printStackTrace();
//			}
	    }
	    
	    
	    protected void setFrameVisibility(boolean b){
	    	this.frameVisibility = b;
	    }
	    	
	    protected boolean getFrameVisibility(){
	    	return this.frameVisibility;
	    }
	    	
	    /////////////////////////////
	    /// protected INIT ROUTINES ///
	    /////////////////////////////
	    

		protected void initLight(GL2 gl)
	    {
	    	// Enable lighting in GL.
	    	gl.glEnable(GL2.GL_LIGHTING);
	    	gl.glEnable(GL2.GL_LIGHT0);

	    	gl.glMatrixMode(gl.GL_MODELVIEW);
	    	// Prepare light parameters.
	        float[] lightPos = {-5, 10, 3 , SHINE_ALL_DIRECTIONS};
	        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
	        
	        // light vector
	        gl.glBegin(gl.GL_LINES);
	        	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[]{1f, 1f, 1f}, 0);
	        	gl.glVertex3f(lightPos[0], lightPos[1], lightPos[2]);
	        	gl.glVertex3f(0f,0f,0f);
	        gl.glEnd();
	        	
	        // Set light parameters.
	        if (colorEncoding == ColorEncoding.TIME){
		        float[] lightColorAmbient = {LIGHT_VALUE_AMBIENT_TIME,LIGHT_VALUE_AMBIENT_TIME, LIGHT_VALUE_AMBIENT_TIME};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightColorAmbient, 0);
		        float[] lightColorDiffuse = {LIGHT_VALUE_DIFFUSE_TIME, LIGHT_VALUE_DIFFUSE_TIME, LIGHT_VALUE_DIFFUSE_TIME};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColorDiffuse, 0);
	        } else {
		        float[] lightColorAmbient = {LIGHT_VALUE_AMBIENT_ALL,LIGHT_VALUE_AMBIENT_ALL, LIGHT_VALUE_AMBIENT_ALL};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightColorAmbient, 0);
		        float[] lightColorDiffuse = {LIGHT_VALUE_DIFFUSE_ALL, LIGHT_VALUE_DIFFUSE_ALL, LIGHT_VALUE_DIFFUSE_ALL};
		        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColorDiffuse, 0);
	        }

	    }


	  
	  
	    // Causes transformation to a new view.
	    public void goToView(CView newView)
	    {
	    	if((isCubeView() || isFrontView() || isSourceSideView() || isTargetSideView())
	    		&& (newView instanceof CubeView || newView instanceof FrontView || newView instanceof SourceSideView || newView instanceof TargetSideView)){
	    		switchToCubeDLMode= true;
		    	switchToSliceDLMode = false;	
	    	}else{
	    		switchToCubeDLMode= false;
	    		switchToSliceDLMode = true;	
	    	}
	    	
	    	// CREATE TRANSITION
	    	Transition t = tm.getTransition(currentView, newView);
	    	if(t == null)
	    		return;

//	    	log.writeNext(new String[]{
//	    			System.currentTimeMillis() + "",
//	    			newView.name, 
//	    			transitionSpeedSlider.getValue() + ""
//	    			});
//	    	try {
//				log.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
	    	
	    	t.addListener(this);
			tm.startTransition(t);
			this.newView = newView;
			currentView = newView;
		}
		
		public void transitionFinished(Transition t)
		{
			CubixVis.SHOW_FRAMES= false; 
			currentView = newView;
			dlMode = DLMode.CELL;
			viewStartTime = System.currentTimeMillis();

			if(!t.getName().equals("SliceRotation"))
			{
				labelTransparency.clear();
				if(isFrontView() || isCubeView())
					setSliceMode(SliceMode.TIME);
				else if(isSourceSideView())
					setSliceMode(SliceMode.VNODE);
				else if (isTargetSideView())
					setSliceMode(SliceMode.HNODE);
				
				for(Slice<?,?> slice : matrixCube.getTimeSlices())
				{
					try{slice.setRightLabelPos(currentView.getLabelPosR(slice));}catch(Exception e){};
					try{slice.setRightLabelAlign(currentView.getLabelAlignR(slice));}catch(Exception e){};
					try{slice.setLeftLabelPos(currentView.getLabelPosL(slice));}catch(Exception e){};
					try{slice.setLeftLabelAlign(currentView.getLabelAlignL(slice));}catch(Exception e){};
					try{labelTransparency.put(slice, currentView.getLabelTrans(slice));}catch(Exception e){};
				}
				for(Slice<?,?> slice : matrixCube.getVisibleHNodeSlices())
				{
					try{slice.setRightLabelPos(currentView.getLabelPosR(slice));}catch(Exception e){};
					try{slice.setLeftLabelPos(currentView.getLabelPosL(slice));}catch(Exception e){};
					try{slice.setLeftLabelAlign(currentView.getLabelAlignL(slice));}catch(Exception e){};
					try{slice.setRightLabelAlign(currentView.getLabelAlignR(slice));}catch(Exception e){};
					try{labelTransparency.put(slice, currentView.getLabelTrans(slice));}catch(Exception e){};
				}
					
				for(Slice<?,?> slice : matrixCube.getVisibleVNodeSlices())
				{
					try{slice.setRightLabelPos(currentView.getLabelPosR(slice));}catch(Exception e){};
					try{slice.setLeftLabelPos(currentView.getLabelPosL(slice));}catch(Exception e){};
					try{slice.setLeftLabelAlign(currentView.getLabelAlignL(slice));}catch(Exception e){};
					try{slice.setRightLabelAlign(currentView.getLabelAlignR(slice));}catch(Exception e){};
					try{labelTransparency.put(slice, currentView.getLabelTrans(slice));}catch(Exception e){};
				}
			}
			else
			{
				if(rotatedSlices.size() == 0)
					this.setSliceMode(t.getFinalSliceMode());
				
				float[] p;
				if(isTSliceMode()){
					for(Slice<?,?> s : renderedSlices){
						p = slicePos.get(s);
						for(Cell c : s.getCells()){
							c.setAbsolutePos(Utils.add(p, c.getRelTimeSlicePos()));
						}
					}
				}else{
					for(Slice<?,?> s : renderedSlices){
						p = slicePos.get(s);
						for(Cell c : s.getCells()){
							c.setAbsolutePos(Utils.add(p, c.getRelHNodeSlicePos()));
						}
					}
				}
			}
			
			display();
		}
	    
		
	    ///////////////
	    /// HELPERS ///
	    ///////////////	    
	    
	    public void showCoordinateSystem(GL2 gl)
	    {
	    	gl.glMatrixMode(gl.GL_MODELVIEW);
	    	int LENGTH = 10;
	    	gl.glLineWidth(1);
	        // Set material properties.
			gl.glBegin(gl.GL_LINES);
	        	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[]{1f, 0.0f, .0f, 1f}, 0);
	        	gl.glVertex3i(0, 0, 0); // start of line
	        	gl.glVertex3i(LENGTH, 0, 0); // end of line
	        	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[]{0f, 1f, 0f, 1f}, 0);
	        	gl.glVertex3i(0, 0, 0);
		        gl.glVertex3i(0, LENGTH, 0);
		        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[]{0f, 0.0f, 1f, 1f}, 0);
	        	gl.glVertex3i(0, 0, 0);
		        gl.glVertex3i(0, 0, LENGTH);
	        gl.glEnd();	
	    }
	   
	    
		
		protected void selectSlice(Slice s, boolean select)
		{
			if(select)
			{
				if(s instanceof TimeSlice)
				{
					selectedTimes.add((CTime) s.getData());
				} 
				else if(s instanceof VNodeSlice)
				{
					selectedVNodes.add((CNode) s.getData());
				} 
				else if(s instanceof HNodeSlice)
				{
					selectedHNodes.add((CNode) s.getData());
				} 
			}
			else // Deselect
			{
				if(s instanceof TimeSlice)
				{
					selectedTimes.remove(s.getData());
				}
				else if(s instanceof VNodeSlice)
				{
					selectedVNodes.remove(s.getData());
				}					
				else if(s instanceof HNodeSlice)
				{
					selectedHNodes.remove(s.getData());
				}					
			}
			
//			_updateCellVisibility();
		}
		
		
	    protected void updateLabelPositionsWithCamera() 
	    {
    		eastAlign.clear();
    		westAlign.clear();
    		eastOffset.clear();
    		westOffset.clear();
       		northOffset.clear();
    		southOffset.clear();
       		rotationChange = false;
       		orderChange = false;
    		invertYOffset = false;
    		invertSliceAlign = false;
    		rightYLabelPos = 0;
    		leftYLabelPos = 0;
    		
    		if(!isCubeView()){
    			return;
    		}
    		
	    	// GET CAMERA STATE
	    	CView v = vm.getView(vm.VIEW_CUBE);
	    	Slice t0 = matrixCube.getFirstTimeSlice();
	    	Slice t1 = matrixCube.getLastTimeSlice();
	    	Slice v0 = matrixCube.getFirstVNodeSlice();
	    	Slice v1 = matrixCube.getLastVNodeSlice();


	    	float x0 = v.getSlicePosition(v0)[X] +.5f;
	    	float x1 = v.getSlicePosition(v1)[X] -.5f;
	    	float z0 = v.getSlicePosition(t0)[Z] -.5f;
	    	float z1 = v.getSlicePosition(t1)[Z] +.5f;
	    	float x = camera.getPos()[X];
	    	float z = camera.getPos()[Z];
	    	
	    	int i=1, j=1;
	    	if(x < x0) 
	    		i=0; 
	    	else if(x > x1) 
	    		i=2;
	    	
	    	if(z < z0) 
	    		j=0; 
	    	else if(z > z1) 
	    		j=2;
	    	
	    	
	    	camCase = -1;
	    	camCase = 3*j +i;
	    	

    		float d = 1f;
    		float top = getSliceHeight(t0)/2f +1;
    		float bottom = - top;
    		// Update label pos + align.
	    	float[] l0 = new float[4],l1= new float[4];
	    	switch(camCase){
	    	case 0 : 
	    		l0 = new float[]{0,1,0,0};
	    		l1 = new float[]{0,0,0,1};
	    		eastAlign.put(t0, Align.RIGHT);
	    		westAlign.put(t1, Align.LEFT);
	    		eastOffset.put(t0, new float[]{d,0,-d});
	    		westOffset.put(t1, new float[]{-d,0, d});
	    		rotationChange = true;
	    		orderChange = false;
	    		sliceLabelRotationDirChange = true;
	    		invertSliceAlign = true;
	    		break;
	    	case 1 : 
	    		l0 = new float[]{0,1,0,1};
	    		l1 = new float[]{0,0,0,0};
	    		eastAlign.put(t0, Align.RIGHT);
	    		westAlign.put(t0, Align.LEFT);
	    		eastOffset.put(t0, new float[]{d,0,-d});
	    		westOffset.put(t0, new float[]{-d,0,-d});
	    		rotationChange = true;
	    		orderChange = false;
	    		sliceLabelRotationDirChange = true;
	    		invertSliceAlign = true;
	    		break;
	    	case 2 : 
	    		l0 = new float[]{0,0,0,1};
	    		l1 = new float[]{0,1,0,0};
	    		westAlign.put(t0, Align.LEFT);
	    		eastAlign.put(t1, Align.RIGHT);
	    		westOffset.put(t0, new float[]{-d,0,-d});
	    		eastOffset.put(t1, new float[]{d,0,d});
	    		rotationChange = false;
	    		orderChange = true;
	    		sliceLabelRotationDirChange = false;
	    		invertYOffset = true;
	    		invertSliceAlign = true;
	    		break; 
	    	case 3 : 
	    		l0 = new float[]{0,0,0,1};
	    		l1 = new float[]{0,0,0,1};
	    		westAlign.put(t0, Align.RIGHT);
	    		westAlign.put(t1, Align.LEFT);
	    		westOffset.put(t0, new float[]{-d,0,-d});
	    		westOffset.put(t1, new float[]{-d,0,d});
	    		rotationChange = true;
	    		orderChange = false;
	    		sliceLabelRotationDirChange = false;
	    		break;
	    	case 5 : 
	    		l0 = new float[]{0,1,0,0};
	    		l1 = new float[]{0,1,0,0};
	    		eastAlign.put(t0, Align.LEFT);
	    		eastAlign.put(t1, Align.RIGHT);
	    		eastOffset.put(t0, new float[]{d,0,-d});
	    		eastOffset.put(t1, new float[]{d,0,d});
	    		rotationChange = true;
	    		orderChange = true;
	    		sliceLabelRotationDirChange = true;
	    		invertYOffset = true;
	    		break;
	    	case 6 : 
	    		l0 = new float[]{0,0,0,1};
	    		l1 = new float[]{0,1,0,0};
	    		westAlign.put(t0, Align.RIGHT);
	    		eastAlign.put(t1, Align.LEFT);
	    		westOffset.put(t0, new float[]{-d,0,-d});
	    		eastOffset.put(t1, new float[]{d,0,d});
	    		rotationChange = false;
	    		orderChange = false;
	    		sliceLabelRotationDirChange = false;
	    		break;
	    	case 7 : 
	    		l0 = new float[]{0,0,0,0};
	    		l1 = new float[]{0,1,0,1};
	    		eastAlign.put(t1, Align.LEFT);
	    		westAlign.put(t1, Align.RIGHT);
	    		eastOffset.put(t1, new float[]{d,0,d});
	    		westOffset.put(t1, new float[]{-d,0,d});
	    		rotationChange = false;
	    		orderChange = false;
	    		sliceLabelRotationDirChange = false;
	    		break;
	    	case 8 : 
	    		l0 = new float[]{0,1,0,0};
	    		l1 = new float[]{0,0,0,1};
	    		eastAlign.put(t0, Align.LEFT);
	    		westAlign.put(t1, Align.RIGHT);
	    		eastOffset.put(t0, new float[]{d,0,-d});
	    		westOffset.put(t1, new float[]{-d,0,d});
	    		rotationChange = true;
	    		orderChange = true;
	    		sliceLabelRotationDirChange = true;
	    		invertYOffset = true;
	    		break;
	    	}
	    	
	    	if(camCase < 4) 
	    	{
	    		l0[S] = 1; 
	    		l1[N] = 1; 
	    		southOffset.put(t0, new float[]{0,-1,-1});
	    		northOffset.put(t1, new float[]{0,1,1});
	    	}	
	    	else{
	    		l0[N] = 1;
	    		l1[S] = 1; 
	    		northOffset.put(t0, new float[]{0,1,-1});
	    		southOffset.put(t1, new float[]{0,-1,1});
		   	}
	    	
	    	
	    	if(i < 2){
	    		rightYLabelPos  = top;
	    		leftYLabelPos = bottom;
	    	}else{
	    		rightYLabelPos = bottom;
	    		leftYLabelPos = top;
	    	}
	    	labelTransparency.put(t0, l0);
	    	labelTransparency.put(t1, l1);
	    	
		}

		
		/////////////////
		/// GET & SET ///
		/////////////////
		
		public CView getCurrentView() {return currentView; }
		
		public void closePopupMenu(){if(contextMenu != null) contextMenu.setVisible(false);}
		
		public MatrixCube getMatrixCube() { return matrixCube;}

		public Camera getCamera() { return camera; }
		
		public boolean isReady() { return this.ready; }


		public float getSliceWidth(Slice m) { return m.getColumnCount() * sliceScale.get(m) * CELL_UNIT;}
		public float getSliceHeight(Slice m) { return m.getRowCount() * sliceScale.get(m) * CELL_UNIT;}

		public float getMatrixScale(Slice m) { return sliceScale.get(m); }
		public void setMatrixScale(Slice m, float scale) { sliceScale.put(m, scale);}

		public void setSliceRotation(Slice slice, float angle) { sliceRotation.put(slice, angle); }
		public float getSliceRotation(Slice slice) { return sliceRotation.get(slice); }

		public void setSlicePos(Slice m, float[] pos) { slicePos.put(m, pos); }
		public float[] getSlicePos(Slice m) {
			if (slicePos.get(m) == null) {
				System.out.println(m.getLabel());
			}
			return slicePos.get(m).clone(); 
		}

		public void setSliceMode(SliceMode mode)
		{ 
			if(rotatedSlices.size() > 0) return;
			if(sliceMode == mode) return;
			
			sliceMode = mode;
			renderedSlices  = new ArrayList<Slice<?,?>>();
			if(isVSliceMode())
			{
			    renderedSlices.addAll(matrixCube.getVisibleVNodeSlices());
			    Collections.reverse(renderedSlices);
			}else if(isHSliceMode()){
			    renderedSlices.addAll(matrixCube.getVisibleHNodeSlices());
			}else{
				renderedSlices.addAll(matrixCube.getTimeSlices());
			}
			// Update geometric positions.
			CView v = vm.getView(vm.VIEW_CUBE);
			for(Slice s: renderedSlices){
//				slicePos.put(s, currentView.getSlicePosition(s));
				slicePos.put(s, v.getSlicePosition(s));

			}
		}
		public SliceMode getSliceMode(){ return sliceMode;}
		
		public void removeRotatedSlice(Slice s){ this.rotatedSlices.remove(s);}
		public Collection<Slice<?,?>> getRotatedSlices() {return this.rotatedSlices;}
		
		public float[] getLabelTransparency(Slice s){return labelTransparency.get(s).clone();}  
		
		public void setLabelTransparency(Slice s, float transparency, int side){
			float[] t = labelTransparency.get(s);
			t[side] = transparency;
			labelTransparency.put(s, t);
		} 
		
		public void setLabelTransparency(Slice s, float[] transparency){
			labelTransparency.put(s, transparency.clone());
		} 

//		public void setColorEntireSlice(Slice<?,?> slice, float[] color) 
//		{ 
//			for(Cell c: slice.getCells())
//			{ 
//				if(queriedCells.contains(c))
//				{
////					color[R] = (color[R] + c.getColor()[R]) / 2f;
////					color[G] = (color[G] + c.getColor()[G]) / 2f;
////					color[B] = (color[B] + c.getColor()[B]) / 2f;
//					color[R] = (color[R] + cellColor.get(c)[R]) / 2f;
//					color[G] = (color[G] + cellColor.get(c)[G]) / 2f;
//					color[B] = (color[B] + cellColor.get(c)[B]) / 2f;
//				}
//				queryColor.ut(c, color);
//				queriedCells.add(c);
//			}
//			queriedSlices.add(slice);
//		}
//		public void resetSliceColor(Slice<?,?> slice)
//		{ 
//			for(Cell c: slice.getCells())
//			{ 
//				c.restoreColor();
//				queriedCells.remove(c);
//			} 
//			queriedSlices.remove(slice);
//		}
		
//		public void setCellColor(Cell c, float[] color) 
//		{c.setColor(color); coloredCells.add(c); 
//		}
//		public void resetCellColor(Cell c) { c.setColor(COLOR_CELL.clone()); coloredCells.remove(c);}


		public ArrayList<Slice<?,?>> getRenderSlices() { return renderedSlices; }

		
		protected boolean _isAnyCellHovered(Slice slice)
		{
			return  hoveredCell != null 
						&& (  hoveredCell.getTimeSlice() == slice 
							|| hoveredCell.getHNodeSlice() == slice 
							|| hoveredCell.getVNodeSlice() == slice
							);
		}
		

		public void setCellColorEncoding(ColorEncoding encoding)
		{
//			log.writeNext(new String[]{
//					System.currentTimeMillis() + "", 
//					currentView.name, 
//					"", 
//					"", 
//					"" + encoding, 
//					""
//					});
//			try {
//				log.flush();
//			} catch (IOException e) { e.printStackTrace(); }
			
			this.colorEncoding = encoding;

			if(encoding == ColorEncoding.WEIGHT)
				currentEdgeWeightColorScale = edgeWeightColorScale1;
			else if(encoding == ColorEncoding.WEIGHT_DIV){
				currentEdgeWeightColorScale = edgeWeightColorScale2;
			}
				
			
			requestFocus(); 
			display();
		}
		
		
		public void setCellShapeEncoding(ShapeEncoding encoding)
		{
//			log.writeNext(new String[]{
//					System.currentTimeMillis() + "", 
//					currentView.name, 
//					"", 
//					"",
//					"", 
//					"" + encoding 
//					});
//			try {
//				log.flush();
//			} catch (IOException e) { e.printStackTrace(); }
			shapeEncoding = encoding;
			requestFocus(); 
			display();
		}
	
		// VI only for testing
		
//		private ArrayList<String> sourceVisible = new ArrayList<>();
//		private ArrayList<String> targetVisible = new ArrayList<>();
//		
//		public void changeNodeVisibility(){
//			ArrayList<CNode> nss = new ArrayList<CNode>();
//			Iterator<CNode> it = tGraph.getVertices().iterator();
//			while(it.hasNext()){
//				HierarchicalCNode node = (HierarchicalCNode) it.next();
//				if(node.belongsToSourceOnto() && node.isVisible()){
//					sourceVisible.add(node.getLabel());
//				}
//				if(!node.belongsToSourceOnto() && node.isVisible()){
//					targetVisible.add(node.getLabel());
//				}			
//			}
//
//			ArrayList<CNode> nodes = new ArrayList<CNode>();
//			Iterator<CNode> iterator = tGraph.getVertices().iterator();
//			while(iterator.hasNext()){
//				HierarchicalCNode node = (HierarchicalCNode) iterator.next();
//				node.setVisible(true);
//				nodes.add(node);
//			}
//
//			// TODO frameVisibility && timeslice labels
//			matrixCube.setNodeOrder(nodes);
//			for(CView v : ViewManager.getInstance().getViews()){
//				v.init(this);
//			}
//
//			float[] p;
//			for(Slice s : renderedSlices){
//				p = currentView.getSlicePosition(s);
//				slicePos.put(s, currentView.getSlicePosition(s));
//			}
//			
//			requestFocus(); 
//			display();
//		}
//		
//		public void changeBackNodeVisibility(){
//
//			ArrayList<CNode> nodes = new ArrayList<CNode>();
//			Iterator<CNode> iterator = tGraph.getVertices().iterator();
//			while(iterator.hasNext()){
//				HierarchicalCNode node = (HierarchicalCNode) iterator.next();
//				if(!sourceVisible.contains(node.getLabel()) && !targetVisible.contains(node.getLabel()))
//					node.setVisible(false);
//				nodes.add(node);
//			}
//			
//			matrixCube.setNodeOrder(nodes);
//			for(CView v : ViewManager.getInstance().getViews()){
//				v.init(this);
//			}
//
//			float[] p;
//			for(Slice s : renderedSlices){
//				p = currentView.getSlicePosition(s);
//				slicePos.put(s, currentView.getSlicePosition(s));
//			}
//			
//			requestFocus(); 
//			display();
//		}
		
		
		public boolean isFrontView(){ return currentView == vm.getView(ViewManager.VIEW_FRONT); }
		public boolean isSourceSideView(){ return currentView == vm.getView(ViewManager.VIEW_SOURCE_SIDE); }
		public boolean isTargetSideView(){ return currentView == vm.getView(ViewManager.VIEW_TARGET_SIDE); }
		public boolean isCubeView(){ return currentView == vm.getView(ViewManager.VIEW_CUBE); }
		public boolean isTimeSMView(){ return currentView == vm.getView(ViewManager.VIEW_TIME_SM); }
		public boolean isVNodeSliceSMView(){ return currentView == vm.getView(ViewManager.VIEW_VNODESLICE_SM); }
		public boolean isHNodeSliceSMView(){ return currentView == vm.getView(ViewManager.VIEW_HNODESLICE_SM); }
		
		
		public boolean isTSliceMode(){return sliceMode == SliceMode.TIME;}
		public boolean isHSliceMode(){return sliceMode == SliceMode.HNODE;}		
		public boolean isVSliceMode(){return sliceMode == SliceMode.VNODE;}								
		
		public float getTransitionDurationFactor(){
			float v = (float) Map.map(transitionSpeedSlider.getValue(), transitionSpeedSlider.getMaximum(), transitionSpeedSlider.getMinimum(), .1f, 3f);
			if(shiftDown) return v * 2;
			if(altDown) return v * .3f;
			return v; 
		}
		
		protected float[] _getColor(ColourTable ct, float r)
		{
			int colo;
			try{
				 colo = ct.findColour(r);
			}catch(java.lang.NullPointerException ex){
				colo = 0;
			}
			int B_MASK = 255;
			int G_MASK = 255<<8; //65280 
			int R_MASK = 255<<16; //16711680
					
			float[] color = new float[]{0f, 0f, 0f,1f};
			color[R] = ((colo & R_MASK)>>16) / 255f;
			color[G] = ((colo & G_MASK)>>8) / 255f;
			color[B] = (colo & B_MASK) / 255f;			

			return color;			
		}
		
		// VI this is only test function; TODO VI to be removed
		protected float[] getGreenColor()
		{
					
			float[] color = new float[]{0f, 0f, 0f,1f};
			color[R] = 0;
			color[G] = 153;
			color[B] = 0;			

			return color;			
		}		
		
		protected float[] _getBiColor(CEdge e, double weight_min, double weight_max, float min, float max)
		{
			float r = e.getWeight() - e.getWeight2(); // pos = more a, neg = more b.
			r = (float) Map.map(r, -WEIGHT_MAX, WEIGHT_MAX, 0, 1);
			float[] color = _getColor(biColorScale, r);
			for(int i=0 ; i<3; i++){
				color[i] = color[i]/2;
			}
			return color;
		}
		
		public boolean isSelectedSlice(Slice s)
		{ 
			return  selectedTimes.contains(s.getData()) 
					|| selectedHNodes.contains(s.getData()) 
					|| selectedVNodes.contains(s.getData());
		}
		
		public boolean isRotated(Slice<?, ?> s) {return this.rotatedSlices.contains(s);}
		
		
		public static void begin2D(GL2 gl, GLU glu, int width, int height)
		{
			
			gl.glDisable(gl.GL_DEPTH_TEST);
			gl.glDisable(gl.GL_CULL_FACE);
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
	    	gl.glPushMatrix();
	    	gl.glLoadIdentity();
	    	
	    	glu.gluOrtho2D(0, width, 0, height);

	    	gl.glDisable(GL2.GL_LIGHTING);

	    	gl.glMatrixMode(GL2.GL_MODELVIEW);
	    	gl.glPushMatrix();
	    	gl.glLoadIdentity();
		}
		
		public static void end2D(GL2 gl)
		{
		
			if(doDepthTest)	
				gl.glEnable(gl.GL_DEPTH_TEST);
		
			gl.glEnable(gl.GL_CULL_FACE);
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
	    	gl.glPopMatrix();

	    	gl.glMatrixMode(GL2.GL_MODELVIEW);
	    	gl.glPopMatrix();

	    	gl.glEnable(GL2.GL_LIGHTING);
	    			
		}

		public float getAverageFramerate() {
			float v = 0;
			if(frameRateArray.length == 0)
				return 0;
			for(float f:frameRateArray){
				v+=f;
			}
			return v / frameRateArray.length;
		}
		
		public void startAnimationFrameCount() {
			animationFrameCount = 0;
			animationStartTime = System.currentTimeMillis();
		}

		public float getVisibileCellsCount() { return visibleCells.size(); }
		
}