package com.gentics.mesh.core.admin;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestSize.FULL;

import org.junit.Test;

import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = true, inMemoryDB = true)
public class AdminCheckDatabaseTest extends AbstractMeshTest {

	@Test
	public void testCheckDatabase() {
		call(() -> client().checkDatabase());
	}
}
