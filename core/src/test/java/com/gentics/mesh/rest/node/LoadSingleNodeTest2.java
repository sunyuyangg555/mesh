package com.gentics.mesh.rest.node;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestSize.PROJECT_AND_NODE;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.gentics.mesh.core.rest.admin.consistency.ConsistencyRating;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.project.ProjectCreateRequest;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.schema.SchemaListResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaUpdateRequest;
import com.gentics.mesh.core.rest.user.UserAPITokenResponse;
import com.gentics.mesh.core.rest.user.UserResponse;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.parameter.impl.PagingParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

@MeshTestSetting(useElasticsearch = false, testSize = PROJECT_AND_NODE, startServer = true, inMemoryDB = false)
public class LoadSingleNodeTest2 extends AbstractMeshTest {

	public static final String PROJECT_NAME = "test";

	@Test
	public void testClusterStatus() throws Exception {

		grantAdminRole();

		setAPIToken(client());

		ProjectResponse project = setupProject();
		SchemaListResponse schemaList = call(() -> client().findSchemas());
		SchemaResponse folderSchema = schemaList.getData().stream().filter(s -> s.getName().equals("folder")).findFirst().get();
		String json = JsonUtil.toJson(folderSchema);
		String schemaUuid = folderSchema.getUuid();
		AtomicBoolean stopFlag = new AtomicBoolean(false);

		// Client A - Create Nodes
		NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
		nodeCreateRequest.setSchemaName("folder");
		nodeCreateRequest.setParentNodeUuid(project.getRootNode().getUuid());
		nodeCreateRequest.getFields().put("slug", new StringFieldImpl().setString("test"));
		nodeCreateRequest.getFields().put("name", new StringFieldImpl().setString("Name"));
		nodeCreateRequest.setLanguage("en");
		call(() -> client().createNode(PROJECT_NAME, nodeCreateRequest));

		// Client B - Update schema
		AtomicInteger schemaCounter = new AtomicInteger();
		long schemaTimer = vertx().setPeriodic(20 * 1000, ch -> {
			SchemaUpdateRequest request = JsonUtil.readValue(json, SchemaUpdateRequest.class);
			request.setDescription("Test" + schemaCounter.getAndIncrement());
			System.out.println("Updating schema: " + schemaCounter.get());
			client().updateSchema(schemaUuid, request).toCompletable().subscribe(() -> {
				System.out.println("Updated schema: " + schemaCounter.get());
			});
		});

		// Client B - Check consistency & list nodes
		vertx().setPeriodic(1000, ch -> {
			client().findNodes(PROJECT_NAME, new PagingParametersImpl().setPerPage(10L)).toSingle().subscribe(list -> {
				System.out.println("Total: " + list.getMetainfo().getTotalCount());
			});

			client().checkConsistency().toSingle().subscribe(result -> {
				System.out.println("Consistency: " + result.getResult());
				if (result.getResult().equals(ConsistencyRating.INCONSISTENT)) {
					stopFlag.set(true);
					System.out.println("INCONSISTENCY FOUND - STOPPING");
					System.out.println(result.toJson());
					vertx().cancelTimer(ch);
					vertx().cancelTimer(schemaTimer);
				}
			});
		});

		System.in.read();
		System.out.println(call(() -> client().checkDatabase()).toJson());
		System.in.read();
		System.out.println(call(() -> client().checkConsistency()).toJson());
		System.in.read();
		System.out.println(call(() -> client().checkConsistency()).toJson());
		System.in.read();

	}

	private void setAPIToken(MeshRestClient client) {
		UserResponse me = call(() -> client.me());
		UserAPITokenResponse token = call(() -> client.issueAPIToken(me.getUuid()));
		client.setAPIKey(token.getToken());
	}

	private ProjectResponse setupProject() {
		ProjectCreateRequest request = new ProjectCreateRequest();
		request.setName(PROJECT_NAME);
		request.setSchemaRef("folder");
		ProjectResponse project = call(() -> client().createProject(request));
		return project;
	}

}
