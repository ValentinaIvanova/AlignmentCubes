package cubix.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import cubix.CubixVis;
import cubix.vis.Slice;

public class ResetColorSliceAction implements ActionListener {

	private Slice<?,?> slice;
	private CubixVis vis;

	public ResetColorSliceAction(CubixVis vis, Slice<?,?> slice) 
	{
		this.vis = vis;
		this.slice = slice;
	}
	
	public void actionPerformed(ActionEvent e) {
//		vis.resetSliceColor(slice);
		vis.closePopupMenu();
	}

}
