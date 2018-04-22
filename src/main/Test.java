package main;

import java.util.Map;

import model.Review;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		RDF rdf = new RDF("./rdf/iphone5.rdf");
		
		Ontology ontology = new Ontology("./rdf/mobile_phone.owl");
		
		Map<Integer, Review> reviews = rdf.getComments();
		
		for(Map.Entry<Integer, Review> entry:reviews.entrySet()){    
			Review review = entry.getValue();
			ontology.createIndividual(review);
		}
		
		//ontology.save("./rdf/new.rdf");
		//ontology.getReviews("iPhone5C", "Battery");
		double dd = ontology.getRating("iPhone5C", "Battery");
		System.out.println(dd);
		//

	}

}
