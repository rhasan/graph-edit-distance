/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package semanticweb.sparql;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

/** Simple example to show parsing a query and producing the
 *  SPARQL agebra expression for the query. */
public class AlgebraEx
{
    public static void main(String []args)
    {
        //String s = "SELECT * { ?s <http://purl.org/dc/elements/1.1/title> ?o1." +
        //		"?s <http://purl.org/dc/elements/1.1/description> ?o2. }";
    	
    	String s = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?name ?email WHERE {  ?x foaf:knows ?y . ?y foaf:name ?name .  OPTIONAL { ?y foaf:mbox ?email }  }";
        
        // Parse
        Query query = QueryFactory.create(s) ;
        System.out.println(query) ;
        
        // Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println("AL: "+op) ;
        
        Element e = query.getQueryPattern();
        
        System.out.println("pattern:"+e);
        
        System.out.println("walk");
        
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
                    	System.out.println(t.toString());
                    	
                    }
                }
                public void visit(ElementTriplesBlock el) {
                    // ...go through all the triples...
                    Iterator<Triple> triples = el.patternElts();
                    while (triples.hasNext()) {
                        // ...and grab the subject
                        //subjects.add(triples.next().getSubject());
                    	Triple t = triples.next();
                    	System.out.println(t.toString());
                    	
                    }
                    
                	
                }
            }
        );       
        
        /*
        
        // Execute it.
        QueryIterator qIter = Algebra.exec(op, Ex1.createModel()) ;
        
        // Results
        for ( ; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            System.out.println(b) ;
        }
        qIter.close() ;*/
    }
}
