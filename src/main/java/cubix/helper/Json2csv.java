package cubix.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import com.opencsv.*;


public class Json2csv {

	
	public static void main(String[] args){
		
		File f = new File("");
		File dir = new File(f.getAbsolutePath() + "/data/tochi/");
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".json");
		    }
		});

		File csvFile = new File(dir.getAbsolutePath() + "/tochi.csv");
		try {
		CSVWriter csv = new CSVWriter(new FileWriter(csvFile));
		String venue;
		for(File jsonFile : files){
			venue = jsonFile.getName().split("\\.")[0];
				BufferedReader br = new BufferedReader(new FileReader(jsonFile));
				
				String line;
				String pub="", year="";
				ArrayList<String> authors = new ArrayList<String>();
				while((line = br.readLine()) != null){
					if(line.startsWith("\"@id\"")){
						for(String a : authors){
							for(String b : authors){
								csv.writeNext(new String[]{year,a,b, "tochi"});
							}
						}
						
						pub = line.split(":")[1].replace("\"", "").replace(",", "");
						authors = new ArrayList<String>();
					}else if(line.startsWith("\"author\":[")){
						while(!line.contains("]}")){
							line = br.readLine();
							authors.add(line.replace("]}", "").replace("\"", "").replace(",",""));
						}
					}else if(line.startsWith("\"year\"")){
						year = line.split(":")[1].replace("\"", "").replace(",", "");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
}
