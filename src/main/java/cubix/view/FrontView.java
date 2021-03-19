package cubix.view;

import cubix.CubixVis;
import cubix.data.MatrixCube;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Camera;
import cubix.vis.Slice;

public class FrontView extends CubeView {

	public FrontView() {
		super();
		name = "Front";
		cameraPos = new float[]{0,0,1};
		cameraLookAt = new float[]{0,0,0};
		viewAngle = ANGLE_ORTHO;
	}
	
	@Override
	public void init(CubixVis vis)
	{
		super.init(vis);

		MatrixCube mc = vis.getMatrixCube();	
		
		float width = mc.getTimeSlice(0).getColumnCount() * vis.CELL_UNIT;
		int num = mc.getVisibleHNodeSlices().size();
		int num2 = mc.getTimeSlices().size();
		int step = 1;
		int count = 0;
		float[] pos;
		for(Slice<?,?> s : mc.getTimeSlices())
		{
			pos = new float[]{(num/2+10) * vis.CELL_UNIT, num2/2 - step*count - vis.CELL_UNIT/2, -num/2 + step*count};
			labelPosR.put(s, pos);
			labelAlignR.put(s, Align.CENTER);
			labelPosL.put(s, pos);
			labelAlignL.put(s, Align.CENTER);	
			count++;
		}
			
		float d = (float) ((mc.getVisibleRowCount() * vis.CELL_UNIT * 2) / (2 * Math.tan(ANGLE_PERSP * Math.PI / 360)));
		
		cameraPos = new float[]{0,0,d};
	}
	
	
}
