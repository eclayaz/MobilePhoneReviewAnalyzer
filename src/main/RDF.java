package main;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import model.Review;

public class RDF {
	Model model;
	
	public RDF(String rdfPath) {
		model = ModelFactory.createOntologyModel();
		model.read(rdfPath, "RDF/XML");
	}
	
	public Map<Integer, Review>	getComments() {
		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"PREFIX c: <http://review-analyzer.local/ontologies/mobile_phone.owl#>\r\n" + 
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"select *\r\n" + 
				"where {\r\n" + 
				"  ?comment c:review ?review .  \r\n" + 
				"  ?comment c:rating ?rating .\r\n" + 
				"  ?comment c:phone ?phone . \r\n" + 
				"  ?comment c:isAbout ?isAbout . \r\n" + 
				"}";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		Map<Integer, Review> mapReviews = new HashMap<Integer, Review>();
		int i = 1;
		try {
			ResultSet results = qexec.execSelect();
			
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				mapReviews.put(i++,
						new Review(soln.getLiteral("review").toString(), soln.getLiteral("rating").toString(),
								soln.getLiteral("phone").toString(), soln.getLiteral("isAbout").toString()));
			}
		} finally {
			qexec.close();
		}
		
		return mapReviews;
	}
}
