package cubix;

import static cubix.Cubix.CONTROL_PANEL_WIDTH;
import static cubix.Cubix.exportSVGNodeslices;
import static cubix.Cubix.exportSVGTimeslices;
import static cubix.Cubix.frame;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.DataModel;
import cubix.data.TimeGraph;
import cubix.dataSets.CSVGDataSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

/**
 * Java API for Cubix which allows to show the analysis window from java code.
 * The main entry point is the run method. If you call it, it will display the window and will stop the program if the user closes the window.
 */
public class CubixAPI {
    
    /**
     * Loads the ontologies and alignment file (in alignment format) and visualize the differences between the alignments.
     * Only equivalent correspondences are loaded and no further measures are included.
     * @param sourceOnto the file pointing to the source ontology. The format can be any format which can be loaded by OWLAPI.
     * @param targetOnto the file pointing to the target ontology. The format can be any format which can be loaded by OWLAPI.
     * @param alignments the files pointing to alignments which should be analyzed. The format should be the alignment format.
     */
    public static void run(File sourceOnto, File targetOnto, List<File> alignments){
        run(sourceOnto, targetOnto, alignments, null, true);
    }
    
    /**
     * Loads the ontologies and alignment file (in alignment format) and visualize the differences between the alignments.
     * @param sourceOnto the file pointing to the source ontology. The format can be any format which can be loaded by OWLAPI.
     * @param targetOnto the file pointing to the target ontology. The format can be any format which can be loaded by OWLAPI.
     * @param alignments the files pointing to alignments which should be analyzed. The format should be the alignment format.
     * @param measures the file which contains further measures.
     * @param onlyLoadEquivalent true, if only equivalence correspondences should be loaded.
     */
    public static void run(File sourceOnto, File targetOnto, List<File> alignments, File measures, boolean onlyLoadEquivalent){
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        int SCREEN = screens.length - 1;
        
        int SCREEN_WIDTH = screens[SCREEN].getDisplayMode().getWidth() - 10;
        int SCREEN_HEIGHT = screens[SCREEN].getDisplayMode().getHeight() - 150;
        int OFFSET_RIGHT = 000;
        int OFFSET_LEFT = 000;


        frame = new JFrame(screens[SCREEN].getDefaultConfiguration());

        Rectangle bounds = screens[SCREEN].getDefaultConfiguration().getBounds();
        frame.setBounds(bounds.x, bounds.y, SCREEN_WIDTH - OFFSET_RIGHT, SCREEN_HEIGHT);
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        frame.add(sp, BorderLayout.CENTER);

        // Create CubeVis
        int visWidth = (SCREEN_WIDTH - OFFSET_RIGHT - CONTROL_PANEL_WIDTH);

        CubixVisInteractive cubeVis1 = new CubixVisInteractive();
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

        frame.setResizable(false);
        frame.setTitle("Cubix");
        
        TimeGraph timeGraph = CSVGDataSet.loadOntologiesAndAlignments(sourceOnto, targetOnto, alignments, measures, onlyLoadEquivalent);
        
        // Visualize
        cubeVis1.createCube(timeGraph);
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
        //exportSVGTimeslices();
        //exportSVGNodeslices();
    }
}
