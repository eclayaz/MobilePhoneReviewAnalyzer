package main;

import java.util.ArrayList;
import java.util.Map;

import model.Review;
import similarity.Corpus;
import similarity.Document;
import similarity.VectorSpaceModel;

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
		Map<Integer, Review> filtered_reviews = ontology.getReviews("iPhone5C", "Display");
		//double dd = ontology.getRating("iPhone5C", "Display");
		
		
		//semantic similarity
		int x = 1;
		ArrayList<Document> documents = new ArrayList<Document>();
		for(Map.Entry<Integer, Review> entry:filtered_reviews.entrySet()){    
			Review review = entry.getValue();			
			documents.add(new Document("ds_"+x, review.getComment()));
		}
		
		Document query = new Document("ds_2012", "retina display");
		documents.add(query);
		
		Corpus corpus = new Corpus(documents);		
		VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
		
		for(int i = 1; i < documents.size(); i++) {
			Document doc = documents.get(i);
			System.out.println("\nComparing to " + doc);
			System.out.println(vectorSpace.cosineSimilarity(query, doc));
		}
		

	}

}
