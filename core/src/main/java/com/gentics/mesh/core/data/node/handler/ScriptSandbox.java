package com.gentics.mesh.core.data.node.handler;

import jdk.nashorn.api.scripting.ClassFilter;

/**
 * Script Sandbox classfilter that filters all classes
 */
public class ScriptSandbox implements ClassFilter {
	@Override
	public boolean exposeToScripts(String className) {
		return false;
	}
}
