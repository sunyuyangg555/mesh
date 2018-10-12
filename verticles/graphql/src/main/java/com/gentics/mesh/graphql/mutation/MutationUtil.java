package com.gentics.mesh.graphql.mutation;

import com.gentics.mesh.json.JsonUtil;
import graphql.schema.DataFetcher;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.function.Function;

public class MutationUtil {
	public static <T, R> DataFetcher<R> mutationFetcher(Class<T> clazz, Function<MutationEnvironment<T>, R> fetcher) {
		return env -> {
			Map<String, Object> data = env.getArgument("data");
			MutationEnvironment<T> mutEnv = new MutationEnvironment<>(env, JsonUtil.readValue(new JsonObject(data).toString(), clazz));
			return fetcher.apply(mutEnv);
		};
	}
}
