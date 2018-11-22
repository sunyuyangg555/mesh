package com.gentics.mesh.graphdb;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.syncleus.ferma.FramedTransactionalGraph;
import com.syncleus.ferma.ext.orientdb.DelegatingFramedOrientGraph;
import com.syncleus.ferma.tx.AbstractTx;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

import java.util.ArrayList;
import java.util.List;

public class OrientDBTransaction extends AbstractTx<FramedTransactionalGraph> implements MeshTx {
	private boolean isWrapped = false;
	private ThreadLocal<List<Runnable>> tasks = new ThreadLocal<>();

	public OrientDBTransaction(OrientGraphFactory factory, TypeResolver typeResolver) {

		// Check if an active transaction already exists.
		Tx activeTx = Tx.getActive();
		if (activeTx != null) {
			isWrapped = true;
			init(activeTx.getGraph());
		} else {
			DelegatingFramedOrientGraph transaction = new DelegatingFramedOrientGraph(factory.getTx(), typeResolver);
			init(transaction);
		}
	}

	@Override
	public void close() {
		if (!isWrapped) {
			try {
				if (isSuccess()) {
					commit();
					List<Runnable> ts = tasks.get();
					if (ts != null) {
						ts.forEach(Runnable::run);
						tasks.remove();
					}
				} else {
					System.out.println("Rolling back!");
					new Throwable().printStackTrace();
					rollback();
					tasks.remove();
				}
			} catch (OConcurrentModificationException e) {
				throw e;
			} finally {
				getGraph().shutdown();
				Tx.setActive(null);
			}
		}
	}

	@Override
	public boolean isSuccess() {
		return super.isSuccess();
	}

	@Override
	public void afterCommit(Runnable task) {
		List<Runnable> ts = tasks.get();
		if (ts == null) {
			ts = new ArrayList<>();
			tasks.set(ts);
		}
		ts.add(task);
	}
}
