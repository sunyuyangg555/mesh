package com.gentics.mesh.graphql.mutation;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.rest.user.UserUpdateRequest;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.gentics.mesh.graphql.type.UserTypeProvider.USER_TYPE_NAME;

@Singleton
public class UserMutations {
	public static final String UPDATE_USER_FIELD_NAME = "updateUser";

	private final BootstrapInitializer boot;

	@Inject
	public UserMutations(BootstrapInitializer boot) {
		this.boot = boot;
	}


	public GraphQLFieldDefinition updateUser() {
		return GraphQLFieldDefinition.newFieldDefinition()
			.name(UPDATE_USER_FIELD_NAME)
			.argument(GraphQLArgument.newArgument()
				.name("data")
				.description("User data to be updated")
				.type(updateInputType())
				.build())
			.type(new GraphQLTypeReference(USER_TYPE_NAME))
			.dataFetcher(MutationUtil.mutationFetcher(GraphQlUserUpdateRequest.class, env -> {
				User user = boot.userRoot().findByUuid(env.data.getUuid());
				user.setFirstname(env.data.getData().getFirstname());
				return user;
			}))
			.build();
	}

	public static class GraphQlUserUpdateRequest {
		private String uuid;
		private UserUpdateRequest data;

		public String getUuid() {
			return uuid;
		}

		public GraphQlUserUpdateRequest setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public UserUpdateRequest getData() {
			return data;
		}

		public GraphQlUserUpdateRequest setData(UserUpdateRequest data) {
			this.data = data;
			return this;
		}
	}

	private GraphQLInputType updateInputType() {
		return GraphQLInputObjectType.newInputObject()
			.name("UpdateUserRequest")
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("uuid")
				.type(Scalars.GraphQLString)
				.build())
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("data")
				.type(GraphQLInputObjectType.newInputObject()
					.name("UpdateUserRequestData")
					.field(GraphQLInputObjectField.newInputObjectField()
						.name("firstname")
						.type(Scalars.GraphQLString)
						.build())
				)
				.build())
			.build();
	}
}
