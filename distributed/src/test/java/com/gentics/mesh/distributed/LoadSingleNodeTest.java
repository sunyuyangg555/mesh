package com.gentics.mesh.distributed;

import static com.gentics.mesh.test.ClientHelper.call;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
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
import com.gentics.mesh.rest.client.impl.MeshRestHttpClientImpl;

import io.vertx.core.Vertx;

public class LoadSingleNodeTest {

	public static final String PROJECT_NAME = "test";

	private MeshRestClient client;

	private Vertx vertx = Vertx.vertx();

	@Before
	public void setup() {
		client = new MeshRestHttpClientImpl("localhost", 8080, false, vertx);
		client.setLogin("admin", "admin");
		client.login().blockingGet();
	}
	

	@Test
	public void testClusterStatus() throws Exception {

		setAPIToken(client);

		ProjectResponse project = setupProject();
		SchemaListResponse schemaList = call(() -> client.findSchemas());
		SchemaResponse folderSchema = schemaList.getData().stream().filter(s -> s.getName().equals("folder")).findFirst().get();
		String json = JsonUtil.toJson(folderSchema);
		String schemaUuid = folderSchema.getUuid();
		AtomicBoolean stopFlag = new AtomicBoolean(false);

		// Client A - Create Nodes
		AtomicLong counter = new AtomicLong();
		Long createTimer = vertx.setPeriodic(250, ch -> {
			long count = counter.getAndIncrement();
			NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
			nodeCreateRequest.setSchemaName("folder");
			nodeCreateRequest.setParentNodeUuid(project.getRootNode().getUuid());
			nodeCreateRequest.getFields().put("slug", new StringFieldImpl().setString("test_" + count));
			nodeCreateRequest.getFields().put("name", new StringFieldImpl().setString("Name " + count));
			nodeCreateRequest.setLanguage("en");
			client.createNode(PROJECT_NAME, nodeCreateRequest).toCompletable().subscribe(() -> {
				System.out.println("Created node " + count);
			});
		});

		// Client B - Update schema
		AtomicInteger schemaCounter = new AtomicInteger();
		long schemaTimer = vertx.setPeriodic(60 * 1000, ch -> {
			SchemaUpdateRequest request = JsonUtil.readValue(json, SchemaUpdateRequest.class);
			request.setDescription("Test" + schemaCounter.getAndIncrement());
			System.out.println("Updating schema: " + schemaCounter.get());
			client.updateSchema(schemaUuid, request).toCompletable().subscribe(() -> {
				System.out.println("Updated schema: " + schemaCounter.get());
			});
		});

		// Client B - Check consistency & list nodes
		vertx.setPeriodic(1000, ch -> {
			client.findNodes(PROJECT_NAME, new PagingParametersImpl().setPerPage(10L)).toSingle().subscribe(list -> {
				System.out.println("Total: " + list.getMetainfo().getTotalCount());
			});

			client.checkConsistency().toSingle().subscribe(result -> {
				System.out.println("Consistency: " + result.getResult());
				if (result.getResult().equals(ConsistencyRating.INCONSISTENT)) {
					stopFlag.set(true);
					System.out.println("INCONSISTENCY FOUND - STOPPING");
					System.out.println(result.toJson());
					vertx.cancelTimer(ch);
					vertx.cancelTimer(schemaTimer);
					vertx.cancelTimer(createTimer);
				}
			});
		});

		System.in.read();
		System.out.println(call(() -> client.checkConsistency()).toJson());
		System.in.read();
		System.out.println(call(() -> client.checkConsistency()).toJson());
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
		ProjectResponse project = call(() -> client.createProject(request));
		return project;
	}

}
