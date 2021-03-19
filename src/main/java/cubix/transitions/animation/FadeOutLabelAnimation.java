package cubix.transitions.animation;

import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public class FadeOutLabelAnimation extends Animation 
{

	private float[] finalTransparency;
	private float[] d = FLOAT4_0.clone();

	public FadeOutLabelAnimation(Slice s, float[] labelTrans) {
		super(s);
		finalTransparency = labelTrans.clone();
	}

	@Override
	protected boolean init() 
	{
		for(int i=0 ; i<4 ; i++)
		{
			d[i] = ( finalTransparency[i] - vis.getLabelTransparency(slice)[i]) / numSteps;
			if(d[i] > 0){
				d[i] = 0;
				finalTransparency[i] = vis.getLabelTransparency(slice)[i];
			}
		}
		return Utils.length(d) != 0; 
	}

	@Override
	public void step() 
	{
		for(int i=0 ; i<4 ; i++)
			if(d[i] != 0) {
//				Log.out(this, "LabelTrans: " + slice.getLabel() + ": " 
//						+ vis.getLabelTransparency(slice)[N] + "," 
//						+ vis.getLabelTransparency(slice)[E] + "," 
//						+ vis.getLabelTransparency(slice)[S] + "," 
//						+ vis.getLabelTransparency(slice)[W]);

				vis.setLabelTransparency(slice, vis.getLabelTransparency(slice)[i] + d[i] ,i);
			}
	}

	public void setAnimationPosition(float fraction)
	{
//		vis.setSliceRotation(slice, vis.getSliceRotation(slice) + deltaAngle);
	}

	
	@Override
	public void finish() 
	{
		vis.setLabelTransparency(slice,  finalTransparency.clone());
//		Log.out(this, "LabelTrans - final: " + slice.getLabel() + ": " 
//				+ vis.getLabelTransparency(slice)[N] + "," 
//				+ vis.getLabelTransparency(slice)[E] + "," 
//				+ vis.getLabelTransparency(slice)[S] + "," 
//				+ vis.getLabelTransparency(slice)[W]);
	}

	@Override
	public Animation getInverseAnimation() {
		// TODO Auto-generated method stub
		return null;
	}

}
