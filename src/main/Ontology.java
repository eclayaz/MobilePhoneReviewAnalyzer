package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;

import model.Review;

public class Ontology {
	
	OntModel ontology;
	String NS = "http://review-analyzer.local/ontologies/mobile_phone.owl#";
	Random rnd = new Random();
	
	public	Ontology(String rdfPath) {
		ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontology.read(rdfPath, "RDFXML");
	}
	
	public void	save(String ontologyName) {
		try {
			FileWriter out = new FileWriter(ontologyName);
			ontology.getWriter("RDF/XML-ABBREV").write(ontology, out, NS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void	createIndividual(Review review) {
		
		//ge the Review class from ontology
		OntClass reviewClasss = getOntologyClass("Review");
		
		//generate random number of 6 digits		
		int n = 100000 + rnd.nextInt(900000);
		
		//create individual
		Individual reviewIndividual = ontology.createIndividual(NS + "review" + n, reviewClasss);

		//set data properties of individual
		DatatypeProperty rating = ontology.createDatatypeProperty(NS + "rating");
		DatatypeProperty comment = ontology.createDatatypeProperty(NS + "comment");
		
		//create object properties
		OntProperty isAbout = ontology.createObjectProperty(NS + "isAbout");
		OntProperty reviewOf = ontology.createObjectProperty(NS + "reviewOf");

		//bind the created data properties and object property to the individual
		reviewIndividual.addProperty(rating, ontology.createTypedLiteral(review.getRating())).addProperty(comment,
				ontology.createLiteral(review.getComment()));

		//find the feature individual from ontology
		Individual featureIndividual = ontology.getIndividual(NS + review.getIsAbout());
		Individual mobileIndividual = ontology.getIndividual(NS + review.getPhone());
		
		//add isAbout, reviewOf relationships to ontology
		ontology.add(reviewIndividual, isAbout, featureIndividual);
		ontology.add(reviewIndividual, reviewOf, mobileIndividual);
	}
	
	private OntClass getOntologyClass(String calssName) {
		return ontology.getOntClass(NS + calssName);
	}
	
	public Map<Integer, Review> getReviews(String phone, String feature) {
		String queryString = 
				"prefix :     <http://review-analyzer.local/ontologies/mobile_phone.owl#>\r\n" + 
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"\r\n" + 
				"select *\r\n" + 
				"where {\r\n" + 
				"  ?review :comment ?comment .\r\n" + 
				"  ?review :rating ?rating .\r\n" + 
				"  ?review :isAbout ?isAbout .\r\n" + 
				"  ?review :reviewOf ?reviewOf .\r\n" + 
				"  ?reviewOf rdf:type/rdfs:subClassOf* :"+phone+".\r\n" + 
				"  ?isAbout rdf:type/rdfs:subClassOf* :"+feature+" \r\n" + 
				"}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontology);
		Map<Integer, Review> mapReviews = new HashMap<Integer, Review>();
		int i = 1;
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				mapReviews.put(i++,
						new Review(soln.getLiteral("comment").toString(), soln.getLiteral("rating").toString(), "", ""));
				
			}
		} finally {
			qexec.close();
		}
		
		return mapReviews;
	}
	
	public double getRating(String phone, String feature) {
		String queryString = 
				"prefix :     <http://review-analyzer.local/ontologies/mobile_phone.owl#>\r\n" + 
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"\r\n" + 
				"select (SUM(xsd:integer(?rating))/COUNT(xsd:integer(?rating)) AS ?avg)\r\n" + 
				"where {\r\n" + 
				"  ?review :comment ?comment .\r\n" + 
				"  ?review :rating ?rating .\r\n" + 
				"  ?review :isAbout ?isAbout .\r\n" + 
				"  ?review :reviewOf ?reviewOf .\r\n" + 
				"  ?reviewOf rdf:type/rdfs:subClassOf* :"+phone+".\r\n" + 
				"  ?isAbout rdf:type/rdfs:subClassOf* :"+feature+" \r\n" + 
				"}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontology);
		
		try {
			ResultSet results = qexec.execSelect();
			
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				
				return soln.getLiteral("avg").getDouble();
			}
		} finally {
			qexec.close();
		}
		
		return 0;
	}
}
