package com.gentics.mesh.test.util;

import static com.gentics.mesh.Events.JOB_WORKER_ADDRESS;
import static com.gentics.mesh.Events.MESH_MIGRATION;
import static com.gentics.mesh.core.rest.admin.migration.MigrationStatus.COMPLETED;
import static com.gentics.mesh.core.rest.admin.migration.MigrationStatus.IDLE;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.util.MeshAssert.failingLatch;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.gentics.mesh.core.rest.admin.migration.MigrationInfo;
import com.gentics.mesh.core.rest.admin.migration.MigrationStatus;
import com.gentics.mesh.core.rest.admin.migration.MigrationStatusResponse;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class TestUtils {

	private TestUtils() {

	}

	private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

	/**
	 * Construct a latch which will release when the migration has finished.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static CountDownLatch latchForMigrationCompleted(MeshRestClient client) throws Exception {
		// Construct latch in order to wait until the migration completed event was received
		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch registerLatch = new CountDownLatch(1);
		client.eventbus(ws -> {
			// Register to migration events
			JsonObject msg = new JsonObject().put("type", "register").put("address", MESH_MIGRATION);
			ws.writeFinalTextFrame(msg.encode());

			// Handle migration events
			ws.handler(buff -> {
				String str = buff.toString();
				JsonObject received = new JsonObject(str);
				JsonObject rec = received.getJsonObject("body");
				log.debug("Migration event:" + rec.getString("type"));
				if ("completed".equalsIgnoreCase(rec.getString("type"))) {
					latch.countDown();
				}
			});
			registerLatch.countDown();

		});

		failingLatch(registerLatch);
		return latch;
	}

	public static void runAndWait(Runnable runnable) {

		Thread thread = run(runnable);
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done waiting");
	}

	public static Thread run(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.start();
		return thread;
	}

	public static void waitFor(CyclicBarrier barrier) {
		try {
			barrier.await(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a random hash
	 * 
	 * @param len
	 * @return
	 */
	public static String getRandomHash(int len) {
		String hash = new String();

		while (hash.length() < len) {
			int e = (int) (Math.random() * 62 + 48);

			// Only use 0-9 and a-z characters
			if (e >= 58 && e <= 96) {
				continue;
			}
			hash += (char) e;
		}
		return hash;
	}

	public static boolean isHost(String hostname) throws UnknownHostException {
		return getHostname().equalsIgnoreCase(hostname);
	}

	public static String getHostname() throws UnknownHostException {
		java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
		return localMachine.getHostName();
	}

	/**
	 * Iterates over the iterable and returns the size.
	 * 
	 * @param it
	 * @return
	 */
	public static long size(Iterable<?> it) {
		Iterator<?> iterator = it.iterator();
		long size = 0;
		while (iterator.hasNext()) {
			iterator.next();
			size++;
		}
		return size;
	}

	/**
	 * Return a free port random port by opening an socket and check whether it is currently used. Not the most elegant or efficient solution, but works.
	 * 
	 * @param port
	 * @return
	 */
	public static int getRandomPort() {
		ServerSocket socket = null;

		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException ioe) {
			return -1;
		} finally {
			// if we did open it cause it's available, close it
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// ignore
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return the json data from classpath.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static String getJson(String name) throws IOException {
		return IOUtils.toString(TestUtils.class.getResourceAsStream("/json/" + name));
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
