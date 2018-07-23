package com.gentics.mesh.core.node;

import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_CONTAINER_PATH;
import static com.gentics.mesh.test.TestSize.FULL;

import org.junit.Test;

import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.container.impl.NodeGraphFieldContainerImpl;
import com.gentics.mesh.core.data.impl.ContainerPathEdgeImpl;
import com.gentics.mesh.core.data.node.ContainerPathEdge;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.impl.NodeImpl;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.syncleus.ferma.tx.Tx;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = false)
public class NodeGraphFieldContainerTest extends AbstractMeshTest {

	@Test(expected = ORecordDuplicatedException.class)
	public void testConflictingWebRootPath() {
		try (Tx tx = tx()) {
			Node node = tx.getGraph().addFramedVertex(NodeImpl.class);
			NodeGraphFieldContainer containerA = tx.getGraph().addFramedVertex(NodeGraphFieldContainerImpl.class);
			NodeGraphFieldContainer containerB = tx.getGraph().addFramedVertex(NodeGraphFieldContainerImpl.class);
			ContainerPathEdge n1 = node.addFramedEdge(HAS_CONTAINER_PATH, containerA, ContainerPathEdgeImpl.class);
			ContainerPathEdge n2 = node.addFramedEdge(HAS_CONTAINER_PATH, containerB, ContainerPathEdgeImpl.class);
			n1.setWebRootSegment("test");
			n2.setWebRootSegment("test");
			tx.success();
		}
	}

	@Test(expected = ORecordDuplicatedException.class)
	public void testPerBranchUniqness() {
		try (Tx tx = tx()) {
			Node node = tx.getGraph().addFramedVertex(NodeImpl.class);
			NodeGraphFieldContainer containerA = tx.getGraph().addFramedVertex(NodeGraphFieldContainerImpl.class);
			ContainerPathEdge n1 = node.addFramedEdge(HAS_CONTAINER_PATH, containerA, ContainerPathEdgeImpl.class);
			ContainerPathEdge n2 = node.addFramedEdge(HAS_CONTAINER_PATH, containerA, ContainerPathEdgeImpl.class);
			n1.setBranchUuid("branchUuid");
			n1.setWebRootSegment("test");
			n2.setBranchUuid("branchUuid");
			n2.setWebRootSegment("test2");
			tx.success();
		}
	}

	@Test(expected = ORecordDuplicatedException.class)
	public void testConflictingPublishWebRootPath() {
		try (Tx tx = tx()) {
			Node node = tx.getGraph().addFramedVertex(NodeImpl.class);
			NodeGraphFieldContainer containerA = tx.getGraph().addFramedVertex(NodeGraphFieldContainerImpl.class);
			NodeGraphFieldContainer containerB = tx.getGraph().addFramedVertex(NodeGraphFieldContainerImpl.class);
			ContainerPathEdge n1 = node.addFramedEdge(HAS_CONTAINER_PATH, containerA, ContainerPathEdgeImpl.class);
			ContainerPathEdge n2 = node.addFramedEdge(HAS_CONTAINER_PATH, containerB, ContainerPathEdgeImpl.class);
			n1.setPublishedWebRootSegment("test");
			n2.setPublishedWebRootSegment("test");
			tx.success();
		}
	}

	public void checkConsistency() {
		// don't run any checks
	}
}
