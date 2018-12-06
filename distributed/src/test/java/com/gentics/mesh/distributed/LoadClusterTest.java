package com.gentics.mesh.distributed;

import static com.gentics.mesh.test.ClientHelper.call;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.gentics.mesh.core.rest.admin.cluster.ClusterInstanceInfo;
import com.gentics.mesh.core.rest.admin.cluster.ClusterStatusResponse;
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
import com.gentics.mesh.rest.client.MeshRestClient;

public class LoadClusterTest extends AbstractTwoNodeClusterTest {

	public static final String PROJECT_NAME = "test";

	public Long createTimer;

	public Long schemaTimer;

	public Long deleteTimer;

	@Test
	public void testClusterStatus() throws Exception {
		ClusterStatusResponse response = call(() -> clientA.clusterStatus());
		assertThat(response.getInstances()).hasSize(2);
		ClusterInstanceInfo first = response.getInstances().get(0);
		assertEquals("ONLINE", first.getStatus());
		ClusterInstanceInfo second = response.getInstances().get(1);
		assertEquals("ONLINE", second.getStatus());

		setAPIToken(clientA, clientB);

		ProjectResponse project = setupProject();
		SchemaListResponse schemaList = call(() -> clientA.findSchemas());
		SchemaResponse folderSchema = schemaList.getData().stream().filter(s -> s.getName().equals("folder")).findFirst().get();
		String json = JsonUtil.toJson(folderSchema);
		String schemaUuid = folderSchema.getUuid();
		AtomicBoolean stopFlag = new AtomicBoolean(false);

		// Client A - Create Nodes
		AtomicLong counter = new AtomicLong();
		Stack<String> nodeUuids = new Stack<>();
		createTimer = vertx.setPeriodic(500, ch -> {
			long count = counter.getAndIncrement();
			NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
			nodeCreateRequest.setSchemaName("folder");
			nodeCreateRequest.setParentNodeUuid(project.getRootNode().getUuid());
			nodeCreateRequest.getFields().put("slug", new StringFieldImpl().setString("test_" + count));
			nodeCreateRequest.getFields().put("name", new StringFieldImpl().setString("Name " + count));
			nodeCreateRequest.setLanguage("en");
			clientA.createNode(PROJECT_NAME, nodeCreateRequest).toSingle().subscribe((node) -> {
				nodeUuids.add(node.getUuid());
				System.out.println("Created node " + count);
			}, err -> stopWorld());
		});

//		// Client B - Delete nodes
//		Thread.sleep(5000);
//		deleteTimer = vertx.setPeriodic(750, rh -> {
//			if (nodeUuids.isEmpty()) {
//				return;
//			}
//			String uuid = nodeUuids.pop();
//			clientB.deleteNode(PROJECT_NAME, uuid).toCompletable().subscribe(() -> {
//				System.out.println("Node Deleted " + uuid);
//			}, err -> stopWorld());
//		});

		// Client B - Update schema
		AtomicInteger schemaCounter = new AtomicInteger();
		schemaTimer = vertx.setPeriodic(10 * 1000, ch -> {
			SchemaUpdateRequest request = JsonUtil.readValue(json, SchemaUpdateRequest.class);
			request.setDescription("Test" + schemaCounter.getAndIncrement());
			System.out.println("Updating schema: " + schemaCounter.get());
			clientB.updateSchema(schemaUuid, request).toCompletable().subscribe(() -> {
				System.out.println("Updated schema: " + schemaCounter.get());
			}, err -> stopWorld());
		});

		// // Client B - Check consistency & list nodes
		// AtomicInteger inconsistencyCounter = new AtomicInteger(0);
		// vertx.setPeriodic(1000, ch -> {
		// clientB.findNodes(PROJECT_NAME, new PagingParametersImpl().setPerPage(10L)).toSingle().subscribe(list -> {
		// System.out.println("Total: " + list.getMetainfo().getTotalCount());
		// });
		//
		// clientB.checkConsistency().toSingle().subscribe(result -> {
		// System.out.println("Consistency: " + result.getResult());
		// if (result.getResult().equals(ConsistencyRating.INCONSISTENT)) {
		// if (inconsistencyCounter.getAndIncrement() > 100) {
		// stopFlag.set(true);
		// System.out.println("INCONSISTENCY FOUND - STOPPING");
		// System.out.println(result.toJson());
		// vertx.cancelTimer(ch);
		//// vertx.cancelTimer(schemaTimer);
		//// vertx.cancelTimer(createTimer);
		// } else {
		// System.out.println("Counter: " + inconsistencyCounter.get());
		//
		// }
		// }
		// });
		// });

		System.in.read();
		stopWorld();
		// System.out.println(call(() -> clientA.checkConsistency()).toJson());
		// System.in.read();
		// System.out.println(call(() -> clientB.checkConsistency()).toJson());
		System.in.read();

	}

	private void stopWorld() {
		if (schemaTimer != null) {
			vertx.cancelTimer(schemaTimer);
		}
		if (createTimer != null) {
			vertx.cancelTimer(createTimer);
		}
		if (deleteTimer != null) {
			vertx.cancelTimer(deleteTimer);
		}
	}

	private void setAPIToken(MeshRestClient clientA, MeshRestClient clientB) {
		UserResponse me = call(() -> clientA.me());
		UserAPITokenResponse token = call(() -> clientA.issueAPIToken(me.getUuid()));
		clientA.setAPIKey(token.getToken());
		clientB.setAPIKey(token.getToken());
	}

	private ProjectResponse setupProject() {
		ProjectCreateRequest request = new ProjectCreateRequest();
		request.setName(PROJECT_NAME);
		request.setSchemaRef("folder");
		ProjectResponse project = call(() -> clientA.createProject(request));
		return project;
	}

}
