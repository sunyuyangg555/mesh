package com.gentics.mesh.rest.ooclient.entities;

import com.gentics.mesh.parameter.PagingParameters;
import com.gentics.mesh.parameter.client.PagingParametersImpl;
import io.reactivex.Observable;

import java.util.function.Function;

public class ListEntityBuilder<T> {
	private final Function<PagingParameters, Observable<T>> fetcher;
	private PagingParameters pagingParameters = new PagingParametersImpl();

	public ListEntityBuilder(Function<PagingParameters, Observable<T>> fetcher) {
		this.fetcher = fetcher;
	}

	public ListEntityBuilder<T> page(long page) {
		pagingParameters.setPage(page);
		return this;
	}

	public ListEntityBuilder<T> perPage(long perPage) {
		pagingParameters.setPerPage(perPage);
		return this;
	}

	public Observable<T> build() {
		// TODO real flowable paging ?
		return fetcher.apply(pagingParameters);
	}
}
