package cubix.helper.histogram;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import cubix.helper.Map;

public class Histogram extends JComponent{

	protected ColorRetriever cr;
	private int w, h;
	public int x0, y0,x1, y1;
	private float xLow = 0;
	private float xHigh = 0;
	private float valueMax = 0;
	private ArrayList<Float> rawValues;
	private HashMap<Integer, Integer> fineValues = new HashMap<Integer, Integer>();
	private float vStep;
	private boolean drawRect;
	private float rectW;
	
	public Histogram(int width, int height, ColorRetriever cr){
		this.cr = cr;
		this.w = width -50;
		this.h = height;
		x0 = 0;
		y0 = h;	
		x1 = w;
		y1 = 0;
		
	}
	
	public void setValues(float min, float max, ArrayList<Float> values, boolean rect)
	{
		this.xLow = min;
		this.rawValues = values;
		this.xHigh = max;
		vStep = (max-min) / (w);
		valueMax = 0;
		int count=0;
		float v =min;
		for(int i=1 ; i <= w ; i++){
			count = 0;
			for(float f : values){
				if(f >= v && f <= (v+vStep)){
					count++;
				}
			}
			v += vStep;
			valueMax = Math.max(valueMax, count);
			fineValues.put(i, count);
		}
		drawRect = rect;
		rectW = (w) / (max-min+0f) / 2f;
	}
	
    public void paint(Graphics g, int yOffset) 
    {
    	int y;
		y0 = yOffset;
		float v = xLow;
		for(int	 x=x0+1 ; x<x1 ; x++) {
			try{
				y = (int) Map.mapLog(fineValues.get(x), 0, valueMax, 0, h);				
			}catch(java.lang.NullPointerException e){
				y = 0;
			}
			if(cr != null)
				g.setColor(cr.getColor(v, v+vStep));
			if(drawRect){
				g.fillRect((int)(x-rectW), (int)y0-y, (int) ((int) 2*rectW), y);
			}else{
				g.drawLine(x, y0, x, y0-y);
			}
			v += vStep;
		}
	}
	
    
    
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize(){
        return new Dimension(w, h);
    }
}
