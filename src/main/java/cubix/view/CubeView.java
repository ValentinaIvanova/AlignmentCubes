package cubix.view;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.data.MatrixCube;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;
import cubix.vis.TimeSlice;

public class CubeView extends CView {

	
	
	public CubeView()
	{
		super("Cube", SliceMode.HNODE);
		cameraPos = new float[]{-1,1,1};
		cameraLookAt = new float[]{0,0,0};
		viewAngle = ANGLE_PERSP;

		
		// SET LABEL OFFSETS
		northOffset_t = new float[]{0, d,	-d};		
		southOffset_t = new float[]{0,	-d,	d};
		eastOffset_t = new float[]{	d,	0, 	d};
		westOffset_t = new float[]{	-d,	0,	-d};

		northOffset_v = new float[]{d,	d,	0};		
		southOffset_v = new float[]{-d,	-d,	0};	
		eastOffset_v = new float[]{d,	0,	d};	
		westOffset_v = new float[]{-d,	0,	-d};	

	}
		
		
	@Override
	public void init(CubixVis vis)
	{
		MatrixCube mc = vis.getMatrixCube();
	
		float[] pos;
		int t=0;
		for(TimeSlice s : mc.getTimeSlices())
		{
			t = mc.getTimeIndex(s.getData()); 
			pos = new float[]{ 0, 0, CubixVis.CELL_UNIT * (t - (mc.getTimeCount()-1)/2f)};
			positions.put(s,pos);
			angles.put(s, 0f);
			scales.put(s, 1f);
			labelTransparency.put(s, FLOAT4_0.clone());
			labelPosR.put(s, Utils.add(s.getRelGridCoords(0, s.getColumnCount()-1), new float[]{d,d,0}));
			labelPosL.put(s, Utils.add(s.getRelGridCoords(s.getRowCount()-1,0), new float[]{-d,-d,0}));
			labelAlignR.put(s, Align.LEFT);
			labelAlignL.put(s, Align.RIGHT);
		}
		
		Slice s;
		for(t=0 ; t < mc.getVisibleColumnCount() ; t++)
		{
			s = mc.getVisibleVNodeSlice(t);
			pos = new float[]{CubixVis.CELL_UNIT * (t-(mc.getVisibleColumnCount()-1)/2f), 0f, 0f};
			positions.put(s, pos);
			angles.put(s, 0f);
			scales.put(s, 1f);
			labelTransparency.put(s, FLOAT4_0.clone());
			labelPosL.put(s, Utils.add(s.getRelGridCoords(0, 0), new float[]{0,d,-d}));
			labelPosR.put(s, Utils.add(s.getRelGridCoords(s.getRowCount()-1,s.getColumnCount()-1), new float[]{0,-d,d}));
			labelAlignR.put(s, Align.LEFT);
			labelAlignL.put(s, Align.RIGHT);
		}
		
		for(t=0 ; t < mc.getVisibleRowCount() ; t++)
		{
			s = mc.getVisibleHNodeSlice(t);
			pos = new float[]{0, CubixVis.CELL_UNIT * (t-(mc.getVisibleRowCount()-1)/2f), 0f};
			positions.put(s, pos);
			angles.put(s, 0f);
			scales.put(s, 1f);
			labelTransparency.put(s, FLOAT4_0.clone());
			labelPosR.put(s, Utils.add(s.getRelGridCoords(0,0), new float[]{-d,0,-d}));
			labelPosL.put(s, Utils.add(s.getRelGridCoords(s.getRowCount()-1,s.getColumnCount()-1), new float[]{d,0,d}));
			labelAlignR.put(s, Align.RIGHT);
			labelAlignL.put(s, Align.LEFT);
			
		}
		
		float d = (float) (((mc.getVisibleRowCount() + mc.getTimeCount()) * vis.CELL_UNIT ) / (2 * Math.tan(viewAngle * Math.PI / 360)));
		float[] cPos = Utils.mult(Utils.normalize(new float[]{-1, 1, 1}), d*1.2f);
		cameraPos = cPos; 
		
		// SET LABELED MATRICES
		labelTransparency.get(mc.getFirstTimeSlice())[N] = 1;
		labelTransparency.get(mc.getFirstTimeSlice())[W] = 1;
		labelTransparency.get(mc.getLastTimeSlice())[E] = 1;
		labelTransparency.get(mc.getLastTimeSlice())[S] = 1;
		
		labelTransparency.get(mc.getLastVNodeSlice())[E] = 1;
		labelTransparency.get(mc.getLastVNodeSlice())[N] = 1;
		labelTransparency.get(mc.getVisibleVNodeSlice(0))[W] = 1;
		labelTransparency.get(mc.getVisibleVNodeSlice(0))[S] = 1;
	}
	
}
