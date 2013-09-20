package semanticweb;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import util.Edge;
import util.Graph;
import util.Node;
import xml.XMLParser;

import nanoxml.XMLElement;

public class RDF2GXL {
	public static String NODE_SYMBOL_ATT_NAME = "symbol";
	public static String EDGE_SYMBOL_ATT_NAME = "symbol";
	
	/**
	 * Transforms a resource URI of an RDF graph into an {@link XMLElement} for GXL graph
	 * @param resURI resource URI
	 * @param resNodeSymbol symbol/label that should be in the GXL graph
	 * @return {@link XMLElement}
	 */
	public static XMLElement transformResourceURI2GXL(String resURI,String resNodeSymbol) {
		
		XMLElement node1 = new XMLElement();
		node1.setName("node");
		node1.setAttribute("id", resURI);
		
		XMLElement attSymbol1 = new XMLElement();
		attSymbol1.setName("attr");
		attSymbol1.setAttribute("name", NODE_SYMBOL_ATT_NAME);
		XMLElement symbolStringValue1 = new XMLElement();
		symbolStringValue1.setName("string");
		symbolStringValue1.setContent(resNodeSymbol);
		attSymbol1.addChild(symbolStringValue1);
		node1.addChild(attSymbol1);
		
		return node1;
	}
	/**
	 * Transforms a resource URI of an RDF graph into an {@link XMLElement} for GXL graph. Symbol/label in the GXL graph will be same as the URI.
	 * @param resURI resURI resource URI
	 * @return {@link XMLElement}
	 */
	
	public static XMLElement transformResourceURI2GXL(String resURI) {
		return transformResourceURI2GXL(resURI,resURI);
	}
	/**
	 * Transforms an RDF triple represented by its subject, predicate, and object URIs into an {@link XMLElement} for GXL graphs
	 * @param subjectURI URI of the subject
	 * @param predicateURI URI of the predicate
	 * @param object URI of the object
	 * @return {@link XMLElement} representing the triple
	 */
	public static XMLElement transformTriple2GXL(String subjectURI, String predicateURI, String object) {
		
		XMLElement edge1 = new XMLElement();
		//String edgeId = uriInria+" "+uriLocatedIn+" "+uriFrance;
		edge1.setName("edge");
		edge1.setAttribute("from",subjectURI);
		edge1.setAttribute("to",object);
		
		XMLElement attSymbol1 = new XMLElement();
		attSymbol1.setName("attr");
		attSymbol1.setAttribute("name", EDGE_SYMBOL_ATT_NAME);
		XMLElement symbolStringValue1 = new XMLElement();
		symbolStringValue1.setName("string");
		symbolStringValue1.setContent(predicateURI);
		attSymbol1.addChild(symbolStringValue1);
		edge1.addChild(attSymbol1);		
		return edge1;
	}
	
	/**
	 * Returns the root element for a GXL graph. A meta element.
	 * @return {@link XMLElement}
	 */
	public static XMLElement getGXLRootElement() {
		XMLElement gxl = new XMLElement();
		//gxl.setName("gxl");
		gxl.parseString("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\"><gxl></gxl>");
		return gxl;
	}
	/**
	 * Returns the graph element in a GXL graph.
	 * @param graphID graph id
	 * @return {@link XMLElement}
	 */
	public static XMLElement getGXLGraphElement(String graphID) {
		XMLElement graph = new XMLElement();
		graph.setName("graph");
		graph.setAttribute("id", graphID);
		return graph;		
	}
	
	/**
	 * Returns a test {@link Graph}
	 * @return {@link Graph}
	 * @throws Exception
	 */
	
	public static Graph getTestGXLGraph() throws Exception {
		//XMLElement xml = new XMLElement();
		
		XMLElement gxl = getGXLRootElement(); 
		
		XMLElement graph = getGXLGraphElement("1");

		gxl.addChild(graph);
		
		//inria resource
		String uriInria = "inria";
		XMLElement node1 = transformResourceURI2GXL(uriInria);
	
		graph.addChild(node1);

		//france resource
		String uriFrance = "france";
		XMLElement node2 = transformResourceURI2GXL(uriFrance);
		graph.addChild(node2);
		
		
		//edge inria locatedIn country
		String uriLocatedIn = "locatedIn";
		
		XMLElement edge1 = transformTriple2GXL(uriInria, uriLocatedIn, uriFrance);

		
		graph.addChild(edge1);
		
		XMLParser xmlParser = new XMLParser();
		
		return parseGXL(gxl);
	}
	/**
	 * Returns a test {@link Graph}
	 * @return {@link Graph}
	 * @throws Exception
	 */
	public static Graph getTestGXLGraph1() throws Exception {
		//XMLElement xml = new XMLElement();
		
		XMLElement gxl = getGXLRootElement(); 
		
		XMLElement graph = getGXLGraphElement("1");

		gxl.addChild(graph);
		
		//inria resource
		String uriInria = "inria";
		XMLElement node1 = transformResourceURI2GXL(uriInria);
	
		graph.addChild(node1);

		//reearch resource
		String uriResearch = "research";
		XMLElement node2 = transformResourceURI2GXL(uriResearch);
		graph.addChild(node2);
		
		
		//edge inria focus research
		String uriFocus = "focus";
		
		XMLElement edge1 = transformTriple2GXL(uriInria, uriFocus, uriResearch);

		
		graph.addChild(edge1);
		
		
		return parseGXL(gxl);
	}	
	
	/**
	 * Parses a GXL graph and puts it into a {@link Graph} object. Method copied from XMLParser in GraphMatchingToolkit.
	 * @param xml xml root (gxl) element
	 * @return a graph
	 * @throws Exception
	 */
	
	public static Graph parseGXL(XMLElement xml) throws Exception {

		Graph graph1 = new Graph();
		Vector children = xml.getChildren();
		XMLElement root = (XMLElement) children.get(0);
		String id = (String) root.getAttribute("id", null);
		String edgemode = (String) root.getAttribute("edgemode", "undirected");
		graph1.setGraphID(id);
		if (edgemode.equals("undirected")){
			graph1.setDirected(false);
		} else {
			graph1.setDirected(true);
		}
		Enumeration enumerator = root.enumerateChildren();
		int n = 0;
		while (enumerator.hasMoreElements()) {
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("node")) {
				String nodeId = (String) (child.getAttribute("id", null));
				Node node = new Node();
				node.setNodeID(nodeId);
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name", null);
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						node.put(key, value);
					}

				}
				graph1.add(node);
				n++;
			}
		}
		Edge[][] edges = new Edge[n][n]; 
		graph1.setAdjacenyMatrix(edges);
		enumerator = root.enumerateChildren();
		while (enumerator.hasMoreElements()) {	
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("edge")) {
				Edge edge = new Edge();
				String from = (String) child.getAttribute("from", null);
				String to = (String) child.getAttribute("to", null);
				edge.put("from", from);
				edge.put("to", to);
				edge.setEdgeID(from + "_<>" + to);
				// *******************************
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name",
								"key failed!");
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						edge.put(key, value);
					}
				}
							
				for (int i = 0; i < graph1.size(); i++){
					Node nodeI = graph1.get(i); 
					if (nodeI.getNodeID().equals(from)) {
						edge.setStartNode(nodeI);
						nodeI.getEdges().add(edge);
						for (int j = 0; j < graph1.size(); j++){
							Node nodeJ = graph1.get(j); 
							if (nodeJ.getNodeID().equals(to)) {
								edge.setEndNode(nodeJ);
								nodeJ.getEdges().add(edge);
								edges[i][j] = edge;
								if (!graph1.isDirected()){
									edges[j][i] = edge;
								}
							}
						}
					}
				}
			}
		}
		
		return graph1;
		
	}
	
	/**
	 * Reads an RDF graph from a file and puts it into a {@link Graph}
	 * @param filename file name for the RDF graph
	 * @return {@link Graph} that represents the RDF graph
	 * @throws Exception
	 */
	
	public static Graph readRDF(String filename) throws Exception{
		
		return readRDF(filename,filename);
	}
	/**
	 * Reads an RDF graph from a file and puts it into a {@link Graph}
	 * @param Filename file name for the RDF graph
	 * @param graphId Graph id. Sometimes useful for indexing
	 * @return {@link Graph} that represents the RDF graph
	 * @throws Exception
	 */
	
	public static Graph readRDF(String filename, String graphId) throws Exception{
		
		XMLElement gxl = getGXLRootElement(); 
		
		XMLElement graph = getGXLGraphElement(graphId);

		gxl.addChild(graph);
		

		 // create an empty model
		 Model model = ModelFactory.createDefaultModel();

		 // use the FileManager to find the input file
		InputStream in = FileManager.get().open( filename );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + filename + " not found");
		}

		// read the RDF/XML file
		model.read(in, null);

		// write it to standard out
		//model.write(System.out);
		
		
		ResIterator subIterator = model.listSubjects();
		while(subIterator.hasNext()) {
			Resource sub = subIterator.nextResource();
			XMLElement gxlSub = null;
			if(sub.asNode().isBlank()) {
				
				gxlSub = transformResourceURI2GXL(sub.toString(),"_");
				
			} else {
				gxlSub = transformResourceURI2GXL(sub.toString());
			}
			
			graph.addChild(gxlSub);
		}
		
		NodeIterator objIterator = model.listObjects();
		while(objIterator.hasNext()){
			RDFNode obj = objIterator.nextNode();
			XMLElement gxlObj = null;
			if(obj.asNode().isBlank()) {
				gxlObj = transformResourceURI2GXL(obj.toString(),"_");
			} else {
				//check in RDF spec whether literals with same values are considered as same RDF graph nodes.
				gxlObj = transformResourceURI2GXL(obj.toString());
			}
			graph.addChild(gxlObj);
		}
		
		
		StmtIterator stmtIterator = model.listStatements();
		
		while(stmtIterator.hasNext()){
			Statement s = stmtIterator.nextStatement();
			//System.out.println(s);
			String fromURI = s.getSubject().toString();
			String predicateURI = s.getPredicate().toString();
			String toResource = s.getObject().toString();
			
			XMLElement edge = transformTriple2GXL(fromURI, predicateURI, toResource);
			graph.addChild(edge);
			
			
		}
		
		return parseGXL(gxl);
		      		
		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		
		//Graph g = RDF2GXL.getTestGXLGraph();
		//System.out.println("Graph:"+g.toString());
		
		Graph g = readRDF("data/vc-db-1.rdf");
		System.out.println("Graph:"+g.toString());
	}

}
