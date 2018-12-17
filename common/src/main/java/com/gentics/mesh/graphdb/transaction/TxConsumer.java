package com.gentics.mesh.graphdb.transaction;

import com.syncleus.ferma.tx.Tx;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import java.util.function.Supplier;


public class TxConsumer<T> {
	private final Consumer<Tx> action;

	private TxConsumer(Consumer<Tx> action) {
		this.action = action;
	}

	public static <T> TxConsumer<T> create(Consumer<Tx> action) {
		return new TxConsumer<>(action);
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

	public <R> TxFunction<R> andThen(Supplier<TxFunction<R>> function) {
		throw new RuntimeException("Not implemented");
	}
}
