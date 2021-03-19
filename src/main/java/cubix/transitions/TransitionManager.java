package cubix.transitions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Timer;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.data.MatrixCube;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.transitions.animation.Animation;
import cubix.transitions.animation.CameraTranslationAnimation;
import cubix.transitions.animation.CameraPerspectiveAnimation;
import cubix.transitions.animation.FadeInLabelAnimation;
import cubix.transitions.animation.FadeOutLabelAnimation;
import cubix.transitions.animation.MoveLabelAnimation;
import cubix.transitions.animation.SliceRotationAnimation;
import cubix.transitions.animation.ScaleAnimation;
import cubix.transitions.animation.SliceTranslationAnimation;
import cubix.view.CView;
import cubix.view.CubeView;
import cubix.view.FrontView;
import cubix.view.GraphSMView;
import cubix.view.NodeSMView;
import cubix.view.SourceSideView;
import cubix.view.TargetSideView;
import cubix.view.ViewManager;
import cubix.vis.Camera;
import cubix.vis.TimeSlice;
import cubix.vis.HNodeSlice;
import cubix.vis.Slice;
import cubix.vis.VNodeSlice;

/**
 * Factory class for creating transitions of particular
 * purpose. Temporarily. Must be a transition Manager that
 * controlls the transitions:
 * 
 * @author benjamin.bach@inria.fr
 *
 */
public class TransitionManager implements Constants, TransitionListener
{
	
	public static final int TRANS_CUBE_FRONT = 0;
	public static final int TRANS_CUBE_SOURCESIDE = 1;
	public static final int TRANS_CUBE_TARGETSIDE = 10;
	public static final int TRANS_CUBE_GRAPH_SM = 2;
	public static final int TRANS_CUBE_NODE_SM = 3;
	public static final int TRANS_FRONT_CUBE = 4;
	public static final int TRANS_FRONT_SIDE = 5;
	public static final int TRANS_SIDE_CUBE = 6;
	public static final int TRANS_SIDE_FRONT = 7;
	public static final int TRANS_GRAPH_SM_CUBE = 8;
	public static final int TRANS_NODE_SM_CUBE = 9;
	private static final int DURATION = 500;
	
	private Transition currentTransition = null;
	private CubixVis vis;
	
	private HashMap<Class, HashMap<Class, Integer>> transitions = new HashMap<Class, HashMap<Class, Integer>>();
	private int nextTransition;
	private ViewManager vm;
	private int nextDuration;
	private float ACCELERATION = 0.95f;
	private int DEFAULT_DURATION_LABEL = 500;
	private int labelDuration;
	
	
	public TransitionManager(CubixVis vis){
		this.vis = vis;
		this.vm = ViewManager.getInstance();
	}
	

	
	////////////////////////
	/// HELPER FUNCTIONS /// 
	////////////////////////

	/** Returns the durations for each eucledean movement
	 * so that speed is the same among all movements.
	 */
	public int[] getEqualDurations(float[] pos, int duration)
	{
		int[] durs = new int[]{0,0,0};
		
		float l = Utils.length(pos);
		durs[X] = (int) (Math.sqrt(pos[X]) / l * duration);
		durs[Y] = (int) (Math.sqrt(pos[Y]) / l * duration);
		durs[Z] = (int) (Math.sqrt(pos[Z]) / l * duration);
		
		return durs;
	}

	///////////////
	/// CONTROL ///
	///////////////

	/**
	 * Starts the passed transition if no other transition is running.
	 */
	public void startTransition(Transition transition){

		if(this.isRunning()) return;
		
		if(transition == null){
//			Log.err(this, "Passed transition is NULL!");
			return;
		}
		
		if(currentTransition != null
				&& currentTransition.isRunning()){
//			Log.err(this, "Cannot start new transition, previous transition still running!");
			return;
		}
//		Log.out(this, "Start transition.");
		currentTransition = transition;
		transition.start();
	}
	
	public void stopTransition(){
		if(currentTransition != null)
			currentTransition.stop();
	}
	public void resumeTransition(){
		if(currentTransition != null)
			currentTransition.stop();
	}
	

	public boolean isRunning() {
		if(currentTransition != null 
				&& currentTransition.isRunning())
		return true; 
		return false;
	}
	
	
	//////////////////
	/// VIEW LEVEL ///
	//////////////////

	
	public Transition getTransition(CView currentView, CView targetView)
	{
		if(this.isRunning()) return null;
		
		if(currentView == targetView)
			return null;
		
		int duration = DURATION;
		
		// CUBE
		HashMap<Class, Integer> trans = new HashMap<Class, Integer>();
		transitions.put(CubeView.class, trans);
		trans.put(FrontView.class, this.TRANS_CUBE_FRONT);
		trans.put(SourceSideView.class, this.TRANS_CUBE_SOURCESIDE);
		trans.put(TargetSideView.class, this.TRANS_CUBE_TARGETSIDE);
		trans.put(GraphSMView.class, this.TRANS_CUBE_GRAPH_SM);
		trans.put(NodeSMView.class, this.TRANS_CUBE_NODE_SM);

		// FRONT
		trans = new HashMap<Class, Integer>();
		transitions.put(FrontView.class, trans);
		trans.put(CubeView.class, this.TRANS_FRONT_CUBE);
		trans.put(SourceSideView.class, this.TRANS_FRONT_SIDE);

		// SIDE
		trans = new HashMap<Class, Integer>();
		transitions.put(SourceSideView.class, trans);
		trans.put(CubeView.class, this.TRANS_SIDE_CUBE);
		trans.put(FrontView.class, this.TRANS_SIDE_FRONT);

		// GRAPH_SM
		trans = new HashMap<Class, Integer>();
		transitions.put(GraphSMView.class, trans);
		trans.put(CubeView.class, this.TRANS_GRAPH_SM_CUBE);

		// NODE_SM
		trans = new HashMap<Class, Integer>();
		transitions.put(NodeSMView.class, trans);
		trans.put(CubeView.class, this.TRANS_NODE_SM_CUBE);
		
		nextTransition = -1;

		int tNum;
		try{
			tNum = transitions.get(currentView.getClass()).get(targetView.getClass());
		}catch(NullPointerException ex)
		{
			tNum = transitions.get(currentView.getClass()).get(CubeView.class);
			nextTransition = transitions.get(CubeView.class).get(targetView.getClass());
			nextDuration = duration;
		}
		Transition t = createTransition(tNum, duration);
		t.addListener(this);
		
		return t;
	}
	
	protected Transition createTransition(int tNum, int duration)
	{
		Transition t = null;
		float durFac = vis.getTransitionDurationFactor();
//		Log.out(this, "durFac " + durFac);
		labelDuration = (int) (DEFAULT_DURATION_LABEL * vis.getTransitionDurationFactor());
		
		switch(tNum)
		{
			case TRANS_CUBE_FRONT 	: t = this.getCube2FrontTransition( 600 * durFac ); break;
			case TRANS_CUBE_SOURCESIDE	: t = this.getCube2SourceSideTransition( 600 * durFac);break;
			case TRANS_CUBE_TARGETSIDE	: t = this.getCube2TargetSideTransition( 600 * durFac);break;
			case TRANS_FRONT_SIDE 	: t = this.getFront2SideTransition(600 * durFac);break;
			case TRANS_FRONT_CUBE 	: t = this.getFront2CubeTransition(500 * durFac);break;
			case TRANS_SIDE_FRONT 	: t = this.getSide2FrontTransition(600 * durFac);break;
			case TRANS_SIDE_CUBE 	: t = this.getSide2CubeTransition(500 * durFac);break;
			
			case TRANS_CUBE_GRAPH_SM: t = this.getCube2GraphSMTransition2(600 * durFac);break;
			case TRANS_CUBE_NODE_SM : t = this.getCube2NodeSMTransition2(600 * durFac);break;
			case TRANS_GRAPH_SM_CUBE: t = this.getGraphSM2CubeTransition(600 * durFac);break;
			case TRANS_NODE_SM_CUBE	: t = this.getNodeSM2CubeTransition(600 * durFac);break;
		}
		return t;
	}
	


	
	public void transitionFinished(Transition transition)
	{
		if(nextTransition > -1)
		{
//			vis.transitionFinished(transition);
			currentTransition = createTransition(nextTransition, nextDuration);
			Timer t1 = new Timer(500, new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentTransition.start();
				}});
			t1.setRepeats(false);
			t1.start();
		}
	}
	
	
	///////////////////
	/// SLICE LEVEL ///
	///////////////////

	
	public Transition getRotateSliceTransition(AbstractList<? extends Slice> slices, Slice<?,?> sliceToRotate, int duration, float angle, float offsetFactor)
	{
		if(!slices.contains(sliceToRotate)){
			return null;
		}
		
		SliceMode sliceMode = null;
		SliceMode finalSliceMode = null;
		if(sliceToRotate instanceof TimeSlice){
			sliceMode = SliceMode.TIME;
			finalSliceMode = SliceMode.VNODE;
		}
		else if(sliceToRotate instanceof VNodeSlice){
			sliceMode = SliceMode.VNODE;
			finalSliceMode = SliceMode.TIME;
		}
		
		
		Transition trans = new Transition(vis, sliceMode);
		trans.setName("SliceRotation");
		trans.setFinalSliceMode(finalSliceMode);
		Animation a;
		float offset = (float) -Math.sin(angle * Math.PI / 180) * (vis.getSliceWidth(sliceToRotate) - CubixVis.CELL_UNIT) / 2f;

		offset *= offsetFactor;
		
		// CAMERA ANIMATION
		float camAnimFraction = .5f;
		int durCameraAnim = (int) (duration * camAnimFraction);
		Camera c = vis.getCamera();
		float viewAngle = c.getViewAngle();
		a = new CameraPerspectiveAnimation(c, 20);
		trans.addAnimation(a, 0, durCameraAnim); 
		a = new CameraPerspectiveAnimation(c, viewAngle);
		trans.addAnimation(a, duration-durCameraAnim, durCameraAnim); 
		
		int rotationDelay = (int) (duration * .1f);
		int rotationDuration = duration - 2 * rotationDelay;
		for(Slice<?,?> s : slices)
		{
			if(s == sliceToRotate)
			{
				a = new SliceRotationAnimation(sliceToRotate, Y_AXIS, vis.getSliceRotation(sliceToRotate) + angle); ;
				trans.addAnimation(a, rotationDelay, rotationDuration);
				
				float[] pos_rLabel, pos_lLabel;
				if(s instanceof TimeSlice){
					if(angle < 0){
						pos_rLabel = sliceToRotate.getRelGridCoords(-6, sliceToRotate.getColumnCount()/2);
						pos_lLabel = sliceToRotate.getRelGridCoords(sliceToRotate.getRowCount()+4, sliceToRotate.getColumnCount()/2);
						sliceToRotate.setLeftLabelAlign(Align.CENTER);
						sliceToRotate.setRightLabelAlign(Align.CENTER);
					}else{
						pos_rLabel = vis.getCurrentView().getLabelPosR(s);
						pos_lLabel = vis.getCurrentView().getLabelPosL(s);
						sliceToRotate.setLeftLabelAlign(Align.RIGHT);
						sliceToRotate.setRightLabelAlign(Align.LEFT);
					}
				}else{
					if(angle > 0){
						pos_lLabel = sliceToRotate.getRelGridCoords(-6, sliceToRotate.getColumnCount()/2);
						pos_rLabel = sliceToRotate.getRelGridCoords(sliceToRotate.getRowCount()+3, sliceToRotate.getColumnCount()/2);
						sliceToRotate.setLeftLabelAlign(Align.CENTER);
						sliceToRotate.setRightLabelAlign(Align.CENTER);
					}else{
						pos_rLabel = vis.getCurrentView().getLabelPosR(s);
						pos_lLabel = vis.getCurrentView().getLabelPosL(s);
						sliceToRotate.setLeftLabelAlign(Align.RIGHT);
						sliceToRotate.setRightLabelAlign(Align.LEFT);
					}
				}
				
				a = new MoveLabelAnimation(sliceToRotate, pos_lLabel, pos_rLabel);
				trans.addAnimation(a, 0, rotationDuration);

				if(sliceToRotate instanceof VNodeSlice)
				{
					if(angle > 0 ){
						a = new FadeInLabelAnimation(sliceToRotate, new float[]{1f,0f,1f,0f});
						trans.addAnimation(a, rotationDuration/2, rotationDuration/2);
					}else{
						a = new FadeOutLabelAnimation(s, new float[]{0f,1f,0f,1f});
						trans.addAnimation(a, rotationDuration / 2, rotationDuration / 2);
					}
				}else{
					if(angle < 0 ){
						a = new FadeInLabelAnimation(sliceToRotate, new float[]{1f,0f,1f,0f});
						trans.addAnimation(a, rotationDuration/2, rotationDuration/2);
					}else{
						a = new FadeOutLabelAnimation(s, new float[]{0f,1f,0f,1f});
						trans.addAnimation(a, rotationDuration / 2, rotationDuration / 2);
					}
				}

				offset = -offset;
			}else
			{
				if(vis.isVSliceMode()) 
					a = new SliceTranslationAnimation(s, Utils.add(vis.getSlicePos(s), new float[]{-offset, 0f, 0f}));
				else 
					a = new SliceTranslationAnimation(s, Utils.add(vis.getSlicePos(s), new float[]{0f, 0f, -offset}));
				trans.addAnimation(a, rotationDelay, rotationDuration);

				if(!vis.isRotated(s))
				{
					a = new FadeOutLabelAnimation(s, new float[]{0f,1f,0f,1f});
					trans.addAnimation(a, 0, rotationDuration / 2);
				}
			}
		}
		
		return trans;
	}
	
	public Transition getRotateHSliceTransition(ArrayList<Slice<?, ?>> renderedSlices, Slice<?,?> sliceToRotate, int duration, float angle)
	{
		if(!renderedSlices.contains(sliceToRotate)){
			Log.err(this, "Slice to rotate is not a rendered slice!");
			return null;
		}
		float[] axis = X_AXIS.clone();
		if(vis.isSourceSideView())
			axis = Z_AXIS;
			
		Transition trans = new Transition(vis, SliceMode.HNODE);
		Animation a;
		float offset = (float) Math.sin(angle * Math.PI / 180) * (vis.getSliceHeight(sliceToRotate) - CubixVis.CELL_UNIT) / 2f;

		float[] pos_lLabel; 
		float[] pos_rLabel;
		
		for(Slice<?,?> s : renderedSlices)
		{
			if(s == sliceToRotate)
			{
				a = new SliceRotationAnimation(sliceToRotate, axis, vis.getSliceRotation(sliceToRotate) + angle); ;
				trans.addAnimation(a, 0, duration);
				a = new FadeInLabelAnimation(sliceToRotate, new float[]{0f,1f,0f,1f}); ;
				trans.addAnimation(a, duration/2, duration/2);

				if(angle < 0){
					sliceToRotate.setLeftLabelAlign(Align.RIGHT);
					sliceToRotate.setRightLabelAlign(Align.LEFT);
					pos_lLabel = sliceToRotate.getRelGridCoords(0, sliceToRotate.getColumnCount() + 3);
					pos_rLabel = sliceToRotate.getRelGridCoords(0, -3);
				}else{
					pos_rLabel = vis.getCurrentView().getLabelPosR(s);
					pos_lLabel = vis.getCurrentView().getLabelPosL(s);
					sliceToRotate.setLeftLabelAlign(Align.RIGHT);
					sliceToRotate.setRightLabelAlign(Align.LEFT);
				}
		
				a = new MoveLabelAnimation(sliceToRotate,pos_lLabel, pos_rLabel);
				trans.addAnimation(a, 0, duration);
				
				offset = -offset;
			}else{
				a = new SliceTranslationAnimation(s, Utils.add(vis.getSlicePos(s), new float[]{0, -offset, 0f}));
				trans.addAnimation(a, 0, duration);
				
				a = new FadeOutLabelAnimation(s, new float[]{0f,1f,0f,1f});
				trans.addAnimation(a, 0, duration);
				
				if(!vis.isRotated(s)){
					a = new FadeOutLabelAnimation(s, new float[]{0f,1f,0f,1f});
					trans.addAnimation(a, 0, 100);
				}
			}
		}
		
		return trans;
	}
	

	////////////////////////
	/// VIEW TRANSITIONS ///
////////////////////////////////////////////////////////////////////////////////////////
	
	

	public Transition getGraphSM2CubeTransition(float duration)
	{
		CView targetView = vm.getView(vm.VIEW_CUBE);
		
		Transition trans = new Transition(vis);
		Animation a;
		CView v = vm.getView(vm.VIEW_CUBE);
		float[] pos;		
		MatrixCube cube = vis.getMatrixCube();
		
		
		// CAMERA
		Camera c = vis.getCamera();
		int end;
		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
		end = trans.addAnimation(a, 0, duration/2f);
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, 500);
		
		int delay = end;
		float delayDelta = duration / 10f;
		float stepDur = duration;
		
		Slice s;
		for(int t=0 ; t < cube.getTimeCount() ; t++)
		{
			s = cube.getTimeSlice(t);
			pos = targetView.getSlicePosition(s);

			a = new SliceTranslationAnimation(s, pos);
			trans.addAnimation(a, delay, stepDur);

			a = new SliceRotationAnimation(s, Y_AXIS, 0); ;
			trans.addAnimation(a, delay, stepDur);

			a = new ScaleAnimation(s, 1f); ;
			trans.addAnimation(a, delay, stepDur);
			
			if(cube.getFirstTimeSlice() != s
			&& cube.getLastTimeSlice() != s)
			{
				a = new FadeOutLabelAnimation(s, new float[]{0f,0f,0f,0f});
				trans.addAnimation(a, 0, 500);
			}
			
			delay += delayDelta;
			delayDelta *= ACCELERATION;
			stepDur *= ACCELERATION;
		}
		
		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, duration/2f, duration/2);

		// LABELS
		labelDuration = 100;
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 500);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);


		return trans;
	}
	
	
	
//	public Transition getCube2GraphSMTransition(float duration)
//	{
//		CView targetView = vm.getView(vm.VIEW_GRAPH_SM);
//		
//		Transition trans = new Transition(vis, SliceMode.TIME);
//		Animation a;
//		MatrixCube cube = vis.getMatrixCube();
//		
//	
//		float stepDur = duration;
//		
//		// MOVE BLOCKS
//		int colAmount = ((GraphSMView) vm.getView(vm.VIEW_GRAPH_SM)).getColAmount();
//		int rowAmount = ((GraphSMView) vm.getView(vm.VIEW_GRAPH_SM)).getRowAmount();
//
//		int count = 0;
//		float yOffset = rowAmount * vis.getSliceHeight(cube.getTimeSlice(0));
//			
//		float[] pos;
//		int delay = 0;
//		int deltaDelay = 0;
//		Slice s;
//		for(int i=0 ; i<cube.getTimeSlices().size() ; i++)
//		{
//			s = cube.getTimeSlice(i);
//			delay = deltaDelay;
//			
//			a = new SliceTranslationAnimation(s, targetView.getSlicePosition(s));
//			trans.addAnimation(a, delay, stepDur);
//			a = new SliceRotationAnimation(s, -90);
//			trans.addAnimation(a, delay, stepDur);
//
//			stepDur *=.95; // Speed up aniamtions
//			deltaDelay += stepDur/5;
//		}
//
//		// CAMERA
//		int cDur = deltaDelay/2;
//
//		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
//		delay = trans.addAnimation(a, 0, cDur);
//		delay += duration/10; 
//		
//		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
//		delay = trans.addAnimation(a, delay, duration/5);
//		
//		// LABELS
//		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, duration/5);
//		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);
//
//		
//		return trans;
//	}

	public Transition getCube2GraphSMTransition2(float duration)
	{
		CView targetView = vm.getView(vm.VIEW_TIME_SM);
		
		Transition trans = new Transition(vis, SliceMode.TIME);
		Animation a;
		MatrixCube cube = vis.getMatrixCube();
		
		float stepDur = duration;
					
		float[] pos;
		int delay = 0;
		Slice<?,?> s;
		for(int i=0 ; i<cube.getTimeSlices().size() ; i++)
		{
			s = cube.getTimeSlice(i);
			pos = vis.getSlicePos(s);
			pos[Y] = targetView.getSlicePosition(s)[Y];
			a = new SliceTranslationAnimation(s, pos);
			delay = trans.addAnimation(a, 0, duration/2);
		}
		delay += 200;
		
		for(int i=0 ; i<cube.getTimeSlices().size() ; i++)
		{
			s = cube.getTimeSlice(i);
			
			a = new SliceTranslationAnimation(s, targetView.getSlicePosition(s));
			trans.addAnimation(a, delay, stepDur);
			a = new SliceRotationAnimation(s, -90);
			trans.addAnimation(a, delay, stepDur);

//			trans.addAnimation(new FadeOutLabelAnimation(s, new float[]{0f,0f,0f}), 100, 200);
			delay += duration/10; 
		}

		// CAMERA
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  delay, duration/2);
		
		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
		delay = trans.addAnimation(a, delay/3, duration);
		
		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
		delay = trans.addAnimation(a, delay+100, 200);
		
		// LABELS
//		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 200);
	//	_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);


		return trans;
	}

//	public Transition getCube2NodeSMTransition(float duration)
//	{
//		duration *=2;
//		CView targetView = vm.getView(vm.VIEW_NODE_SM);
//		
//		Transition trans = new Transition(vis, SliceMode.VNODE);
//		Animation a;
//		MatrixCube cube = vis.getMatrixCube();
//		
//	
//		float stepDur = duration;
//		
//		// MOVE BLOCKS
//		int colAmount = ((NodeSMView) vm.getView(vm.VIEW_NODE_SM)).getColAmount();
//		int rowAmount = ((NodeSMView) vm.getView(vm.VIEW_NODE_SM)).getRowAmount();
//
//		int count = 0;
//		float yOffset = rowAmount * vis.getSliceHeight(cube.getTimeSlice(0));
//			
//		float[] pos;
//		int delay = 0;
//		int deltaDelay = 0;
//		Slice s;
//		for(int i=0 ; i<cube.getVNodeSlices().size() ; i++)
//		{
//			s = cube.getVNodeSlice(i);
//			delay = deltaDelay;
//			
//			a = new TranslationAnimation(s, targetView.getSlicePosition(s));
//			trans.addAnimation(a, delay, stepDur);
//			a = new RotationAnimation(s, 90);
//			delay = trans.addAnimation(a, delay, stepDur);
//			stepDur *=.95;
//			deltaDelay += stepDur/5;
//		}
//
//		// CAMERA
//		int cDur = delay/2;
//		Camera c = vis.getCamera();
//		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
//		delay = trans.addAnimation(a, 0, cDur);
//		delay += duration/10; 
//		
//		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
//		delay = trans.addAnimation(a, delay, duration/5);
//	
//		// LABELS
//		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 200);
//		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);
//	
//		return trans;
//	}
	
	public Transition getCube2NodeSMTransition2(float duration)
	{
		CView targetView = vm.getView(vm.VIEW_VNODESLICE_SM);
		
		Transition trans = new Transition(vis, SliceMode.VNODE);
		Animation a;
		MatrixCube cube = vis.getMatrixCube();
		
		
		float stepDur = duration;
		
		float[] pos;
		int delay = 0;
		Slice<?,?> s;
		for(int i=0 ; i<cube.getVisibleVNodeSlices().size() ; i++)
		{
			s = cube.getVisibleVNodeSlice(i);
			pos = vis.getSlicePos(s);
			pos[Y] = targetView.getSlicePosition(s)[Y];
			a = new SliceTranslationAnimation(s, pos);
			delay = trans.addAnimation(a, 0, duration/2);
		}
		delay += 200;
		
		for(int i=0 ; i<cube.getVisibleVNodeSlices().size() ; i++)
		{
			s = cube.getVisibleVNodeSlice(i);
			
			a = new SliceTranslationAnimation(s, targetView.getSlicePosition(s));
			trans.addAnimation(a, delay, stepDur);
			a = new SliceRotationAnimation(s, 90);
			trans.addAnimation(a, delay, stepDur);

			delay += duration/10; 
		}

		// CAMERA
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  delay, duration/2);
		
		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
		delay = trans.addAnimation(a, delay/3, duration);
		
		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
		delay = trans.addAnimation(a, delay+100, 200);
			
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 200);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);
	

		return trans;
	}
	
	
	public Transition getNodeSM2CubeTransition(float duration)
	{
		CubixVis.SHOW_FRAMES= true; 
		CView targetView = vm.getView(vm.VIEW_CUBE);
		
		Transition trans = new Transition(vis);
		Animation a;
		float[] pos;		
		MatrixCube cube = vis.getMatrixCube();
		Slice s;
		
		// CAMERA
		Camera c = vis.getCamera();
		int end;
		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
		end = trans.addAnimation(a, 0, duration/2);
		
		int delay = end;
		float delayDelta = duration / 5;
		float stepDur = duration;
		
		for(int t=0 ; t < cube.getVisibleVNodeSlices().size() ; t++)
		{
//			s = cube.getVNodeSlice(t);
//			pos = targetView.getSlicePosition(s);
//			
//			a = new SliceTranslationAnimation(s, pos);
//			trans.addAnimation(a, delay, stepDur);
//
//			a = new SliceRotationAnimation(s, Y_AXIS, targetView.getSliceRotation(s)); ;
//			trans.addAnimation(a, delay, stepDur);
//
//			a = new ScaleAnimation(s, targetView.getSliceScale(s));
//			trans.addAnimation(a, delay, stepDur);
//			
//			if(cube.getFirstHNodeSlice() != s
//				&& cube.getLastHNodeSlice() != s)
//			{
//				a = new FadeOutLabelAnimation(s, new float[]{0f,0f,0f,0f});
//				trans.addAnimation(a, 0, duration/2);
//			}
//			
//			delay += delayDelta;
			s = cube.getVisibleVNodeSlice(t);
			pos = targetView.getSlicePosition(s);

			a = new SliceTranslationAnimation(s, pos);
			trans.addAnimation(a, delay, stepDur);

			a = new SliceRotationAnimation(s, Y_AXIS, 0); ;
			trans.addAnimation(a, delay, stepDur);

			a = new ScaleAnimation(s, 1f); ;
			trans.addAnimation(a, delay, stepDur);
			
			if(cube.getFirstVNodeSlice() != s
			&& cube.getLastVNodeSlice() != s)
			{
				a = new FadeOutLabelAnimation(s, new float[]{0f,0f,0f,0f});
				trans.addAnimation(a, 0, duration/2);
			}
			
			delay += delayDelta;
			delayDelta *= ACCELERATION;
			stepDur *= ACCELERATION;
		}
		
//		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
//		end = trans.addAnimation(a, end, delay + duration);
		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, duration/2f, duration/2);

		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 200);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, delay-labelDuration, labelDuration);

		
		return trans;
	}
	
//	public Transition getGraphExplosionTransition(int duration, float scale)
//	{
//		Transition trans = new Transition(vis);
//		Animation a;
//		MatrixCube cube = vis.getMatrixCube();
//		float numMatrices = cube.getGraphSlices().size(); 
//		
//		int delayDelta = duration / 10;
//		int stepDur = (int) (duration - delayDelta * (numMatrices-1));
//		
//		int zPos =0;
//		for(Slice m : cube.getGraphSlices()){
//			zPos += vis.getSlicePos(m)[Z];
//		}
//		zPos /= numMatrices;
//		
//		Slice s;
//		float[] newPos;
//		for(int t=0 ; t < cube.getTimeCount() ; t++)
//		{
//			s = cube.getGraphSlice(t);
//			newPos = vis.getSlicePos(s).clone();
//			a = new TranslationAnimation(s, Utils.add(newPos, new float[]{0,0, (newPos[Z] - zPos) * scale}));
//			trans.addAnimation(a, 0, stepDur);
//		}
//		
//		return trans;
//	}

	protected Transition getFront2CubeTransition(float f)
	{
		CView targetView = vm.getView(vm.VIEW_CUBE);
		
		Transition trans = new Transition(vis);	
		MatrixCube cube = vis.getMatrixCube();
		
		Animation a;
		int end = 0;
		Slice[] toRemove = new Slice[vis.getRotatedSlices().size()]; 
		int i=0;
		for(Slice s : vis.getRotatedSlices())
		{
			a = new SliceRotationAnimation(s, Y_AXIS, targetView.getSliceRotation(s)); ;
			end = trans.addAnimation(a, 0, f/2);
			toRemove[i] = s;
			i++;
		}
		
		for(i=0 ; i < toRemove.length ; i++)
			vis.removeRotatedSlice(toRemove[i]);
			
		Slice s;
		for(int n=0 ; n < cube.getVisibleVNodeSlices().size() ; n++){
			s = cube.getVisibleVNodeSlice(n);
			a = new SliceTranslationAnimation(s, targetView.getSlicePosition(s));
			trans.addAnimation(a, end, f/2);
		}
		
		Camera c = vis.getCamera();
		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end, f/2);	
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  end, f/2);
		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, end, f/2);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, f/2);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		
		return trans;
	}

	protected Transition getSide2CubeTransition(float f)
	{
		Transition trans = new Transition(vis);	
		MatrixCube cube = vis.getMatrixCube();
		Animation a;
		CView targetView = vm.getView(vm.VIEW_CUBE);
		int end = 0;
		Slice[] toRemove = new Slice[vis.getRotatedSlices().size()]; 
		int i=0;
		for(Slice s : vis.getRotatedSlices())
		{
			a = new SliceRotationAnimation(s, Y_AXIS, 0);
			end = trans.addAnimation(a, 0, f/2);
			toRemove[i] = s;
			i++;
		}
		for(i=0 ; i < toRemove.length ; i++)
			vis.removeRotatedSlice(toRemove[i]);


		Slice s;
		for(int n=0 ; n < cube.getTimeSlices().size() ; n++)
		{
			s = cube.getTimeSlice(n);
			a = new SliceTranslationAnimation(s, targetView.getSlicePosition(s));
			trans.addAnimation(a, end, f/2);
		}
		
		a = new CameraPerspectiveAnimation(vis.getCamera(), targetView.getViewAngle());
		end = trans.addAnimation(a, end + f/2, f/2);
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  end, f/2);
		a = new CameraTranslationAnimation(vis.getCamera(), targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, end, f/2);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, f/2);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		
		return trans;
	}

	protected Transition getCube2FrontTransition(float duration)
	{
		Camera c = vis.getCamera();
		CView targetView = vm.getView(vm.VIEW_FRONT);
		Transition trans = new Transition(vis, SliceMode.TIME);	
		Animation a;
		
		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		int end = trans.addAnimation(a, duration/2f);
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, end);
		
		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end+100, duration/2f);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, duration/2f);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		return trans;
	}
	
	
	protected Transition getCube2SourceSideTransition(float duration)
	{
		Camera c = vis.getCamera();
		CView targetView = vm.getView(vm.VIEW_SOURCE_SIDE);
		Transition trans = new Transition(vis, SliceMode.VNODE);	
		Animation a;
		
		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		int end = trans.addAnimation(a, duration/2f);	
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, duration/2f);

		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end+100, duration/2f);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, duration/2);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		
		return trans;
	}
	
	// VI created analogically to the one above
	protected Transition getCube2TargetSideTransition(float duration)
	{
		Camera c = vis.getCamera();
		CView targetView = vm.getView(vm.VIEW_TARGET_SIDE);
		Transition trans = new Transition(vis, SliceMode.HNODE);	
		Animation a;
		System.out.println(targetView.getCameraPos()[0]);
		System.out.println(targetView.getCameraPos()[1]);

		System.out.println(targetView.getCameraPos()[2]);

		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		int end = trans.addAnimation(a, duration/2f);	
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, duration/2f);

		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end+100, duration/2f);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, duration/2);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		
		return trans;
	}	
	
	
	private Transition getFront2SideTransition(float f) 
	{
		Camera c = vis.getCamera();
		CView targetView = vm.getView(vm.VIEW_SOURCE_SIDE);
		
		Transition trans = new Transition(vis);	
		Animation a; 
		vis.setSliceMode(SliceMode.TIME);
		a = new CameraPerspectiveAnimation(c, 40f);
		int end = trans.addAnimation(a, 0, f *.25f);	
		
		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, end, f *.5f);	
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, f * .5f);


		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end, f * .25f);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, f/2);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-labelDuration, labelDuration);

		
		return trans;
	}
	
	private Transition getSide2FrontTransition(float f) 
	{
		Camera c = vis.getCamera();
		CView targetView = vm.getView(vm.VIEW_FRONT);
		Transition trans = new Transition(vis);	
		Animation a; 
		
		a = new CameraPerspectiveAnimation(c, 40f);
		int end = trans.addAnimation(a, 0, f * .25f);	

		a = new CameraTranslationAnimation(c, targetView.getCameraPos(), targetView.getCameraLookAt());
		end = trans.addAnimation(a, end, f * .5f);	
		_addMoveLabelAnimations(trans, vis.getCurrentView(), targetView,  0, f * .5f);

		a = new CameraPerspectiveAnimation(c, targetView.getViewAngle());
		end = trans.addAnimation(a, end, f * .25f);	
		
		// LABELS
		_addHideLabelAnimations(trans, vis.getCurrentView(), targetView, 0, 200);
		_addShowLabelAnimations(trans, vis.getCurrentView(), targetView, end-DEFAULT_DURATION_LABEL, DEFAULT_DURATION_LABEL);

		
		return trans;
	}

	protected Transition _addHideLabelAnimations(Transition trans, CView v1, CView v2, float delay, float duration)
	{
		Animation a;
		ArrayList<Slice> slices1 = v1.getSlicesWithLabels();
		ArrayList<Slice> slices2 = v2.getSlicesWithLabels();
		for(Slice s1 : slices1)
		{
			if(!slices2.contains(s1)
			|| Utils.length(Utils.dir(v1.getLabelTrans(s1), v2.getLabelTrans(s1))) != 0)
			{
				a = new FadeOutLabelAnimation(s1, v2.getLabelTrans(s1));
				trans.addAnimation(a, delay, duration);
			}
		}
		return trans;
	}
	
	protected Transition _addMoveLabelAnimations(Transition trans, CView v1, CView v2, float delay, float duration)
	{
		Animation a;
		
		// FADE IN LABELS
		for(Slice s : vis.getRenderSlices())
		{
			try{
				a = new MoveLabelAnimation(s, v2.getLabelPosL(s), v2.getLabelPosR(s));
			trans.addAnimation(a, delay, duration);
			}catch(NullPointerException ex){
//				Log.err(this, "MoveLabelAnimation = NULL");
				}
		}
		return trans;
	}

	protected Transition _addShowLabelAnimations(Transition trans, CView v1, CView v2, float delay, float duration)
	{
		Animation a;
		
		ArrayList<Slice> slices1 = v1.getSlicesWithLabels();
		ArrayList<Slice> slices2 = v2.getSlicesWithLabels();

		// FADE IN LABELS
		for(Slice s2 : v2.getSlicesWithLabels())
		{
			if(!slices1.contains(s2)
			|| Utils.length(Utils.dir(v1.getLabelTrans(s2), v2.getLabelTrans(s2))) != 0)
			{
				a = new FadeInLabelAnimation(s2, v2.getLabelTrans(s2));
				trans.addAnimation(a, delay, duration);
			}
		}
		return trans;
	}
	

	//////////////
	/// CAMERA ///
	//////////////
	
	public Transition getCameraTransition(Camera c, float[] pos, float[] lookAt, int duration)
	{
		Transition t = new Transition(vis, vis.getSliceMode());
		Animation a = new CameraTranslationAnimation(c, pos, lookAt);
		t.addAnimation(a, duration);
		return t;
	}



}
