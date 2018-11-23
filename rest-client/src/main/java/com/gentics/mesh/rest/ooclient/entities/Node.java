package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.rest.client.MeshRestClient;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.gentics.mesh.rest.ooclient.Util.flatZip;
import static com.gentics.mesh.rest.ooclient.Util.flatZipObservable;

public class Node extends AbstractMeshEntity<NodeResponse> {
	private final Single<String> projectName;

	protected Node(Single<MeshRestClient> client, Single<NodeResponse> entity, Single<String> projectName) {
		super(client, entity);
		this.projectName = projectName;
	}

	public User creator() {
		return new User(client, flatZip(client, entity, (client, entity) ->
			client.findUserByUuid(entity.getCreator().getUuid()).toSingle()
		));
	}

	public ListEntityBuilder<Node> children() {
		return new ListEntityBuilder<>(paging ->
			flatZipObservable(client, entity, projectName, (c, entity, projectName) ->
			c.findNodeChildren(projectName, entity.getUuid(), paging).toSingle()
			.flatMapObservable(list -> Observable.fromIterable(list.getData()))
			.map(node -> new Node(client, Single.just(node), Single.just(projectName)))
		));
	}
}
