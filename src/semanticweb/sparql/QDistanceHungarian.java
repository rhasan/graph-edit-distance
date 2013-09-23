package semanticweb.sparql;



import org.apache.jena.atlas.logging.Log;

import ged.AlgorithmConfig;
import semanticweb.RDFGraphMatching;
import utils.Utils;

public class QDistanceHungarian {
	

	
	public static void error() {
		System.out.println("--help for help");
		System.exit(1);
	}
	public static void error(String str) {
		System.out.println(str);
		System.exit(1);
	}	
	public static void help(){
		System.out.println("--file for getting the input query from files followed by two file names seperated by space containing sparql queries");
		System.out.println("--std for getting the input query from standerd input followed by two sparql queries in quotes");
		//System.out.println("--encoded if the input queries are URL parsed");
	}
	
	public static void file(String file1, String file2) throws Exception {
		//System.out.println("Processing files "+file1+" and "+file2);
		String q1 = Utils.readFile(file1);
		String q2 = Utils.readFile(file2);
		//System.out.println(q1);
		//System.out.println(q2);
		System.out.println(distance(q1, q2));
		
	}
	
	public static void std(String q1, String q2) throws Exception {
		//System.out.println("Processing queries "+q1+" and "+q2);
		System.out.println(distance(q1, q2));
		
		
	}
	
	public static double distance(String q1, String q2) throws Exception{
		AlgorithmConfig algorithmConfig = AlgorithmConfig.createBipartiteHungarian();
		RDFGraphMatching matcher = new RDFGraphMatching();
		return matcher.queryGraphDistance(q1, q2, algorithmConfig);		
	}
	public static void test() throws Exception {
		String q1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt>.  OPTIONAL { ?y foaf:mbox ?email }  }";
		String q2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt> }";
		System.out.println(distance(q1, q2));
		
	}

	public static void main(String[] args) {
		try {
			//LogManager.getRootLogger().setLevel(Level.OFF);
			Log.setCmdLogging() ;
			
			int n = args.length;
			
			//test();
			if(n > 0) {
				
				if(args[0].equals("--help")) {
					help();
				}else if(args[0].equals("--file")) {
					if(args.length < 3) {
						error("Please provide the file names containing sparql queries");
					}
					String file1 = args[1];
					String file2 = args[2];
					
					file(file1, file2);
				}
				else if(args[0].equals("--std")) {
					if(args.length < 3) {
						error("Please provide two sparql queries seperated by space. The queries should be in quotes.");
					}
					String q1 = args[1];
					String q2 = args[2];
					
						std(q1, q2);
				}			
				
				
			} else {
				error();
				
			}
			
			System.exit(0);

		} catch (Exception e) {
			System.exit(1);
	
		}
	
	}
}
