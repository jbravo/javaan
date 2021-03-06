package org.javaan.print;

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

import java.io.PrintStream;

import org.javaan.graph.GraphVisitorAdapter;
import org.javaan.graph.VertexEdge;
import org.javaan.graph.VertexEdgeGraphVisitor;

public class VertexEdgeGraphPrinter<V>  extends GraphVisitorAdapter<V, VertexEdge<V>> implements VertexEdgeGraphVisitor<V> {

	private final ObjectFormatter<V> formatter;
	
	private final PrintStream output;
	
	public VertexEdgeGraphPrinter(PrintStream output, ObjectFormatter<V> formatter) {
		this.output = output;
		this.formatter = formatter;
	}

	@Override
	public void visitVertex(V node, int level) {
		PrintUtil.indent(output, formatter, node, level);
	}
}
