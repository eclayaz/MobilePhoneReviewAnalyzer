package similarity;
//package ir;

import java.util.ArrayList;

/**
 * the tester class.
 * @author swapneel
 */
public class VectorSpaceModelTester {

	public static void main(String[] args) {
		
		Document query = new Document("ds_2012", "The game of life is a game of everlasting learning");
		Document d1 = new Document("ds_2013", "4 months after I got the phone the screen stopped working. No touch screen. It was obvious when I recieved it that the screen had been replaced, I'm guessing it was cheaply done. Do not recommend.");
		Document d2 = new Document("ds_2014", "The unexamined life is not worth living");
		Document d3 = new Document("ds_2015", "Never stop learning");
		Document d4 = new Document("ds_2016", "It worked great for about a month and now the screen will go black for no reason and it won't lock. To make the screen not black anymore I have to hit the middle button even if I'm on the home page.");
		Document d5 = new Document("ds_2012", "Data Science defines a discipline that incorporates applying varying degrees of statistical analysis");
	
		ArrayList<Document> documents = new ArrayList<Document>();
		documents.add(query);
		documents.add(d1);
		documents.add(d2);
		documents.add(d3);
		documents.add(d4);
		documents.add(d5);
		
		Corpus corpus = new Corpus(documents);
		
		VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
		
		for (int i = 0; i < documents.size(); i++) {
			for (int j = i + 1; j < documents.size(); j++) {
				Document doc1 = documents.get(i);
				Document doc2 = documents.get(j);
				System.out.println("\nComparing " + doc1 + " and " +  doc2);
				System.out.println(vectorSpace.cosineSimilarity(doc1, doc2));
			}
		}
		

		for(int i = 1; i < documents.size(); i++) {
			Document doc = documents.get(i);
			System.out.println("\nComparing to " + doc);
			System.out.println(vectorSpace.cosineSimilarity(query, doc));
		}
	}

}
