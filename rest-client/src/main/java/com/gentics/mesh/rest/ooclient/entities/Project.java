package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

import static com.gentics.mesh.rest.ooclient.Util.flatZip;

public class Project extends AbstractMeshEntity<ProjectResponse> {
	protected Project(Single<MeshRestClient> client, Single<ProjectResponse> entity) {
		super(client, entity);
	}

	public Node rootNode() {
		Single<ProjectResponse> cProject = entity.cache();
		return new Node(client, flatZip(client, cProject, (client, project) ->
			client.findNodeByUuid(project.getName(), project.getRootNode().getUuid()).toSingle()
		), cProject.map(ProjectResponse::getName));
	}
}
