package com.gentics.mesh.distributed;

import static com.gentics.mesh.util.TokenUtil.randomToken;
import static com.gentics.mesh.util.UUIDUtil.randomUUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;

import com.gentics.mesh.context.impl.LoggingConfigurator;
import com.gentics.mesh.distributed.containers.MeshDockerServer;
import com.gentics.mesh.rest.client.MeshRestClient;

public abstract class AbstractTwoNodeClusterTest extends AbstractClusterTest {

	private static String clusterPostFix = randomUUID();

	public static MeshDockerServer serverA = new MeshDockerServer(vertx)
		.withClusterName("dockerCluster" + clusterPostFix)
		.withNodeName("nodeA")
		.withDataPathPostfix(randomToken())
		.withInitCluster()
		.waitForStartup()
		.withClearFolders();

	public static MeshDockerServer serverB = new MeshDockerServer(vertx)
		.withClusterName("dockerCluster" + clusterPostFix)
		.withNodeName("nodeB")
		.withDataPathPostfix(randomToken())
		.withClearFolders();

	public static MeshRestClient clientA;
	public static MeshRestClient clientB;

	@ClassRule
	public static RuleChain chain = RuleChain.outerRule(serverB).around(serverA);

	@BeforeClass
	public static void waitForNodes() throws InterruptedException {
		LoggingConfigurator.init();
		serverB.awaitStartup(200);
		clientA = serverA.client();
		clientB = serverB.client();
	}

	@Before
	public void setupLogin() {
		clientA.setLogin("admin", "admin");
		clientA.login().blockingGet();
		clientB.setLogin("admin", "admin");
		clientB.login().blockingGet();
	}

}
