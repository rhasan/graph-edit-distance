package semanticweb;

import ged.AlgorithmConfig;

import java.io.ObjectInputStream.GetField;
import java.util.concurrent.TimeUnit;

import javax.naming.OperationNotSupportedException;

import com.google.common.base.Stopwatch;

import algorithms.BipartiteMatching;

import semanticweb.sparql.SparqlUtils;
import util.CostFunction;
import util.EditDistance;
import util.Graph;
import util.MatrixGenerator;

public class RDFGraphMatching {
	
	/**
	 * computes the approximated or exact graph edit distance 
	 */
	private EditDistance editDistance;
	
	/**
	 * computes an optimal bipartite matching of local graph structures
	 */
	private BipartiteMatching bipartiteMatchingHungarian;
	
	/**
	 * computes an optimal bipartite matching of local graph structures
	 */
	private BipartiteMatching bipartiteMatchingVJ;	

	/**
	 * generates the cost matrix whereon the optimal bipartite matching can
	 * be computed
	 */
	private MatrixGenerator matrixGenerator;
	
	/**
	 * the cost function to be applied
	 */
	private CostFunction costFunction;	
	
	/**
	 * whether the edges of the graphs are undirected (=1) or directed (=0)
	 */
	private int undirected;
	
	/**
	 * log options:
	 * output the individual graphs
	 * output the cost matrix for bipartite graph matching
	 * output the matching between the nodes based on the cost matrix (considering the local substructures only)
	 * output the edit path between the graphs
	 */
	private int outputGraphs;
	private int outputCostMatrix;
	private int outputMatching;
	private int outputEditpath;	
	
	public RDFGraphMatching() {
		this.costFunction = getRDFGraphCostFunction();

		// the matrixGenerator generates the cost-matrices according to the costfunction
		this.matrixGenerator = new MatrixGenerator(this.costFunction,
				this.outputCostMatrix);
		
		// bipartite matching procedure (Hungarian)
		this.bipartiteMatchingHungarian = new BipartiteMatching("Hungarian", this.outputMatching);

		// bipartite matching procedure (VolgenantJonker)
		this.bipartiteMatchingVJ = new BipartiteMatching("VJ", this.outputMatching);
		
		// editDistance computes either the approximated edit-distance according to the bipartite matching 
		// or computes the exact edit distance
		this.editDistance = new EditDistance(this.undirected, this.outputEditpath);
		
	}	
	
	/**
	 * Returns the cost funtion for RDF graph edit distance
	 * @return the cost funtion for RDF graph edit distance
	 */
	
	public CostFunction getRDFGraphCostFunction() {
		
		int debug = 0;
		//log options
		if(debug==1) {
			this.outputEditpath = 1;
			this.outputMatching = 1;
			this.outputCostMatrix = 1;
			this.outputGraphs = 1;
		} else {
			this.outputEditpath = 0;
			this.outputMatching = 0;
			this.outputCostMatrix = 0;
			this.outputGraphs = 0;
			
		}
		
		//directed or undirected graph
		this.undirected = 0;
		//cost for node/edge deletions/insertions (both cost values have to be > 0)
		double node = 1.0;
		double edge = 1.0;
		
		//alpha weights the node and edge costs:
		//alpha * nodeCost; (1-alpha) * edgeCost
		double alpha=0.5;
		
		//number of node attributes and the individual names of these attributes
		int numOfNodeAttr = 1;
		String[] nodeAttributes = new String[numOfNodeAttr];
		nodeAttributes[0] = RDF2GXL.NODE_SYMBOL_ATT_NAME;
		
		//cost function type per individual node attribute 			
		//(possible choices are: squared, absolute, discrete, sed)		
		//if cost-function=discrete for node attribute i: nodeCostMui and nodeCostNui
		//(non-negative real values) have to be additionally defined 		
		String[] nodeCostTypes = new String[numOfNodeAttr];
		nodeCostTypes[0] = "equality";
		
		//weighting parameters per individual node attribute 		
		//(min=0, max=1.0; default = 1.0)									
		double[] nodeAttrImportance = new double[numOfNodeAttr];
		nodeAttrImportance[0] = 1.0;
		
		//number of edge attributes and the individual names of these attributes
		int numOfEdgeAttr = 1;
		String[] edgeAttributes = new String[numOfEdgeAttr];
		edgeAttributes[0] = RDF2GXL.EDGE_SYMBOL_ATT_NAME;
		
		//cost function type per individual edge attribute 				
		//(possible choices are: squared, absolute, discrete, sed)			
		//if cost-function=discrete for edge attribute i: nodeCostMui and nodeCostNui	 
		//(non-negative real values) have to be additionally defined 	
		String[] edgeCostTypes = new String[numOfEdgeAttr];
		edgeCostTypes[0] = "equality";
		
		//weighting parameters per individual edge attribute 		
		//(min=0, max=1.0; default = 1.0)									
		double[] edgeAttrImportance = new double[numOfEdgeAttr];
		edgeAttrImportance[0] = 1.0;
		
		// whether or not the costs are "p-rooted"
		double squareRootNodeCosts = 1.0;
		double squareRootEdgeCosts = 1.0;

		// whether costs are multiplied or summed
		int multiplyNodeCosts = 0;
		int multiplyEdgeCosts = 0;

		/**
		 * mu and nu are non-negative real values to be defined by the user if cost-function=discrete
		 * mu = cost for substitution of equal labels
		 * nu = cost for substitution of unequal labels
		 */		
		double[] nodeCostMu = new double[numOfNodeAttr];
		double[] nodeCostNu = new double[numOfNodeAttr];		
		
		return new CostFunction(node, edge, alpha, nodeAttributes,
				nodeCostTypes, nodeAttrImportance, edgeAttributes,
				edgeCostTypes, edgeAttrImportance, squareRootNodeCosts,
				multiplyNodeCosts, squareRootEdgeCosts, multiplyEdgeCosts, nodeCostMu, nodeCostNu);			
	}
	/**
	 * Distance between two GXL graphs using A* 
	 * @param sourceGraph graph 1 
	 * @param targetGraph  graph 2
	 * @return distance
	 */
	
	public double distanceAStar(Graph sourceGraph,Graph targetGraph) {
		return this.editDistance.getEditDistance(
				sourceGraph, targetGraph, this.costFunction, Integer.MAX_VALUE);	
	}
	/**
	 * Distance between two GXL graphs using A*-beam
	 * @param sourceGraph graph 1
	 * @param targetGraph graph 2
	 * @param s size of the beam
	 * @return distance
	 */
	
	public double distanceAStarBeam(Graph sourceGraph,Graph targetGraph,int s) {
		return this.editDistance.getEditDistance(
				sourceGraph, targetGraph, this.costFunction,s);	
	}
	
	/**
	 * Distance between two GXL graphs using Bipartite Hungarian
	 * @param sourceGraph graph 1
	 * @param targetGraph graph 2
	 * @return distance
	 */
	
	public double distanceBipartiteHungarian(Graph sourceGraph,Graph targetGraph) {
		
		if (sourceGraph.size()<targetGraph.size()){
			Graph temp = sourceGraph;
			sourceGraph = targetGraph;
			targetGraph = temp;
		}		
		
		// generate the cost-matrix between the local substructures of the source and target graphs
		double[][] costMatrix = this.matrixGenerator.getMatrix(sourceGraph, targetGraph);
		// compute the matching using Hungarian
		int[][] matching = bipartiteMatchingHungarian.getMatching(costMatrix);
		// calculate the approximated edit-distance according to the bipartite matching 
		double d = this.editDistance.getEditDistance(
				sourceGraph, targetGraph, matching, costFunction);
		return d;
	}
	
	/**
	 * Distance between two GXL graphs using Bipartite VolgenantJonker
	 * @param sourceGraph graph 1
	 * @param targetGraph graph 2
	 * @return distance
	 */	
	
	public double distanceBipartiteVolgenantJonker(Graph sourceGraph,Graph targetGraph) {
		
		if (sourceGraph.size()<targetGraph.size()){
			Graph temp = sourceGraph;
			sourceGraph = targetGraph;
			targetGraph = temp;
		}		
		
		// generate the cost-matrix between the local substructures of the source and target graphs
		double[][] costMatrix = this.matrixGenerator.getMatrix(sourceGraph, targetGraph);
		// compute the matching using VolgenantJonker
		int[][] matching = bipartiteMatchingVJ.getMatching(costMatrix);
		// calculate the approximated edit-distance according to the bipartite matching 
		double d = this.editDistance.getEditDistance(
				sourceGraph, targetGraph, matching, costFunction);
		return d;
	}
	
	/**
	 * 
	 * @param q1 SPARQL query string for query 1
	 * @param q2 SPARQL query string for query 2
	 * @param algorithmConfig algorithm configuration {@link AlgorithmConfig}
	 * @return graph edit distance between query graph of query 1 and query graph of query 2
	 * @throws Exception 
	 */
	public double queryGraphDistance(String q1, String q2, AlgorithmConfig algorithmConfig) throws Exception {
		
		Graph g1 = SparqlUtils.buildSPARQL2GXLGraph(q1, "1");
		Graph g2 = SparqlUtils.buildSPARQL2GXLGraph(q2, "2");

		
		if(algorithmConfig.isAStarBeam())
			return distanceAStarBeam(g1, g2, algorithmConfig.getBeamSize());
		if(algorithmConfig.isBipartiteHungarian())
			return distanceBipartiteHungarian(g1, g2);
		if(algorithmConfig.isBipartiteVolgenantJonker())
			return distanceBipartiteVolgenantJonker(g1, g2);
		
		throw new OperationNotSupportedException("Supported algorithms: A*-beam search, Bipartite Hungarian, and Bipartite VolgenantJonker");
	}
		
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Graph g1 = RDF2GXL.readRDF("data/vc-db-1.rdf");
		Graph g2 = RDF2GXL.readRDF("data/vc-db-2.rdf");
		
		Stopwatch stopwatch = new Stopwatch();
		long millis = 0;
		
		RDFGraphMatching matcher = new RDFGraphMatching();

		
		
		
		System.out.println("Graph 1 size: "+g1.size());
		System.out.println("Graph 2 size: "+g2.size());
		
		
		System.out.println("--------------------------------------");
		stopwatch.reset();
		stopwatch.start();
		double hungarian = matcher.distanceBipartiteHungarian(g1, g2);
		stopwatch.stop();
		millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
		System.out.println("Bipartite Hungarian: "+hungarian);
		System.out.println("Execution time: "+millis+" milliseconds");

		
		System.out.println("--------------------------------------");
		stopwatch.reset();
		stopwatch.start();
		double vj = matcher.distanceBipartiteVolgenantJonker(g1, g2);
		stopwatch.stop();
		millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
		System.out.println("Bipartite VolgenantJonker: "+vj);
		System.out.println("Execution time: "+millis+" milliseconds");

		
		System.out.println("--------------------------------------");
		int s = 10;
		stopwatch.reset();
		stopwatch.start();
		double aStarBeam = matcher.distanceAStarBeam(g1, g2, s);
		stopwatch.stop();
		millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
		
		System.out.println("A*-beam (s="+s+"): "+aStarBeam);
		System.out.println("Execution time: "+millis+" milliseconds");
		
		/*
		System.out.println("--------------------------------------");
		stopwatch.reset();
		stopwatch.start();
		
		double aStar = matcher.distanceAStar(g1, g2);
		stopwatch.stop();
		millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

		System.out.println("A*: "+aStar);
		System.out.println("Execution time: "+millis+" milliseconds");
		*/
		
	
	}

}
