package org.corpus_tools.demo;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.eclipse.emf.common.util.URI;

public class SaltDemo {
	public static void main(String[] args) {
		System.out.println("***********************************************");
		System.out.println("*** create Salt model");
		System.out.println("***********************************************");
		createSaltModel();

		System.out.println("***********************************************");
		System.out.println("*** access Salt model");
		System.out.println("***********************************************");
		accessSaltModel();

		System.out.println("***********************************************");
		System.out.println("*** tarverse Salt model");
		System.out.println("***********************************************");
		accessTraverse();

		System.out.println("***********************************************");
		System.out.println("*** store and load Salt model");
		System.out.println("***********************************************");
		loadSave();
	}

	public static void createSaltModel() {
		// create the Salt project, which is the entry point for Salt
		SaltProject saltProject = SaltFactory.createSaltProject();

		// create a new corpus structure
		SCorpusGraph corpGraph = saltProject.createCorpusGraph();
		// add a corpus with name sampleCorpus to the corpus structure
		SCorpus sampleCorpus = corpGraph.createCorpus(URI.createURI("/sampleCorpus")).get(0);

		// create a single meta annotation
		sampleCorpus.createMetaAnnotation(null, "annotator", "A. Nyone");

		// create a new document named sampleDocument, and adds it as part of
		// sampleCorpus
		SDocument sampleDocument = corpGraph.createDocument(URI.createURI("/sampleCorpus/sampleDocument"));
		// ***********
		// Alternative
		// ***********
		// SDocument sampleDocument= SaltFactory.createSDocument();
		// sampleDocument.setName("sampleDocument");
		// corpGraph.addDocument(sampleCorpus, sampleDocument);

		// create document structure
		sampleDocument.setDocumentGraph(SaltFactory.createSDocumentGraph());

		// create primary data
		STextualDS primaryText = sampleDocument.getDocumentGraph().createTextualDS("Is this example more complicated than it appears to be?");
		// ***********
		// Alternative
		// *********
		// STextualDS primaryText = SaltFactory.createSTextualDS();
		// // set the primary text to the sentence
		// // "Is this example more complicated than it appears to be?"
		// primaryText.setText("Is this example more complicated than it appears to?");
		// // add the text to the document-graph
		// sampleDocument.getDocumentGraph().addNode(primaryText);

		// create tokenization of text
		sampleDocument.getDocumentGraph().tokenize();

		// access the token list
		System.out.println("\tcreated tokens");
		System.out.println("\t"+sampleDocument.getDocumentGraph().getTokens());
		// access the first token in token list
		sampleDocument.getDocumentGraph().getTokens().get(0);

		// ***********
		// Alternative
		// *********
		// create tokens manually
		SToken tok_is = sampleDocument.getDocumentGraph().createToken(primaryText, 0, 2);
		SToken tok_this = sampleDocument.getDocumentGraph().createToken(primaryText, 3, 7);
		SToken tok_example = sampleDocument.getDocumentGraph().createToken(primaryText, 8, 15);
		SToken tok_more = sampleDocument.getDocumentGraph().createToken(primaryText, 16, 20);
		SToken tok_complicated = sampleDocument.getDocumentGraph().createToken(primaryText, 21, 32);
		SToken tok_than = sampleDocument.getDocumentGraph().createToken(primaryText, 33, 37);
		SToken tok_it = sampleDocument.getDocumentGraph().createToken(primaryText, 38, 41);
		SToken tok_appears = sampleDocument.getDocumentGraph().createToken(primaryText, 42, 48);
		SToken tok_to = sampleDocument.getDocumentGraph().createToken(primaryText, 49, 51);
		SToken tok_be = sampleDocument.getDocumentGraph().createToken(primaryText, 52, 54);
		SToken tok_PUNC = sampleDocument.getDocumentGraph().createToken(primaryText, 54, 55);

		// creating a layer named morphology
		SLayer morphLayer = SaltFactory.createSLayer();
		morphLayer.setName("morphology");
		sampleDocument.addLayer(morphLayer);

		// adding the new token to the morphology layer
		morphLayer.addNode(tok_is);
		// adding a part-of-speech annotation to the new token
		tok_is.createAnnotation(null, "pos", "VBZ");
		// adding a lemma annotation to the new token
		tok_is.createAnnotation(null, "lemma", "be");

		// create a list of nodes, which shall be overlapped by the NP-node
		List<SStructuredNode> overlappingNodes = new ArrayList<>();
		overlappingNodes.add(tok_this);
		overlappingNodes.add(tok_example);
		// adding NP-Node to graph and automatically creating edges to
		// overlapping nodes
		SStructure np_1 = sampleDocument.getDocumentGraph().createStructure(overlappingNodes);
		np_1.createAnnotation(null, "cat", "NP");
		// ...

		// create a list of nodes, which shall be overlapping by SQ-node
		overlappingNodes = new ArrayList<>();
		overlappingNodes.add(tok_is);
		overlappingNodes.add(np_1);
		SStructure sq = sampleDocument.getDocumentGraph().createStructure(overlappingNodes);
		sq.createAnnotation(null, "cat", "SQ");

		// create a syntax layer
		SLayer syntaxLayer = SaltFactory.createSLayer();
		syntaxLayer.setName("syntax");
		sampleDocument.getDocumentGraph().addLayer(syntaxLayer);

		// add syntactic nodes to syntax layer
		syntaxLayer.addNode(np_1);
		syntaxLayer.addNode(sq);
		// ...

		sampleDocument.getDocumentGraph().createRelation(np_1, tok_is, SALT_TYPE.SDOMINANCE_RELATION, null);

		sampleDocument.getDocumentGraph().getRelations(np_1.getId(), tok_is.getId()).get(0).createAnnotation("myNamespace", "myName", "myValue");

		// ***********
		// Alternative
		// *********
		// SStructure sq = SaltFactory.createSStructure();
		// sampleDocument.getDocumentGraph().addNode(sq);
		// sq.createAnnotation(null, "cat", "SQ");
		//
		// SDominanceRelation domRel = SaltFactory.createSDominanceRelation();
		// domRel.setSource(sq);
		// domRel.setTarget(tok_is);
		// sampleDocument.getDocumentGraph().addRelation(domRel);

		// create span overlaping only one token
		SSpan contrast_focus = sampleDocument.getDocumentGraph().createSpan(tok_is);
		contrast_focus.createAnnotation(null, "inf-struct", "contrast-focus");

		// create a list of tokens taking part in the set to be annotated as a
		// bundle
		List<SToken> overlappingTokens = new ArrayList<>();
		overlappingTokens.add(tok_this);
		overlappingTokens.add(tok_example);
		overlappingTokens.add(tok_more);
		overlappingTokens.add(tok_complicated);
		overlappingTokens.add(tok_than);
		overlappingTokens.add(tok_it);
		overlappingTokens.add(tok_appears);
		overlappingTokens.add(tok_to);
		overlappingTokens.add(tok_be);
		overlappingTokens.add(tok_PUNC);

		// create span overlaping a set of tokens
		SSpan topic = sampleDocument.getDocumentGraph().createSpan(overlappingTokens);
		topic.createAnnotation(null, "inf-struct", "topic");

		// ***********
		// Alternative
		// *********
		// SSpan contrast_focus= SaltFactory.createSSpan();
		// sampleDocument.getDocumentGraph().addNode(contrast_focus);
		// SSpanningRelation spanRel= SaltFactory.createSSpanningRelation();
		// spanRel.setSource(contrast_focus);
		// spanRel.setTarget(tok_is);
		// docGraph.addSRelation(spanRel);

		// create a span covering the words "the" and "example"
		overlappingTokens = new ArrayList<>();
		overlappingTokens.add(tok_this);
		overlappingTokens.add(tok_example);
		SSpan span = sampleDocument.getDocumentGraph().createSpan(overlappingTokens);

		// creating a pointing relations
		SPointingRelation pointingRelation = SaltFactory.createSPointingRelation();
		// setting token "it" as source of this relation
		pointingRelation.setSource(tok_it);
		// setting span "this example" as target of this relation
		pointingRelation.setTarget(span);
		// adding the created relation to the document-graph
		sampleDocument.getDocumentGraph().addRelation(pointingRelation);
		// adding the type to the relation
		pointingRelation.setType("anaphoric");
	}

	public static void accessSaltModel() {
		SDocument sampleDocument = SaltFactory.createSDocument();
		SampleGenerator.createDocumentStructure(sampleDocument);
		SDocumentGraph docGraph = sampleDocument.getDocumentGraph();

		// access all primary text nodes (getting a list)
		docGraph.getTextualDSs();
		// access the primary text of the first primary text node
		docGraph.getTextualDSs().get(0).getText();

		// access all token nodes (getting a list)
		docGraph.getTokens();
		// access the primary text, which is overlapped by the first token
		// (note: changing the returned text has no effects)
		docGraph.getText(docGraph.getTokens().get(0));
		// access all annotations of the first token
		docGraph.getTokens().get(0).getAnnotations();

		// access the relations between all tokens and their primary text nodes
		docGraph.getTextualRelations();
		// access the interval determining the overlapped text of the first
		// relation (which probably is related to the first token)
		docGraph.getTextualRelations().get(0).getStart();
		docGraph.getTextualRelations().get(0).getEnd();

		// access all span nodes (getting a list)
		docGraph.getSpans();
		// access the primary text, which is overlapped by the first span (note:
		// changing the returned text has no effects)
		docGraph.getText(docGraph.getSpans().get(0));
		// access all relations between all spans and their tokens
		docGraph.getSpanningRelations();

		// access all hierarchical nodes (structures)
		docGraph.getStructures();
		// access the primary text, which is overlapped by the first structure
		// (note: changing the returned text has no effects)
		docGraph.getText(docGraph.getStructures().get(0));
		// access all dominance relations (relations between all structures and
		// their other nodes)
		docGraph.getDominanceRelations();
		// access all annotations of the first dominance relation
		docGraph.getDominanceRelations().get(0).getAnnotations();

		// access all pointing relations
		docGraph.getPointingRelations();
		// access all annotations of the first pointing relation
		docGraph.getDominanceRelations().get(0).getAnnotations();

		// access all roots of the document graph
		docGraph.getRoots();

		// access all nodes and edges contained in the document graph
		docGraph.getNodes();
		docGraph.getRelations();

		// access all outgoing edges of a node (the first structure node)
		List<SRelation<SNode, SNode>> out = docGraph.getOutRelations(docGraph.getStructures().get(0).getId());
		// access all incoming edges of a node (the first structure node)
		List<SRelation<SNode, SNode>> in = docGraph.getInRelations(docGraph.getStructures().get(0).getId());
	}

	public static void accessTraverse() {
		SDocument myDocument = SaltFactory.createSDocument();
		SampleGenerator.createDocumentStructure(myDocument);
		SDocumentGraph myDocGraph = myDocument.getDocumentGraph();
		
		System.out.println("traverse top down...");
		myDocGraph.traverse(myDocGraph.getRoots(), GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "td", new GraphTraverseHandler() {

			@Override
			public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation relation, SNode fromNode, long order) {
				System.out.println("\t>reached node "+currNode);
			}

			@Override
			public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation relation, SNode currNode, long order) {
				System.out.println("\t-check constraint "+currNode);
				return true;
			}

			@Override
			public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {
				System.out.println("\t<leave node "+currNode);
			}
		});
		
		System.out.println("traverse bottom up...");
		// traversing the graph form the tokens to the top
		myDocGraph.traverse(myDocGraph.getTokens(), GRAPH_TRAVERSE_TYPE.BOTTOM_UP_BREADTH_FIRST, "bu", new GraphTraverseHandler() {

			@Override
			public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation relation, SNode fromNode, long order) {
				System.out.println("\t>reached node "+currNode);
			}

			@Override
			public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation relation, SNode fromNode, long order) {
				System.out.println("\t<leave node "+currNode);
			}

			@Override
			public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation relation, SNode currNode, long order) {
				System.out.println("\t-check constraint "+currNode);
				return true;
			}
		}, false);
	}

	public static void loadSave() {
		URI saltProjectUri = URI.createFileURI("./output/mySaltProject");
		URI documentStructureUri = URI.createFileURI("./output/myDocument.salt");

		// create the Salt project, which is the entry point for Salt
		SaltProject saltProject = SaltFactory.createSaltProject();

		// create a new corpus structure
		SCorpusGraph corpGraph = saltProject.createCorpusGraph();
		// create a new document named sampleDocument, and adds it as part of
		// sampleCorpus
		SDocument sampleDocument = corpGraph.createDocument(URI.createURI("/myCorpus/myDocument"));
		SampleGenerator.createDocumentStructure(sampleDocument);

		System.out.println("\tstore Salt project to " + saltProjectUri + "'");
		saltProject.saveSaltProject(saltProjectUri);
		saltProject = null;

		System.out.println("\tload Salt project from '" + saltProjectUri + "'");
		saltProject = SaltFactory.createSaltProject();
		saltProject.loadSaltProject(saltProjectUri);
		
		System.out.println("\tstore document structure to '" + documentStructureUri + "'");
		sampleDocument= saltProject.getCorpusGraphs().get(0).getDocument(URI.createURI("salt:/myCorpus/myDocument"));
		// storing
		sampleDocument.saveDocumentGraph(documentStructureUri);
		System.out.println("\tload document structure from '" + documentStructureUri + "'");
		// loading
		sampleDocument.loadDocumentGraph(documentStructureUri);
	}
}
