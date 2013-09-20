package semanticweb.sparql;

import ged.AlgorithmConfig;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.naming.OperationNotSupportedException;

import nanoxml.XMLElement;
import semanticweb.RDF2GXL;
import semanticweb.RDFGraphMatching;
import util.Graph;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.util.FileManager;

public class SparqlUtils {
	
	final public static String SPARQL_VAR_NS = "http://wimmics.inria.fr/kolflow/qp#"; 
	
	/**
	 * Retrieve the set of triples in a sparql query pattern
	 * @param s a sparql query
	 * @return Set<Triple>
	 */
	public static Set<Triple> retrieveTriples(String s) {
		
		final Set<Triple> allTriples = new HashSet<Triple>();
	
        // Parse
        Query query = QueryFactory.create(s) ;
        Element e = query.getQueryPattern();
        
        // This will walk through all parts of the query
        ElementWalker.walk(e,
            // For each element...
            new ElementVisitorBase() {
                // ...when it's a block of triples...
                public void visit(ElementPathBlock el) {
                    // ...go through all the triples...
                    Iterator<TriplePath> triples = el.patternElts();
                    while (triples.hasNext()) {
                        // ...and grab the subject
                        //subjects.add(triples.next().getSubject());
                    	TriplePath t = triples.next();
                    	//System.out.println(t.toString());
                    	allTriples.add(t.asTriple());
                    	
                    	
                    }
                }
                public void visit(ElementTriplesBlock el) {
                    // ...go through all the triples...
                    Iterator<Triple> triples = el.patternElts();
                    while (triples.hasNext()) {
                        // ...and grab the subject
                        //subjects.add(triples.next().getSubject());
                    	Triple t = triples.next();
                    	//System.out.println(t.toString());
                    	allTriples.add(t);
                    	
                    	
                    }
                    
                	
                }
            }
        );
        return allTriples;
		
	}
	
	
	/**
	 * Replaces the ? with a URI to hel create an RDF graph with the sparql variables 
	 * @param symbol name of the variable
	 * @return refined String URI for the sparql variable 
	 */
	private static String refineSymbol(String symbol) {
		if(symbol.contains("?")) {
			symbol = symbol.replaceAll("\\?", SPARQL_VAR_NS);
		}
		return symbol;
	}
	/**
	 * Replaces the ? with a URI to hel create an RDF graph with the sparql variables 
	 * @param node node for the sparql variable
	 * @return refined String URI for the sparql variable
	 */
	private static String refineSymbol(Node node) {
		return refineSymbol(node.toString());
		
	}
	
	/**
	 * Builds an RDF graph from the sparql query pattern
	 * @param s sparql query string
	 * @return an RDF graph from the sparql query pattern
	 */
	
	public static Model buildQueryRDFGraph(String s) {
		 // create an empty model
		 Model model = ModelFactory.createDefaultModel();
		 
		 Set<Triple> triples = retrieveTriples(s);
		 
		 for(Triple t:triples) {
			 Node sub = t.getSubject();

			 
			 Resource rSub = null;
			 
			 if(sub.isVariable()) {
				
				String refineSubURI = refineSymbol(sub);
				
				rSub = model.createResource(refineSubURI);
			
			 } else {
				 rSub = model.asRDFNode(sub).asResource(); 
			 }
			 
			 Node pred = t.getPredicate();
			 
			 Property rPred = null;
			 
			 if(pred.isVariable()) {
				 
				 
				 String refinePredUri = refineSymbol(pred);
				 
				 rPred = model.createProperty(refinePredUri);
			 } else {
				 rPred = model.createProperty(pred.toString());
				 
			 }
			 
			 
			 Node obj = t.getObject();			 
			 RDFNode rObj = null;
			 
			 if(obj.isVariable()) {
				 
				 
				 String refineObjUri = refineSymbol(obj);
				 
				 rObj = model.createResource(refineObjUri);
			 } else {
				 rObj = model.asRDFNode(obj);
			 }
			 
			 //System.out.println(rSub.getClass());
			 //System.out.println(rPred.getClass());
			 //System.out.println(rObj.getClass());
			 
			 Statement st = model.createStatement(rSub, rPred, rObj);
			 //System.out.println(st);
			 model.add(st);
			 
		 }
		 return model;
		
	}
	
	/**
	 * Returns true if the Resource represented by the URI was a variable in the original sparql query 
	 * @param uri a RDF resource URI
	 * @return true or false
	 */
	private static boolean wasVariable(String uri) {
		if(uri.contains(SPARQL_VAR_NS)) return true;
		return false;
	}
	
	/**
	 * Builds a GXL graph suitable for the GMT library from a sparql query
	 * @param qr a sparql query
	 * @param graphId an id for the query, sometimes useful for indexing
	 * @return a representation of the GXL graph
	 * @throws Exception
	 */
	
	public static Graph buildSPARQL2GXLGraph(String qr, String graphId) throws Exception{
		
		Model model = buildQueryRDFGraph(qr);
		
		XMLElement gxl = RDF2GXL.getGXLRootElement(); 
		
		XMLElement graph = RDF2GXL.getGXLGraphElement(graphId);

		gxl.addChild(graph);
		


		// write it to standard out
		//model.write(System.out);
		
		
		ResIterator subIterator = model.listSubjects();
		while(subIterator.hasNext()) {
			Resource sub = subIterator.nextResource();
			XMLElement gxlSub = null;
			if(wasVariable(sub.toString())) {
				
				gxlSub = RDF2GXL.transformResourceURI2GXL(sub.toString(),"?");
				
			} else {
				gxlSub = RDF2GXL.transformResourceURI2GXL(sub.toString());
			}
			
			graph.addChild(gxlSub);
		}
		
		NodeIterator objIterator = model.listObjects();
		while(objIterator.hasNext()){
			RDFNode obj = objIterator.nextNode();
			XMLElement gxlObj = null;
			if(wasVariable(obj.toString())) {
				gxlObj = RDF2GXL.transformResourceURI2GXL(obj.toString(),"?");
			} else {
				//check in RDF spec whether literals with same values are considered as same RDF graph nodes.
				gxlObj = RDF2GXL.transformResourceURI2GXL(obj.toString());
			}
			graph.addChild(gxlObj);
		}
		
		
		StmtIterator stmtIterator = model.listStatements();
		
		while(stmtIterator.hasNext()){
			Statement s = stmtIterator.nextStatement();
			//System.out.println(s);
			String fromURI = s.getSubject().toString();
			String predicateURI = wasVariable(s.getPredicate().toString())?"?":s.getPredicate().toString();
			String toResource = s.getObject().toString();
			
			XMLElement edge = RDF2GXL.transformTriple2GXL(fromURI, predicateURI, toResource);
			graph.addChild(edge);
			
			
		}
		
		return RDF2GXL.parseGXL(gxl);
		      		
		
	}

	
	public static void main(String[] args) throws Exception {
		String q1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt>.  OPTIONAL { ?y foaf:mbox ?email }  }";
		String q2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt> }";
		
		Graph g1 = buildSPARQL2GXLGraph(q1, "1");
		Graph g2 = buildSPARQL2GXLGraph(q2, "2");
		//System.out.println(g2);
		
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
