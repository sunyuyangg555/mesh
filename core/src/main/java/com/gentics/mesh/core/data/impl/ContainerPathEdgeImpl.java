package com.gentics.mesh.core.data.impl;

import static com.gentics.mesh.core.data.node.ContainerPathEdge.composeIndexName;
import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_CONTAINER_PATH;
import static com.gentics.mesh.graphdb.spi.FieldType.LINK;
import static com.gentics.mesh.graphdb.spi.FieldType.STRING;
import static com.gentics.mesh.graphdb.spi.FieldType.STRING_SET;

import java.util.Iterator;

import com.gentics.mesh.core.data.ContainerType;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.container.impl.NodeGraphFieldContainerImpl;
import com.gentics.mesh.core.data.node.ContainerPathEdge;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.impl.NodeImpl;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.graphdb.spi.FieldMap;
import com.syncleus.ferma.AbstractEdgeFrame;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.tx.Tx;

@GraphElement
public class ContainerPathEdgeImpl extends AbstractEdgeFrame implements ContainerPathEdge {

	public static void init(Database db) {
		db.addEdgeType(ContainerPathEdgeImpl.class.getSimpleName());
		db.addEdgeType(HAS_CONTAINER_PATH, ContainerPathEdgeImpl.class);

		FieldMap fields = new FieldMap();
		fields.put("in", LINK);
		fields.put("out", LINK);
		fields.put(BRANCH_UUID_KEY, STRING);
		db.addCustomEdgeIndex(HAS_CONTAINER_PATH, "uniqueness", fields, true);

		// Webroot index:
		fields = new FieldMap();
		fields.put(BRANCH_UUID_KEY, STRING);
		fields.put(WEBROOT_PROPERTY_KEY, STRING);
		db.addCustomEdgeIndex(HAS_CONTAINER_PATH, WEBROOT_INDEX_NAME, fields, true);
		fields = new FieldMap();
		fields.put(BRANCH_UUID_KEY, STRING);
		fields.put(PUBLISHED_WEBROOT_PROPERTY_KEY, STRING);
		db.addCustomEdgeIndex(HAS_CONTAINER_PATH, PUBLISHED_WEBROOT_INDEX_NAME, fields, true);

		// Webroot url field index:
		fields = new FieldMap();
		fields.put(BRANCH_UUID_KEY, STRING);
		fields.put(WEBROOT_URLFIELD_PROPERTY_KEY, STRING_SET);
		db.addCustomEdgeIndex(HAS_CONTAINER_PATH, WEBROOT_URLFIELD_INDEX_NAME, fields, true);
		fields = new FieldMap();
		fields.put(BRANCH_UUID_KEY, STRING);
		fields.put(PUBLISHED_WEBROOT_URLFIELD_PROPERTY_KEY, STRING_SET);
		db.addCustomEdgeIndex(HAS_CONTAINER_PATH, PUBLISHED_WEBROOT_URLFIELD_INDEX_NAME,
			fields, true);
	}

	/**
	 * Invoke an index lookup to locate edges with the given parameters.
	 * 
	 * @param segment
	 * @param branchUuid
	 * @return Found conflict of null if no conflict has been found
	 */
	public static ContainerPathEdge lookup(ContainerPathEdge edge, ContainerType type, String segment, String branchUuid, Node parent) {
		Database db = MeshInternal.get().database();
		Object key = db.createComposedIndexKey(branchUuid, segment);
		String indexName = composeIndexName(type);
		if (edge != null) {
			// composeWebrootIndexKey(segment, branchUuid, parent)
			return db.checkIndexUniqueness(indexName, edge, key);
		} else {
			FramedGraph graph = Tx.getActive().getGraph();
			// String key = ContainerPathEdge.composeWebrootIndexKey(segment, branchUuid, this);
			Iterator<? extends ContainerPathEdge> edges = graph.getFramedEdges(indexName, key, ContainerPathEdgeImpl.class).iterator();
			if (edges.hasNext()) {
				return edges.next();
			} else {
				return null;
			}
		}
	}

	@Override
	public NodeGraphFieldContainer getContainer() {
		return inV().nextOrDefault(NodeGraphFieldContainerImpl.class, null);
	}

	@Override
	public Node getNode() {
		return outV().nextOrDefault(NodeImpl.class, null);
	}

	@Override
	public String getBranchUuid() {
		return getProperty(BRANCH_UUID_KEY);
	}

	@Override
	public void setBranchUuid(String branchUuid) {
		setProperty(BRANCH_UUID_KEY, branchUuid);
	}

	@Override
	public void setWebRootSegment(String segment) {
		setProperty(WEBROOT_PROPERTY_KEY, segment);
	}

	@Override
	public void setPublishedWebRootSegment(String segment) {
		setProperty(PUBLISHED_WEBROOT_PROPERTY_KEY, segment);
	}

}
