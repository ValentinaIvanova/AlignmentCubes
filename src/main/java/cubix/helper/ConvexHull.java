package cubix.helper;

import java.util.ArrayList;
import javax.media.opengl.GL2;

public class ConvexHull {

	public static boolean isInside(float[] point, ArrayList<float[]> hullPoints, ArrayList<float[]> rayDirections, GL2 gl)
	{
		
		float[] p1, p2;
		float[] d;
		float[] n;
		float[] a;
		float add, sub;
		
		boolean inside = true;
		for(int i=0 ; i < hullPoints.size() ; i++)
		{
			p1 = hullPoints.get(i);
			p2 = hullPoints.get((i+1) % hullPoints.size());
			
			d = Utils.normalize(rayDirections.get(i));
			
			n = Utils.normalize(Utils.cross(d,Utils.dir(p1, p2)));
			a = Utils.normalize(Utils.sub(point, p1));
			add = Utils.length(Utils.add(n, a));
			sub = Utils.length(Utils.sub(n, a));
			
			inside = inside && (add <= sub);

		}			

		return inside;		
	}

}
