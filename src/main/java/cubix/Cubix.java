package cubix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.DataModel;
import cubix.data.MatrixCube;
import cubix.dataSets.CSVGDataSet;
import cubix.dataSets.FilenameContainsFilter;
import cubix.helper.Log;
import cubix.helper.Map;
import cubix.helper.Utils;
import cubix.view.GraphSMView;
import cubix.view.NodeSMView;
import cubix.view.ViewManager;
import cubix.vis.Cell;
import cubix.vis.Slice;

public class Cubix {

	private static int SCREEN_WIDTH;
	private static int SCREEN_HEIGHT;
	private static int OFFSET_RIGHT = 000;
	private static int OFFSET_LEFT = 000;
	private static JToggleButton timeEncodingButton;
	private static CubixVisInteractive cubeVis1;
	private static CubixVisInteractive cubeVis2;

	// DATA SETS
	private static final int TRADE_1 = 19;
	private static final int TRADE_2 = 16;
	private static final int TRADE_3 = 21;
	private static final int TRADE_100 = 13;
	private static final int COLLAB_TAO = 8;
	private static final int BRAIN = 9;
	private static final int ARCH = 22;
	private static final int ANTENNAS_AMP = 23;
	private static final int ANTENNAS_DELAY = 24;
	private static final int ANTENNAS_RMS = 25;
	private static final int ANTENNAS_SNR = 26;
	private static final int TRADE_4 = 101;
	private static final int TRADE_5 = 102;
	private static final int COLLAB_VALPO_1 = 200;
	public static final int CONTROL_PANEL_WIDTH = 260;
	private static final int COLLAB_INFOVIS = 300;
	private static final int NEWCOMB = 400;
	private static final int LIEVE = 500;
	private static boolean CSV = true;
	
	private static String sourceOntoFilePath;
	private static String targetOntoFilePath;
	private static String alignmentsFilePath;
	private static String measuresFilePath;
	private static boolean onlyEquRelations;

	// Brain data from Stephane and Habib
	private static final int BRAIN_1 = 3001;

	static Toolkit kit = Toolkit.getDefaultToolkit();
	public static JFrame frame;
	public static String dataSetName;

	public static final boolean DEPLOY = true;

	public static void main(String[] args) {
		System.out.println("start " + DEPLOY);
		// Create Frame
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		int SCREEN = screens.length - 1;
//		int SCREEN = 1;
		// if (SCREEN == 0 && screens.length == 1)
		// OFFSET_RIGHT = 0;

		// VI to make it full screen
		//SCREEN_WIDTH = 1920;
		//SCREEN_HEIGHT = 1070;
		
		SCREEN_WIDTH = screens[SCREEN].getDisplayMode().getWidth() - 10;
		SCREEN_HEIGHT = screens[SCREEN].getDisplayMode().getHeight() - 150;
		
		//System.out.println("SCREEN_WIDTH"+SCREEN_WIDTH);
		//System.out.println("SCREEN_HEIGHT"+SCREEN_HEIGHT);
		
		//SCREEN_WIDTH = 2120; 	
		//SCREEN_HEIGHT = 1310; 
		
		//SCREEN_WIDTH = 1720; 	
		//SCREEN_HEIGHT = 1110; 
		
		//SCREEN_WIDTH = 1520; // the screencast
		//SCREEN_HEIGHT = 1010; // the screencast
		//SCREEN_WIDTH = screens[SCREEN].getDisplayMode().getWidth() - 10;
		//SCREEN_HEIGHT = screens[SCREEN].getDisplayMode().getHeight() - 30;

		// SCREEN_WIDTH = screens[SCREEN].getDisplayMode().getWidth() - 10;
		// SCREEN_WIDTH = screens[SCREEN].getDisplayMode().getWidth() -500;
		// SCREEN_WIDTH = 1024;
		// SCREEN_HEIGHT = 768;
		// SCREEN_WIDTH = 1500;
		// SCREEN_HEIGHT = 1000;
		// SCREEN_WIDTH = 800;
		// SCREEN_HEIGHT = 600;

		frame = new JFrame(screens[SCREEN].getDefaultConfiguration());

		Rectangle bounds = screens[SCREEN].getDefaultConfiguration().getBounds();
		frame.setBounds(bounds.x, bounds.y, SCREEN_WIDTH - OFFSET_RIGHT, SCREEN_HEIGHT);
		frame.setBackground(Color.WHITE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		frame.add(sp, BorderLayout.CENTER);

		// Create CubeVis
		int visWidth = (SCREEN_WIDTH - OFFSET_RIGHT - CONTROL_PANEL_WIDTH);
		// cubeVis1 = CubixVisInteractive.getInstance();
		cubeVis1 = new CubixVisInteractive();
		cubeVis1.setDimensions(visWidth, SCREEN_HEIGHT);
		cubeVis1.addGLEventListener(cubeVis1);
		cubeVis1.addMouseListener(cubeVis1);
		cubeVis1.addMouseMotionListener(cubeVis1);
		cubeVis1.addMouseWheelListener(cubeVis1);
		cubeVis1.addKeyListener(cubeVis1);
		cubeVis1.setPreferredSize(new Dimension(visWidth, SCREEN_HEIGHT));
		sp.setDividerLocation(SCREEN_WIDTH - CONTROL_PANEL_WIDTH);
		
		//JScrollPane scrollPane = new JScrollPane(cubeVis1);
		//scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		//scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//scrollPane.setPreferredSize(new Dimension(5000, 5000));
		//sp.setLeftComponent(scrollPane);
		sp.setLeftComponent(cubeVis1);
		sp.setResizeWeight(1);
		sp.setDividerSize(0);
		sp.setRightComponent(cubeVis1.getControlPanel());

		frame.setSize(frame.getContentPane().getPreferredSize());

		// Load Data
		DataModel<CNode, CEdge, CTime> model = DataModel.getInstance();
		int dataSet;

		// Select dataset by key on startup

		// DEMO DATA SETS:

		FileChooserDialog fileChooserDialog = new FileChooserDialog();
		fileChooserDialog.setVisible(true);
		
		if (sourceOntoFilePath != null && targetOntoFilePath != null 
				&& alignmentsFilePath != null)
		{
			File sourceOnto = new File(sourceOntoFilePath);
    		File targetOnto = new File(targetOntoFilePath);
    		File alignments = new File(alignmentsFilePath);
    		File alignmentsMeasures = null;
    		if (measuresFilePath != null && !measuresFilePath.equals(""))
    			alignmentsMeasures = new File(measuresFilePath);
			model.setTimeGraph(
					CSVGDataSet.loadOntologiesAndAlignments(sourceOnto, targetOnto, alignments, alignmentsMeasures, onlyEquRelations));

		} else if (sourceOntoFilePath == null && targetOntoFilePath == null 
				&& alignmentsFilePath != null){
			
			// TODO for the integration with Matrix Cubes
			File alignment = new File(alignmentsFilePath);
			model.setTimeGraph(
					CSVGDataSet.loadData(alignment));
		} else {
			JOptionPane.showMessageDialog(frame, "Could not load file format.", "Loading error",
								JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		frame.setResizable(false);
		frame.setTitle("Cubix " + alignmentsFilePath.split("\\.")[0]);

		// Visualize
		cubeVis1.createCube(model.getTimeGraph());
		cubeVis1.updateData();
		cubeVis1.display();
		frame.pack();
		frame.setVisible(true);

		// shutdown the program on windows close event
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});

		exportSVGTimeslices();
		exportSVGNodeslices();

		// Export graph
	}
	
	public static void setSelectedFilePaths(String sourceOnto, 
			String targetOnto, String alignments, String measures, boolean onlyEquRels){
		sourceOntoFilePath = sourceOnto;
		targetOntoFilePath = targetOnto;
		alignmentsFilePath = alignments;
		measuresFilePath = measures;
		onlyEquRelations = onlyEquRels;
	}

	public static void restart() {
		frame.setVisible(false);
		CubixVis.WEIGHT_MIN = 1000000;
		CubixVis.WEIGHT_MAX = 0;
		// CubixVisInteractive.destroyInstance();
		DataModel.destroyInstance();
		ViewManager.destroyInstance();

		main(new String[] {});
	}

	public static void createScreenshot() {
		BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		frame.printAll(g);
		image.flush();
		File here = new File("");
		File f = new File(here.getAbsolutePath() + "/screenshots/");
		if (!f.exists())
			f.mkdir();

		f = new File(here.getAbsolutePath() + "/screenshots/" + dataSetName + "/");
		if (!f.exists())
			f.mkdir();

		File[] files = f.listFiles(new FilenameContainsFilter(dataSetName + "_" + cubeVis1.getCurrentView().getName()));
		try {
			ImageIO.write(image, "png", new File(here.getAbsoluteFile() + "/screenshots/" + dataSetName + "/"
					+ dataSetName + "_" + cubeVis1.getCurrentView().getName() + "_" + files.length + ".png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void exportSVGTimeslices() {

		File f = new File("");
		f = new File(f.getAbsolutePath() + "/svg/");
		if (!f.exists())
			f.mkdir();

		int num = f.listFiles(new FilenameContainsFilter("TimeSlices")).length;
		f = new File(f.getAbsolutePath() + "/" + dataSetName + "-TimeSlices-" + num + ".svg");
		try {
			FileWriter w = new FileWriter(f);
			w.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

			MatrixCube cube = cubeVis1.getMatrixCube();
			float[] sPos, cPos, fPos;
			GraphSMView v = (GraphSMView) ViewManager.getInstance().getView(ViewManager.VIEW_TIME_SM);
			String line;
			int ySpace, xSpace = 0;
			int sliceCount = 0;
			int GAP = 10;
			for (Slice<?, ?> s : cube.getTimeSlices()) {
				xSpace = (sliceCount % v.colAmount);
				xSpace *= GAP;
				ySpace = (sliceCount / v.colAmount);
				ySpace *= GAP;

				sPos = Utils.add(Utils.add(v.getSlicePosition(s), new float[] { 0f, -ySpace + 0.0f, xSpace + 0.0f }),
						new float[] { 0, cubeVis1.getSliceHeight(s) / 2f, -cubeVis1.getSliceWidth(s) / 2 });

				// Matrix frame
				line = "<rect x=\"" + sPos[2] + "\" y=\"" + -sPos[1] + "\" width=\"" + cubeVis1.getSliceWidth(s)
						+ "\" height=\"" + cubeVis1.getSliceHeight(s)
						+ "\" fill=\"none\" style=\"stroke:rgb(0,0,0);stroke-width:0.2\"/>";
				w.append("\t" + line + "\n");

				// FRAME CUT
				fPos = Utils.add(sPos, new float[] { 0, 3, -2 });
				line = "<rect x=\"" + fPos[2] + "\" y=\"" + -fPos[1] + "\" width=\"" + (cubeVis1.getSliceWidth(s) + 7)
						+ "\" height=\"" + (cubeVis1.getSliceHeight(s) + 3)
						+ "\" fill=\"none\" style=\"stroke:rgb(0,0,0);stroke-width:0.2\"/>";
				w.append("\t" + line + "\n");

				float width = 0;
				for (Cell c : s.getCells()) {
					cPos = Utils.add(c.getRelTimeSlicePos(),
							new float[] { sPos[2] + cubeVis1.getSliceWidth(s) / 2f - .5f,
									(sPos[1] - cubeVis1.getSliceHeight(s) / 2f + .5f), 0f });
					width = (float) Map.map(c.getData().getWeight(), cubeVis1.WEIGHT_MIN, cubeVis1.WEIGHT_MAX, 0.1, 1)
							* .8f;
					cPos = Utils.add(cPos, new float[] { .4f - width / 2f, -(.4f - width / 2f) });
					line = "<rect x=\"" + cPos[0] + "\" y=\"" + -cPos[1] + "\" width=\"" + width + "\" height=\""
							+ width + "\"/>";
					w.append("\t" + line + "\n");
				}

				// SLICE LABEL
				float[] p = new float[2];
				p[0] = sPos[2] + cubeVis1.getSliceWidth(s) + .5f;
				p[1] = (-sPos[1] - 1f);
				line = "<text x=\"" + p[0] + "\" y=\"" + p[1] + "\" font-size=\".9\">" + s.getLabel() + "</text>";
				w.append("\t" + line + "\n");

				// LABELS
				// NORTH
				Slice s2;
				float[] lPos;
				for (int col = 0; col < s.getColumnCount(); col++) {
					s2 = cube.getVisibleVNodeSlice(col);

					lPos = s.getRelGridCoords(0, col).clone();
					lPos[0] = sPos[2] + lPos[0] + cubeVis1.getSliceWidth(s) / 2f;
					lPos[1] = (-sPos[1] - .5f);
					line = "<text x=\"" + lPos[0] + "\" y=\"" + lPos[1] + "\" transform=\"rotate(-90 " + lPos[0] + ","
							+ lPos[1] + ")\" font-size=\".6\">" + s2.getLabel() + "</text>";
					w.append("\t" + line + "\n");
				}
				// EAST
				for (int row = 0; row < s.getRowCount(); row++) {
					s2 = cube.getVisibleHNodeSlice(row);

					lPos = s.getRelGridCoords(row, s.getColumnCount()).clone();
					lPos[0] = sPos[2] + lPos[0] + cubeVis1.getSliceWidth(s) / 2f;
					lPos[1] = -sPos[1] - lPos[1] + cubeVis1.getSliceHeight(s) / 2f;
					line = "<text x=\"" + lPos[0] + "\" y=\"" + lPos[1] + "\" font-size=\".6\">" + s2.getLabel()
							+ "</text>";
					w.append("\t" + line + "\n");
				}

				// HOLES
				w.append("\t<circle cx=\"" + (sPos[2] - 1) + "\" cy=\"" + (sPos[1] - .5f)
						+ "\" r=\"0.5\" fill-opacity=\"0.0\" style=\"stroke:rgb(0,0,0);stroke-width:0.1\"/>");
				w.append("\t<circle cx=\"" + (sPos[2] - 1) + "\" cy=\"" + (sPos[1] - (cubeVis1.getSliceHeight(s) - 1f))
						+ "\" r=\"0.5\" fill-opacity=\"0.0\" style=\"stroke:rgb(0,0,0);stroke-width:0.1\"/>");

				sliceCount++;
			}

			w.append("</svg>");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void exportSVGNodeslices() {

		File f = new File("");
		f = new File(f.getAbsolutePath() + "/svg/");
		if (!f.exists())
			f.mkdir();

		int num = f.listFiles(new FilenameContainsFilter("NodeSlices")).length;
		f = new File(f.getAbsolutePath() + "/" + dataSetName + "-NodeSlices-" + num + ".svg");
		try {
			FileWriter w = new FileWriter(f);
			w.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

			MatrixCube cube = cubeVis1.getMatrixCube();
			float[] sPos, cPos, fPos;
			NodeSMView v = (NodeSMView) ViewManager.getInstance().getView(ViewManager.VIEW_VNODESLICE_SM);
			String line;
			int ySpace, xSpace = 0;
			int sliceCount = 0;
			int GAP = 10;
			float width = 0;
			for (Slice<?, ?> s : cube.getVisibleVNodeSlices()) {
				xSpace = (sliceCount % v.colAmount);
				xSpace *= GAP;
				ySpace = (sliceCount / v.colAmount);
				ySpace *= GAP;
				Log.out(xSpace + " " + ySpace);
				sPos = Utils.add(Utils.add(v.getSlicePosition(s), new float[] { xSpace + 0.0f, -ySpace + 0.0f, 0f }),
						new float[] { 0, cubeVis1.getSliceHeight(s) / 2f, -cubeVis1.getSliceWidth(s) / 2 });

				// Matrix frame
				line = "<rect x=\"" + sPos[0] + "\" y=\"" + -sPos[1] + "\" width=\"" + cubeVis1.getSliceWidth(s)
						+ "\" height=\"" + cubeVis1.getSliceHeight(s)
						+ "\" fill=\"none\" style=\"stroke:rgb(0,0,0);stroke-width:0.2\"/>";
				w.append("\t" + line + "\n");

				// FRAME CUT
				fPos = Utils.add(sPos, new float[] { -2, 3, 0 });
				line = "<rect x=\"" + fPos[0] + "\" y=\"" + -fPos[1] + "\" width=\"" + (cubeVis1.getSliceWidth(s) + 7)
						+ "\" height=\"" + (cubeVis1.getSliceHeight(s) + 3)
						+ "\" fill=\"none\" style=\"stroke:rgb(0,0,0);stroke-width:0.2\"/>";
				w.append("\t" + line + "\n");

				// CELLS
				for (Cell c : s.getCells()) {
					cPos = Utils.add(c.getRelVNodeSlicePos(),
							new float[] { 0, (sPos[1] - cubeVis1.getSliceHeight(s) / 2f + .5f),
									sPos[0] + cubeVis1.getSliceWidth(s) / 2f - .5f });
					width = (float) Map.map(c.getData().getWeight(), cubeVis1.WEIGHT_MIN, cubeVis1.WEIGHT_MAX, 0.1, 1)
							* .8f;
					cPos = Utils.add(cPos, new float[] { 0, -(.4f - width / 2f), .4f - width / 2f });
					line = "<rect x=\"" + cPos[2] + "\" y=\"" + -cPos[1] + "\" width=\"" + width + "\" height=\""
							+ width + "\"/>";
					w.append("\t" + line + "\n");
				}

				// SLICE LABEL
				float[] p = new float[2];
				p[0] = sPos[0] + cubeVis1.getSliceWidth(s) + .5f;
				p[1] = (-sPos[1] - 1f);
				line = "<text x=\"" + p[0] + "\" y=\"" + p[1] + "\" font-size=\".9\">" + s.getLabel() + "</text>";
				w.append("\t" + line + "\n");

				// NORTH
				Slice s2;
				float[] lPos;
				for (int col = 0; col < s.getColumnCount(); col++) {
					s2 = cube.getTimeSlice(col);

					lPos = s.getRelGridCoords(0, col).clone();
					lPos[0] = sPos[0] + lPos[2] + cubeVis1.getSliceWidth(s) / 2f;
					lPos[1] = (-sPos[1] - .5f);
					line = "<text x=\"" + lPos[0] + "\" y=\"" + lPos[1] + "\" transform=\"rotate(-90 " + lPos[0] + ","
							+ lPos[1] + ")\" font-size=\".6\">" + s2.getLabel() + "</text>";
					w.append("\t" + line + "\n");
				}
				// EAST
				for (int row = 0; row < s.getRowCount(); row++) {
					s2 = cube.getVisibleHNodeSlice(row);

					lPos = s.getRelGridCoords(row, s.getColumnCount()).clone();
					lPos[0] = sPos[0] + lPos[2] + cubeVis1.getSliceWidth(s) / 2f;
					lPos[1] = -sPos[1] - lPos[1] + cubeVis1.getSliceHeight(s) / 2f;
					line = "<text x=\"" + lPos[0] + "\" y=\"" + lPos[1] + "\" font-size=\".6\">" + s2.getLabel()
							+ "</text>";
					w.append("\t" + line + "\n");
				}
				// HOLES
				w.append("\t<circle cx=\"" + (sPos[0] - 1) + "\" cy=\"" + (-sPos[1] + 1f)
						+ "\" r=\"0.5\" fill-opacity=\"0.0\" style=\"stroke:rgb(0,0,0);stroke-width:0.1\"/>");
				w.append("\t<circle cx=\"" + (sPos[0] - 1) + "\" cy=\"" + (-sPos[1] + (cubeVis1.getSliceHeight(s) - 1))
						+ "\" r=\"0.5\" fill-opacity=\"0.0\" style=\"stroke:rgb(0,0,0);stroke-width:0.1\"/>");

				sliceCount++;
			}

			w.append("</svg>");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
