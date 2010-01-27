package com.tinkerpop.gremlin;

import com.tinkerpop.gremlin.db.tg.TinkerGraph;
import com.tinkerpop.gremlin.model.Edge;
import com.tinkerpop.gremlin.model.Graph;
import com.tinkerpop.gremlin.model.parser.GraphMLReader;
import com.tinkerpop.gremlin.statements.Tokens;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DynamicPathTest extends TestCase {

    public void testKnowsPath() throws Exception {
        GremlinEvaluator ge = new GremlinEvaluator();
        Graph graph = new TinkerGraph();
        ge.setVariable(Tokens.GRAPH_VARIABLE, graph);
        InputStream stream = GraphMLReader.class.getResourceAsStream("graph-example-1.xml");
        GraphMLReader.inputGraph(graph, stream);

        InputStream simpleFunc = new ByteArrayInputStream("path knows\n./outE[@label='knows']\nend\n$_ := g:id('1')\n./knows".getBytes());
        List results = ge.evaluate(simpleFunc);
        assertEquals(results.size(), 2);
        for (Object result : results) {
            assertTrue(result instanceof Edge);
            assertEquals(((Edge) result).getLabel(), "knows");
        }

    }

    public void testKnowsWeightPath() throws Exception {
        GremlinEvaluator ge = new GremlinEvaluator();
        Graph graph = new TinkerGraph();
        ge.setVariable(Tokens.GRAPH_VARIABLE, graph);
        InputStream stream = GraphMLReader.class.getResourceAsStream("graph-example-1.xml");
        GraphMLReader.inputGraph(graph, stream);

        InputStream simpleFunc = new ByteArrayInputStream("path knows\n./outE[@label='knows' and @weight > 0.5]\nend\n$_ := g:id('1')\n./knows".getBytes());
        List results = ge.evaluate(simpleFunc);
        assertEquals(results.size(), 1);
        for (Object result : results) {
            assertTrue(result instanceof Edge);
            assertEquals(((Edge) result).getLabel(), "knows");
            assertTrue((Float) ((Edge) result).getProperty("weight") > 0.5);
        }

    }

    public void testMultiLinePath() throws Exception {
        GremlinEvaluator ge = new GremlinEvaluator();
        Graph graph = new TinkerGraph();
        ge.setVariable(Tokens.GRAPH_VARIABLE, graph);
        InputStream stream = GraphMLReader.class.getResourceAsStream("graph-example-1.xml");
        GraphMLReader.inputGraph(graph, stream);

        InputStream simpleFunc = new ByteArrayInputStream("path knows\n$x := ./outE[@label='knows']\n$x := $x[@weight > 0.5]\n$x\nend\n$_ := g:id('1')\n./knows".getBytes());
        List results = ge.evaluate(simpleFunc);
        assertEquals(results.size(), 1);
        for (Object result : results) {
            assertTrue(result instanceof Edge);
            assertEquals(((Edge) result).getLabel(), "knows");
            assertTrue((Float) ((Edge) result).getProperty("weight") > 0.5);
        }

    }

    public void testOverrideError() throws Exception {
        GremlinEvaluator ge = new GremlinEvaluator();
        InputStream simpleFunc = new ByteArrayInputStream("path outE\nend\n".getBytes());
        try {
            ge.evaluate(simpleFunc);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
        simpleFunc = new ByteArrayInputStream("path inE\nend\n".getBytes());
        try {
            ge.evaluate(simpleFunc);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
        simpleFunc = new ByteArrayInputStream("path bothV\nend\n".getBytes());
        try {
            ge.evaluate(simpleFunc);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }

    }
}
