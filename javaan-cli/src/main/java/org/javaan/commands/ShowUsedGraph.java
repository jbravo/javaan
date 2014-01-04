package org.javaan.commands;

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

import java.util.Set;

import org.javaan.graph.GraphVisitor;
import org.javaan.model.CallGraph;
import org.javaan.model.Clazz;
import org.javaan.model.Method;

public class ShowUsedGraph extends BaseDependencyGraphCommand {

	private final static String NAME = "used";

	private final static String DESCRIPTION = "Display the graph of classes being used by another class. "
			+ "This is the top down view of the class dependency graph.";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void traverse(CallGraph callGraph, Clazz clazz, GraphVisitor<Clazz, Method> graphPrinter) {
		callGraph.traverseUsedTypes(clazz, graphPrinter);
	}

	@Override
	protected Set<Clazz> collectLeafObjects(CallGraph callGraph, Clazz namedObject) {
		return callGraph.getLeafUsedTypes(namedObject);
	}	
}