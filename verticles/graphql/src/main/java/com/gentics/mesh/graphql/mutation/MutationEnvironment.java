package com.gentics.mesh.graphql.mutation;

import graphql.schema.DataFetchingEnvironment;

public class MutationEnvironment<T> {
	public final DataFetchingEnvironment environment;
	public final T data;

	public MutationEnvironment(DataFetchingEnvironment environment, T data) {
		this.environment = environment;
		this.data = data;
	}
}
