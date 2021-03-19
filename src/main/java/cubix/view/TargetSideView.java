package cubix.view;

import cubix.CubixVis;
import cubix.data.MatrixCube;
import cubix.helper.Constants.Align;
import cubix.vis.Slice;

public class TargetSideView extends CubeView{
	
	
	public TargetSideView(){
		super();
		name = "TargetSide";
		cameraPos = new float[]{0,1,0}; // Camera position vector. Needs to be streched or skewed when cube coordinats are ready
		cameraLookAt = new float[]{0,0,0};
		viewAngle = ANGLE_ORTHO;
	}
	
	
	@Override
	public void init(CubixVis vis){
		
		super.init(vis);
		
		MatrixCube mc = vis.getMatrixCube();
		
		int num = mc.getTimeSlices().size();
		int num2 = mc.getVisibleVNodeSlices().size();
		int step = 1;
		int count = 0;
		float[] pos;		
 		for(Slice<?,?> s : mc.getVisibleHNodeSlices()) 
		{
			pos = new float[]{-num/2 + step*count, -num2/2 + step*count - vis.CELL_UNIT/2, (num/2+5) * vis.CELL_UNIT};
			labelPosR.put(s, pos.clone());
			labelAlignR.put(s, Align.CENTER);
			labelPosL.put(s, pos.clone());
			labelAlignL.put(s, Align.CENTER);	
			count++;
		}
	
	
		float d = (float) ((mc.getVisibleColumnCount() * vis.CELL_UNIT * 2) / (2 * Math.tan(ANGLE_PERSP * Math.PI / 360)));
		cameraPos = new float[]{0, d, 0};	}

}
