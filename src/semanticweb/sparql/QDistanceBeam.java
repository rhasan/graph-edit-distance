package semanticweb.sparql;



import org.apache.jena.atlas.logging.Log;

import ged.AlgorithmConfig;
import semanticweb.RDFGraphMatching;
import utils.Utils;

public class QDistanceBeam {
	
	
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
		System.out.println("\t --beam for beam size");
		//System.out.println("--encoded if the input queries are URL parsed");
	}
	
	public static void file(String file1, String file2, int beam) throws Exception {
		//System.out.println("Processing files "+file1+" and "+file2);
		String q1 = Utils.readFile(file1);
		String q2 = Utils.readFile(file2);
		//System.out.println(q1);
		//System.out.println(q2);
		System.out.println(distance(q1, q2, beam));
		
	}
	
	public static void std(String q1, String q2, int beam) throws Exception {
		//System.out.println("Processing queries "+q1+" and "+q2);
		System.out.println(distance(q1, q2, beam));
		
		
	}
	
	public static double distance(String q1, String q2, int beam) throws Exception{
		AlgorithmConfig algorithmConfig = AlgorithmConfig.createAStarBeam(beam);
		RDFGraphMatching matcher = new RDFGraphMatching();
		return matcher.queryGraphDistance(q1, q2, algorithmConfig);		
	}
	public static void test() throws Exception {
		String q1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt>.  OPTIONAL { ?y foaf:mbox ?email }  }";
		String q2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name . ?a ?b <http://wimmics.inria.fr/kolflow/qp#tt> }";
		System.out.println(distance(q1, q2, 10));
		
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
					if(args.length < 5) {
						error("Please provide the file names containing sparql queries and the beam size");
					}
					String file1 = args[1];
					String file2 = args[2];
					
					if(args[3].equals("--beam")==false) {
						error("Please provide the beam size by --beam followed by an integer value");
					}
					int beam = Integer.parseInt(args[4]);
					
					file(file1, file2, beam);
				}
				else if(args[0].equals("--std")) {
					if(args.length < 5) {
						error("Please provide two sparql queries seperated by space and the beam size. The queries should be in quotes.");
					}
					String q1 = args[1];
					String q2 = args[2];

					if(args[3].equals("--beam")==false) {
						error("Please provide the beam size by --beam followed by an integer value");
					}
					int beam = Integer.parseInt(args[4]);
					
					std(q1, q2, beam);
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
