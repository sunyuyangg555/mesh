package com.gentics.mesh.core.graphql;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Vector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gentics.mesh.FieldUtil;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.binary.Binary;
import com.gentics.mesh.core.data.node.Micronode;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.field.list.BooleanGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.DateGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.HtmlGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.MicronodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NumberGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.StringGraphFieldList;
import com.gentics.mesh.core.data.node.field.nesting.MicronodeGraphField;
import com.gentics.mesh.core.data.schema.MicroschemaContainer;
import com.gentics.mesh.core.data.schema.SchemaContainer;
import com.gentics.mesh.core.rest.graphql.GraphQLResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaResponse;
import com.gentics.mesh.core.rest.node.FieldMap;
import com.gentics.mesh.core.rest.node.FieldMapImpl;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.HtmlField;
import com.gentics.mesh.core.rest.node.field.StringField;
import com.gentics.mesh.core.rest.node.field.image.FocalPoint;
import com.gentics.mesh.core.rest.node.field.impl.HtmlFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.HtmlFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.StringFieldListImpl;
import com.gentics.mesh.core.rest.schema.BinaryFieldSchema;
import com.gentics.mesh.core.rest.schema.BooleanFieldSchema;
import com.gentics.mesh.core.rest.schema.DateFieldSchema;
import com.gentics.mesh.core.rest.schema.HtmlFieldSchema;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.MicronodeFieldSchema;
import com.gentics.mesh.core.rest.schema.NodeFieldSchema;
import com.gentics.mesh.core.rest.schema.NumberFieldSchema;
import com.gentics.mesh.core.rest.schema.SchemaModel;
import com.gentics.mesh.core.rest.schema.StringFieldSchema;
import com.gentics.mesh.core.rest.schema.impl.BinaryFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.BooleanFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.DateFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.HtmlFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.MicronodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaReferenceImpl;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.core.rest.user.NodeReference;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.parameter.impl.PublishParametersImpl;
import com.gentics.mesh.parameter.impl.VersioningParametersImpl;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.gentics.madl.tx.Tx;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.json.JsonObject;

@RunWith(Parameterized.class)
@MeshTestSetting(useElasticsearch = false, testSize = TestSize.FULL, startServer = true)
public class GraphQLEndpointTest extends AbstractMeshTest {

	private static final String CONTENT_UUID = "43ee8f9ff71e4016ae8f9ff71e10161c";
	private static final String FOLDER_SCHEMA_UUID = "70bf14ed1267446eb70c5f02cfec0e38";
	private static final String NODE_WITH_LINKS_UUID = "8d2f5769fe114353af5769fe11e35355";
	private static final String NODE_WITH_NODE_REF_UUID = "e8f5c7875b2f49a7b5c7875b2fa9a718";
	private static final String NEWS_UUID = "4b1346a2163a4ff89346a2163a9ff883";

	private final String queryName;

	private final boolean withMicroschema;

	private final String version;

	public GraphQLEndpointTest(String queryName, boolean withMicroschema, String version) {
		this.queryName = queryName;
		this.withMicroschema = withMicroschema;
		this.version = version;
	}

	@Parameters(name = "query={0},version={2}")
	public static Collection<Object[]> paramData() {
		Collection<Object[]> testData = new Vector<>();
		testData.add(new Object[] { "full-query", true, "draft" });
		testData.add(new Object[] { "role-user-group-query", true, "draft" });
		testData.add(new Object[] { "group-query", true, "draft" });
		testData.add(new Object[] { "schema-query", true, "draft" });
		// testData.add(new Object[] { "schema-projects-query", true, "draft" });
		testData.add(new Object[] { "microschema-query", true, "draft" });
		testData.add(new Object[] { "paging-query", true, "draft" });
		testData.add(new Object[] { "tagFamily-query", true, "draft" });
		testData.add(new Object[] { "node-query", true, "draft" });
		testData.add(new Object[] { "node-tag-query", true, "draft" });
		testData.add(new Object[] { "nodes-query", true, "draft" });
		testData.add(new Object[] { "node-breadcrumb-query", true, "draft" });
		testData.add(new Object[] { "node-language-fallback-query", true, "draft" });
		testData.add(new Object[] { "node-languages-query", true, "draft" });
		testData.add(new Object[] { "node-webroot-query", true, "draft" });
		testData.add(new Object[] { "node-webroot-urlfield-query", true, "draft" });
		testData.add(new Object[] { "node-relations-query", true, "draft" });
		testData.add(new Object[] { "node-fields-query", true, "draft" });
		testData.add(new Object[] { "node-fields-no-microschema-query", false, "draft" });
		testData.add(new Object[] { "node/link/webroot", true, "draft" });
		testData.add(new Object[] { "node/link/children", true, "draft" });
		testData.add(new Object[] { "node/link/webroot-language", true, "draft" });
		testData.add(new Object[] { "node/link/reference", true, "draft" });
		testData.add(new Object[] { "node-field-list-path-query", true, "draft" });
		testData.add(new Object[] { "project-query", true, "draft" });
		testData.add(new Object[] { "tag-query", true, "draft" });
		testData.add(new Object[] { "branch-query", true, "draft" });
		testData.add(new Object[] { "user-query", true, "draft" });
		testData.add(new Object[] { "mesh-query", true, "draft" });
		testData.add(new Object[] { "microschema-projects-query", true, "draft" });
		testData.add(new Object[] { "node-version-published-query", true, "published" });
		testData.add(new Object[] { "filtering/children", true, "draft" });
		testData.add(new Object[] { "filtering/nodes", true, "draft" });
		testData.add(new Object[] { "filtering/nodes-en", true, "draft" });
		testData.add(new Object[] { "filtering/nodes-jp", true, "draft" });
		testData.add(new Object[] { "filtering/nodes-creator-editor", true, "draft" });
		testData.add(new Object[] { "filtering/users", true, "draft" });
		testData.add(new Object[] { "filtering/groups", true, "draft" });
		testData.add(new Object[] { "filtering/roles", true, "draft" });
		testData.add(new Object[] { "node/breadcrumb-root", true, "draft" });
		return testData;
	}

	@Test
	public void testNodeQuery() throws Exception {
		String microschemaUuid = null;
		if (withMicroschema) {
			// 1. Create the microschema
			MicroschemaCreateRequest microschemaRequest = new MicroschemaCreateRequest();
			microschemaRequest.setName("TestMicroschema");
			microschemaRequest.addField(FieldUtil.createStringFieldSchema("text"));
			microschemaRequest.addField(FieldUtil.createNodeFieldSchema("nodeRef").setAllowedSchemas("content"));
			microschemaRequest.addField(FieldUtil.createListFieldSchema("nodeList", "node"));
			MicroschemaResponse microschemaResponse = call(() -> client().createMicroschema(microschemaRequest));
			microschemaUuid = microschemaResponse.getUuid();
			call(() -> client().assignMicroschemaToProject(PROJECT_NAME, microschemaResponse.getUuid()));
		} else {
			try (Tx tx = db().tx()) {
				for (MicroschemaContainer microschema : meshRoot().getMicroschemaContainerRoot().findAll()) {
					microschema.remove();
				}
				tx.success();
			}
		}

		try (Tx tx = tx()) {
			Node node = folder("2015");
			Node folder = folder("news");
			folder.setUuid(NEWS_UUID);
			folder.getGraphFieldContainer("de").updateWebrootPathInfo(initialBranchUuid(), null);
			folder.getGraphFieldContainer("de").updateWebrootPathInfo(initialBranchUuid(), null);

			Node node2 = content();
			node2.setUuid(CONTENT_UUID);
			node2.getGraphFieldContainer("en").updateWebrootPathInfo(initialBranchUuid(), null);
			node2.getGraphFieldContainer("de").updateWebrootPathInfo(initialBranchUuid(), null);
			Node node3 = folder("2014");

			// Update the folder schema to contain all fields
			SchemaContainer schemaContainer = schemaContainer("folder");
			schemaContainer.setUuid(FOLDER_SCHEMA_UUID);
			SchemaModel schema = schemaContainer.getLatestVersion().getSchema();
			schema.setUrlFields("niceUrl");
			NodeFieldSchema nodeFieldSchema = new NodeFieldSchemaImpl();
			nodeFieldSchema.setName("nodeRef");
			nodeFieldSchema.setLabel("Some label");
			nodeFieldSchema.setAllowedSchemas("folder");
			schema.addField(nodeFieldSchema);

			BinaryFieldSchema binaryFieldSchema = new BinaryFieldSchemaImpl();
			binaryFieldSchema.setName("binary");
			schema.addField(binaryFieldSchema);

			NumberFieldSchema numberFieldSchema = new NumberFieldSchemaImpl();
			numberFieldSchema.setName("number");
			schema.addField(numberFieldSchema);

			DateFieldSchema dateFieldSchema = new DateFieldSchemaImpl();
			dateFieldSchema.setName("date");
			schema.addField(dateFieldSchema);

			HtmlFieldSchema htmlFieldSchema = new HtmlFieldSchemaImpl();
			htmlFieldSchema.setName("html");
			schema.addField(htmlFieldSchema);

			HtmlFieldSchema htmlLinkFieldSchema = new HtmlFieldSchemaImpl();
			htmlLinkFieldSchema.setName("htmlLink");
			schema.addField(htmlLinkFieldSchema);

			StringFieldSchema stringFieldSchema = new StringFieldSchemaImpl();
			stringFieldSchema.setName("string");
			schema.addField(stringFieldSchema);

			StringFieldSchema niceUrlFieldSchema = new StringFieldSchemaImpl();
			niceUrlFieldSchema.setName("niceUrl");
			schema.addField(niceUrlFieldSchema);

			StringFieldSchema stringLinkFieldSchema = new StringFieldSchemaImpl();
			stringLinkFieldSchema.setName("stringLink");
			schema.addField(stringLinkFieldSchema);

			BooleanFieldSchema booleanFieldSchema = new BooleanFieldSchemaImpl();
			booleanFieldSchema.setName("boolean");
			schema.addField(booleanFieldSchema);

			ListFieldSchema stringListSchema = new ListFieldSchemaImpl();
			stringListSchema.setListType("string");
			stringListSchema.setName("stringList");
			schema.addField(stringListSchema);

			ListFieldSchema dateListSchema = new ListFieldSchemaImpl();
			dateListSchema.setListType("date");
			dateListSchema.setName("dateList");
			schema.addField(dateListSchema);

			ListFieldSchema nodeListSchema = new ListFieldSchemaImpl();
			nodeListSchema.setListType("node");
			nodeListSchema.setName("nodeList");
			schema.addField(nodeListSchema);

			ListFieldSchema htmlListSchema = new ListFieldSchemaImpl();
			htmlListSchema.setListType("html");
			htmlListSchema.setName("htmlList");
			schema.addField(htmlListSchema);

			ListFieldSchema booleanListSchema = new ListFieldSchemaImpl();
			booleanListSchema.setListType("boolean");
			booleanListSchema.setName("booleanList");
			schema.addField(booleanListSchema);

			ListFieldSchema numberListSchema = new ListFieldSchemaImpl();
			numberListSchema.setListType("number");
			numberListSchema.setName("numberList");
			schema.addField(numberListSchema);

			ListFieldSchema micronodeListSchema = new ListFieldSchemaImpl();
			micronodeListSchema.setListType("micronode");
			micronodeListSchema.setName("micronodeList");
			schema.addField(micronodeListSchema);

			if (withMicroschema) {
				MicronodeFieldSchema micronodeFieldSchema = new MicronodeFieldSchemaImpl();
				micronodeFieldSchema.setAllowedMicroSchemas("vcard");
				micronodeFieldSchema.setName("micronode");
				schema.addField(micronodeFieldSchema);
			}
			schemaContainer("folder").getLatestVersion().setSchema(schema);

			// Setup some test data
			NodeGraphFieldContainer container = node.getGraphFieldContainer("en");

			// node
			container.createNode("nodeRef", node2);

			// number
			container.createNumber("number").setNumber(42.1);

			// date
			long milisec = dateToMilis("2012-07-11 10:55:21");
			container.createDate("date").setDate(milisec);

			// html
			container.createHTML("html").setHtml("some html");

			// htmlLink
			container.createHTML("htmlLink").setHtml("Link: {{mesh.link(\"" + CONTENT_UUID + "\", \"en\")}}");

			// string
			container.createString("string").setString("some string");

			// niceUrl
			container.createString("niceUrl").setString("/some/url");

			// stringLink
			container.createString("stringLink").setString("Link: {{mesh.link(\"" + CONTENT_UUID + "\", \"en\")}}");

			// boolean
			container.createBoolean("boolean").setBoolean(true);

			// binary
			Binary binary = MeshInternal.get().boot().binaryRoot().create("hashsumvalue", 1L);
			binary.setImageHeight(10).setImageWidth(20).setSize(2048);
			container.createBinary("binary", binary).setImageDominantColor("00FF00")
				.setMimeType("image/jpeg").setImageFocalPoint(new FocalPoint(0.2f, 0.3f));

			// stringList
			StringGraphFieldList stringList = container.createStringList("stringList");
			stringList.createString("A");
			stringList.createString("B");
			stringList.createString("C");
			stringList.createString("D Link: {{mesh.link(\"" + CONTENT_UUID + "\", \"en\")}}");

			// htmlList
			HtmlGraphFieldList htmlList = container.createHTMLList("htmlList");
			htmlList.createHTML("A");
			htmlList.createHTML("B");
			htmlList.createHTML("C");
			htmlList.createHTML("D Link: {{mesh.link(\"" + CONTENT_UUID + "\", \"en\")}}");

			// dateList
			DateGraphFieldList dateList = container.createDateList("dateList");
			dateList.createDate(dateToMilis("2012-07-11 10:55:21"));
			dateList.createDate(dateToMilis("2014-07-11 10:55:30"));
			dateList.createDate(dateToMilis("2000-07-11 10:55:00"));

			// numberList
			NumberGraphFieldList numberList = container.createNumberList("numberList");
			numberList.createNumber(42L);
			numberList.createNumber(1337);
			numberList.createNumber(0.314f);

			// booleanList
			BooleanGraphFieldList booleanList = container.createBooleanList("booleanList");
			booleanList.createBoolean(true);
			booleanList.createBoolean(null);
			booleanList.createBoolean(false);

			// nodeList
			NodeGraphFieldList nodeList = container.createNodeList("nodeList");
			nodeList.createNode("0", node2);
			nodeList.createNode("1", node3);

			if (withMicroschema) {
				// micronodeList
				MicronodeGraphFieldList micronodeList = container.createMicronodeFieldList("micronodeList");
				Micronode firstMicronode = micronodeList.createMicronode();
				firstMicronode.setSchemaContainerVersion(microschemaContainer("vcard").getLatestVersion());
				firstMicronode.createString("firstName").setString("Joe");
				firstMicronode.createString("lastName").setString("Doe");
				firstMicronode.createString("address").setString("Somewhere");
				firstMicronode.createString("postcode").setString("1010");

				Micronode secondMicronode = micronodeList.createMicronode();
				secondMicronode.setSchemaContainerVersion(boot().microschemaContainerRoot().findByUuid(microschemaUuid).getLatestVersion());
				secondMicronode.createString("text").setString("Joe");
				secondMicronode.createNode("nodeRef", content());
				NodeGraphFieldList micrnodeNodeList = secondMicronode.createNodeList("nodeList");
				micrnodeNodeList.createNode("0", node2);
				micrnodeNodeList.createNode("1", node3);

				// micronode
				MicronodeGraphField micronodeField = container.createMicronode("micronode", microschemaContainer("vcard").getLatestVersion());
				micronodeField.getMicronode().createString("firstName").setString("Joe");
				micronodeField.getMicronode().createString("lastName").setString("Doe");
				micronodeField.getMicronode().createString("address").setString("Somewhere");
				micronodeField.getMicronode().createString("postcode").setString("1010");
			}
			container.updateWebrootPathInfo(initialBranchUuid(), null);
			tx.success();
		}

		// Publish all nodes
		String baseNodeUuid = tx(() -> project().getBaseNode().getUuid());
		call(() -> client().publishNode(PROJECT_NAME, baseNodeUuid, new PublishParametersImpl().setRecursive(true)));

		// Create a draft node
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setSchema(new SchemaReferenceImpl().setName("content"));
		request.getFields().put("title", FieldUtil.createStringField("some title"));
		request.getFields().put("teaser", FieldUtil.createStringField("some teaser"));
		request.getFields().put("slug", FieldUtil.createStringField("new-page"));
		request.setParentNode(new NodeReference().setUuid(baseNodeUuid));
		call(() -> client().createNode(PROJECT_NAME, request));

		// Create a node which contains mesh links
		createLanguageLinkResolvingNode(NODE_WITH_LINKS_UUID, baseNodeUuid, CONTENT_UUID).blockingAwait();

		// Create referencing node (en)
		NodeCreateRequest request2 = new NodeCreateRequest();
		request2.setLanguage("en");
		request2.setSchema(new SchemaReferenceImpl().setName("folder"));
		request2.getFields().put("nodeRef", FieldUtil.createNodeField(NODE_WITH_LINKS_UUID));
		request2.getFields().put("nodeList", FieldUtil.createNodeListField(NODE_WITH_LINKS_UUID));
		request2.getFields().put("slug", FieldUtil.createStringField("node-with-reference-en"));
		request2.setParentNode(new NodeReference().setUuid(NEWS_UUID));
		call(() -> client().createNode(NODE_WITH_NODE_REF_UUID, PROJECT_NAME, request2));

		// Create referencing node content (de)
		NodeUpdateRequest nodeUpdateRequest = new NodeUpdateRequest();
		nodeUpdateRequest.setLanguage("de");
		nodeUpdateRequest.setVersion("0.1");
		nodeUpdateRequest.getFields().put("nodeRef", FieldUtil.createNodeField(NODE_WITH_LINKS_UUID));
		nodeUpdateRequest.getFields().put("nodeList", FieldUtil.createNodeListField(NODE_WITH_LINKS_UUID));
		nodeUpdateRequest.getFields().put("slug", FieldUtil.createStringField("node-with-reference-de"));
		call(() -> client().updateNode(PROJECT_NAME, NODE_WITH_NODE_REF_UUID, nodeUpdateRequest));

		// Now execute the query and assert it
		GraphQLResponse response = call(
			() -> client().graphqlQuery(PROJECT_NAME, getGraphQLQuery(queryName), new VersioningParametersImpl().setVersion(version)));
		System.out.println(response.toJson());
		assertThat(new JsonObject(response.toJson())).compliesToAssertions(queryName);
	}

	private long dateToMilis(String date) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime();
	}

	private Completable createLanguageLinkResolvingNode(String nodeUuid, String parentUuid, String referencedUuid) throws Exception {

		Function<String, FieldMap> createFields = language -> {
			FieldMap map = new FieldMapImpl();

			// stringList
			StringFieldListImpl stringList = new StringFieldListImpl();
			stringList.add("A Link: {{mesh.link(\"" + referencedUuid + "\", \"en\")}}");
			stringList.add("B Link: {{mesh.link(\"" + referencedUuid + "\", \"de\")}}");
			stringList.add("C Link: {{mesh.link(\"" + referencedUuid + "\")}}");
			map.put("stringList", stringList);

			// htmlList
			HtmlFieldListImpl htmlList = new HtmlFieldListImpl();
			htmlList.add("A Link: {{mesh.link(\"" + referencedUuid + "\", \"en\")}}");
			htmlList.add("B Link: {{mesh.link(\"" + referencedUuid + "\", \"de\")}}");
			htmlList.add("C Link: {{mesh.link(\"" + referencedUuid + "\")}}");
			map.put("htmlList", htmlList);

			// html
			HtmlField htmlField = new HtmlFieldImpl().setHTML("HTML Link: {{mesh.link(\"" + referencedUuid + "\")}}");
			map.put("html", htmlField);

			// string
			StringField stringField = new StringFieldImpl().setString("String Link: {{mesh.link(\"" + referencedUuid + "\")}}");
			map.put("string", stringField);

			map.put("slug", new StringFieldImpl().setString("new-page-" + language));
			return map;
		};

		Function<NodeResponse, Single<NodeResponse>> updateNode = response -> {
			NodeUpdateRequest updateRequest = new NodeUpdateRequest();
			updateRequest.setFields(createFields.apply("de"));
			updateRequest.setLanguage("de");
			return client().updateNode(PROJECT_NAME, response.getUuid(), updateRequest).toSingle();
		};

		NodeCreateRequest createRequest = new NodeCreateRequest();
		createRequest.setLanguage("en");
		createRequest.setSchema(new SchemaReferenceImpl().setName("folder"));
		createRequest.setParentNode(new NodeReference().setUuid(parentUuid));
		createRequest.setFields(createFields.apply("en"));

		return client().createNode(nodeUuid, PROJECT_NAME, createRequest).toSingle()
			.flatMap(updateNode)
			.toCompletable();
	}
}
