package com.gentics.mesh.rest.ooclient;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function3;

public final class Util {
	public static <T1, T2, R> Single<R> flatZip(
		SingleSource<? extends T1> source1, SingleSource<? extends T2> source2,
		BiFunction<? super T1, ? super T2, ? extends SingleSource<R>> zipper
	) {
		return Single.zip(source1, source2, zipper).flatMap(x -> x);
	}

	public static <T1, T2, R> Observable<R> flatZipObservable(
		SingleSource<? extends T1> source1, SingleSource<? extends T2> source2,
		BiFunction<? super T1, ? super T2, ? extends ObservableSource<R>> zipper
	) {
		return Single.zip(source1, source2, zipper).flatMapObservable(x -> x);
	}

	public static <T1, T2, T3, R> Observable<R> flatZipObservable(
		SingleSource<? extends T1> source1, SingleSource<? extends T2> source2, SingleSource<? extends T3> source3,
		Function3<? super T1, ? super T2, ? super T3, ? extends ObservableSource<R>> zipper
	) {
		return Single.zip(source1, source2, source3, zipper).flatMapObservable(x -> x);
	}
}
