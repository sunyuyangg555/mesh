package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.core.rest.user.UserResponse;
import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Single;

public class User extends AbstractMeshEntity<UserResponse> {
	public User(Single<MeshRestClient> meshRestClient, Single<UserResponse> user) {
		super(meshRestClient, user);
	}
}
