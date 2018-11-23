package com.gentics.mesh.rest;

import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.user.UserResponse;
import com.gentics.mesh.rest.ooclient.MeshObjectOrientedClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectOrientedClientTest {
	String url = "demo.getmesh.io";
	MeshObjectOrientedClient client = MeshObjectOrientedClient.withAnonymousUser(url);
	MeshObjectOrientedClient adminClient = MeshObjectOrientedClient.withUser(url, "admin", "admin");

	@Test
	public void getProjectByName() {
		ProjectResponse project = client.projects().byName("demo").blockingGet();
		assertThat(project.getName()).isEqualTo("demo");
	}

	@Test
	public void getRootNode() {
		NodeResponse rootNode = client.projects().byName("demo").rootNode().blockingGet();

		assertThat(rootNode.getVersion()).isEqualTo("1.0");
	}

	@Test
	public void getRootNodeCreator() {
		UserResponse creator = adminClient.projects().byName("demo").rootNode().creator().blockingGet();

		assertThat(creator.getUsername()).isEqualTo("admin");
	}

	@Test
	public void getMyselfAnonymous() {
		UserResponse me = client.auth().me().blockingGet();

		assertThat(me.getUsername()).isEqualTo("anonymous");
	}

	@Test
	public void getMyselfAdmin() {
		UserResponse me = adminClient.auth().me().blockingGet();

		assertThat(me.getUsername()).isEqualTo("admin");
	}

	@Test
	public void getChildren() {
		Long count = client.projects().byName("demo")
			.rootNode()
			.children().build()
			.count().blockingGet();

		assertThat(count).isEqualTo(4);
	}

	@Test
	public void getChildrenPaged() {
		Long count = client.projects().byName("demo")
			.rootNode()
			.children().perPage(2).build()
			.count().blockingGet();

		assertThat(count).isEqualTo(2);
	}
}
