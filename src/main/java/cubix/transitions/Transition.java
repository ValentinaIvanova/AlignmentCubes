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
import cubix.helper.Map;
import cubix.transitions.animation.Animation;
import cubix.transitions.animation.CameraAnimation;
import cubix.transitions.animation.CameraTranslationAnimation;
import cubix.transitions.animation.CameraPerspectiveAnimation;
import cubix.vis.TimeSlice;
import cubix.vis.VNodeSlice;

public class Transition implements Constants {

	
	// ANIMATION PARAMETERS
	public float FRAME_RATE = 30f;
	
	Timer updateDisplayTimer;
	
	// VARS
	protected CubixVis vis;
	protected HashSet<TransitionListener> listeners = new HashSet<TransitionListener>();
	private HashMap<Animation, Timer> animationTimers = new HashMap<Animation, Timer>();
	private int maxTime = 0;
	private ArrayList<Animation> runningAnimations = new ArrayList<Animation>();
	protected int openAnimationCount = 0;
	
	// STATES
	protected boolean running = false;
	private SliceMode sliceMode = null;
	private Animation currentCameraAnimation = null;

	protected Timer nextCameraStepTimer;

	private HashMap<Animation, Integer> durations = new HashMap<Animation, Integer>();

	protected String name = "";

	private SliceMode finalSliceMode = null;

	public Transition(final CubixVis vis)
	{
		this(vis, vis.getSliceMode());
	}
	
	
	public Transition(final CubixVis vis, SliceMode sliceMode)
	{
		this.sliceMode = sliceMode;
		this.vis = vis;
		int intervall = (int) (1000f / FRAME_RATE);
		
		updateDisplayTimer = new Timer(intervall, new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{				
				ArrayList<Animation> arr = new ArrayList<Animation>();
				arr.addAll(runningAnimations);
				for(Animation a : arr)
				{
					if(a instanceof CameraAnimation)
					{
						if(currentCameraAnimation != null){
							if(currentCameraAnimation != a){
								continue;
							} 
						}else{
							currentCameraAnimation = a;
						} 
					}
					if(!a.isInit()) 
						a.init(durations.get(a), vis);
					
					if(!a.doStep()){
						openAnimationCount--;
						runningAnimations.remove(a);
						if(a instanceof CameraAnimation){
							currentCameraAnimation = null;
						}
					}
				}
				
				vis.display();
				
				if(openAnimationCount == 0)
				{
					updateDisplayTimer.stop();
					animationTimers.clear();
					runningAnimations.clear();
					vis.display();
					notifyListeners();		
				}
			}
		});
	}
	
	

	public void start()
	{
		running = true;
		vis.startAnimationFrameCount();
		vis.setSliceMode(sliceMode);

		updateDisplayTimer.start();
		for(Timer t : animationTimers.values())
		{
			t.start();
		}
	}
	
	public void stop()
	{
		running = false;
		
		for(Timer t : animationTimers.values()){
			t.stop();
		}
		updateDisplayTimer.stop();
	}
	
	public void resume()
	{
		running = true;
		for(Timer t : animationTimers.values()){
			t.start();
		}
		updateDisplayTimer.start();
	}
	
	
	protected void notifyListeners()
	{
		for(TransitionListener l : listeners)
			l.transitionFinished(this);
	}

	
	/////////////////
	/// GET & SET /// 
	/////////////////
	
	/** Adds a single animation **/
	public int addAnimation(final Animation a, final float delay, final float d)
	{
		openAnimationCount++;
		
		Timer startTimer = new Timer((int) delay, null);
		startTimer.setInitialDelay((int) delay);
		startTimer.setRepeats(false);
		startTimer.addActionListener(new ActionListener(){
		
			public void actionPerformed(ActionEvent e)
			{	
				durations.put(a, (int) (d/1000f * FRAME_RATE));
				runningAnimations.add(a);
			}}
		);
		animationTimers.put(a, startTimer);
		if(delay+d > maxTime)
		{
			maxTime = (int) Math.max(maxTime , delay+d);
		}
		
		return (int) (delay + d);
	}
	
	public int addAnimation(final Animation a, final float duration){
		return addAnimation(a, 0, duration);
	}

	public void addListener(TransitionListener l) {listeners.add(l);}
	public void removeListeners() {listeners.clear();}


	public boolean isRunning() {
		return openAnimationCount > 0;
	}
	
	public void setName(String s){this.name = s;}
	public String getName(){return this.name;}


	public void setFinalSliceMode(SliceMode sliceMode) {
		this.finalSliceMode  = sliceMode;
	}
	
	public SliceMode getFinalSliceMode(){return this.finalSliceMode;}
	
}
