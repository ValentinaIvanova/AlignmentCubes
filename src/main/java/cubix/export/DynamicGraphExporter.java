package cubix.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

//import Jama.Matrix;

//import com.google.gson.Gson;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import cubix.Cubix;
import cubix.CubixVis;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.MatrixCube;
import cubix.data.MatrixUtils;
import cubix.data.TimeGraph;
import cubix.helper.Log;
import cubix.helper.Map;
import edu.uci.ics.jung.graph.Graph;
//import fr.aviz.versionmatrix.server.datastructure.VersionGraph;
//import fr.aviz.versionmatrix.server.datastructure.VersionGraph.Edge;
//import fr.aviz.versionmatrix.server.datastructure.VersionGraph.Node;
//import fr.aviz.versionmatrix.server.export.Exporter;


public class DynamicGraphExporter //extends Exporter 
{

/*	private ArrayList<VersionGraph.Node> nodes;
	private ArrayList<VersionGraph.Edge> edges;


	public void export(String name, TimeGraph<CNode, CEdge, CTime> timeGraph)
	{
		this.directory = "similarity";
		this.outputFileName = name;
	
		int n = timeGraph.getTimes().size();
		
		nodes = new ArrayList<VersionGraph.Node>();
		edges = new ArrayList<VersionGraph.Edge>();

//		DoubleMatrix2D distMat = MatrixUtils.getTimeDistanceMatrix(timeGraph, timeGraph.getTimes(), timeGraph.getEdges());
		DoubleMatrix2D distMat = getAffineInvariantDistanceMatrix(timeGraph, timeGraph.getTimes(), timeGraph.getEdges());

		int o=0;
		// Add each time step as point/node to version grsph
		for (CTime ct : timeGraph.getTimes())
		{
			nodes.add(new VersionGraph.Node(ct.getLabel() + "", 
											ct.getID(), 
											0,
											ct.getLabel(),
											o++,
											0.0,
											o,
											0.0,
											o));
		}

		
		
		double forces[][] = new double[n][n];

		// Calc max distance for normalization
				double maxDist = 0;
				for(int i=0; i<n ; i++){
					for(int j=i; j<n ; j++){
						maxDist = Math.max(maxDist, distMat.get(i, j));
					}
				}			
				for(int i=0; i<n ; i++){
					for(int j=i; j<n ; j++){
						forces[i][j] = Map.map(distMat.get(i, j), 0, maxDist, 0,1);
						forces[j][i] = forces[i][j];
//						Log.out(this, i+ "," + j + " = " + forces[i][j]);
						edges.add(new VersionGraph.Edge(String.valueOf(i), String.valueOf(j), forces[i][j], 1.0));
						edges.add(new VersionGraph.Edge(String.valueOf(j), String.valueOf(i), forces[i][j], 2.0));
					}			
				}		
		graph = new VersionGraph(nodes,edges);
		
		this.exportToJson();
	
	}
	
	private DoubleMatrix2D getAffineInvariantDistanceMatrix(
			TimeGraph<CNode, CEdge, CTime> timeGraph, ArrayList<CTime> times,
			Collection<CEdge> edges2) {

		int t = timeGraph.getTimes().size();
		DoubleMatrix2D distMat = new DenseDoubleMatrix2D(t, t);
		
		
		// create array matrices and normalize
		int n = timeGraph.getVertices().size();
		ArrayList<CNode> nodes = new ArrayList<CNode>();
		nodes.addAll(timeGraph.getVertices());
		double[][][] matrices = new double[t][n][n]; 
		double[][] m;
		int tc = 0, sou, tar;
		for(Graph<CNode, CEdge> g : timeGraph.getGraphs()){
			m = new double[n][n];
			for(CEdge e : g.getEdges()){
				sou = nodes.indexOf(g.getEndpoints(e).getFirst());
				tar = nodes.indexOf(g.getEndpoints(e).getSecond());
				m[sou][tar] = Map.map(e.getWeight(), CubixVis.WEIGHT_MIN, CubixVis.WEIGHT_MAX, 0, 1);
			}
			matrices[tc++] = m;
			for(int d=0 ; d<n ; d++){
				m[d][d] = 1;
			}
		}
		
		
		double s;
		for(int t1=0 ; t1<t ; t1++){
			for(int t2=t1 ; t2<t ; t2++){
				Matrix a = new Matrix(matrices[t1]);
				Matrix b = new Matrix(matrices[t2]);
				Matrix aI = a.transpose();
				Matrix c = aI.times(b);
				Matrix bI = b.transpose();
				Matrix d = bI.times(a);
				Matrix e = c.plus(d);
				s = .5 * Math.sqrt(e.trace() - 2*n);
				distMat.set(t1, t2, s);
				distMat.set(t2, t1, s);
			}
		}
		
		return distMat;
	}
	
	*/
}
