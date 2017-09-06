package com.gentics.mesh.test.context;

import static com.gentics.mesh.Events.JOB_WORKER_ADDRESS;
import static com.gentics.mesh.core.rest.admin.migration.MigrationStatus.IDLE;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.util.TestUtils.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Rule;

import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.MeshCoreVertex;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.data.search.IndexHandler;
import com.gentics.mesh.core.rest.admin.migration.MigrationInfo;
import com.gentics.mesh.core.rest.admin.migration.MigrationStatus;
import com.gentics.mesh.core.rest.admin.migration.MigrationStatusResponse;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.etc.RouterStorage;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.search.IndexHandlerRegistry;
import com.gentics.mesh.test.TestDataProvider;
import com.syncleus.ferma.tx.Tx;

import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.RoutingContext;
import rx.functions.Action0;

public abstract class AbstractMeshTest implements TestHelperMethods {

	static {
		// Use slf4j instead of jul
		System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
	}

	@Rule
	@ClassRule
	public static MeshTestContext testContext = new MeshTestContext();

	@Override
	public MeshTestContext getTestContext() {
		return testContext;
	}

	/**
	 * Drop all indices and create a new index using the current data.
	 * 
	 * @throws Exception
	 */
	protected void recreateIndices() throws Exception {
		// We potentially modified existing data thus we need to drop all indices and create them and reindex all data
		MeshInternal.get().searchProvider().clear();
		// We need to call init() again in order create missing indices for the created test data
		for (IndexHandler<?> handler : MeshInternal.get().indexHandlerRegistry().getHandlers()) {
			handler.init().await();
		}
		IndexHandlerRegistry registry = MeshInternal.get().indexHandlerRegistry();
		for (IndexHandler<?> handler : registry.getHandlers()) {
			handler.reindexAll().await();
		}
	}

	public String getJson(Node node) throws Exception {
		InternalActionContext ac = mockActionContext("lang=en&version=draft");
		ac.data().put(RouterStorage.PROJECT_CONTEXT_KEY, TestDataProvider.PROJECT_NAME);
		return JsonUtil.toJson(node.transformToRest(ac, 0).toBlocking().value());
	}

	protected void testPermission(GraphPermission perm, MeshCoreVertex<?, ?> element) {
		RoutingContext rc = tx(() -> mockRoutingContext());

		try (Tx tx = tx()) {
			role().grantPermissions(element, perm);
			tx.success();
		}

		try (Tx tx = tx()) {
			assertTrue("The role {" + role().getName() + "} does not grant permission on element {" + element.getUuid()
					+ "} although we granted those permissions.", role().hasPermission(perm, element));
			assertTrue("The user has no {" + perm.getRestPerm().getName() + "} permission on node {" + element.getUuid() + "/"
					+ element.getClass().getSimpleName() + "}", getRequestUser().hasPermission(element, perm));
		}

		try (Tx tx = tx()) {
			role().revokePermissions(element, perm);
			rc.data().clear();
			tx.success();
		}

		try (Tx tx = tx()) {
			boolean hasPerm = role().hasPermission(perm, element);
			assertFalse("The user's role {" + role().getName() + "} still got {" + perm.getRestPerm().getName() + "} permission on node {"
					+ element.getUuid() + "/" + element.getClass().getSimpleName() + "} although we revoked it.", hasPerm);

			hasPerm = getRequestUser().hasPermission(element, perm);
			assertFalse("The user {" + getRequestUser().getUsername() + "} still got {" + perm.getRestPerm().getName() + "} permission on node {"
					+ element.getUuid() + "/" + element.getClass().getSimpleName() + "} although we revoked it.", hasPerm);
		}
	}

	/**
	 * Return the graphql query for the given name.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected String getGraphQLQuery(String name) throws IOException {
		return IOUtils.toString(getClass().getResourceAsStream("/graphql/" + name));
	}

	/**
	 * Return the es query for the given name.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected String getESQuery(String name) throws IOException {
		return IOUtils.toString(getClass().getResourceAsStream("/elasticsearch/" + name));
	}

	/**
	 * Execute the action and check that the migration is executed and yields the given status.
	 * 
	 * @param action
	 * @param status
	 * @return Migration status
	 */
	protected MigrationStatusResponse waitForMigration(Action0 action, MigrationStatus status) {
		// Load a status just before the action
		MigrationStatusResponse before = call(() -> client().migrationStatus());

		// Invoke the action
		action.call();

		// Now poll the migration status and check the response
		final int MAX_WAIT = 120;
		for (int i = 0; i < MAX_WAIT; i++) {
			MigrationStatusResponse response = call(() -> client().migrationStatus());
			MigrationStatus currentStatus = response.getStatus();
			if (currentStatus == IDLE && response.getMigrations().size() > before.getMigrations().size()) {
				if (status != null) {
					for (MigrationInfo info : response.getMigrations()) {
						assertEquals("One migration did not finish {\n" + info.toJson() + "\n} with the expected status.", status, info.getStatus());
					}
				}
				return response;
			}
			if (i > 30) {
				System.out.println(response.toJson());
			}
			if (i == MAX_WAIT) {
				throw new RuntimeException("Migration did not complete within " + MAX_WAIT + " seconds");
			}
			sleep(1000);
		}
		return null;
	}

	/**
	 * Inform the job worker that new jobs have been enqueued and block until all jobs complete or the timeout has been reached.
	 */
	protected MigrationStatusResponse triggerAndWaitForMigration() {
		return triggerAndWaitForMigration(MigrationStatus.COMPLETED);
	}

	/**
	 * Inform the job worker that new jobs are enqueued and check the migration status. This method will block until the migration finishes or a timeout has
	 * been reached.
	 * 
	 * @param status
	 *            Expected status for all migrations
	 */
	protected MigrationStatusResponse triggerAndWaitForMigration(MigrationStatus status) {
		return waitForMigration(() -> {
			vertx().eventBus().send(JOB_WORKER_ADDRESS, null);
		}, status);
	}

}
