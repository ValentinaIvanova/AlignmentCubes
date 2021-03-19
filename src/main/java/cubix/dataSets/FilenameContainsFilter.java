package cubix.dataSets;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameContainsFilter implements FilenameFilter {

	private String[] patterns;


	public FilenameContainsFilter(String pattern){
		this.patterns = new String[]{pattern};
	}
	
	public FilenameContainsFilter(String[] patterns){
		this.patterns = patterns;
	}

	public boolean accept(File file, String string) {
		boolean contains = true; 
		for(String p : patterns) 
			contains = contains && string.contains(p);
		return contains;
	}

}
