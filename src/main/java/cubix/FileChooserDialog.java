package cubix;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileChooserDialog extends JDialog implements ActionListener {

	private JLabel sourceOntoLabel; 
	private JLabel targetOntoLabel; 
	private JLabel alignmentLabelFromFile; 
	//private JLabel alignmentLabelFromFolder;
	private JLabel alignmentMeasuresLabel;
	//private JLabel relationLabel; 

	private JTextField sourceOntoText;
	private JTextField targetOntoText;
	private JTextField alignmentTextFromFile;
	//private JTextField alignmentTextFromFolder;
	private JTextField alignmentMeasuresText;
	
	private JButton sourceOntoOpenButton;
	private JButton targetOntoOpenButton;
	private JButton alignmentOpenButtonFromFile;
	//private JButton alignmentOpenButtonFromFolder;	
	private JButton alignmentMeasuresButton;
	private JButton openButton;
	private JButton cancelButton;

	private JCheckBox relationCheckBox;
	private JFileChooser fileChooser;
	
	private String sourceOntoFilePath;
	private String targetOntoFilePath;
	private String alignmentFilePath;
	//private String alignmentFolderPath;
	private String alignmentMeasuresFilePath;
	
	private File file = null;
	
    public FileChooserDialog() {
    	
    	this.setModalityType(ModalityType.APPLICATION_MODAL);
    	
    	JPanel panel = new JPanel();
    	this.setContentPane(panel);
    	
    	panel.setLayout(new GridBagLayout());
        
    	panel.setPreferredSize(new Dimension(850, 300));
        
        sourceOntoLabel = new JLabel("Source Ontology - Green Axis:"); 
        targetOntoLabel = new JLabel("Target Ontology - Red Axis:"); 
    	alignmentMeasuresLabel = new JLabel("Performance Measures (optional):");	
    	alignmentLabelFromFile = new JLabel("Alignments - Blue Axis:");    
    	//JLabel alignmentLabel = new JLabel("Load alignments from file OR Folder");
    	//alignmentLabelFromFile = new JLabel("Alignments From csv File - Blue Axis:");    
    	//alignmentLabelFromFolder = new JLabel("Alignments From Folder - Blue Axis:"); 
    	//relationLabel= new JLabel("Load only equivence mappings:");
    	
    	sourceOntoText = new JTextField(50);
    	targetOntoText = new JTextField(50);
    	alignmentTextFromFile = new JTextField(50);    
    	//alignmentTextFromFolder = new JTextField(50);    
    	alignmentMeasuresText = new JTextField(50); 
    	
    	sourceOntoOpenButton = new JButton("Choose Source Ontology");
    	targetOntoOpenButton = new JButton("Choose Target Ontology");
    	alignmentOpenButtonFromFile = new JButton("Choose Alignment (folder)");	
    	//alignmentOpenButtonFromFolder = new JButton("Choose Alignment (folder with alignments)");	
    	alignmentMeasuresButton = new JButton("Choose Measures File (only .csv)");	
    	relationCheckBox = new JCheckBox();
    	relationCheckBox.setSelected(true);
    	relationCheckBox.setText("Load only equivence mappings");

    	openButton = new JButton("Open");
    	cancelButton = new JButton("Cancel");
        
        fileChooser = new JFileChooser();
    	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        sourceOntoOpenButton.addActionListener(this);
        targetOntoOpenButton.addActionListener(this);
        alignmentOpenButtonFromFile.addActionListener(this);
        //alignmentOpenButtonFromFolder.addActionListener(this);
        alignmentMeasuresButton.addActionListener(this);
        openButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0.2, 0.25, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        //sourceOntoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(sourceOntoLabel, c);
        
        c = new GridBagConstraints(1, 0, 1, 1, 0.5, 0.25, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(sourceOntoText, c);       
        
        c = new GridBagConstraints(2, 0, 1, 1, 0.3, 0.25, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(sourceOntoOpenButton, c);         
        
        
        c = new GridBagConstraints(0, 1, 1, 1, 0.2, 0.25, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(targetOntoLabel, c);     
        
        c = new GridBagConstraints(1, 1, 1, 1, 0.5, 0.25, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(targetOntoText, c);  
        
        c = new GridBagConstraints(2, 1, 1, 1, 0.3, 0.25, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(targetOntoOpenButton, c);        
        
        //c = new GridBagConstraints(0, 3, 1, 1, 0.2, 0.25, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);        
        //panel.add(alignmentLabel, c);
        
        c = new GridBagConstraints(0, 2, 1, 1, 0.2, 0.25, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,3,10), 10, 10);        
        panel.add(alignmentLabelFromFile, c);    
        
        c = new GridBagConstraints(1, 2, 1, 1, 0.5, 0.25, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,10,3,10), 10, 10);
        panel.add(alignmentTextFromFile, c);  
        
        c = new GridBagConstraints(2, 2, 1, 1, 0.3, 0.25, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(10,10,3,10), 10, 10);
        panel.add(alignmentOpenButtonFromFile, c); 
        
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //checkBoxPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkBoxPanel.add(relationCheckBox);
        c = new GridBagConstraints(0, 3, 3, 1, 0.2, 1, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(0,10,10,10), 10, 10);        
        panel.add(checkBoxPanel, c);
        
        c = new GridBagConstraints(0, 4, 1, 1, 0.2, 0.25, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);        
        panel.add(alignmentMeasuresLabel, c);    
        
        c = new GridBagConstraints(1, 4, 1, 1, 0.5, 0.25, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(alignmentMeasuresText, c);  
        
        c = new GridBagConstraints(2, 4, 1, 1, 0.3, 0.25, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
        panel.add(alignmentMeasuresButton, c);  
        
//        c = new GridBagConstraints(0, 5, 1, 1, 0.2, 0.25, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);        
//        panel.add(alignmentLabelFromFolder, c);    
//        
//        c = new GridBagConstraints(1, 5, 1, 1, 0.5, 0.25, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
//        panel.add(alignmentTextFromFolder, c);  
//        
//        c = new GridBagConstraints(2, 5, 1, 1, 0.3, 0.25, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10), 10, 10);
//        panel.add(alignmentOpenButtonFromFolder, c); 
        
        c = new GridBagConstraints(0, 5, 3, 1, 1, 1, GridBagConstraints.LAST_LINE_START, GridBagConstraints.BOTH, new Insets(10,10,10,10), 10, 10);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(openButton);
        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel, c);
        
        this.pack();
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		
    	
		if (file != null)
			fileChooser.setSelectedFile(file.getAbsoluteFile());

		else {
			File here = new File("");
	    	fileChooser.setSelectedFile(here.getAbsoluteFile());
		}

        if (e.getSource() == sourceOntoOpenButton) {

            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                sourceOntoFilePath = file.getAbsolutePath();
                sourceOntoText.setText(sourceOntoFilePath);
            } 
        } else if (e.getSource() == targetOntoOpenButton) {
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                targetOntoFilePath = file.getAbsolutePath();
                targetOntoText.setText(targetOntoFilePath);
            } 
        } else if (e.getSource() == alignmentOpenButtonFromFile) {
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                //file = fileChooser.getCurrentDirectory();
                alignmentFilePath = file.getAbsolutePath();
                alignmentTextFromFile.setText(alignmentFilePath);
            } 
//        } else if (e.getSource() == alignmentOpenButtonFromFolder) {
//        	fileChooser.setCurrentDirectory(here);
//            int returnVal = fileChooser.showOpenDialog(null);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                file = fileChooser.getSelectedFile();
//            	//file = fileChooser.getCurrentDirectory();
//                alignmentFolderPath = file.getPath();
//                alignmentTextFromFolder.setText(alignmentFolderPath);
//            } 
        } else if (e.getSource() == alignmentMeasuresButton) {
        	int returnVal = fileChooser.showOpenDialog(null);
        	
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                alignmentMeasuresFilePath = file.getAbsolutePath();
                alignmentMeasuresText.setText(alignmentMeasuresFilePath);
            }
        } else if (e.getSource() == openButton){

/*    		File sourceOnto = new File(sourceOntoFilePath);
    		System.out.println(sourceOnto.exists());
    		File targetOnto = new File(targetOntoFilePath);
    		System.out.println(targetOnto.exists());
    		File alignment = new File(alignmentFilePath);
    		System.out.println(alignment.exists());
    		File alignmentMeasures = new File(alignmentMeasuresFilePath);
    		System.out.println(alignmentMeasures.exists());*/
        	
    		Cubix.setSelectedFilePaths(sourceOntoFilePath, 
    				targetOntoFilePath, alignmentFilePath, alignmentMeasuresFilePath, relationCheckBox.isSelected());

    		this.dispose();

        } else if (e.getSource() == cancelButton) {
        	
        	this.dispose();
        }
	}
	
	/*   public void createAndShowGUI(JFrame frame) {
        //Create and set up the window.
        //JFrame frame = new JFrame("Ontology File Chooser");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        //frame.add(new CubixFileChooser());

        //Display the window.
        //frame.pack();
        //frame.setVisible(true);
    }	
	
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }*/	

}
