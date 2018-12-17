package com.gentics.mesh.graphdb.transaction;

import com.syncleus.ferma.tx.Tx;
import io.reactivex.Single;
import io.reactivex.functions.Function;


public class TxFunction<T> {
	private final Function<Tx, T> function;

	private TxFunction(Function<Tx, T> function) {
		this.function = function;
	}

	public static <T> TxFunction<T> create(Function<Tx, T> function) {
		return new TxFunction<>(function);
	}

	public T runInTx(Tx tx) {
		throw new RuntimeException("Not implemented");
	}

	public T runInNewSyncTx() {
		throw new RuntimeException("Not implemented");
	}

	public Single<T> runInNewAsyncTx() {
		throw new RuntimeException("Not implemented");
	}

	public <R> TxFunction<R> flatMap(Function<T, TxFunction<R>> function) {
		throw new RuntimeException("Not implemented");
	}
}
