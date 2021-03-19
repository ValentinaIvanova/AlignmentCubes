package cubix.transitions.animation;

import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public class MoveLabelAnimation extends Animation {

	private float[] finalLeftPos;
	private float[] finalRightPos;
	private float[] initLeftPos;
	private float[] deltaLeftPos;
	private float[] initRightPos;
	private float[] deltaRightPos;

	public MoveLabelAnimation(Slice s, float[] leftLabelPos, float[] rightLabelPos) {
		super(s);
		this.finalLeftPos = leftLabelPos.clone();
		this.finalRightPos = rightLabelPos.clone();
		
	}

	@Override
	protected boolean init() 
	{
		initLeftPos = slice.getLeftLabelPos().clone();
		deltaLeftPos = Utils.dir(initLeftPos, finalLeftPos);
		deltaLeftPos = Utils.mult(deltaLeftPos, 1f/numSteps);
		initRightPos = slice.getLeftLabelPos().clone();
		deltaRightPos = Utils.dir(initRightPos, finalRightPos);
		deltaRightPos = Utils.mult(deltaRightPos, 1f/numSteps);
	
		return Utils.length(deltaLeftPos) != 0 && Utils.length(deltaRightPos) != 0;
	}

	@Override
	public void step() 
	{
		slice.setLeftLabelPos(Utils.add(slice.getLeftLabelPos(), deltaLeftPos));
		slice.setRightLabelPos(Utils.add(slice.getRightLabelPos(), deltaRightPos));
	}
	
	public void setAnimationPosition(float fraction)
	{
//		vis.setSliceRotation(slice, vis.getSliceRotation(slice) + deltaAngle);
	}

	@Override
	public void finish() 
	{
		slice.setLeftLabelPos(finalLeftPos);
		slice.setRightLabelPos(finalRightPos);
	}

	@Override
	public Animation getInverseAnimation() {
		return null;
	}

}
