package com.gentics.mesh.search.index.node;

import com.gentics.elasticsearch.client.HttpErrorException;
import com.gentics.elasticsearch.client.okhttp.RequestBuilder;
import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.ContainerType;
import com.gentics.mesh.core.data.Language;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.NodeContent;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.data.page.impl.DynamicStreamPageImpl;
import com.gentics.mesh.core.data.root.RootVertex;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.verticle.handler.HandlerUtilities;
import com.gentics.mesh.error.MeshConfigurationException;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.parameter.PagingParameters;
import com.gentics.mesh.search.SearchProvider;
import com.gentics.mesh.search.impl.SearchClient;
import com.gentics.mesh.search.index.AbstractSearchHandler;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.gentics.mesh.search.impl.ElasticsearchErrorHelper.mapError;
import static com.gentics.mesh.search.impl.ElasticsearchErrorHelper.mapToMeshError;

/**
 * Collection of handlers which are used to deal with search requests.
 */
@Singleton
public class NodeSearchHandler extends AbstractSearchHandler<Node, NodeResponse> {

	private static final Logger log = LoggerFactory.getLogger(NodeSearchHandler.class);

	private BootstrapInitializer boot;

	@Inject
	public NodeSearchHandler(SearchProvider searchProvider, Database db, NodeIndexHandler nodeIndexHandler, HandlerUtilities utils,
		BootstrapInitializer boot) {
		super(db, searchProvider, nodeIndexHandler);
		this.boot = boot;
	}

	/**
	 * Invoke the given query and return a page of node containers.
	 * 
	 * @param query
	 *            Elasticsearch query
	 * @param pagingInfo
	 * @return
	 * @throws MeshConfigurationException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Single<Page<NodeContent>> handleContainerSearch(InternalActionContext ac, String query, PagingParameters pagingInfo, Predicate<NodeContent> filter) {
		return db.asyncTx(() -> {
			RootVertex<Node> root = getIndexHandler().getRootVertex();
			ContainerType type = ContainerType.forVersion(ac.getVersioningParameters().getVersion());
			return searchNodes(ac, query)
				.flatMapMaybe(hit -> hit.toNodeContent(root, type, ac))
				.filter(filter)
				.to(pageCollector(pagingInfo));
		});
	}

	private <T> Function<Flowable<T>, Single<Page<T>>> pageCollector(PagingParameters pagingInfo) {
		long toSkip = pagingInfo.getPerPage() * (pagingInfo.getPage() - 1);
		return upstream -> upstream.toList().map()
	}

	private Flowable<Hit> searchNodes(InternalActionContext ac, String query) {
		SearchClient client = searchProvider.getClient();
		if (log.isDebugEnabled()) {
			log.debug("Invoking search with query {" + query + "} for {Containers}");
		}
		Set<String> indices = getIndexHandler().getSelectedIndices(ac);

		// Add permission checks to the query
		JsonObject queryJson = prepareSearchQuery(ac, query, true);

		// Apply paging
		// TODO use scroll api
		queryJson.put("size", 1000);

		// Only load the documentId we don't care about the indexed contents. The graph is our source of truth here.
		queryJson.put("_source", false);

		if (log.isDebugEnabled()) {
			log.debug("Using parsed query {" + queryJson.encodePrettily() + "}");
		}

		JsonObject queryOption = new JsonObject();
		queryOption.put("index", StringUtils.join(indices.stream().toArray(String[]::new), ","));
		queryOption.put("search_type", "dfs_query_then_fetch");
		log.debug("Using options {" + queryOption.encodePrettily() + "}");

		JsonObject response;
		try {
			RequestBuilder<JsonObject> searchRequest = client.multiSearch(queryOption, queryJson);
			response = searchRequest.sync();
		} catch (HttpErrorException e) {
			log.error("Error while processing query", e);
			throw mapToMeshError(e);
		}

		JsonArray responses = response.getJsonArray("responses");
		JsonObject firstResponse = responses.getJsonObject(0);

		// Process the nested error
		JsonObject errorInfo = firstResponse.getJsonObject("error");
		if (errorInfo != null) {
			throw mapError(errorInfo);
		}

		HitsInfo hitsInfo = new HitsInfo(firstResponse.getJsonObject("hits"));
	}

	private <T> Stream<T> toStream(Optional<T> opt) {
		return opt.map(Stream::of).orElseGet(Stream::empty);
	}

	private class HitsInfo {
		private final JsonObject info;

		public HitsInfo(JsonObject info) {
			this.info = info;
		}

		public Stream<Hit> streamHits() {
			return info.getJsonArray("hits").stream()
				.map(hit -> new Hit((JsonObject)hit));
		}
	}

	private class Hit {
		private final String language;
		private final String uuid;

		public Hit(String ESid) {
			int pos = ESid.indexOf("-");
			language = pos > 0 ? ESid.substring(pos + 1) : null;
			uuid = pos > 0 ? ESid.substring(0, pos) : ESid;
		}

		public Hit(JsonObject hit) {
			this(hit.getString("_id"));
		}

		public String getLanguage() {
			return language;
		}

		public String getUuid() {
			return uuid;
		}

		/**
		 * Maps an elasticsearch hit to a node content.
		 * This fetches info from the database and creates a transaction for that.
		 */
		public Maybe<NodeContent> toNodeContent(RootVertex<Node> root, ContainerType type, InternalActionContext ac) {
			return db.<Optional<NodeContent>>asyncTx(() -> {
				Node element = root.findByUuid(uuid);
				if (element == null) {
					log.warn("Object could not be found for uuid {" + uuid + "} in root vertex {" + root.getRootLabel() + "}");
					return Single.just(Optional.empty());
				}

				Language languageTag = boot.languageRoot().findByLanguageTag(language);
				if (languageTag == null) {
					log.warn("Could not find language {" + language + "}");
					return Single.just(Optional.empty());
				}

				// Locate the matching container and add it to the list of found containers
				NodeGraphFieldContainer container = element.getGraphFieldContainer(languageTag, ac.getRelease(), type);
				if (container != null) {
					return Single.just(Optional.of(new NodeContent(element, container)));
				} else {
					return Single.just(Optional.empty());
				}
			}).flatMapMaybe(opt -> opt.map(Maybe::just).orElseGet(Maybe::empty));
		}
	}
}
