package org.javaan.graph;

/*
 * #%L
 * Java Static Code Analysis
 * %%
 * Copyright (C) 2013 Andreas Behnke
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.javaan.model.GraphView;
import org.jgrapht.DirectedGraph;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

/**
 * Provides extended graph functionality by implementing {@link DirectedGraph} and {@link GraphView}
 */
public class ExtendedDirectedGraph<V, E> extends AddVerticesAutomatically<V, E> implements DirectedGraph<V, E>, GraphView<V, E> {

	private static final long serialVersionUID = 1L;
	
	private final DirectedGraph<V, E> delegate;

	public ExtendedDirectedGraph(DirectedGraph<V, E> delegate) {
		super(delegate);
		this.delegate = delegate;
	}
	
	public DirectedGraph<V, E> getDelegate() {
		return delegate;
	}
	
	private Set<V> getTargetSet(Set<E> edges) {
		Set<V> targets = new HashSet<V>();
		for (E e : edges) {
			targets.add(getEdgeTarget(e));
		}
		return targets;
	}

	private Set<V> getSourceSet(Set<E> edges) {
		Set<V> sources = new HashSet<V>();
		for (E e : edges) {
			sources.add(getEdgeSource(e));
		}
		return sources;
	}

	@Override
	public Set<V> targetVerticesOf(V vertex) {
		return getTargetSet(outgoingEdgesOf(vertex));
	}

	@Override
	public Set<V> sourceVerticesOf(V vertex) {
		return getSourceSet(incomingEdgesOf(vertex));
	}

	@Override
	public Set<V> successorsOf(V vertex) {
		Set<V> successors = new HashSet<V>();
		Stack<V> successorStack = new Stack<V>();
		successorStack.addAll(targetVerticesOf(vertex));
		while(!successorStack.isEmpty()) {
			V successor = successorStack.pop();
			// detect cycle, ignore existing nodes
			if (!successors.contains(successor)) {
				successors.add(successor);
				successorStack.addAll(targetVerticesOf(successor));
			}
		}
		return successors;
	}

	@Override
	public Set<V> predecessorsOf(V vertex) {
		Set<V> predecessors = new HashSet<V>();
		Stack<V> predecessorStack = new Stack<V>();
		predecessorStack.addAll(sourceVerticesOf(vertex));
		while(!predecessorStack.isEmpty()) {
			V predecessor = predecessorStack.pop();
			// detect cycle, ignore existing nodes
			if (!predecessors.contains(predecessor)) {
				predecessors.add(predecessor);
				predecessorStack.addAll(sourceVerticesOf(predecessor));
			}
		}
		return predecessors;
	}

	private void traverseGraph(DirectedGraph<V, E> graph, V startVertex, GraphVisitor<V, E> visitor, boolean depthFirst) {
		if (startVertex != null && !containsVertex(startVertex)) {
			return;
		}
		TraversalListener<V, E> listener = null;
		GraphIterator<V, E> iterator = null;
		if (depthFirst) {
			listener = new DepthFirstTraversalListener<V, E>(graph, visitor);
			iterator = new DepthFirstIterator<V, E>(graph, startVertex);
		} else {
			listener = new BreadthFirstTraversalListener<V, E>(visitor);
			iterator = new BreadthFirstIterator<V, E>(graph, startVertex);
		}
		iterator.addTraversalListener(listener);
		while (iterator.hasNext() && !visitor.finished()) {
			iterator.next();
		}
	}

	@Override
	public void traverseDepthFirst(V startVertex, GraphVisitor<V, E> visitor, boolean reverse) {
		if (reverse) {
			DirectedGraph<V, E> graph = new EdgeReversedGraph<V, E>(this);
			traverseGraph(graph, startVertex, visitor, true);
		} else {
			traverseGraph(this, startVertex, visitor, true);
		}
	}

	@Override
	public void traverseBreadthFirst(V startVertex, GraphVisitor<V, E> visitor, boolean reverse) {
		if (reverse) {
			DirectedGraph<V, E> graph = new EdgeReversedGraph<V, E>(this);
			traverseGraph(graph, startVertex, visitor, false);
		} else {
			traverseGraph(this, startVertex, visitor, false);
		}
	}

	private Set<V> collectLeafVertices(DirectedGraph<V, E> graph, V startVertex) {
		Set<V> leaves = new HashSet<V>();
		if (graph.containsVertex(startVertex)) {
			GraphIterator<V, E> iterator = new DepthFirstIterator<V, E>(graph, startVertex);
			iterator.next();
			while (iterator.hasNext()) {
				V vertex = iterator.next();
				if (graph.outDegreeOf(vertex) == 0) {
					leaves.add(vertex);
				}
			}
		}
		return leaves;
	}
	
	@Override
	public Set<V> collectLeaves(V startVertex, boolean reverse) {
		if (reverse) {
			DirectedGraph<V, E> graph = new EdgeReversedGraph<V, E>(this);
			return collectLeafVertices(graph, startVertex);
		} else {
			return collectLeafVertices(this, startVertex);
		}
	}

	@Override
	public GraphView<V, E> createSubgraph(Set<V> vertexFilter, boolean reversed) {
		return GraphFactory.createSubgraphView(this, vertexFilter, reversed);
	}
}
