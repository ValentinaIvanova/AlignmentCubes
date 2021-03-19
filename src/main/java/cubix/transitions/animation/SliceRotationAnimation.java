package cubix.transitions.animation;


import cubix.helper.Log;
import cubix.vis.Slice;

public class SliceRotationAnimation extends Animation{

	private float initAngle;
	private float finalAngle;
	private float deltaAngle;
	private float[] axis;
	private float deltaAngle_total;
	
	/**
	 * @param s - slice
	 * @param angle - in degree.
	 */
	public SliceRotationAnimation(Slice s, float angle) {
		super(s);
		this.finalAngle = angle;
	}

	public SliceRotationAnimation(Slice s, float[] axis, float angle) {
		super(s);
		finalAngle = angle;
	}

	@Override
	public boolean init() 
	{
		initAngle = vis.getSliceRotation(slice);
		deltaAngle_total = finalAngle - initAngle;
		deltaAngle = deltaAngle_total / (numSteps-1);
		return deltaAngle > 0;
	}

	@Override
	public void step() 
	{
		vis.setSliceRotation(slice, vis.getSliceRotation(slice) + deltaAngle);
	}
	
	public void setAnimationPosition(float fraction)
	{
		vis.setSliceRotation(slice, initAngle + fraction * deltaAngle_total);
	}

	@Override
	public void finish() 
	{
		vis.setSliceRotation(slice, finalAngle);
	}
	

	@Override
	public Animation getInverseAnimation() 
	{
		return null;
	}



}
