package cubix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.HierarchicalCNode;
import cubix.dataSets.CSVGDataSet;
import cubix.helper.Map;
import cubix.helper.Utils;
import cubix.helper.histogram.ColorRetriever;
import cubix.helper.histogram.Histogram;
import cubix.transitions.Transition;
import cubix.view.CView;
import cubix.view.ViewManager;
import cubix.vis.Cell;
import cubix.vis.HNodeSlice;
import cubix.vis.Lasso;
import cubix.vis.Slice;
import cubix.vis.TimeSlice;
import cubix.vis.VNodeSlice;
import cubix.vis.slider.DefaultDoubleBoundedRangeModel;
import cubix.vis.slider.DoubleRangeSlider;

public class CubixVisInteractive extends CubixVis implements KeyListener, 
														 	 MouseListener, 
															 MouseMotionListener, 
															 MouseWheelListener
{
//	protected static CubixVisInteractive instance;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private static final int DRAG_DIR_XY = 0;
	private static final int DRAG_DIR_X = 1;
	private static final int DRAG_DIR_Y = 2;
	private int dragDirection = DRAG_DIR_XY;

	private float[] dragStart;
	private float[] dragPos_last = new float[]{0,0};

	
	private JButton cellSizeEncodingButton;
	private JButton reorderGlobalButton;
	private JButton reorderLocalButton;

	private JMenu cellEncodingComboBox;
	private JMenuBar menuBar;
	private JToggleButton cellTypeEncodingButton;
	private JButton reorderByNameButton;

	private JRadioButton order1;

	private JButton order2;

	private JButton order3;

	private JToggleButton weightAdaption;

	private JToggleButton hideFrame;

	private Histogram weightHistogram;

	private Histogram timeHistogram;

	private JCheckBox logScaleCheckBox;

	protected boolean shiftDown = false;
	protected boolean altDown = false;

	private MenuItem miHideFrame;

	private JCheckBox divergingScaleCheckBox;

	private JRadioButton color3;


	protected CubixVisInteractive()
	{
		super();

		controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(20, 20, 20, 20) );
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
		controlPanel.setPreferredSize(new Dimension(Cubix.CONTROL_PANEL_WIDTH, 10));
		
		// CELL ENCODING SELECTION

		//JLabel visMappLabel = new JLabel("=== Visual Mapping === ");
		//visMappLabel.getFont().deriveFont(Font.BOLD);
		controlPanel.add(new JLabel("=== Visual Mapping === "));
		controlPanel.add(Box.createVerticalStrut(10));		
		JLabel colorLabel = new JLabel("Cell Color Encoding:");
		controlPanel.add(colorLabel);
		controlPanel.add(Box.createVerticalStrut(10));
		JRadioButton color2 = new JRadioButton("Edge Weight (light to blue)");
		color2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellColorEncoding(ColorEncoding.WEIGHT);
			}});
		controlPanel.add(color2);
		JRadioButton color4 = new JRadioButton("Edge Weight Diverging (red, gray, blue)");
		color4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellColorEncoding(ColorEncoding.WEIGHT_DIV);
			}});
		controlPanel.add(color4);
		JRadioButton color1 = new JRadioButton("Alignment (distinct color per matcher)");
		color1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellColorEncoding(ColorEncoding.TIME);
			}});
		controlPanel.add(color1);
//		color1.setSelected(false);
		JRadioButton color3 = new JRadioButton("None (all same gray)");
		color3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellColorEncoding(ColorEncoding.NONE);
			}});
		controlPanel.add(color3);
//		color3.setSelected(false);
		
		ButtonGroup group = new ButtonGroup();
		group.add(color2);
		group.add(color4);
		group.add(color1);
		group.add(color3);
		color1.setSelected(true);
		
		
		// CELL SHAPE ENCODING SELECTION
		controlPanel.add(Box.createVerticalStrut(20));
		JLabel shapeLabel = new JLabel("Cell Size and Shape Encoding:");
		controlPanel.add(shapeLabel);
		controlPanel.add(Box.createVerticalStrut(10));

		JRadioButton shape1 = new JRadioButton("Edge Weight 1 (small to large)");
		shape1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellShapeEncoding(ShapeEncoding.WEIGHT);
			}});
		shape1.setSelected(true);
		controlPanel.add(shape1);
		JRadioButton shape2 = new JRadioButton("Edge Weight 2 (small to large)");
		shape2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellShapeEncoding(ShapeEncoding.CONE);
			}});
		shape2.setSelected(false);
		controlPanel.add(shape2);
		JRadioButton shape3 = new JRadioButton("None (equal size)");
		shape3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCellShapeEncoding(ShapeEncoding.NONE);
			}});
		shape3.setSelected(false);
		controlPanel.add(shape3);
		
		ButtonGroup shapeGroup = new ButtonGroup();
		shapeGroup.add(shape1);
		shapeGroup.add(shape2);
		shapeGroup.add(shape3);
		
		//controlPanel.add(Box.createVerticalStrut(10));
		
		controlPanel.add(Box.createVerticalStrut(20));
		JLabel scaleLabel = new JLabel("Cell Size Scale:");
		controlPanel.add(scaleLabel);
		controlPanel.add(Box.createVerticalStrut(10));
		
		weightAdaption = new JCheckBox("Adapt Weight");
		controlPanel.add(weightAdaption);
		weightAdaption.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setWeightAdaption(weightAdaption.isSelected());
				display();
			}});

		logScaleCheckBox = new JCheckBox("Logarithmic scale");
		controlPanel.add(logScaleCheckBox);
		logScaleCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				logScale = logScaleCheckBox.isSelected();
				display();
			}});

		
		divergingScaleCheckBox = new JCheckBox("Diverging scale");
		controlPanel.add(divergingScaleCheckBox);
		divergingScaleCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				divergingScale = divergingScaleCheckBox.isSelected();
				display();
			}});
		
		
		controlPanel.add(Box.createVerticalStrut(20));
		controlPanel.add(new JLabel("Cell Opacity Slider:"));
		controlPanel.add(Box.createVerticalStrut(10));
		JPanel sliderPanel2 = new JPanel();
		sliderPanel2.setMaximumSize(new Dimension(300, 400));
		sliderPanel2.setLayout(new BoxLayout(sliderPanel2,BoxLayout.PAGE_AXIS));
		opacityRangeSlider = new DoubleRangeSlider(0, 1, 0, 1);
		opacityRangeSlider.setUserSize(30, 20);
		opacityRangeSlider.setEnabled(true);
		opacityRangeSlider.setLowLabel("F");
		opacityRangeSlider.setHighLabel("V");
		sliderPanel2.add(opacityRangeSlider);
		controlPanel.add(sliderPanel2);
		opacityRangeSlider.addMouseListener(timeRangeSlider);
		opacityRangeSlider.getModel().setMinimum(0);
		opacityRangeSlider.getModel().setMaximum(1);
		opacityRangeSlider.getModel().addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e) 
			{
				float v;
				float w;
				for(Cell c : matrixCube.getCells()){
					w = c.getData().getWeight();
					if(w <= WEIGHT_MAX && w >= WEIGHT_MIN){
						v = (float) opacityRangeSlider.getHighValue();
						c.setTranslucency(v);
					}
				}				
//				_updateCellVisibility();
				requestFocus();
				display();
			}	
		});


		// REORDER BUTTON
//		controlPanel.add(Box.createVerticalStrut(20));
//		controlPanel.add(Box.createVerticalStrut(10));
//		controlPanel.add(Box.createVerticalStrut(10));

		order2 = new JButton("Topological Order");
		order2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Collection<CEdge> edges = new HashSet<CEdge>();
				CEdge edge;
				for(int t=(int) timeRangeSlider.getLowValue(); t < timeRangeSlider.getHighValue() ; t++){
					for(Cell c : matrixCube.getTimeSlice(t).getCells()){
						edge = c.getData();
						if(edge.getWeight() >= weightRangeSlider.getLowValue()
								&& edge.getWeight() <= weightRangeSlider.getHighValue())
						{
							edges.add(edge);
						}
					}
				}
				reorder(edges);
			}});
//		controlPanel.add(order2);
		
		order3 = new JButton("Name Ordering");
		order3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(reorderByName());	
					order3.setSelected(false);
				}
			});
//		controlPanel.add(order3);
	
		ButtonGroup orderGroup = new ButtonGroup();
		orderGroup.add(order1);
		orderGroup.add(order2);
		orderGroup.add(order3);
		
		
//		JButton timeReorderBtn = new JButton("Order times");
//		controlPanel.add(timeReorderBtn);
//		timeReorderBtn.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				Collection<CEdge> edges = new HashSet<CEdge>();
//				CEdge edge;
//				for(int t=(int) timeRangeSlider.getLowValue(); t < timeRangeSlider.getHighValue() ; t++){
//					for(Cell c : matrixCube.getTimeSlice(t).getCells()){
//						edge = c.getData();
//						if(edge.getWeight() >= weightRangeSlider.getLowValue()
//								&& edge.getWeight() <= weightRangeSlider.getHighValue())
//						{
//							edges.add(edge);
//						}
//					}
//				}
//				reorderTime(edges);
//			}});


		controlPanel.add(Box.createVerticalStrut(30));
		controlPanel.add(new JLabel("=== Alignment Manipulation ==="));
		controlPanel.add(Box.createVerticalStrut(10));

		int sliderWidth = 30;
		int sliderHeight = 20;
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.setMaximumSize(new Dimension(300, 400));
		sliderPanel.setLayout(new BoxLayout(sliderPanel,BoxLayout.PAGE_AXIS));
		
		JButton performanceMetricsButton = new JButton("Reorder by Alignment Metrics");
		performanceMetricsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				JDialog dialog = new JDialog(Cubix.frame, "Performance Metrics", false);
				JPanel contentPanel = new JPanel();
				contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
				
				ArrayList<String[]> performanceMetrics = CSVGDataSet.getPerformanceMetricsData();
				if (!performanceMetrics.isEmpty()) {
					
					String[][] data = new String[performanceMetrics.size()-1][4];
					for (int i = 0; i < performanceMetrics.size()-1; i++) {
						data[i][0] = performanceMetrics.get(i+1)[0];
						data[i][1] = performanceMetrics.get(i+1)[1];
						data[i][2] = performanceMetrics.get(i+1)[2];
						data[i][3] = performanceMetrics.get(i+1)[3];
					}
					
			        DefaultTableModel model = new DefaultTableModel(data, performanceMetrics.get(0)) {
			            @Override
			            public Class getColumnClass(int column) {
			                switch (column) {
			                    case 0:
			                        return String.class;
			                    case 1:
			                        return String.class;
			                    case 2:
			                        return String.class;
			                    default:
			                        return String.class;
			                }
			            }
			        };
			        JTable table = new JTable(model);
			        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
			        table.setFillsViewportHeight(true);
			        table.setAutoCreateRowSorter(true);
			        
			        DefaultTableCellRenderer columnNamesRenderer = new DefaultTableCellRenderer();
			        columnNamesRenderer.setHorizontalAlignment( JLabel.LEFT );
			        table.getColumnModel().getColumn(0).setCellRenderer( columnNamesRenderer );
			        
			        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
			        table.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			        table.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
			        table.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
			        
		        
			        JScrollPane scrollPane = new JScrollPane(table);
			        contentPanel.add(scrollPane);
			        
			        JButton reorderAlignments = new JButton("Reorder Alignments in the Cube");
			        reorderAlignments.setAlignmentX(Component.CENTER_ALIGNMENT);
					reorderAlignments.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							
							int rowCount = model.getRowCount();
							String[] order = new String[rowCount];
							for (int i = 0; i < rowCount; i++) {
								// collect all values from the first column
								order[i] = (String) model.getValueAt(table.convertRowIndexToModel(i), 0);
								//System.out.println("button "+order[i]);
							}
							
							//String[] orderr = new String[]{"AML-2013", "AML-2014", "AML-2015",
							//"LogMap-2011", "LogMap-2012", "LogMap-2013", "LogMap-2014", "LogMap-2015",	
							//"LogMapC-2014",	"LogMapC-2015",	
							//"LogMapLite-2012", "LogMapLite-2013", "LogMapLite-2014", "LogMapLite-2015",
							//"OAEI-RA-2016"};
							
							reorderAlignments(order);
						}
					});	
			        contentPanel.add(reorderAlignments);
				} else {
					contentPanel.add(new JLabel("The file with performance metrics has not been loaded."));
				}
		        
				dialog.setContentPane(contentPanel);
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		sliderPanel.add(performanceMetricsButton);		
		
	
		sliderPanel.add(Box.createVerticalStrut(20));
//		controlPanel.add(new JLabel("=== CELL VISIBILITY ==="));
		//controlPanel.add(Box.createVerticalStrut(10));

		// TIME RANGE SLIDER
		sliderPanel.add(new JLabel("Alignment Range Filter:"));
		sliderPanel.add(Box.createVerticalStrut(10));

				timeRangeSlider = new DoubleRangeSlider(0, 1, 0, 1);
				timeRangeSlider.setUserSize(sliderWidth, sliderHeight);
				timeRangeSlider.setEnabled(true);
				sliderPanel.add(timeRangeSlider);
				timeRangeSlider.addMouseListener(timeRangeSlider);
				timeRangeSlider.getModel().addChangeListener(new ChangeListener()
				{
					public void stateChanged(ChangeEvent e) 
					{
						timeRangeSlider.setLowValue((int)timeRangeSlider.getLowValue());
						timeRangeSlider.setHighValue((int)timeRangeSlider.getHighValue());
						int timeMin = (int) timeRangeSlider.getLowValue();
						int timeMax = (int) timeRangeSlider.getHighValue();
						if(timeMax - timeMin == 0) { 
							timeRangeSlider.setHighValue(timeRangeSlider.getHighValue()+1);
							return;
						}
						CTime t;
						timeSliderTimes.clear();				
						for(TimeSlice s: matrixCube.getTimeSlices())
						{	
							t = s.getData();
							if(matrixCube.getTimeIndex(t) >= timeMin && matrixCube.getTimeIndex(t) < timeMax)
							{
								timeSliderTimes.add(t);
							}
						}
//						_updateCellVisibility();

						requestFocus();
						display();
					}	
				});
//		timeHistogram = new Histogram(100, 10, new ColorRetriever(){
//			@Override
//			public Color getColor(float v_min, float v_max) {
//				float[] c = timeColors.get((int) v_min);
//				return new Color(c[0], c[1], c[2]);
//			}});

		sliderPanel.add(Box.createVerticalStrut(20));
		sliderPanel.add(new JLabel("Alignment Mode:"));
		sliderPanel.add(Box.createVerticalStrut(10));
		JRadioButton similarityValuesButton = new JRadioButton("Similarities");
		similarityValuesButton.setSelected(true);
		sliderPanel.add(similarityValuesButton);
		similarityValuesButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				alignmentMode = AlignmentMode.SIMILARITY_VALUES;
				
				updateWeightRangeSlider();
				
				display();
			}});	
		
		JRadioButton mappingsButton = new JRadioButton("Mappings");
		sliderPanel.add(mappingsButton);
		mappingsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				alignmentMode = AlignmentMode.SUM_MAPPINGS;
				
				updateWeightRangeSlider();
				
				display();
			}
		});
		
		ButtonGroup alignmentModeButtonGroup = new ButtonGroup();
		alignmentModeButtonGroup.add(similarityValuesButton);
		alignmentModeButtonGroup.add(mappingsButton);
		sliderPanel.add(Box.createVerticalStrut(20));
		
		sliderPanel.add(new JLabel("Cell Weight Filter:"));
		//sliderPanel.add(Box.createVerticalStrut(10));

		// WEIGHT RANGE SLIDER
		weightRangeSlider = new DoubleRangeSlider(0, 1, 0, 1);
		weightRangeSlider.setUserSize(sliderWidth, sliderHeight+50);
		weightRangeSlider.setEnabled(true);
		sliderPanel.add(weightRangeSlider);
		weightRangeSlider.addMouseListener(weightRangeSlider);
		weightRangeSlider.getModel().addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e) 
			{
				if(currentView != null){
//					log.writeNext(new String[]{System.currentTimeMillis() + "", currentView.name, "", "", "", "", weightRangeSlider.getLowValue() + "", weightRangeSlider.getHighValue() + ""});
//					try {
//						log.flush();
//					} catch (IOException e1) { e1.printStackTrace();}
				}
				//DefaultDoubleBoundedRangeModel sliderModel = (DefaultDoubleBoundedRangeModel) e.getSource();
				//System.out.println(sliderModel);
				cellsInWeightRange = new HashSet<Cell>();
				for(Cell c : matrixCube.getCells())
				{
					//if (isVisible(c)) {
						// VI TODO VI look again weightRangeSlider and how it works - which distribution to show - only current or the entire
						if (alignmentMode == AlignmentMode.SIMILARITY_VALUES) {
							//System.out.println(c.getData().getID());

							if(c.getData().getWeight() >= weightRangeSlider.getLowValue()
									&& c.getData().getWeight() <= weightRangeSlider.getHighValue()){
								cellsInWeightRange.add(c);
							}
						} else {
							if(c.getData().getAccumulatedWeight() >= weightRangeSlider.getLowValue()
									&& c.getData().getAccumulatedWeight() <= weightRangeSlider.getHighValue()){
								cellsInWeightRange.add(c);
							}
						}
					//}
				}
//				_updateCellVisibility();
				requestFocus();
				display();
			}	
		});
		weightHistogram = new Histogram(255, 40, new ColorRetriever(){
			@Override
			public Color getColor(float v_min, float v_max) {
				float[] c = _getColor(currentEdgeWeightColorScale,  (float) Map.map(v_min, WEIGHT_MIN, WEIGHT_MAX, 0.0, 1));
				return new Color(c[0], c[1], c[2]);
			}});
		weightRangeSlider.setHistogram(weightHistogram);
		
		
		inverseFilterCheckBox = new JCheckBox("Inverse Filter");
		inverseFilterCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				display();
			}
		});
		//controlPanel.add(inverseFilterCheckBox);
		
		
/*		JButton reorderAlignments = new JButton("Reorder Alignments");
		reorderAlignments.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				String[] order = new String[]{"AML-2013", "AML-2014", "AML-2015",
				"LogMap-2011", "LogMap-2012", "LogMap-2013", "LogMap-2014", "LogMap-2015",	
				"LogMapC-2014",	"LogMapC-2015",	
				"LogMapLite-2012", "LogMapLite-2013", "LogMapLite-2014", "LogMapLite-2015",
				"OAEI-RA-2016"};
				
				reorderAlignments(order);
			}
		});		
		sliderPanel.add(reorderAlignments);*/
		
		// OPACITY RANGE SLIDER
//		sliderPanel.add(Box.createVerticalStrut(10));
//		sliderPanel.add(new JLabel("Cell Opacity:"));
//		sliderPanel.add(Box.createVerticalStrut(10));
//		opacityRangeSlider = new DoubleRangeSlider(0, 1, 0, 1);
//		opacityRangeSlider.setUserSize(sliderWidth, sliderHeight);
//		opacityRangeSlider.setEnabled(true);
//		opacityRangeSlider.setLowLabel("F");
//		opacityRangeSlider.setHighLabel("V");
//		sliderPanel.add(opacityRangeSlider);
//		opacityRangeSlider.addMouseListener(timeRangeSlider);
//		opacityRangeSlider.getModel().setMinimum(0);
//		opacityRangeSlider.getModel().setMaximum(1);
//		opacityRangeSlider.getModel().addChangeListener(new ChangeListener()
//		{
//			public void stateChanged(ChangeEvent e) 
//			{
//				float v;
//				float w;
//				for(Cell c : matrixCube.getCells()){
//					w = c.getData().getWeight();
//					if(w <= WEIGHT_MAX && w >= WEIGHT_MIN){
//						v = (float) opacityRangeSlider.getHighValue();
//						c.setTranslucency(v);
//					}
//				}				
////				_updateCellVisibility();
//				requestFocus();
//				display();
//			}	
//		});
		
		controlPanel.add(sliderPanel);
		sliderPanel.add(Box.createVerticalStrut(10));
	
		// VI removed from the UI
		selfEdgeVisibilityCheckBox = new JCheckBox("Show Self Edges");
		//selfEdgeVisibilityCheckBox.addActionListener(new ActionListener(){
		//	public void actionPerformed(ActionEvent e) {
		//		updateSelfEdgeVisibility();
		//	}});
		selfEdgeVisibilityCheckBox.setSelected(true);
		//controlPanel.add(selfEdgeVisibilityCheckBox);
		
		nonSelfEdgeVisibilityCheckBox = new JCheckBox("Show Non-Self Edges");
		//nonSelfEdgeVisibilityCheckBox.addActionListener(new ActionListener(){
		//	public void actionPerformed(ActionEvent e) {
		//		updateSelfEdgeVisibility();
		//	}});
		nonSelfEdgeVisibilityCheckBox.setSelected(true);
		//controlPanel.add(nonSelfEdgeVisibilityCheckBox);
		
		
		
		controlPanel.add(Box.createVerticalStrut(20));
		controlPanel.add(new JLabel("=== Misc ==="));
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(new JLabel("Animation Speed:"));
		controlPanel.add(Box.createVerticalStrut(10));
		transitionSpeedSlider = new JSlider(1, 30, 25);
		Hashtable<Integer, JLabel> labels =
				new Hashtable<Integer, JLabel>();
				labels.put(1, new JLabel("Slow"));
				labels.put(30, new JLabel("Fast"));
		transitionSpeedSlider.setLabelTable(labels);
		transitionSpeedSlider.setPaintLabels(true);
		transitionSpeedSlider.setMajorTickSpacing(5);
		transitionSpeedSlider.setPaintTicks(true);
		transitionSpeedSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				requestFocus();
			}});
		controlPanel.add(transitionSpeedSlider);

		
	
		//// MENU BAR
		
		MenuBar mbar = new MenuBar();
		Cubix.frame.setMenuBar(mbar);
		
		
		// FILE MENU
		Menu m = new Menu("File");
		mbar.add(m);
		
		// New data set
		MenuItem mi = new MenuItem("New Data");
		mi.setShortcut(new MenuShortcut(KeyEvent.VK_N, false));
		m.add(mi);
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Cubix.restart();
			}
		});
		
		MenuItem screenshotMI = new MenuItem("Take Screenshot");
		screenshotMI.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Cubix.createScreenshot();
			}
		}); 
		screenshotMI.setShortcut(new MenuShortcut(KeyEvent.VK_S, false));
		m.add(screenshotMI);
		
		
		
		
		m = new Menu("View");
		mbar.add(m);
		
		// New data set
		miHideFrame = new MenuItem("Hide Frame");
		m.add(miHideFrame);
		miHideFrame.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				setFrameVisibility(!getFrameVisibility());
				if(getFrameVisibility()){
					miHideFrame.setLabel("Hide Frame");
				}else{
					miHideFrame.setLabel("Show Frame");
				}
				display();
			}
		});
		
		m = new Menu("Export");
		mbar.add(m);
		
		mi = new MenuItem("Time Slices (SVG)");
		m.add(mi);
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Cubix.exportSVGTimeslices();
			}
		});
		
		mi = new MenuItem("Time Slice Similarity (JSON)");
		mi.setShortcut(new MenuShortcut(KeyEvent.VK_J, false));
		m.add(mi);
//		mi.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				DynamicGraphExporter dge = new DynamicGraphExporter();
//				dge.export(Cubix.dataSetName, matrixCube.getTimeGraph());
//				JOptionPane.showMessageDialog(Cubix.frame, "Exported to : " + dge.jsonFileName());
//			}
//		});
		
		mi = new MenuItem("Graph as CSV");
		m.add(mi);
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				CSVGDataSet.exportGraph(tGraph, Cubix.frame , Cubix.dataSetName, false);
			}
		});
		mi = new MenuItem("Graph 3D model as OBJ");
		m.add(mi);
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				CSVGDataSet.exportOBJ(tGraph, Cubix.frame , Cubix.dataSetName, false);
			}
		});

		
		requestFocus();
	} 

	
		public void mouseClicked(MouseEvent e)
		{ 
			this.requestFocus();
		
			dlMode = DLMode.CELL;
			switchToSliceDLMode = false;
			switchToCubeDLMode = false;
			if(hoveredCell != null)
			{
				if(hoveredCell == selectedCell)
				{
					if(queryMode == Query.SLICES){
						// Show vectors for same cells
						queryMode = Query.VECTORS;
					}else{
						// Deselect cell.
						queryMode = Query.CELLS;
						selectedCell = null;
						selectedHNodes.clear();
						selectedVNodes.clear();
						selectedTimes.clear();
					}
				}else{
					// Select cell and show corresponding slices
					queryMode = Query.SLICES;
					selectedCell = hoveredCell;
					selectedHNodes.clear();
					selectedHNodes.add(selectedCell.getHNodeSlice().getData());
					selectedVNodes.clear();
					selectedVNodes.add(selectedCell.getVNodeSlice().getData());
					selectedTimes.clear();
					selectedTimes.add(selectedCell.getTimeSlice().getData());
				}
			}else
			if(hoveredSlice != null)
			{
				
				/* double click on a label (but not time label) expands or collapses it */
				if(e.getClickCount() == 2) {
					
					HierarchicalCNode hoveredNode = null;
					if(hoveredSlice instanceof VNodeSlice || hoveredSlice instanceof HNodeSlice){
						hoveredNode = (HierarchicalCNode) hoveredSlice.getData();
					} 
					
					if(hoveredNode != null) {
						if(!hoveredNode.isExpanded())
							expandNode(hoveredNode);
						else
							collapseNode(hoveredNode);
					}
				}
				
	    		if(e.getButton() == 1)
	    		{
	    			if(e.isShiftDown()){
	    				if(coloredSlices.containsKey(hoveredSlice))
	    				{
	    					selectionColors.add(coloredSlices.get(hoveredSlice));
	    					coloredSlices.remove(hoveredSlice);
	    				}else{
	    					if(selectionColors.size() == 0){
	    						selectionColors.add(new float[]{(float)Math.random(),(float)Math.random(),(float)Math.random()});
	    					}
	    					coloredSlices.put(hoveredSlice, selectionColors.get(0));
	    					selectionColors.remove(0);
	    				}
	    			}else if(e.isControlDown()){
	    				if(!isSelectedSlice(hoveredSlice))
	    				{
	    					selectSlice(hoveredSlice, false);
	    				}else{
	    					selectSlice(hoveredSlice, true);
	    				}
	    				
	    			}else{
	    				if(isSelectedSlice(hoveredSlice))
	    				{
	    					selectSlice(hoveredSlice, false);
	    				}else{
	    					selectSlice(hoveredSlice, true);
	    				}
	    			}
	    		}
	    		else if(e.getButton() == 3)
				{
	    			if(isFrontView()
					|| isSourceSideView()
					|| isTargetSideView())
					{
						if(currentView == vm.getView(vm.VIEW_FRONT))
						{
							if(!(hoveredSlice instanceof VNodeSlice)) return;
							setSliceMode(SliceMode.VNODE);
						}
						else
						if(currentView == vm.getView(vm.VIEW_SOURCE_SIDE))
						{
							if(!(hoveredSlice instanceof TimeSlice)) return;
							setSliceMode(SliceMode.TIME);
						}
						else if (currentView == vm.getView(vm.VIEW_TARGET_SIDE))
						{
							if(!(hoveredSlice instanceof TimeSlice)) return;
							setSliceMode(SliceMode.TIME);
						}
						
						switchToSliceDLMode = true;
						
						int dur = (int) (400 * getTransitionDurationFactor());
						Transition t = null;
						if(!rotatedSlices.contains(hoveredSlice))
						{
							if(hoveredSlice instanceof VNodeSlice){
								t = tm.getRotateSliceTransition(renderedSlices, hoveredSlice, dur, 90, 1f);
							}else if(hoveredSlice instanceof TimeSlice) {
								t = tm.getRotateSliceTransition(renderedSlices, hoveredSlice, dur, -90, 1f);
							}
//							else if(hoveredSlice instanceof HNodeSlice){
//								t = tm.getRotateHSliceTransition(renderedSlices, hoveredSlice, dur, 90);
//							}
							if(t == null) return;
							
							rotatedSlices.add(hoveredSlice);
						}else{
							if(hoveredSlice instanceof VNodeSlice){
								t = tm.getRotateSliceTransition(renderedSlices, hoveredSlice, dur, -90, 1f);
							}else if (hoveredSlice instanceof TimeSlice){
								t = tm.getRotateSliceTransition(renderedSlices, hoveredSlice, dur, 90, 1f);
							}
//							else if(hoveredSlice instanceof HNodeSlice){
//								t = tm.getRotateHSliceTransition(renderedSlices, hoveredSlice, dur, -90);
//							}
							if(t == null) return;

							rotatedSlices.remove(hoveredSlice);
						}
						t.addListener(this);
						tm.startTransition(t);
						
					}else if(currentView == vm.getView(ViewManager.VIEW_TIME_SM)
							|| currentView == vm.getView(ViewManager.VIEW_VNODESLICE_SM) 
							|| currentView == vm.getView(ViewManager.VIEW_HNODESLICE_SM)){
						_closeUp(hoveredSlice);
					}
					
					
					if(isCubeView())
					{
						contextMenu = new JPopupMenu();
//						
//						if(queriedSlices.contains(hoveredSlice)){
//							JMenuItem reserColor = new JMenuItem("Reset Color");
//							reserColor.addActionListener(new ResetColorSliceAction(this, hoveredSlice));
//							contextMenu.add(reserColor);
//							contextMenu.addSeparator();
//						}
//						
//						JMenuItem color1 = new JMenuItem("Color Blue");
//						color1.addActionListener(new ColorSliceAction(this, hoveredSlice, Colors.BLUE));
//						color1 = new JMenuItem("Color Turquise");
//						color1.addActionListener(new ColorSliceAction(this, hoveredSlice, Colors.TURQ));
//						contextMenu.add(color1);
//						color1 = new JMenuItem("Color Green");
//						color1.addActionListener(new ColorSliceAction(this, hoveredSlice, Colors.GREEN));
//						contextMenu.add(color1);
//						color1 = new JMenuItem("Color Orange");
//						color1.addActionListener(new ColorSliceAction(this, hoveredSlice, Colors.ORANGE));
//						contextMenu.add(color1);
//						color1 = new JMenuItem("Color Red");
//						color1.addActionListener(new ColorSliceAction(this, hoveredSlice, Colors.RED));
//						contextMenu.add(color1);
//						
//						contextMenu.show(getParent(), e.getX(), e.getY());
//						contextMenu.setVisible(true);
//						
//					_closeUp(hoveredSlice);
						
					}
				}
	    	}
			else if(cubelet.isGraphFace(e.getX(), e.getY())){
				cubelet.highlightGraphFace();
				if(!isTimeSMView()){
					goToView(vm.getView(ViewManager.VIEW_FRONT));
				}
			}else if(cubelet.isTimeFace(e.getX(), e.getY())){
				cubelet.highlightTimeFace();
				if(!isVNodeSliceSMView()){
					goToView(vm.getView(ViewManager.VIEW_SOURCE_SIDE));
				}
			}else if(cubelet.isCubeFace(e.getX(), e.getY())){
				cubelet.highlightAllFaces(); 
				//if (!isHNodeSliceSMView()) { // VI UNCOMMENT for introducing other views 
				//	goToView(vm.getView(ViewManager.VIEW_TARGET_SIDE));
				//}
				if(!isCubeView()){
					goToView(vm.getView(ViewManager.VIEW_CUBE));
				}
			}else if(rotatedSlices.size() > 0 && hoveredCell != null){
//				for(Slice<?,?> s : rotatedSlices)
//				{
//					if(s.containsCell(hoveredCell))
//					{
//						Transition t;
//						if(s instanceof HNodeSlice){
//							t = tm.getRotateHSliceTransition(matrixCube.getHNodeSlices(), s, 1000, 90);
//						}else{
//							t = tm.getRotateSliceTransition(matrixCube.getTimeSlices(), s, 1000, 90, 1f);
//						}
//						tm.startTransition(t);
//						rotatedSlices.remove(s);
//					}
//				}
			}else{
				selectedCell = null;
				queryMode = Query.CELLS;
				Lasso.cells.clear();
				this.selectedHNodes.clear();
				this.selectedVNodes.clear();
				this.selectedTimes.clear();
			}
	    	
			display();
	    }
		
		public void mouseEntered(MouseEvent e){ }
		public void mouseExited(MouseEvent arg0){ }
		public void mousePressed(MouseEvent e) 
		{
			dragStart = new float[]{e.getX(),e.getY()};
			switchToSliceDLMode = true;
			
			dragPos_last = dragStart.clone();
	
			if(e.isAltDown())
			{
				Lasso.state  = Lasso.STATE_ADD_POINT;
			}
		}
		public void mouseReleased(MouseEvent arg0) 
		{ 
			dragStart = new float[]{0,0};
			dlMode = DLMode.CELL;
			
			// finalize convexHull
			if(Lasso.state == Lasso.STATE_ADD_POINT)
			{
				Lasso.state = Lasso.STATE_CALCULATE;
				display();
			}
		}
		public void mouseDragged(MouseEvent e) 
		{
			dragDirScreen = new float[]{dragPos_last[X] - e.getX(), dragPos_last[Y] - e.getY()};
			dragPos_last = new float[]{e.getX(), e.getY()};
			
			if(Lasso.state == Lasso.STATE_ADD_POINT)
			{
//				Log.out(this, "add lasso pooint");
				xPick = e.getX();
				yPick = e.getY();
				doPicking = true;
				display();
				return;
			}
			
			// TEST IF CUBELET CUBE IS HOVERED
			if(cubelet.isGraphFace(e.getX(), e.getY())){
				cubelet.highlightGraphFace();
				if(!isTimeSMView()){
					goToView(vm.getView(ViewManager.VIEW_TIME_SM));
				}
			}
			else if(cubelet.isTimeFace(e.getX(), e.getY())){
				cubelet.highlightTimeFace();
				if(!isVNodeSliceSMView()){
					goToView(vm.getView(ViewManager.VIEW_VNODESLICE_SM));
				}
			} 
			else if(cubelet.isCubeFace(e.getX(), e.getY())){
				cubelet.highlightAllFaces();
				if(!isHNodeSliceSMView()){
					goToView(vm.getView(ViewManager.VIEW_HNODESLICE_SM));
				}
			} 
			// DO OTHER STUFF...
			else
			{
				// ROTATE CAMERA
				if(e.getModifiers() == MouseEvent.BUTTON1_MASK)
				{
					if(hoveredSlice == null && isCubeView())
					{
						if(dragDirection == DRAG_DIR_XY && e.isShiftDown())
						{
							if(Math.abs(dragDirScreen[X]) > Math.abs(dragDirScreen[Y])){
								dragDirection = DRAG_DIR_X;
							}else{
								dragDirection = DRAG_DIR_Y;
							}
						}
						
						float dist = Utils.length(camera.getPos());
						Rotation r;
						Vector3D v = new Vector3D(camera.getPos()[X], camera.getPos()[Y], camera.getPos()[Z]);
						if(dragDirection != DRAG_DIR_Y){
							// ROTATION in X direction
							r = new Rotation(new Vector3D(0,1,0), dragDirScreen[X] * Math.PI / 180 );
							v = new Vector3D(camera.getPos()[X], camera.getPos()[Y], camera.getPos()[Z]);
							v = r.applyTo(v);
						}
						
						if(dragDirection != DRAG_DIR_X){
							// ROTATION in Y direction
							float[] ray = Utils.dir(Utils.getFloat(v), camera.getLookAt());
							float[] a = Utils.cross(ray, Y_AXIS);
							float angle = (float) (dragDirScreen[Y] * Math.PI / 180);
							float[] vec = new float[]{a[X],a[Y],a[Z]};
							if(Utils.length(vec) < 0.1) 
								return;
							r = new Rotation(new Vector3D(a[X],a[Y],a[Z]), angle );
							v = r.applyTo(v);
						}
						
						// Set camera position
						float p[] = new float[]{(float) v.getX(), (float) v.getY(), (float) v.getZ()};
						// Bound camera so that cube cannot be seen from back side.
//						if(isCubeView()){
//							if(p[X] > 0.1f) return;
//							if(p[Y] < 0.1f) return;			
//							if(p[Z] < 0.1f) return;					
//						}
						camera.setPos(p); 
						camera.setLookAt(new float[]{0f,0f,0f,0f});
						
					}
					else
					{
// 						if(currentView == vm.getView(vm.VIEW_GRAPH_SM)
//						|| currentView == vm.getView(vm.VIEW_NODE_SM))
//						{
//							float a = 5;
//							float[] newPos;
//							if(this.isTSliceMode())	
//								newPos = Utils.add(slicePos.get(hoveredSlice), new float[]{0f, dragDirScreen[Y]/a, -dragDirScreen[X]/a});
//							else	
//								newPos = Utils.add(slicePos.get(hoveredSlice), new float[]{-dragDirScreen[X]/a, dragDirScreen[Y]/a, 0});
//							
//							float[] dist;
//							for(Slice s : renderedSlices)
//							{
//								if(hoveredSlice == s) continue;
//								
//								dist = Utils.dir(slicePos.get(s), newPos);
//								if( Math.abs(dist[Y]) < 5
//										&& ((isTSliceMode() && Math.abs(dist[X]) < 5)
//												|| (isVSliceMode()  && Math.abs(dist[Z]) < 5)))
//								{
//									newPos = slicePos.get(s);
////									setColorEntireSlice(s, Colors.DIFF_SECOND);
////									setColorEntireSlice(hoveredSlice, Colors.DIFF_FIRST);
//									break;
//								}
//							}
//							
//							slicePos.put(hoveredSlice, newPos);
//						}
					}
				}
				else if(e.getModifiers() == MouseEvent.BUTTON3_MASK)
				{
					if(hoveredSlice == null)
					{
						// PAN
						if(dragDirection == DRAG_DIR_XY && e.isShiftDown())
						{
							if(dragDirScreen[X] > dragDirScreen[Y]){
								dragDirection = DRAG_DIR_X;
							}else{
								dragDirection = DRAG_DIR_Y;
							}
						}
						float[] ray = Utils.dir(camera.getPos(), camera.getLookAt());
						float[] a = Utils.normalize(Utils.cross(ray, Y_AXIS));
						float[] b = Utils.normalize(Utils.cross(ray, a));
						float[] offset = Utils.add(Utils.mult(a, dragDirScreen[X]), Utils.mult(b, dragDirScreen[Y]));
						
						switch(dragDirection){
						case DRAG_DIR_X: offset[Y] =0; break;
						case DRAG_DIR_Y: offset[X] =0; break;
						}
						
						// Adaption 
//						float d = Utils.length(Utils.dir(camera.getLookAt(), camera.getPos()));
//						offset = Utils.mult(offset, d/100);
						
						camera.setPos(Utils.add(camera.getPos(), offset));
						camera.setLookAt(Utils.add(camera.getLookAt(), offset));
					}
				}
				
			}
			
			
			
			display();
		}
		public void mouseMoved(MouseEvent e) 
		{
			hoveredCell = null;
			hoveredSlice = null;
	
			if(cubelet == null) return;
			
				cubelet.resetAllFaces();
			
			if(cubelet.isGraphFace(e.getX(), e.getY())){
				cubelet.highlightGraphFace();
			}else if(cubelet.isTimeFace(e.getX(), e.getY())){
				cubelet.highlightTimeFace();
			}else if(cubelet.isCubeFace(e.getX(), e.getY())){
				cubelet.highlightAllFaces();
			}else if(!e.isAltDown())
			{
				// Test if user hovers any label. If so, highlight the slice.
		        float[] closestPoint = null;
		        float smallestDistance = 10000;
		        for(float[] p : labelBounds.keySet())
				{
		        	float d = Utils.length(Utils.dir(p, new float[]{ e.getX(),e.getY() }));
		        	if(d < smallestDistance){
						smallestDistance = d;
						closestPoint = p;
					}
				}
		        // Was label hovered? 
		        if(smallestDistance < 40 && closestPoint != null){
		        	hoveredSlice = labelBounds.get(closestPoint);
		        }else{
		        	// Prepare for ray picking
		        	xPick = e.getX();
		        	yPick = e.getY();
		        	doPicking = true;
		        }
			}
			xMouse = e.getX();
			yMouse = e.getY();
			
			display();
		}
		public void mouseWheelMoved(MouseWheelEvent e) 
		{ 
			if(e.isAltDown()){
				this.SCALE_LABELS *= (10 - e.getUnitsToScroll())/10f;
			}else if (e.isShiftDown())
				camera.changePerspective(e.getUnitsToScroll());
			else{
				camera.zoom(e.getUnitsToScroll());
			}
			display();
		}

		//////////////////
		/// KEY EVENTS ///
		//////////////////
		
		public void keyPressed(KeyEvent e) 
		{

			// VIEW CHANGE
			if(e.getKeyCode() >= 49 && e.getKeyCode() <= 57)
			{
				int viewNumber = e.getKeyCode() - 49;
				goToView(vm.getView(viewNumber));
			}
			
			if(e.isShiftDown()){
				shiftDown = true;
			}
			
			if(e.isAltDown()){
				altDown = true;
			}
			
			if(e.getKeyCode() == KeyEvent.VK_UP)
			{
				dlMode = DLMode.CELL;
				if(isCubeView())
				{
					ArrayList<CNode> tempSelection = new ArrayList<CNode>();
					int i;
					for(CNode c : selectedHNodes){
						i = matrixCube.getVisibleColumnIndex(c);
						if(i == 0)
							return;
						tempSelection.add(matrixCube.getVisibleHNodeSlice(i-1).getData());
					}
					selectedHNodes.clear();
					selectedHNodes.addAll(tempSelection);
				}
				else if(isTimeSMView() || isVNodeSliceSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(Y_AXIS, 3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(Y_AXIS, 3)));
				}
					
			}
			if(e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				dlMode = DLMode.CELL;

//				Log.out(this, "Down");
				if(isCubeView() || isSourceSideView() || isTargetSideView()){
					ArrayList<CNode> tempSelection = new ArrayList<CNode>();
					int i;
					for(CNode c : selectedHNodes){
						i = matrixCube.getVisibleColumnIndex(c);
						if(i == matrixCube.getVisibleColumnCount()-1)
							return;
						tempSelection.add(matrixCube.getVisibleHNodeSlice(i+1).getData());
					}
					selectedHNodes.clear();
					selectedHNodes.addAll(tempSelection);
				}
				else if(isTimeSMView() || isVNodeSliceSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(Y_AXIS, -3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(Y_AXIS, -3)));

				}

			}
			if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				dlMode = DLMode.CELL;

				// Moves all selected nodes one slice TO THE RIGHT
				if(isCubeView() || isSourceSideView() || isTargetSideView()){
					ArrayList<CNode> tempSelection = new ArrayList<CNode>();
					int i;
					for(CNode c : selectedVNodes){
						i = matrixCube.getVisibleColumnIndex(c);
						if(i == matrixCube.getVisibleColumnCount()-1)
							return;
						tempSelection.add(matrixCube.getVisibleVNodeSlice(i+1).getData());
					}
					selectedVNodes.clear();
					selectedVNodes.addAll(tempSelection);
				}
				else if(isTimeSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(Z_AXIS, 3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(Z_AXIS, 3)));

				}
				else if(isVNodeSliceSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(X_AXIS, 3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(X_AXIS, 3)));
				}
			}
			if(e.getKeyCode() == KeyEvent.VK_LEFT)
			{
				dlMode = DLMode.CELL;

				// Moves all selected nodes one slice TO THE LEFT
				if(isCubeView() || isSourceSideView() || isTargetSideView()){
					ArrayList<CNode> tempSelection = new ArrayList<CNode>();
					int i;
					for(CNode c : selectedVNodes){
						i = matrixCube.getVisibleColumnIndex(c);
						if(i == 0)
							return;
						tempSelection.add(matrixCube.getVisibleVNodeSlice(i-1).getData());
					}
					selectedVNodes.clear();
					selectedVNodes.addAll(tempSelection);
				}
				else if(isTimeSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(Z_AXIS, -3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(Z_AXIS, -3)));
				}
				else if(isVNodeSliceSMView()){
					camera.setLookAt(Utils.add(camera.getLookAt(), Utils.mult(X_AXIS, -3)));
					camera.setPos(Utils.add(camera.getPos(), Utils.mult(X_AXIS, -3)));
				}

			} 
			
			display();
		}

		public void keyReleased(KeyEvent e) 
		{		
			if(e.getKeyCode() == KeyEvent.VK_SHIFT)
			{
				shiftDown = false;
				dragDirection = DRAG_DIR_XY;
			}
			if(e.getKeyCode() == KeyEvent.VK_ALT)
			{
				altDown = false;
			}
		}
		
		
		public void keyTyped(KeyEvent arg0) 
		{ 
	
		}


		
		public void updateData()
		{
			weightRangeSlider.getModel().setMinimum(WEIGHT_MIN);
			weightRangeSlider.getModel().setMaximum(WEIGHT_MAX);
			weightRangeSlider.setLowValue(WEIGHT_MIN);
			weightRangeSlider.setHighValue(WEIGHT_MAX);
		
			timeRangeSlider.getModel().setMinimum(0);
			timeRangeSlider.getModel().setMaximum(matrixCube.getTimeSlices().size());
			timeRangeSlider.setLowValue(0);
			timeRangeSlider.setHighValue(matrixCube.getTimeSlices().size());
		
			weightValues.clear();
			for(Cell c : matrixCube.getCells()){
				weightValues.add(c.getData().getWeight());
			}
			weightHistogram.setValues(WEIGHT_MIN, WEIGHT_MAX, weightValues, false);
			 
			display();
		}



		public Component getControlPanel() {return this.controlPanel;}


//
//		public static void destroyInstance() {
//			activeMatrixIndex = -1;
//			
//			WEIGHT_MIN = 1000000;
//			WEIGHT_MAX = 0;
//			instance.setVisible(false);
//			instance.destroy();
//			instance = null;			
//		}
		
}
