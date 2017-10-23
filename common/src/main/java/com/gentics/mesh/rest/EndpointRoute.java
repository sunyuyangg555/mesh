package com.gentics.mesh.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;

import com.gentics.mesh.core.rest.common.RestModel;
import com.gentics.mesh.example.AdminExamples;
import com.gentics.mesh.example.GraphQLExamples;
import com.gentics.mesh.example.GroupExamples;
import com.gentics.mesh.example.JobExamples;
import com.gentics.mesh.example.MicroschemaExamples;
import com.gentics.mesh.example.MiscExamples;
import com.gentics.mesh.example.NodeExamples;
import com.gentics.mesh.example.ProjectExamples;
import com.gentics.mesh.example.ReleaseExamples;
import com.gentics.mesh.example.RoleExamples;
import com.gentics.mesh.example.SchemaExamples;
import com.gentics.mesh.example.TagExamples;
import com.gentics.mesh.example.TagFamilyExamples;
import com.gentics.mesh.example.UserExamples;
import com.gentics.mesh.example.VersioningExamples;
import com.gentics.mesh.parameter.ParameterProvider;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

/**
 * Simple wrapper for vert.x routes. The wrapper is commonly used to generate RAML descriptions for the route.
 */
public interface EndpointRoute extends Comparable<EndpointRoute> {

	/**
	 * Wrapper for {@link Route#path(String)}.
	 * 
	 * @param path
	 * @return Fluent API
	 */
	EndpointRoute path(String path);

	/**
	 * Set the http method of the endpoint.
	 * 
	 * @param method
	 * @return Fluent API
	 */
	EndpointRoute method(HttpMethod method);

	/**
	 * Add a content type consumed by this endpoint. Used for content based routing.
	 *
	 * @param contentType
	 *            the content type
	 * @return Fluent API
	 */
	EndpointRoute consumes(String contentType);

	/**
	 * Set the request handler for the endpoint.
	 * 
	 * @param requestHandler
	 * @return Fluent API
	 */
	EndpointRoute handler(Handler<RoutingContext> requestHandler);

	/**
	 * Wrapper for {@link Route#last()}
	 * 
	 * @return Fluent API
	 */
	EndpointRoute last();

	/**
	 * Wrapper for {@link Route#order(int)}
	 * 
	 * @param order
	 * @return Fluent API
	 */
	EndpointRoute order(int order);

	/**
	 * Validate that all mandatory fields have been set.
	 * 
	 * @return Fluent API
	 */
	EndpointRoute validate();

	/**
	 * Wrapper for {@link Route#remove()}
	 * 
	 * @return Fluent API
	 */
	EndpointRoute remove();

	/**
	 * Wrapper for {@link Route#disable()}
	 * 
	 * @return Fluent API
	 */
	EndpointRoute disable();

	/**
	 * Wrapper for {@link Route#enable()}
	 * 
	 * @return Fluent API
	 */
	EndpointRoute enable();

	/**
	 * Wrapper for {@link Route#useNormalisedPath(boolean)}.
	 * 
	 * @param useNormalisedPath
	 * @return
	 */
	EndpointRoute useNormalisedPath(boolean useNormalisedPath);

	/**
	 * Wrapper for {@link Route#getPath()}
	 * 
	 * @return the path prefix (if any) for this route
	 */
	String getPath();

	/**
	 * Return the endpoint description.
	 * 
	 * @return Endpoint description
	 */
	String getDescription();

	/**
	 * Return the display name for the endpoint.
	 * 
	 * @return Endpoint display name
	 */
	String getDisplayName();

	/**
	 * Add the given response to the example responses.
	 * 
	 * @param status
	 *            Status code of the response
	 * @param description
	 *            Description of the response
	 * @return Fluent API
	 */
	EndpointRoute exampleResponse(HttpResponseStatus status, String description);

	/**
	 * Add the given response to the example responses.
	 * 
	 * @param status
	 *            Status code for the example response
	 * @param model
	 *            Model which will be turned into JSON
	 * @param description
	 *            Description of the example response
	 * @return Fluent API
	 */
	EndpointRoute exampleResponse(HttpResponseStatus status, Object model, String description);

	/**
	 * Add the given response to the example responses.
	 * 
	 * @param status
	 *            Status code of the example response
	 * @param description
	 *            Description of the example
	 * @param headerName
	 *            Name of the header value
	 * @param example
	 *            Example header value
	 * @param headerDescription
	 *            Description of the header
	 * @return
	 */
	EndpointRoute exampleResponse(HttpResponseStatus status, String description, String headerName, String example, String headerDescription);

	/**
	 * Create a blocking handler for the endpoint.
	 * 
	 * @param requestHandler
	 * @return Fluent API
	 */
	EndpointRoute blockingHandler(Handler<RoutingContext> requestHandler);

	/**
	 * Create a blocking handler for the endpoint.
	 * 
	 * @param requestHandler
	 * @param ordered
	 * @return Fluent API
	 */
	EndpointRoute blockingHandler(Handler<RoutingContext> requestHandler, boolean ordered);

	/**
	 * Create a failure handler for the endpoint.
	 * 
	 * @param failureHandler
	 * @return Fluent API
	 */
	EndpointRoute failureHandler(Handler<RoutingContext> failureHandler);

	/**
	 * Parse the RAML path and return a list of all segment name variables.
	 * 
	 * @return List of path segments
	 */
	List<String> getNamedSegments();

	/**
	 * Set the content type for elements which are returned by the endpoint.
	 * 
	 * @param contentType
	 * @return Fluent API
	 */
	EndpointRoute produces(String contentType);

	/**
	 * Set the path using a regex.
	 * 
	 * @param path
	 * @return Fluent API
	 */
	EndpointRoute pathRegex(String path);

	/**
	 * Return the path used for RAML. If non null the path which was previously set using {@link #setRAMLPath(String)} will be returned. Otherwise the converted
	 * vert.x route path is returned. A vert.x path /:nodeUuid is converted to a RAML path /{nodeUuid}.
	 * 
	 * @return RAML path
	 */
	String getRamlPath();

	/**
	 * Set the endpoint display name.
	 * 
	 * @param name
	 * @return Fluent API
	 */
	EndpointRoute displayName(String name);

	/**
	 * Set the endpoint description.
	 * 
	 * @param description
	 *            Description of the endpoint.
	 * @return Fluent API
	 */
	EndpointRoute description(String description);

	/**
	 * Add an uri parameter with description and example to the endpoint.
	 * 
	 * @param key
	 *            Key of the endpoint (e.g.: query, perPage)
	 * @param description
	 * @param example
	 *            Example URI parameter value
	 */
	EndpointRoute addUriParameter(String key, String description, String example);

	/**
	 * Return the uri parameters for the endpoint.
	 * 
	 * @return Map with uri parameters
	 */
	Map<String, UriParameter> getUriParameters();

	/**
	 * Explicitly set the RAML path. This will override the path which is otherwise transformed using the vertx route path.
	 * 
	 * @param path
	 */
	EndpointRoute setRAMLPath(String path);

	/**
	 * Add a query parameter provider to the endpoint. The query parameter provider will in turn provide examples, descriptions for all query parameters which
	 * the parameter provider provides.
	 * 
	 * @param clazz
	 *            Class which provides the parameters
	 * @return Fluent API
	 */
	EndpointRoute addQueryParameters(Class<? extends ParameterProvider> clazz);

	/**
	 * Return the list of query parameters for the endpoint.
	 * 
	 * @return
	 */
	Map<String, QueryParameter> getQueryParameters();

	/**
	 * Return the Vert.x route path regex.
	 * 
	 * @return configured path regex or null if no path regex has been set
	 */
	String getPathRegex();

	/**
	 * Return the endpoint HTTP example request map.
	 * 
	 * @return
	 */
	HashMap<String, MimeType> getExampleRequestMap();

	/**
	 * Return the map of example responses. The map contains examples per http status code.
	 * 
	 * @return
	 */
	Map<Integer, Response> getExampleResponses();

	/**
	 * Return the method used for the endpoint.
	 * 
	 * @return
	 */
	HttpMethod getMethod();

	/**
	 * Return the traits which were set for this endpoint.
	 * 
	 * @return
	 */
	String[] getTraits();

	/**
	 * Set the traits information.
	 * 
	 * @param traits
	 *            Traits which the endpoint should inherit
	 * @return Fluent API
	 */
	EndpointRoute traits(String... traits);

	/**
	 * Set the endpoint json example request via the provided json object. The JSON schema will not be generated.
	 * 
	 * @param jsonObject
	 * @return Fluent API
	 */
	EndpointRoute exampleRequest(JSONObject jsonObject);

	/**
	 * Set the endpoint example request via a JSON example model. The json schema will automatically be generated.
	 * 
	 * @param model
	 *            Example Rest Model
	 * @return Fluent API
	 */
	EndpointRoute exampleRequest(RestModel model);

	/**
	 * Set the endpoint request example via a form parameter list.
	 * 
	 * @param parameters
	 * @return Fluent API
	 */
	EndpointRoute exampleRequest(Map<String, List<FormParameter>> parameters);

	/**
	 * Set the endpoint request example via a plain text body.
	 * 
	 * @param bodyText
	 * @return Fluent API
	 */
	EndpointRoute exampleRequest(String bodyText);

	/**
	 * Return map with status code and the response class.
	 * 
	 * @return
	 */
	Map<Integer, Class<?>> getExampleResponseClasses();

	Class<? extends RestModel> getExampleRequestClass();

}
