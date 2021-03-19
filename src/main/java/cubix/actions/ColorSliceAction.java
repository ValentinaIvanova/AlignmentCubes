package cubix.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import cubix.CubixVis;
import cubix.vis.Slice;

public class ColorSliceAction implements ActionListener {

	private Slice<?,?> slice;
	private float[] color;
	private CubixVis vis;

	public ColorSliceAction(CubixVis vis, Slice<?,?> slice, float[] color) 
	{
		this.vis = vis;
		this.slice = slice;
		this.color = color;
	}
	
	public void actionPerformed(ActionEvent e) {
//		vis.setColorEntireSlice(slice, color);
		vis.closePopupMenu();
	}

}
