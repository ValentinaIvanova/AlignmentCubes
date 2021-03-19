package cubix.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CTime {

	private long id;
	private DateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
	private String label = "";
			
	
	public CTime(long id){
		this.id = id;	
	}
	
	public void setDateFormat(DateFormat dateFormat){
		this.dateFormat = dateFormat;
	}
	
	public long getID(){ return this.id; }
	
	public String getLabel(){
		if(label.length() > 0) return label; 
		Date d = new Date(id);
		return dateFormat.format(id);
	}
	
	public void setLabel(String l){
		this.label = l;
	}
	
	@Override
	public String toString(){
		return getLabel();
	}
}

