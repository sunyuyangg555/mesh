package com.gentics.mesh.core.user;

import static com.gentics.mesh.test.TestSize.PROJECT_AND_NODE;

import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

@MeshTestSetting(useElasticsearch = false, testSize = PROJECT_AND_NODE, startServer = true)
public class UserRoleEndpointTest extends AbstractMeshTest {

	public void testReadRoleList() {

	}

	public void testReadRoleListWithNoRolePerm() {

	}

	public void testReadRoleListWithNoUserPerm() {

	}

	public void testAddRoleToUser() {

	}

	public void testUserRolePerm() {

		// 1. Add role -> check perm
		// 2. Remove role -> check perm

	}

	public void testAddBogusRoleToUser() {

	}

	public void testAddRoleToUserWithNoPermOnUser() {

	}

	public void testRemoveRoleFromUser() {

	}

	public void testRemoveBogusRoleFromUser() {

	}
}
