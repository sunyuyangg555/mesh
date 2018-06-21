package com.gentics.mesh.core.admin;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestSize.FULL;

import org.junit.Test;

import com.gentics.mesh.core.rest.node.NodeDownloadResponse;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = true, inMemoryDB = false)
public class AdminEndpointSupportTest extends AbstractMeshTest {

	@Test
	public void testDumpDownload() {
		grantAdminRole();
		NodeDownloadResponse dump = call(() -> client().fetchSupportDump());
	}

}
