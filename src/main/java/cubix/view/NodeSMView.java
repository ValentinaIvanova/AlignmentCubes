package cubix.view;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.data.MatrixCube;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public class NodeSMView extends CView {

	public int colAmount;
	public int rowCount;

	public NodeSMView() {
		super("VertexSM", SliceMode.VNODE);
		cameraPos = new float[]{-1,0,0};
		viewAngle = ANGLE_ORTHO;
		
		northOffset_v = new float[]{	0,	d,	0};		
		southOffset_v = new float[]{	0,	-d,	0};	
		eastOffset_v = new float[]{	d,	0,	d};	
		westOffset_v = new float[]{	d,	0,	-d};
	}

	@Override
	public void init(CubixVis vis) 
	{
		MatrixCube mc = vis.getMatrixCube();

		float zOffset = .2f;
		float yOffset = 1.5f;

		float sliceHeight = vis.getSliceHeight(mc.getLastVNodeSlice());
		float sliceWidth = vis.getSliceWidth(mc.getLastVNodeSlice());

		float a = mc.getVisibleVNodeSlices().size() * (sliceHeight+yOffset) * (sliceWidth+zOffset); 
		float b = (float) Math.sqrt(a);
		int wCount = (int) (b / (sliceWidth+zOffset));
		colAmount = Math.max(1,wCount);
		colAmount = 6;
		
			
		float[] pos = new float[]{
				-(colAmount-1)/2f * sliceWidth/2,
				(colAmount-1)/2f * sliceHeight/2,
				0
		};
		float[] pos2;
		
		float[] colOffset = new float[]{sliceWidth + zOffset,0, 0};
		float[] rowOffset = new float[]{0, -sliceHeight - yOffset, 0};
	
		int  colCount = 0;
//		int  colCount = 11;
		Slice s;
		rowCount = 0;
		for(int t=0 ; t < mc.getVisibleVNodeSlices().size(); t++)
		{
			s = mc.getVisibleVNodeSlice(t);
			labelTransparency.put(s, FLOAT4_0.clone());
			
			if(colCount == 0){
				labelTransparency.get(s)[W] = 1;
			}
			if(rowCount == 0){
				labelTransparency.get(s)[N] = 1;
			}
			pos2 = pos.clone();
			pos2[Z] = vis.getSlicePos(s)[Z];	
			
			positions.put(s, pos2);
	
			pos = Utils.add(pos, colOffset);
			colCount++;
			if(colCount == colAmount)
			{
				labelTransparency.get(s)[E] = 1;
				pos = Utils.add(pos, Utils.mult(colOffset, -colAmount));
				pos = Utils.add(pos, rowOffset);
				colCount=0;
				rowCount++;
			}

			angles.put(s,0f);
			scales.put(s,1f);
			labelPosR.put(s, s.getRelGridCoords(s.getRowCount()+.5f, s.getColumnCount()/2));
			labelPosL.put(s, s.getRelGridCoords(s.getRowCount()+.5f, s.getColumnCount()/2));
			labelAlignR.put(s, Align.CENTER);
			labelAlignL.put(s, Align.CENTER);
		}
		
		s = mc.getVisibleVNodeSlice(0);
		float xMean = positions.get(s)[X] + vis.getSliceWidth(s) * (colAmount-1)/2f;
		float yMean = positions.get(s)[Y] - vis.getSliceHeight(s) * (rowCount-2)/2f;
		//float xMean = positions.get(s)[X] + sliceWidth * (colAmount-1)/2f;
		//float yMean = positions.get(s)[Y] - sliceHeight * (rowCount-2)/2f;

		float d = (float) ((vis.getSliceWidth(s) * colAmount/2 * 1.5) / ( Math.tan(ANGLE_PERSP * Math.PI / 360)));
		cameraPos = new float[]{xMean, yMean, d};	
		cameraLookAt = cameraPos.clone();
		cameraLookAt[Z] = -1;	
		
		labelTransparency.get(mc.getVisibleVNodeSlice(0))[N] = 1;
		labelTransparency.get(mc.getLastVNodeSlice())[E] = 1;
//		labelTransparency.get(mc.getLastVNodeSlice())[S] = 1;
		labelTransparency.get(mc.getVisibleVNodeSlice(0))[W] = 1;

	}
	
	public int getRowAmount() { return this.rowCount; }
	public int getColAmount() { return this.colAmount; }


}
