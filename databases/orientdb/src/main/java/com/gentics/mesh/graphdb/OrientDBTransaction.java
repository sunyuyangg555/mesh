package com.gentics.mesh.graphdb;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.syncleus.ferma.FramedTransactionalGraph;
import com.syncleus.ferma.ext.orientdb.DelegatingFramedOrientGraph;
import com.syncleus.ferma.tx.AbstractTx;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class OrientDBTransaction extends AbstractTx<FramedTransactionalGraph> {
	private boolean isWrapped = false;

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
				} else {
					rollback();
				}
			} catch (OConcurrentModificationException e) {
				throw e;
			} finally {
				getGraph().shutdown();
				Tx.setActive(null);
			}
		}
	}
}
