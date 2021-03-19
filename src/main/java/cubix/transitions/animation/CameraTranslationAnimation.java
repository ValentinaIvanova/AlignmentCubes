package cubix.transitions.animation;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Camera;

public class CameraTranslationAnimation extends CameraAnimation{

	private Camera cam;
	private float[] finalLookAt;
	private float[] initLookAt;
	private float[] deltaLookAt;
//	private boolean lookChange;

	protected boolean translate;
	protected float[] initPos;
	protected float[] deltaPos;
	private float[] finalPos;

	protected float initScale;
	protected float deltaScale; // reflects the angle of rotation at EACH animation STEP!
	protected boolean scale;

//	protected boolean skipAnimation = true;
//	private int currentStep;
//	private Object t;
//	private float allSteps;
//	private CubixVis vis;
//	private Timer timer;
	
	protected ArrayList<AnimationListener> listeners = new ArrayList<AnimationListener>() ;
private float[] deltaPos_total;
private float[] deltaLookAt_total;

	
	public CameraTranslationAnimation(Camera c, float[] destPos, float[] finalLookAt)
	{
		super(null);
		this.finalLookAt = finalLookAt;
		this.cam = c;
		this.finalPos = destPos;
	}



	@Override
	protected boolean init() 
	{
		// Position
		initPos = cam.getPos().clone();
		deltaPos_total = Utils.dir(initPos, finalPos);   
		deltaPos = Utils.mult(deltaPos_total, 1f / numSteps);   
		
		// LookAt
		initLookAt = cam.getLookAt().clone();
		deltaLookAt_total = Utils.dir(initLookAt, finalLookAt);   
		deltaLookAt = Utils.mult(deltaLookAt_total, 1f / (numSteps + 0f));   

		return 	Utils.length(deltaPos) > 0 
			|| Utils.length(deltaLookAt) > 0;
	}



	@Override
	public void step() 
	{
		cam.setPos(Utils.add(cam.getPos(), deltaPos));
		cam.setLookAt(Utils.add(cam.getLookAt(), deltaLookAt));	
	}
	
	@Override
	public void setAnimationPosition(float fraction)
	{
		cam.setPos(Utils.add(initPos, Utils.mult(deltaPos_total, fraction)));
		cam.setLookAt(Utils.add(initLookAt, Utils.mult(deltaLookAt_total, fraction)));	
	}


	@Override
	public void finish() 
	{
		cam.setPos(finalPos);		
		cam.setLookAt(finalLookAt);	
	}



	@Override
	public Animation getInverseAnimation() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
