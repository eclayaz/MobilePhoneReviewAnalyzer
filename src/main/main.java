package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.util.FileManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;

public class main {

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		// dd();
		Model model = ModelFactory.createOntologyModel();
		model.read("./rdf/old/119.rdf", "RDF/XML");
		Map<Integer, Review2> reviews = readCommentsFromRDF(model);
		
		OntModel ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontology.read("./rdf/old/reviews_2.owl", "RDFXML");
		createIndividuals(ontology, reviews);
		
		sparqltest2();
	}
*/
	static void sparqltest() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		FileManager.get().addLocatorClassLoader(main.class.getClassLoader());

		/*
		 * Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		 * model.read("./rdf/tt.owl", "TURTLE");
		 */

		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream("./rdf/tt.owl"), null, "TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String queryString = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "prefix owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX cinema: <http://review-analyzer.local/ontologies/tt.owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \r\n" + "select ?film ?location ?label where {\r\n"
				+ "  ?film rdf:type cinema:Review ;\r\n" + "        cinema:isAbout ?location .\r\n"
				+ "  ?film cinema:comment ?label .\r\n" + "  ?location rdf:type/rdfs:subClassOf* cinema:Display .\r\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Literal name = soln.getLiteral("label");
				System.out.println(name);
			}
		} finally {
			qexec.close();
		}
	}

	static void dd() {
		final String filename = "./rdf/tt.owl";

		// Create a PipedRDFStream to accept input and a PipedRDFIterator to
		// consume it
		// You can optionally supply a buffer size here for the
		// PipedRDFIterator, see the documentation for details about recommended
		// buffer sizes
		PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
		final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

		// PipedRDFStream and PipedRDFIterator need to be on different threads
		ExecutorService executor = Executors.newSingleThreadExecutor();

		// Create a runnable for our parser thread
		Runnable parser = new Runnable() {

			@Override
			public void run() {
				// Call the parsing process.
				RDFDataMgr.parse(inputStream, filename);
			}
		};

		// Start the parser on another thread
		executor.submit(parser);

		// We will consume the input on the main thread here

		// We can now iterate over data as it is parsed, parsing only runs as
		// far ahead of our consumption as the buffer size allows
		while (iter.hasNext()) {
			Triple next = iter.next();
			// Do something with each triple
			System.out.println("Subject:  " + next.getSubject());
			System.out.println("Object:  " + next.getObject());
			System.out.println("Predicate:  " + next.getPredicate());
			System.out.println("\n");
		}
	}

	static void createIndividuals(OntModel ontology, Map<Integer, Review2> reviews) {		
		String NS = "http://review-analyzer.local/ontologies/reviews_2.owl#";
		OntClass reviewClasss = ontology.getOntClass(NS + "Review");
		
		
		
		for(Map.Entry<Integer, Review2> entry:reviews.entrySet()){    
			Review2 review = entry.getValue();
			System.out.print(review.getIsAbout());
			Individual sampleReview = ontology.createIndividual(NS + "review" + entry.getKey(), reviewClasss);

			DatatypeProperty rating = ontology.createDatatypeProperty(NS + "rating");
			DatatypeProperty comment = ontology.createDatatypeProperty(NS + "comment");

			OntProperty isAbout = ontology.createObjectProperty(NS + "isAbout");

			sampleReview.addProperty(rating, ontology.createTypedLiteral(review.getRating())).addProperty(comment,
					ontology.createLiteral(review.getComment()));

			Individual amoledIndiv = ontology.getIndividual(NS + review.getIsAbout());
			ontology.add(sampleReview, isAbout, amoledIndiv);
		}

		try {
			FileWriter out = new FileWriter("mm.owl");
			ontology.getWriter("RDF/XML-ABBREV").write(ontology, out, NS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String queryString = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "prefix owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX cinema: <http://review-analyzer.local/ontologies/reviews_2.owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \r\n" + "select ?film ?location ?label where {\r\n"
				+ "  ?film rdf:type cinema:Review ;\r\n" + "        cinema:isAbout ?location .\r\n"
				+ "  ?film cinema:comment ?label .\r\n" + "  ?location rdf:type/rdfs:subClassOf* cinema:Display .\r\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontology);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Literal name = soln.getLiteral("label");
				System.out.println(name);
			}
		} finally {
			qexec.close();
		}
	}
	
	static Map<Integer, Review2> readCommentsFromRDF(Model model) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX c: <http://review-analyzer.local/ontologies/reviews_2.owl#>\r\n"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "select *\r\n" + "where {\r\n"
				+ "  ?Comment c:review ?review .\r\n" + "  ?Comment c:rating ?rating .\r\n"
				+ "  ?Comment c:phone ?phone .\r\n" + "  ?Comment c:isAbout ?isAbout .\r\n" + "}";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		Map<Integer, Review2> mapReviews = new HashMap<Integer, Review2>();
		int i = 1;
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();				
				mapReviews.put(i++,
						new Review2(soln.getLiteral("review").toString(), soln.getLiteral("rating").toString(),
								soln.getLiteral("phone").toString(), soln.getLiteral("isAbout").toString()));
			}
		} finally {
			qexec.close();
		}
		
		return mapReviews;
	}

	static void sparqltest2() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		FileManager.get().addLocatorClassLoader(main.class.getClassLoader());

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		model.read("./rdf/old/reviews_2.owl", "RDFXML");
		model.read("./rdf/old/reviews_3.owl", "RDFXML");
		String NS = "http://review-analyzer.local/ontologies/reviews_2.owl#";
		OntClass reviewClasss = model.getOntClass(NS + "Review");

		// create individual
		Individual sampleReview = model.createIndividual(NS + "sampleReview", reviewClasss);

		// create some properties
		DatatypeProperty rating = model.createDatatypeProperty(NS + "rating");
		DatatypeProperty comment = model.createDatatypeProperty(NS + "comment");

		OntProperty isAbout = model.createObjectProperty(NS + "isAbout");

		sampleReview.addProperty(rating, model.createTypedLiteral(3)).addProperty(comment,
				model.createLiteral("Sample comment"));

		Individual amoledIndiv = model.getIndividual(NS + "amoled");
		model.add(sampleReview, isAbout, amoledIndiv);

		try {
			FileWriter out = new FileWriter("mm.owl");
			model.getWriter("RDF/XML-ABBREV").write(model, out, NS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String queryString = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "prefix owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX cinema: <http://review-analyzer.local/ontologies/reviews_2.owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \r\n" + "select ?film ?location ?label where {\r\n"
				+ "  ?film rdf:type cinema:Review ;\r\n" + "        cinema:isAbout ?location .\r\n"
				+ "  ?film cinema:comment ?label .\r\n" + "  ?location rdf:type/rdfs:subClassOf* cinema:Display .\r\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Literal name = soln.getLiteral("label");
				System.out.println(name);
			}
		} finally {
			qexec.close();
		}
	}

}
