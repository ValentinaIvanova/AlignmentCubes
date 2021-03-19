package cubix.transitions.animation;

import java.awt.event.ActionEvent;

import cubix.CubixVis;
import cubix.helper.Log;
import cubix.vis.Slice;

public class ScaleAnimation extends Animation {

	private float finalScale;
	private float deltaScale;
	private float initScale = -1;
	private float deltaScale_total;

	public ScaleAnimation(Slice s, float scale) {
		super(s);
		this.finalScale = scale;
	}

	@Override
	public boolean init() {
		initScale = vis.getMatrixScale(slice);
		deltaScale_total = finalScale - initScale;
		deltaScale = deltaScale_total / (numSteps + 0f);
		return deltaScale > 0;
	}

	@Override
	public void step() 
	{
		vis.setMatrixScale(slice, vis.getMatrixScale(slice) + deltaScale);
	}

	public void setAnimationPosition(float fraction)
	{
		vis.setMatrixScale(slice, initScale + fraction * deltaScale_total);
	}
	
	@Override
	public void finish() {
		vis.setMatrixScale(slice, finalScale);
	}

	@Override
	public Animation getInverseAnimation() 
	{
		if(initScale == -1){
			Log.err(this, "InverseAnimation cannot be created, since animation has not been initiated.");
			return null;
		}
		return new ScaleAnimation(slice, initScale);
	}

}
