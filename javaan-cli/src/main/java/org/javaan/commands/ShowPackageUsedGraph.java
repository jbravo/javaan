package org.javaan.commands;

import java.util.Set;

import org.javaan.graph.GraphVisitor;
import org.javaan.model.CallGraph;
import org.javaan.model.Dependency;
import org.javaan.model.Package;

public class ShowPackageUsedGraph extends BasePackageDependencyGraphCommand {

	private final static String NAME = "used-packages";

	private final static String DESCRIPTION = "Display the graph of packages being used by another package. "
			+ "This is the top down view of the package dependency graph.";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void traverse(CallGraph callGraph, Package type, GraphVisitor<Package, Dependency> graphPrinter) {
		callGraph.traverseUsedPackages(type, graphPrinter);
	}
	
	@Override
	protected void showGraph2d(CallGraph callGraph, Set<Package> filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Set<Package> collectLeafObjects(CallGraph callGraph, Package type) {
		return callGraph.getLeafUsedPackages(type);
	}

}
