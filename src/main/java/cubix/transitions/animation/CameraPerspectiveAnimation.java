package cubix.transitions.animation;

import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Camera;

public class CameraPerspectiveAnimation extends CameraAnimation{

	private float finalViewAngle;
	private Camera cam;
	private float initViewAngle;
	private float deltaViewAngle;
	private float w;
	private float[] initPos;
	private float[] finalPos;
	private float a = (float) (Math.PI / 360);
	private float[] dir;
	private float dStart;
	private float deltaViewAngle_total;
	
	/** 
	 * 
	 * @param c - the camera
	 * @param viewAngleInDegree - in degree
	 */
	public CameraPerspectiveAnimation(Camera c, float viewAngleInDegree) 
	{
		super(null);
		cam = c;
		this.finalViewAngle = viewAngleInDegree;
	}

	@Override
	protected boolean init() 
	{
		initViewAngle = cam.getViewAngle();
		deltaViewAngle_total = finalViewAngle - initViewAngle;
		deltaViewAngle = deltaViewAngle_total / (float) numSteps;

		initPos = cam.getPos();
		w = (float) (2 * Utils.length(initPos) * Math.tan(initViewAngle * Math.PI / 360f)); 
	
		dStart = (float) (w / (2 * Math.tan(initViewAngle * Math.PI / 360)));
		float dFinal = (float) (w / (2 * Math.tan(finalViewAngle * Math.PI / 360)));
		dir = Utils.normalize(Utils.dir(cam.getLookAt(), initPos));
		finalPos = Utils.add(initPos, Utils.mult(dir, dFinal-dStart));
		
		return finalViewAngle - initViewAngle != 0;
	}

	@Override
	public void step() 
	{
		cam.setViewAngle(cam.getViewAngle() + deltaViewAngle);
		float d = (float) (w / (2 * Math.tan(cam.getViewAngle() * a)));
		cam.setPos(Utils.add(initPos, Utils.mult(dir, d-dStart)));
		
	}
	
	public void setAnimationPosition(float fraction)
	{
		cam.setViewAngle(initViewAngle + fraction * deltaViewAngle_total);
		float d = (float) (w / (2 * Math.tan(cam.getViewAngle() * a)));
		cam.setPos(Utils.add(initPos, Utils.mult(dir, d-dStart)));		
	}

	@Override
	public void finish() 
	{
		cam.setViewAngle(finalViewAngle);
		cam.setPos(finalPos);
	}

	@Override
	public Animation getInverseAnimation() {
		
		// TODO Auto-generated method stub
		return null;
	}

}
