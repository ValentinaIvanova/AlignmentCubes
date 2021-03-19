package cubix.transitions;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Timer;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.transitions.animation.Animation;
import cubix.transitions.animation.CameraTranslationAnimation;
import cubix.transitions.animation.CameraPerspectiveAnimation;
import cubix.vis.TimeSlice;
import cubix.vis.VNodeSlice;

public class Transition_a implements Constants {

	
	// ANIMATION PARAMETERS
	public float FRAME_RATE = 20;
	
	Timer updateDisplayTimer;
	
	// VARS
	protected CubixVis vis;
	protected HashSet<TransitionListener> listeners = new HashSet<TransitionListener>();
	private HashMap<Animation, Timer> animationTimers = new HashMap<Animation, Timer>();
	private int maxTime = 0;
//	private Timer endTimer;
	private ArrayList<Animation> animations = new ArrayList<Animation>();
	
	// STATES
	protected boolean running = false;
	private SliceMode sliceMode = null;
	private Animation currentCameraAnimation = null;
	private Animation nextCameraAnimation = null;

	protected Timer nextCameraStepTimer;


	public Transition_a(final CubixVis vis)
	{
		this(vis, vis.getSliceMode());
	}
	
	public Transition_a(final CubixVis vis, SliceMode sliceMode)
	{
		FRAME_RATE = (int) vis.getAverageFramerate();
		if(FRAME_RATE < 15)
			FRAME_RATE =15;
		
//		Log.out(this, "fps; " + FRAME_RATE);
		this.sliceMode = sliceMode;
		this.vis = vis;
		updateDisplayTimer = new Timer((int) (1000 / FRAME_RATE), new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				vis.display();
			}
		});
	}
	
	

	public void start()
	{
		running = true;
		
		vis.setSliceMode(sliceMode);

		for(Animation a : animations)
		{
			animationTimers.get(a).start();
		}
		
//		endTimer = new Timer(maxTime, new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent arg0) 
//			{
//				Log.out(this, "transition done"); 
//				updateDisplayTimer.stop();
//				endTimer.stop();
//				animationTimers.clear();
//			}}); 
		updateDisplayTimer.start();
//		endTimer.start();
		
	}
	
	public void stop()
	{
		running = false;
		
		for(Timer t : animationTimers.values()){
			t.stop();
		}
		updateDisplayTimer.stop();
//		endTimer.stop();
	}
	
	public void resume()
	{
		running = true;
		for(Timer t : animationTimers.values()){
			t.start();
		}
		updateDisplayTimer.start();
//		endTimer.start();
	}
	
	
	protected void notifyListeners()
	{
//		for(TransitionListener l : listeners)
//			l.transitionFinished(this);
	}

	
	/////////////////
	/// GET & SET /// 
	/////////////////
	
	/** Adds a single animation **/
	public int addAnimation(final Animation a, int delay, final int duration)
	{
		animations.add(a);

		final Timer stepTimer = new Timer((int) (1000 / FRAME_RATE), null);
		stepTimer.addActionListener(new ActionListener(){
		
			public void actionPerformed(ActionEvent arg0) {
				if(!a.doStep())
				{
					stepTimer.stop();
					animations.remove(a);
//					Log.out(this, "Finish animation. " + animations.size() + " left.");
					if((a instanceof CameraTranslationAnimation || a instanceof CameraPerspectiveAnimation))
					{
						currentCameraAnimation = nextCameraAnimation;
						if(currentCameraAnimation != null)
						{
							Timer startTimer = new Timer(0, null);
							startTimer.setRepeats(false);
							startTimer.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e)
								{
									currentCameraAnimation.init((int) ((duration/1000f) * FRAME_RATE), vis);
									nextCameraStepTimer.start();
								}}
							);
							startTimer.start();
						}
						nextCameraAnimation = null;
//						nextCameraStepTimer = null;
					}	
						
					if(animations.size() == 0)
					{
						Log.out(this, "end transition");
						updateDisplayTimer.stop();
						animationTimers.clear();
						vis.display();
						notifyListeners();
					}
				}
			}}
		);
		

		Timer startTimer = new Timer(0, null);
		startTimer.setInitialDelay(delay);
		startTimer.setRepeats(false);
		startTimer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				a.init((int) ((duration/1000f) * FRAME_RATE), vis);
				if((a instanceof CameraTranslationAnimation || a instanceof CameraPerspectiveAnimation))
				{
					if(currentCameraAnimation == null)
					{
						currentCameraAnimation = a;
						stepTimer.start();
					}else{
						// Keep animation and step timer to start this animation after the other
						// camera animation has been finished. 
						nextCameraAnimation = a;
						nextCameraStepTimer = stepTimer;
					}
				}else{
					stepTimer.start();
				}
			}}
		);
		animationTimers.put(a, startTimer);
		if(delay+duration > maxTime)
		{
			maxTime = Math.max(maxTime , delay+duration);
		}
		
		return delay + duration;
	}
	
	public int addAnimation(final Animation a, final int duration){
		return addAnimation(a, 0, duration);
	}

	public Collection<Animation> getAnimations(){
		return animations;
	}

	public void removeAnimation(Animation a){
		this.animations.remove(a);
		this.animationTimers.remove(a);
	}
	
	public void addListener(TransitionListener l) {listeners.add(l);}
	public void removeListeners() {listeners.clear();}


	public boolean isRunning() {
		return animations.size() > 0;
	}
	
}
