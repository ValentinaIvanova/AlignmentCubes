package cubix.ordering;

import java.util.ArrayList;

import cubix.helper.Log;

import cern.colt.matrix.DoubleMatrix2D;

public abstract class MatrixOrdering{
	
	public abstract int[] order(DoubleMatrix2D mat);
		
	
	public static double getEuclideanDistance(ArrayList<Float> a, ArrayList<Float> b){
		float v = 0; 
		if(a.size() != b.size()){
			Log.err("[MatrixOrdering] Unequal array lenght");
		} 
			
		for(int i=0 ; i<a.size(); i++){
			v += Math.pow(a.get(i)-b.get(i),2);
		}
		return Math.sqrt(v);
	}

}
