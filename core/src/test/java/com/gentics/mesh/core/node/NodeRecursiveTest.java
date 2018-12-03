package com.gentics.mesh.core.node;

import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.role.RolePermissionRequest;
import com.gentics.mesh.core.rest.role.RoleResponse;
import com.gentics.mesh.parameter.impl.DeleteParametersImpl;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import org.junit.Before;
import org.junit.Test;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.test.TestSize.FULL;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = true)
public class NodeRecursiveTest extends AbstractMeshTest {
	NodeResponse folder;
	@Before
	public void setUpNodes() {
		folder = createFolder("toBeDeleted");
		int nodeCount = 10000;
		for (int i = 0; i < nodeCount; i++) {
			createFolder("node" + i, folder);
		}
	}

	@Test
	public void deleteNodes() {
		call(() -> client().deleteNode(PROJECT_NAME, folder.getUuid(), new DeleteParametersImpl().setRecursive(true)));
	}

	@Test
	public void assignPermissions() {
		RoleResponse role = createRole("someRole");
		RolePermissionRequest permissions = new RolePermissionRequest();
		permissions.getPermissions().setOthers(true);
		permissions.setRecursive(true);
		call(() -> client().updateRolePermissions(role.getUuid(), String.format(
			"/projects/%s/nodes/%s", projectUuid(), folder.getUuid()
		), permissions));
	}
}
