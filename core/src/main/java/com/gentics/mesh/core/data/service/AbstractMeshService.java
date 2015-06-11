package com.gentics.mesh.core.data.service;

import org.jglue.totorom.FramedGraph;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.etc.MeshSpringConfiguration;

public class AbstractMeshService {

	@Autowired
	protected I18NService i18n;

	@Autowired
	protected MeshSpringConfiguration springConfiguration;

	@Autowired
	protected FramedGraph framedGraph;

}
