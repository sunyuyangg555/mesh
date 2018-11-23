package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

public class ProjectRoot extends AbstractEntity {
	public ProjectRoot(Single<MeshRestClient> client) {
		super(client);
	}

	public Project byName(String name) {
		return new Project(client, client.flatMap(client -> client.findProjectByName(name).toSingle()));
	}
}
