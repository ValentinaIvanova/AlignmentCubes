package cubix.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.data.CNode;
import cubix.data.MatrixCube;
import cubix.helper.Constants;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public abstract class CView implements Constants{

	protected HashMap<Slice, float[]> positions = new HashMap<Slice, float[]>();
	protected HashMap<Slice, Float> scales = new HashMap<Slice, Float>();
	protected HashMap<Slice, Float> angles = new HashMap<Slice, Float>();
	
	protected HashMap<Slice, float[]> labelTransparency = new HashMap<Slice, float[]>();
	protected HashMap<Slice, float[]> labelPosL = new HashMap<Slice, float[]>();
	protected HashMap<Slice, Align> labelAlignL = new HashMap<Slice, Align>();
	protected HashMap<Slice, float[]> labelPosR = new HashMap<Slice, float[]>();
	protected HashMap<Slice, Align> labelAlignR = new HashMap<Slice, Align>();
	
	
	protected float[] cameraPos = new float[]{0,0,0};
	protected float[] cameraLookAt = new float[]{0,0,0};
	protected static final float ANGLE_ORTHO = .1f;
	protected static final float ANGLE_PERSP = 45f;
	protected float viewAngle = ANGLE_PERSP;
	
	protected SliceMode sliceMode = null;
	public String name = "";

	protected float d = CubixVis.CELL_UNIT;
	protected float[] northOffset_t = new float[]{0,0,0,0};
	protected float[] eastOffset_t = new float[]{0,0,0,0};
	protected float[] southOffset_t = new float[]{0,0,0,0};
	protected float[] westOffset_t = new float[]{0,0,0,0};
	protected float[] northOffset_v = new float[]{0,0,0,0};;
	protected float[] eastOffset_v = new float[]{0,0,0,0};;
	protected float[] westOffset_v = new float[]{0,0,0,0};;
	protected float[] southOffset_v = new float[]{0,0,0,0};;


	
	protected CView(String name, SliceMode sliceMode){
		this.name = name;
		this.sliceMode = sliceMode;
	}
	
	public abstract void init(CubixVis vis);
	
	
	/////////////////
	/// GET & SET ///
	/////////////////
	
	public ArrayList<Slice> getSlicesWithLabels()
	{
		ArrayList<Slice> slices = new ArrayList<Slice>();
		for(Slice s: labelTransparency.keySet()){
			if(Utils.length(labelTransparency.get(s)) > 0){
				slices.add(s);
			}
		}
		return slices;
	}
	
	public float[] getSlicePosition(Slice s){ return this.positions.get(s).clone(); }
	public float getSliceScale(Slice s){ return this.scales.get(s); }
	public float getSliceRotation(Slice s){ return this.angles.get(s); }
	
	public SliceMode getSliceMode(){ return sliceMode;}

	public float[] getCameraPos(){return cameraPos.clone();}
	public float[] getCameraLookAt(){return cameraLookAt.clone();}
//	public boolean isCameraOrthogonal(){return ORTHO;}
	public float getViewAngle(){return viewAngle;}
	
	public String getName(){return this.name;}
	
	public float[] getLabelTrans(Slice s)
	{
		if(!labelTransparency.containsKey(s)){
//			Log.out(this, "no label trans for slice " + s.getLabel());
			return new float[]{0, 0, 0, 0};
		} 
		return labelTransparency.get(s).clone();
	}
	
	public float[] getLabelPosL(Slice s){return labelPosL.get(s); }
	public Align getLabelAlignL(Slice s){return labelAlignL.get(s); }
	public float[] getLabelPosR(Slice s){return labelPosR.get(s); }
	public Align getLabelAlignR(Slice s){return labelAlignR.get(s); }

	public float[] getNorthLabelOffset(boolean timeSliceMode) { if(timeSliceMode) return northOffset_t.clone() ; return northOffset_v.clone(); }
	public float[] getEastLabelOffset(boolean timeSliceMode) { if(timeSliceMode) return eastOffset_t.clone() ; return eastOffset_v.clone(); }
	public float[] getSouthLabelOffset(boolean timeSliceMode) { if(timeSliceMode) return southOffset_t.clone() ; return southOffset_v.clone(); }
	public float[] getWestLabelOffset(boolean timeSliceMode) { if(timeSliceMode) return westOffset_t.clone() ; return westOffset_v.clone(); }

	
}
