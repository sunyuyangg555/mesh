package com.gentics.mesh.rest.ooclient;

import com.gentics.mesh.rest.client.MeshRestClient;
import com.gentics.mesh.rest.ooclient.entities.Auth;
import com.gentics.mesh.rest.ooclient.entities.ProjectRoot;
import io.reactivex.Single;
import io.vertx.core.Vertx;

public class MeshObjectOrientedClient {

	private final Single<MeshRestClient> meshRestClient;

	private MeshObjectOrientedClient() {
		throw new RuntimeException("Not implemented");
	}

	private MeshObjectOrientedClient(Single<MeshRestClient> meshRestClient) {
		this.meshRestClient = meshRestClient;
	}

	public static MeshObjectOrientedClient withUser(String url, String username, String password) {
		MeshRestClient client = createClient(url);
		return new MeshObjectOrientedClient(client.setLogin(username, password).login().map(ignore -> client).cache());
	}

	public static MeshObjectOrientedClient withToken(String url, String token) {
		throw new RuntimeException("Not implemented");
	}

	public static MeshObjectOrientedClient withAnonymousUser(String url) {
		// TODO: 23.11.18 Use some other vertx instance
		return new MeshObjectOrientedClient(Single.just(createClient(url)));
	}

	public ProjectRoot projects() {
		return new ProjectRoot(meshRestClient);
	}

	public Auth auth() {
		return new Auth(meshRestClient);
	}

	private static MeshRestClient createClient(String url) {
		return MeshRestClient.create(url, 443, true, Vertx.vertx());
	}
}
