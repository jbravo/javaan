package org.javaan.model;

import java.util.Set;

import org.javaan.graph.GraphVisitor;

/**
 * Abstraction of the graph implementation. Prevents direct dependencies 
 * to internal graph structures and provides convenience functions.
 */
public interface GraphView<V, E> {
	
	Set<V> vertexSet();
	
	Set<E> edgeSet();
	
	Set<V> targetVerticesOf(V vertex);

	Set<V> sourceVerticesOf(V vertex);

	Set<V> successorsOf(V vertex);

	Set<V> predecessorsOf(V vertex);

	void traverse(V startVertex, GraphVisitor<V, E> visitor, boolean reverse);

	Set<V> collectLeaves(V startVertex, boolean reverse);
}