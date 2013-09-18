package semanticweb;

import java.io.ObjectInputStream.GetField;

import util.Graph;
import xml.XMLParser;

import nanoxml.XMLElement;

public class RDF2GXL {
	public static String NODE_SYMBOL_ATT_NAME = "symbol";
	public static String EDGE_SYMBOL_ATT_NAME = "symbol";
	
	private static XMLElement transformResourceURI2GXL(String resURI,String resNodeSymbol) {
		
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
	
	private static XMLElement transformResourceURI2GXL(String resURI) {
		return transformResourceURI2GXL(resURI,resURI);
	}
	
	private static XMLElement transformTriple2GXL(String subjectURI, String predicateURI, String object) {
		
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
	
	private static XMLElement getGXLRootElement() {
		XMLElement gxl = new XMLElement();
		//gxl.setName("gxl");
		gxl.parseString("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\"><gxl></gxl>");
		return gxl;
	}
	private static XMLElement getGXLGraphElement(String graphID) {
		XMLElement graph = new XMLElement();
		graph.setName("graph");
		graph.setAttribute("id", graphID);
		return graph;		
	}
	
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
		
		return xmlParser.parseGXL(gxl);
	}

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
		
		XMLParser xmlParser = new XMLParser();
		
		return xmlParser.parseGXL(gxl);
	}	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		
		Graph g = RDF2GXL.getTestGXLGraph();
		System.out.println("Graph:"+g.toString());
		

	}

}
