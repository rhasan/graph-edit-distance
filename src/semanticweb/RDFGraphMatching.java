package semanticweb;

import java.io.ObjectInputStream.GetField;

import algorithms.BipartiteMatching;

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
	
	public double distanceAStar(Graph sourceGraph,Graph targetGraph) {
		return this.editDistance.getEditDistance(
				sourceGraph, targetGraph, this.costFunction, Integer.MAX_VALUE);	
	}
	
	public double distanceAStarBeam(Graph sourceGraph,Graph targetGraph,int s) {
		return this.editDistance.getEditDistance(
				sourceGraph, targetGraph, this.costFunction,s);	
	}
	
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
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		RDFGraphMatching matcher = new RDFGraphMatching();
		Graph g1 = RDF2GXL.getTestGXLGraph();
		Graph g2 = RDF2GXL.getTestGXLGraph1();
		double aStar = matcher.distanceAStar(g1, g2);
		System.out.println("A*: "+aStar);
		
		int s = 2;
		double aStarBeam = matcher.distanceAStarBeam(g1, g2, s);
		System.out.println("A*-beam (s="+s+"): "+aStarBeam);
		
		double hungarian = matcher.distanceBipartiteHungarian(g1, g2);
		System.out.println("Bipartite Hungarian: "+hungarian);

		
		double vj = matcher.distanceBipartiteVolgenantJonker(g1, g2);
		System.out.println("Bipartite VolgenantJonker: "+vj);		
	}

}
