package com.gentics.mesh.core.verticle.user;

import static com.gentics.mesh.http.HttpConstants.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.context.impl.InternalRoutingActionContextImpl;
import com.gentics.mesh.core.AbstractEndpoint;
import com.gentics.mesh.etc.RouterStorage;
import com.gentics.mesh.parameter.impl.NodeParametersImpl;
import com.gentics.mesh.parameter.impl.PagingParametersImpl;
import com.gentics.mesh.parameter.impl.RolePermissionParametersImpl;
import com.gentics.mesh.parameter.impl.UserParametersImpl;
import com.gentics.mesh.parameter.impl.VersioningParametersImpl;
import com.gentics.mesh.rest.Endpoint;
import com.gentics.mesh.util.UUIDUtil;

@Singleton
public class UserEndpoint extends AbstractEndpoint {

	private UserCrudHandler crudHandler;

	private UserTokenAuthHandler userTokenHandler;

	public UserEndpoint() {
		super("users", null);
	}

	@Inject
	public UserEndpoint(RouterStorage routerStorage, UserCrudHandler userCrudHandler, UserTokenAuthHandler userTokenHandler) {
		super("users", routerStorage);
		this.crudHandler = userCrudHandler;
		this.userTokenHandler = userTokenHandler;
	}

	@Override
	public String getDescription() {
		return "Provides endpoints which allow the manipulation of users.";
	}

	@Override
	public void registerEndPoints() {
		addUpdateHandler();
		secureAll();
		addCreateHandler();
		addReadHandler();
		addDeleteHandler();
		addTokenHandler();

		addRoleHandler();
		addReadPermissionHandler();
	}

	public void addRoleHandler() {
		Endpoint readRoles = createEndpoint();
		readRoles.path("/:userUuid/roles");
		readRoles.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		readRoles.description("Load multiple roles that are assigned to the user. Return a paged list response.");
		readRoles.method(GET);
		readRoles.produces(APPLICATION_JSON);
		readRoles.exampleResponse(OK, roleExamples.getRoleListResponse(), "List of roles which were assigned to the user.");
		readRoles.addQueryParameters(PagingParametersImpl.class);
		readRoles.addQueryParameters(RolePermissionParametersImpl.class);
		readRoles.handler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String userUuid = ac.getParameter("userUuid");
			crudHandler.handleUserRolesList(ac, userUuid);
		});

		Endpoint addRole = createEndpoint();
		addRole.path("/:userUuid/roles/:roleUuid");
		addRole.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		addRole.addUriParameter("roleUuid", "Uuid of the role.", UUIDUtil.randomUUID());
		addRole.method(POST);
		addRole.description("Add the specified role to the user.");
		addRole.produces(APPLICATION_JSON);
		addRole.exampleResponse(OK, roleExamples.getRoleResponse2(), "Added role.");
		addRole.handler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String userUuid = ac.getParameter("userUuid");
			String roleUuid = ac.getParameter("roleUuid");
			crudHandler.handleAddRoleToUser(ac, userUuid, roleUuid);
		});

		Endpoint removeRole = createEndpoint();
		removeRole.path("/:userUuid/roles/:roleUuid");
		removeRole.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		removeRole.addUriParameter("roleUuid", "Uuid of the role.", UUIDUtil.randomUUID());
		removeRole.method(DELETE);
		removeRole.description("Remove the given role from the user.");
		removeRole.exampleResponse(NO_CONTENT, "Role was removed from the user.");
		removeRole.produces(APPLICATION_JSON).handler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String userUuid = ac.getParameter("userUuid");
			String roleUuid = ac.getParameter("roleUuid");
			crudHandler.handleRemoveRoleFromUser(ac, userUuid, roleUuid);
		});
	}

	private void addReadPermissionHandler() {
		Endpoint endpoint = createEndpoint();
		endpoint.pathRegex("\\/([^\\/]*)\\/permissions\\/(.*)");
		endpoint.setRAMLPath("/{userUuid}/permissions/{path}");
		endpoint.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		endpoint.addUriParameter("path", "Path to the element from which the permissions should be loaded.", "projects/:projectUuid/schemas");
		endpoint.description("Read the user permissions on the element/s that are located by the specified path.");
		endpoint.method(GET);
		endpoint.produces(APPLICATION_JSON);
		endpoint.exampleResponse(OK, userExamples.getUserPermissionResponse(), "Response which contains the loaded permissions.");
		endpoint.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String userUuid = ac.getParameter("param0");
			String pathToElement = ac.getParameter("param1");
			crudHandler.handlePermissionRead(ac, userUuid, pathToElement);
		});
	}

	private void addTokenHandler() {
		Endpoint endpoint = createEndpoint();
		endpoint.path("/:userUuid/token");
		endpoint.setRAMLPath("/{userUuid}/token");
		endpoint.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		endpoint.description("Return a one time token which can be used by any user to update a user (e.g.: Reset the password)");
		endpoint.method(GET);
		endpoint.produces(APPLICATION_JSON);
		endpoint.exampleResponse(OK, userExamples.getTokenResponse(), "User token response.");
		endpoint.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String uuid = ac.getParameter("userUuid");
			crudHandler.handleFetchToken(ac, uuid);
		});
	}

	private void addReadHandler() {
		Endpoint readOne = createEndpoint();
		readOne.path("/:userUuid");
		readOne.description("Read the user with the given uuid");
		readOne.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		readOne.method(GET);
		readOne.produces(APPLICATION_JSON);
		readOne.exampleResponse(OK, userExamples.getUserResponse1("jdoe"), "User response which may also contain an expanded node.");
		readOne.addQueryParameters(NodeParametersImpl.class);
		readOne.addQueryParameters(VersioningParametersImpl.class);
		readOne.addQueryParameters(RolePermissionParametersImpl.class);
		readOne.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String uuid = ac.getParameter("userUuid");
			crudHandler.handleRead(ac, uuid);
		});

		/*
		 * List all users when no parameter was specified
		 */
		Endpoint readAll = createEndpoint();
		readAll.path("/");
		readAll.description("Load multiple users and return a paged list response.");
		readAll.method(GET);
		readAll.produces(APPLICATION_JSON);
		readAll.exampleResponse(OK, userExamples.getUserListResponse(), "User list response which may also contain an expanded node references.");
		readAll.addQueryParameters(NodeParametersImpl.class);
		readAll.addQueryParameters(VersioningParametersImpl.class);
		readAll.addQueryParameters(RolePermissionParametersImpl.class);
		readAll.addQueryParameters(PagingParametersImpl.class);
		readAll.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			crudHandler.handleReadList(ac);
		});
	}

	private void addDeleteHandler() {
		Endpoint endpoint = createEndpoint();
		endpoint.path("/:userUuid");
		endpoint.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		endpoint.method(DELETE);
		endpoint.description(
				"Deactivate the user with the given uuid. Please note that users can't be deleted since they are needed to construct creator/editor information.");
		endpoint.produces(APPLICATION_JSON);
		endpoint.exampleResponse(NO_CONTENT, "User was deactivated.");
		endpoint.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String uuid = ac.getParameter("userUuid");
			crudHandler.handleDelete(ac, uuid);
		});
	}

	private void addUpdateHandler() {

		// Add the user token handler first in order to allow for recovery token handling
		getRouter().route("/:userUuid").method(POST).handler(userTokenHandler);
		// Chain the regular auth handler afterwards in order to handle non-token code requests
		getRouter().route("/:userUuid").method(POST).handler(authHandler);

		Endpoint endpoint = createEndpoint();
		endpoint.path("/:userUuid");
		endpoint.addUriParameter("userUuid", "Uuid of the user.", UUIDUtil.randomUUID());
		endpoint.description("Update the user with the given uuid.");
		endpoint.method(POST);
		endpoint.consumes(APPLICATION_JSON);
		endpoint.produces(APPLICATION_JSON);
		endpoint.addQueryParameters(UserParametersImpl.class);
		endpoint.exampleRequest(userExamples.getUserUpdateRequest("jdoe42"));
		endpoint.exampleResponse(OK, userExamples.getUserResponse1("jdoe42"), "Updated user response.");
		endpoint.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			String uuid = ac.getParameter("userUuid");
			crudHandler.handleUpdate(ac, uuid);
		});
	}

	private void addCreateHandler() {
		Endpoint endpoint = createEndpoint();
		endpoint.path("/");
		endpoint.description("Create a new user.");
		endpoint.method(POST);
		endpoint.consumes(APPLICATION_JSON);
		endpoint.produces(APPLICATION_JSON);
		endpoint.exampleRequest(userExamples.getUserCreateRequest("newuser"));
		endpoint.exampleResponse(CREATED, userExamples.getUserResponse1("newuser"), "User response of the created user.");
		endpoint.blockingHandler(rc -> {
			InternalActionContext ac = new InternalRoutingActionContextImpl(rc);
			crudHandler.handleCreate(ac);
		});
	}
}
