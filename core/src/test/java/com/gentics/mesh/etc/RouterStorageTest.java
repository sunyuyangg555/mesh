package com.gentics.mesh.etc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import com.gentics.mesh.auth.MeshAuthChain;
import com.gentics.mesh.graphdb.spi.LegacyDatabase;
import com.gentics.mesh.router.RouterStorage;

import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouteImpl;

public class RouterStorageTest {

	@Test
	public void testFailureHandler() throws Exception {
		MeshAuthChain chain = Mockito.mock(MeshAuthChain.class);
		RouterStorage storage = new RouterStorage(null, chain, null, null, null, () -> {
			return Mockito.mock(LegacyDatabase.class);
		});

		RoutingContext rc = mock(RoutingContext.class);
		Route currentRoute = mock(RouteImpl.class);
		when(currentRoute.getPath()).thenReturn("/blub");
		when(rc.currentRoute()).thenReturn(currentRoute);
		ParsedHeaderValues headerValues = mock(ParsedHeaderValues.class);
		when(rc.parsedHeaders()).thenReturn(headerValues);
		HttpServerRequest request = mock(HttpServerRequest.class);
		when(request.query()).thenReturn("?blub");
		when(request.method()).thenReturn(HttpMethod.GET);
		when(request.uri()).thenReturn("");
		when(rc.request()).thenReturn(request);
		when(rc.normalisedPath()).thenReturn("/blub");
		when(rc.queryParams()).thenReturn(new CaseInsensitiveHeaders());
		storage.root().getRouter().handleFailure(rc);
	}
}
