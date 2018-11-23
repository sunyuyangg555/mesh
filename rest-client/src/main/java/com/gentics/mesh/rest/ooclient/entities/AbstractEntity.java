package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

public abstract class AbstractEntity {
	protected final Single<MeshRestClient> client;

	protected AbstractEntity(Single<MeshRestClient> client) {
		this.client = client;
	}
}
