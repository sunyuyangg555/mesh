package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

public class AbstractMeshEntity<T> extends AbstractEntity {
	protected final Single<T> entity;

	protected AbstractMeshEntity(Single<MeshRestClient> client, Single<T> entity) {
		super(client);
		this.entity = entity;
	}

	public T blockingGet() {
		return entity.blockingGet();
	}
}
