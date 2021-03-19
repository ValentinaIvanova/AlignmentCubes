package cubix.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import cubix.CubixVis;
import cubix.data.HierarchicalCNode;
import cubix.data.MatrixCube;
import cubix.vis.Slice;

public class SourceSideView extends CubeView {

	public SourceSideView() {
		super();
		name = "SourceSide";
		cameraPos = new float[]{-1,0,0}; // Camera position vector. Needs to be streched or skewed when cube coordinats are ready
		cameraLookAt = new float[]{0,0,0};
		viewAngle = ANGLE_ORTHO;
		
	}
	@Override
	public void init(CubixVis vis)
	{
		super.init(vis);
		
		MatrixCube mc = vis.getMatrixCube();

		int num = mc.getTimeSlices().size();
		int num2 = mc.getVisibleHNodeSlices().size();
		float step = 1.25f; //VI it was 1
		int count = 0;
		float[] pos;
		ArrayList<Slice<?, ?>> slices = new ArrayList<>();
		slices.addAll(mc.getVisibleVNodeSlices());
		Collections.reverse(slices);
 		for(Slice<?,?> s : slices) 
		{
 			// VI original
			//pos = new float[]{-num/2 + step*count, -num2/2 + step*count - vis.CELL_UNIT/2, (num/2+5) * vis.CELL_UNIT};
 			HierarchicalCNode node = (HierarchicalCNode) s.getData();
 			pos = new float[]{-num/2 + step*count, -num2/2 + step*count - CubixVis.CELL_UNIT/2, 
 					(node.getNodeDepth() * CubixVis.LABEL_LEVEL_INDENT) + (num/2+20) * CubixVis.CELL_UNIT};
			labelPosR.put(s, pos.clone());
			labelAlignR.put(s, Align.LEFT);	//VI it was CENTER
			labelPosL.put(s, pos.clone());
			labelAlignL.put(s, Align.LEFT);	//VI it was CENTER
			count++;
		}
	
	
		float d = (float) ((mc.getVisibleRowCount() * CubixVis.CELL_UNIT * 2) / (2 * Math.tan(ANGLE_PERSP * Math.PI / 360)));
		cameraPos = new float[]{-d, 0, 0};
	}

}
