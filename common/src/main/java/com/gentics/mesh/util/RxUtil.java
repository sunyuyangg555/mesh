package com.gentics.mesh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.gentics.mesh.Mesh;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.AsyncFile;

public final class RxUtil {

	public static final OpenOptions READ_ONLY = new OpenOptions().setRead(true);

	private static final Logger log = LoggerFactory.getLogger(RxUtil.class);

	private RxUtil() {
	}

	public static <T> Completable andThenCompletable(Single<T> source, Function<T, Completable> mappingFunction) {
		return Observable.merge(source.toObservable().map(v -> mappingFunction.apply(v).toObservable())).ignoreElements();
	}

	public static <T> void noopAction(T nix) {

	}

	/**
	 * Reads the entire stream and returns its contents as a buffer.
	 * 
	 * @deprecated Try to avoid this method in order to prevent memory issues.
	 */
	@Deprecated
	public static Single<Buffer> readEntireData(Flowable<Buffer> stream) {
		return stream.reduce((a, b) -> a.appendBuffer(b)).toSingle();
	}

	/**
	 * Provide a blocking {@link InputStream} by reading the byte buffers from the observable.
	 * 
	 * @param stream
	 * @param vertx
	 * @return
	 * @throws IOException
	 */
	// public static InputStream toInputStream(Observable<Buffer> stream, Vertx vertx) throws IOException {
	// WrapperWriteStream wstream = new WrapperWriteStream();
	// stream.observeOn(RxHelper.blockingScheduler(vertx.getDelegate(), false))
	//
	// .doOnComplete(wstream::end)
	//
	// .subscribe(wstream::write);
	// return wstream.createInputStream();
	// }
	public static Function<Flowable<Buffer>, InputStream> inputStream(Vertx vertx) throws IOException {
		return stream -> {
			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream(pis);
			stream.map(b -> b.getDelegate().getBytes()).observeOn(RxHelper.blockingScheduler(vertx.getDelegate(), false)).doOnComplete(() -> {
				try {
					pos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}).subscribe(buf -> {
				try {
					pos.write(buf);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, error -> {
				log.error("Error while reading stream", error);
			});
			return pis;
		};
	}

	/**
	 * Securely open the file and return the {@link AsyncFile} to be used.
	 * 
	 * @param path
	 * @param options
	 * @return
	 */
	public static Single<AsyncFile> openFile(String path, OpenOptions options) {
		return Mesh.rxVertx().fileSystem().rxOpen(path, options).flatMap(f -> {
			return Single.just(f).doOnDispose(f::close).doFinally(f::close);
		});
	}

	/**
	 * Securely open the {@link AsyncFile} for the path and register the close call.
	 * 
	 * @param file
	 * @return
	 */
	public static Flowable<io.vertx.reactivex.core.buffer.Buffer> openFileBuffer(String path, OpenOptions options) {
		return Mesh.rxVertx().fileSystem().rxOpen(path, options)
			.flatMapPublisher(f -> f.toFlowable().doOnTerminate(f::close).doOnCancel(f::close));
	}
}
