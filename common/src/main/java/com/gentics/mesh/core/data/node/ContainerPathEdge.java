package com.gentics.mesh.core.data.node;

import static com.gentics.mesh.core.data.ContainerType.PUBLISHED;
import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_CONTAINER_PATH;

import com.gentics.mesh.core.data.ContainerType;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.syncleus.ferma.EdgeFrame;

public interface ContainerPathEdge extends EdgeFrame {

	String BRANCH_UUID_KEY = "branchUuid";

	// Webroot index

	String WEBROOT_PROPERTY_KEY = "webrootPathInfo";

	String WEBROOT_INDEX_NAME = "webrootPathInfoIndex";

	String PUBLISHED_WEBROOT_PROPERTY_KEY = "publishedWebrootPathInfo";

	String PUBLISHED_WEBROOT_INDEX_NAME = "publishedWebrootPathInfoIndex";

	// Url Field index

	String WEBROOT_URLFIELD_PROPERTY_KEY = "webrootUrlInfo";

	String WEBROOT_URLFIELD_INDEX_NAME = "webrootUrlInfoIndex";

	String PUBLISHED_WEBROOT_URLFIELD_PROPERTY_KEY = "publishedWebrootUrlInfo";

	String PUBLISHED_WEBROOT_URLFIELD_INDEX_NAME = "publishedWebrootInfoIndex";

	// default void defaultClearDraftPaths() {
	// setProperty(WEBROOT_PROPERTY_KEY, null);
	// setProperty(WEBROOT_URLFIELD_PROPERTY_KEY, null);
	// }

	/**
	 * Return the node from which this edge originates.
	 * 
	 * @return
	 */
	Node getNode();

	NodeGraphFieldContainer getContainer();

	void setBranchUuid(String branchUuid);

	String getBranchUuid();

	void setWebRootSegment(String segment);

	void setPublishedWebRootSegment(String segment);

	static String composeIndexName(ContainerType type) {
		String indexName = type == PUBLISHED ? PUBLISHED_WEBROOT_INDEX_NAME : WEBROOT_INDEX_NAME;
		indexName = "e." + HAS_CONTAINER_PATH + "_" + indexName;
		return indexName.toLowerCase();
	}

	static String composeUrlFieldIndexName(ContainerType type) {
		String index = type == PUBLISHED ? PUBLISHED_WEBROOT_URLFIELD_INDEX_NAME : WEBROOT_URLFIELD_INDEX_NAME;

		// Prefix each path with the branch uuid in order to scope the paths by branch
		String indexKey = "e." + HAS_CONTAINER_PATH + "." + index;
		return indexKey.toLowerCase();
	}

	/**
	 * Creates the key for the webroot index.
	 *
	 * @param segmentValue
	 *            Value of the segment field
	 * @param branchUuid
	 *            Uuid of the branch
	 * @param parent
	 *            Parent of the node to which the container belongs
	 * @return The composed key
	 */
	static String composeWebrootIndexKey(String segmentValue, String branchUuid, Node parent) {
		StringBuilder webRootInfo = new StringBuilder(segmentValue);
		webRootInfo.append("-").append(branchUuid);
		if (parent != null) {
			webRootInfo.append("-").append(parent.getUuid());
		}
		return webRootInfo.toString();
	}

}
