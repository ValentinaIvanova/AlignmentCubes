package cubix.transitions.animation;


import cubix.CubixVis;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public class SliceTranslationAnimation extends Animation {

	protected float[] initPos;
	protected float[] finalPos;
	protected float[] deltaPos;
	private float[] deltaPos_total;

	
	public SliceTranslationAnimation(Slice s, float[] pos)
	{
		super(s); 
		finalPos = pos;
	}
	
	public SliceTranslationAnimation(Slice s, float x, float y, float z)
	{
		super(s); 
		finalPos = new float[]{x,y,z};
	}

	@Override
	public boolean init() 
	{
		initPos = vis.getSlicePos(slice).clone();
		deltaPos_total = Utils.dir(initPos, finalPos);
		deltaPos = Utils.mult(deltaPos_total, 1f/numSteps);
	
		return Utils.length(deltaPos) > 0;
	}
	
	
	@Override
	public void step() 
	{
		vis.setSlicePos(slice, Utils.add(vis.getSlicePos(slice), deltaPos));		
	}

	@Override
	public void setAnimationPosition(float fraction)
	{
		vis.setSlicePos(slice, Utils.add(initPos, Utils.mult(deltaPos_total, fraction)));		
	}
	
	@Override
	public void finish()
	{
		vis.setSlicePos(slice, finalPos);
	}

	@Override
	public Animation getInverseAnimation() 
	{
		if(initPos == null){
			Log.err(this, "InverseAnimation cannot be created, since animation has not been initiated.");
			return null;
		}
		return new SliceTranslationAnimation(slice, initPos);
	}
	
}
