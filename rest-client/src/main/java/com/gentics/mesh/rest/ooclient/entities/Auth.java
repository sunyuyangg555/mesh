package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

public class Auth {
	private final Single<MeshRestClient> meshRestClient;

	public Auth(Single<MeshRestClient> meshRestClient) {
		this.meshRestClient = meshRestClient;
	}

	public User me() {
		return new User(meshRestClient, meshRestClient.flatMap(client -> client.me().toSingle()));
	}
}
