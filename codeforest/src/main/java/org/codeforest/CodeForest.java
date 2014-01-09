package org.codeforest;

import java.awt.GraphicsConfiguration;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.codeforest.model.VertexSceneContext;
import org.codeforest.scenegraph.BoxTreeLayout;
import org.codeforest.scenegraph.EdgeNodeFactory;
import org.codeforest.scenegraph.LineEdgeFactory;
import org.codeforest.scenegraph.TableLayout;
import org.codeforest.scenegraph.TreePlanter;
import org.codeforest.scenegraph.TreeWidthCalculator;
import org.codeforest.scenegraph.VertexNodeFactory;
import org.codeforest.scenegraph.VertexTreeSceneBuilder;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * First test: Display a simple JGraph
 */
public class CodeForest extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;

	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";
	private static final String E = "E";
	private static final String F = "F";
	private static final String G = "G";
	private static final String H = "H";
	private static final String I = "I";
	private static final String J = "J";
	private static final String K = "K";
	private static final String L = "L";
	private static final String M = "M";
	private static final String N = "N";
	private static final String O = "O";

	private SimpleUniverse univ = null;
	private BranchGroup scene = null;

	private javax.swing.JPanel drawingPanel;

	public CodeForest() {
		// Initialize the GUI components
		initComponents();

		// Create Canvas3D and SimpleUniverse; add canvas to drawing panel
		Canvas3D c = createUniverse();
		drawingPanel.add(c, java.awt.BorderLayout.CENTER);

		// Create the content branch and add it to the universe
		scene = createSceneGraph();
		univ.addBranchGraph(scene);

		OrbitBehavior orbit = new OrbitBehavior(c);
		orbit.setSchedulingBounds(new BoundingSphere(
				new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE));
		univ.getViewingPlatform().setViewPlatformBehavior(orbit);
	}

	public BranchGroup createSceneGraph() {
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		DirectedGraph<String, String> graph = new DefaultDirectedGraph<String, String>(
				new EdgeFactory<String, String>() {
					public String createEdge(String sourceVertex,
							String targetVertex) {
						return sourceVertex + targetVertex;
					};
				});
		graph.addVertex(A);
		graph.addVertex(B);
		graph.addVertex(C);
		graph.addVertex(D);
		graph.addVertex(E);
		graph.addVertex(F);
		graph.addVertex(G);
		graph.addVertex(H);
		graph.addVertex(I);
		graph.addVertex(J);
		graph.addVertex(K);
		graph.addVertex(L);
		graph.addVertex(M);
		graph.addVertex(N);
		graph.addVertex(O);
		graph.addEdge(A, B);
		graph.addEdge(A, C);
		graph.addEdge(B, D);
		graph.addEdge(B, E);
		graph.addEdge(B, F);
		graph.addEdge(C, G);
		graph.addEdge(D, H);
		graph.addEdge(D, I);
		graph.addEdge(D, J);
		graph.addEdge(E, K);
		graph.addEdge(E, L);
		graph.addEdge(F, M);
		graph.addEdge(F, N);
		graph.addEdge(G, O);
		
		VertexSceneContext<String> context = new VertexSceneContext<String>();
		// calculate vertex widths for subgraph
		new TreeWidthCalculator<String, String>(context, graph).calculateVertexWidth(A);

		VertexNodeFactory<String> shapeFactory = new VertexNodeFactory<String>() {
			public Node createNode(String vertex) {
				return new ColorCube(0.4);
			}
		};
		EdgeNodeFactory<String, String> edgeNodeFactory = new LineEdgeFactory<String, String>();
		VertexTreeSceneBuilder<String, String> treeBuilder = new VertexTreeSceneBuilder<String, String>(
				context, graph, shapeFactory, edgeNodeFactory, new BoxTreeLayout<String>(context, 2d, 3d));
		context.get(A).setRow(0);
		context.get(B).setRow(0);
		context.get(C).setRow(1);
		context.get(D).setRow(1);
		context.get(E).setRow(2);
		context.get(F).setRow(2);
		context.get(G).setRow(2);
		TreePlanter<String> planter = new TreePlanter<String>(context, treeBuilder, 
				new TableLayout<String>(context, 2d, 10d, 2d));
		List<String> trees = new ArrayList<String>(4);
		trees.add(A);
		trees.add(B);
		trees.add(C);
		trees.add(D);
		trees.add(E);
		trees.add(F);
		trees.add(G);
		TransformGroup transformGroup = planter.createScene(trees);
		objRoot.addChild(transformGroup);

		// Have Java 3D perform optimizations on this scene graph.
		objRoot.compile();

		return objRoot;
	}

	private Canvas3D createUniverse() {
		// Get the preferred graphics configuration for the default screen
		GraphicsConfiguration config = SimpleUniverse
				.getPreferredConfiguration();

		// Create a Canvas3D using the preferred configuration
		Canvas3D c = new Canvas3D(config);

		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		ViewingPlatform viewingPlatform = univ.getViewingPlatform();

		TransformGroup viewTransform = viewingPlatform
				.getViewPlatformTransform();
		Transform3D t3d = new Transform3D();
		t3d.lookAt(new Point3d(0, 0, 150), new Point3d(0, 0, 0), new Vector3d(
				0, 1, 0));
		t3d.invert();
		viewTransform.setTransform(t3d);
		View view = univ.getViewer().getView();
		view.setBackClipDistance(100.0);
		view.setMinimumFrameCycleTime(5);

		return c;
	}

	private void initComponents() {
		drawingPanel = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("HelloUniverse");
		drawingPanel.setLayout(new java.awt.BorderLayout());

		drawingPanel.setPreferredSize(new java.awt.Dimension(500, 500));
		getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);

		pack();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new CodeForest().setVisible(true);
			}
		});
	}

}