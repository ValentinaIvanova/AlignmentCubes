package cubix.transitions.animation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.TimeSlice;
import cubix.vis.HNodeSlice;
import cubix.vis.Slice;
import cubix.vis.VNodeSlice;

/**
 * Simple animation to interpolate between two values
 * 
 * 
 * @author benjamin.bach@inria.fr
 *
 */
public abstract class Animation implements Constants{

	protected int duration;
	protected int currStep = 0;
	protected int numSteps = 0;
	
	protected Slice slice;
	protected CubixVis vis;
	

	public Animation(Slice s)
	{
		this.slice = s;
	}
	
	public final boolean init(int steps, CubixVis vis)
	{
		this.vis = vis;
		this.numSteps = steps;
		currStep= 0;
		
		return init();
	}

	protected abstract boolean init();

	/** Makes a step in this animation.
	 * 
	 * @return true if animation has to continue, false if animation is done.
	 */
	public final boolean doStep()
	{
		currStep++;
		if(currStep < numSteps){
			step();
			return true;
		}
		finish();
		return false;
	}
	
	/** Sets the animation to this fraction
	 * @param frac
	 */
	public abstract void setAnimationPosition(float frac);
	
	public abstract void step();

	public abstract void finish();

	public abstract Animation getInverseAnimation();

	public Slice getSlice() { return slice; }

	public SliceMode getSliceMode() 
	{ 
		if(slice instanceof TimeSlice) return SliceMode.TIME;
		if(slice instanceof HNodeSlice) return SliceMode.HNODE;
		if(slice instanceof VNodeSlice) return SliceMode.VNODE;
		return vis.getSliceMode();
	}

	public boolean isInit() { return currStep > 0;
	}
}
