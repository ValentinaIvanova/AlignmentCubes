package cubix.helper;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileNameComparator implements Comparator<File> {

	
    public int compare(File f1, File f2) {
	        try {
	        	String n1 = f1.getName();
	        	String n2 = f2.getName();
	        	Pattern p = Pattern.compile("[0-9]+");

	        	Matcher m1 = p.matcher(n1);
	        	Matcher m2 = p.matcher(n2);
	        	
	        	int i1,i2;
	        	while(m1.find()){
	        		i1 = Integer.parseInt(m1.group());
	        	    m2.find();
	        	    i2 = Integer.parseInt(m2.group());
	        	    if(i1 != i2)
	        	    	return i1 - i2;
	        	}
	        	Log.err(this, "File order not distinguishable");
	        	return 0;
	        
	        } catch(NumberFormatException e) {
	            throw new AssertionError(e);
	        }
	    }
}
