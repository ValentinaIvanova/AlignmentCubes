package cubix.view;

import cubix.CubixVis;
import cubix.CubixVis.SliceMode;
import cubix.data.MatrixCube;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.Slice;

public class GraphSMView extends CView {

	public int colAmount;
	public int rowCount;

	public GraphSMView() {
		super("TimeSM", SliceMode.TIME);
		viewAngle = ANGLE_ORTHO;
		cameraPos = new float[]{0,0,1};
		
		northOffset_t = new float[]{	0,	d,	0};		
		southOffset_t = new float[]{	0,	-d,	0};	
		eastOffset_t = new float[]{	d,	0,	d};	
		westOffset_t = new float[]{	-d,	0,	-d};	
	}

	@Override
	public void init(CubixVis vis) 
	{
		MatrixCube mc = vis.getMatrixCube();

		float zOffset = .2f;
		float yOffset = 1.5f;
		
		float sliceWidth = vis.getSliceWidth(mc.getLastTimeSlice());
		float sliceHeight = vis.getSliceHeight(mc.getLastTimeSlice());
		colAmount = (int) (Math.sqrt(mc.getTimeSlices().size() * 1.1) +1);
//		colAmount = 9;
		
		float[] pos = new float[]{
				0,
				(colAmount-1)/2f * sliceWidth,
				-(colAmount-1)/2f * sliceWidth,  // TODO VI maybe here (or above) as well use sliceHeight instead of sliceWidth
				};
		float[] pos2;
		
		float[] colOffset = new float[]{0, 0, sliceWidth + zOffset};
		//float[] rowOffset = new float[]{0, -sliceWidth - yOffset, 0};
		float[] rowOffset = new float[]{0, -sliceHeight - yOffset, 0};
	
		int colCount = 0;
		Slice s;
		rowCount = 0;
		for(int t=0 ; t < mc.getTimeCount() ; t++)
		{
			s = mc.getTimeSlice(t);
			labelTransparency.put(s, FLOAT4_0.clone());
			
			if(colCount == 0){
				labelTransparency.get(s)[W] = 1;
			}
			if(rowCount == 0){
				labelTransparency.get(s)[N] = 1;
			}
			pos2 = pos.clone();
			pos2[X] = vis.getSlicePos(s)[X];
			
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
			angles.put(s, 0f);
			scales.put(s, 1f);
			labelPosR.put(s, s.getRelGridCoords(s.getRowCount()+.5f, s.getColumnCount()/2));
			labelPosL.put(s, s.getRelGridCoords(s.getRowCount()+.5f, s.getColumnCount()/2));

			labelAlignR.put(s, Align.CENTER);
			labelAlignL.put(s, Align.CENTER);
		}
		
		s = mc.getTimeSlice(0);
		float zMean = positions.get(s)[Z] + sliceWidth * (colAmount-1)/2f;
		float yMean = positions.get(s)[Y] - sliceHeight * (rowCount-1)/2f;

		float d = (float) ((vis.getSliceWidth(s) * colAmount/2 * 1) / ( Math.tan(ANGLE_PERSP * Math.PI / 360)));
		cameraPos = new float[]{-d,yMean, zMean};	
		cameraLookAt = cameraPos.clone();
		cameraLookAt[X] = 1;	
		
		// MATRIX LABELS
//		
		labelTransparency.get(mc.getTimeSlice(0))[N] = 1;
		labelTransparency.get(mc.getLastTimeSlice())[E] = 1;
		labelTransparency.get(mc.getTimeSlice(0))[W] = 1;
	}
	
	public int getRowAmount() { return this.rowCount; }
	public int getColAmount() { return this.colAmount; }


}
