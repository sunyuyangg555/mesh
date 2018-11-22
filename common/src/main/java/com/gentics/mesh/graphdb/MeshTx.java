package com.gentics.mesh.graphdb;

import com.syncleus.ferma.tx.Tx;

public interface MeshTx extends Tx {
	void afterCommit(Runnable task);
}
