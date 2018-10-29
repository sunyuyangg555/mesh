package com.gentics.mesh.graphql.type.scalar;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;

public final class AnyType {
	public static final String SCALAR_ANY_NAME = "Any";

	public static GraphQLType createAnyScalar() {
		return GraphQLScalarType.newScalar()
			.name(SCALAR_ANY_NAME)
			.coercing(new Coercing() {
				@Override
				public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
					return dataFetcherResult;
				}

				@Override
				public Object parseValue(Object input) throws CoercingParseValueException {
					return input;
				}

				@Override
				public Object parseLiteral(Object input) throws CoercingParseLiteralException {
					return input;
				}
			})
			.description("Any value")
			.build();
	}
}
