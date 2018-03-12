package com.gentics.mesh.storage;

import static com.gentics.mesh.core.rest.error.Errors.error;
import static com.gentics.mesh.util.RxUtil.READ_ONLY;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

import java.io.File;
import java.nio.file.NoSuchFileException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.Mesh;
import com.gentics.mesh.core.data.node.field.BinaryGraphField;
import com.gentics.mesh.etc.config.MeshUploadOptions;
import com.gentics.mesh.util.RxUtil;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.FileSystem;

@Singleton
public class LocalBinaryStorage extends AbstractBinaryStorage {

	private static final Logger log = LoggerFactory.getLogger(LocalBinaryStorage.class);

	@Inject
	public LocalBinaryStorage() {
	}

	@Override
	public Completable store(Flowable<Buffer> stream, String uuid) {
		return Completable.defer(() -> {
			String path = getFilePath(uuid);
			log.debug("Saving data for field to path {" + path + "}");
			MeshUploadOptions uploadOptions = Mesh.mesh().getOptions().getUploadOptions();
			File uploadFolder = new File(uploadOptions.getDirectory(), getSegmentedPath(uuid));

			if (!uploadFolder.exists()) {
				if (!uploadFolder.mkdirs()) {
					log.error("Failed to create target folder {" + uploadFolder.getAbsolutePath() + "}");
					throw error(BAD_REQUEST, "node_error_upload_failed");
				}

				if (log.isDebugEnabled()) {
					log.debug("Created folder {" + uploadFolder.getAbsolutePath() + "}");
				}
			}

			File targetFile = new File(uploadFolder, uuid + ".bin");
			return Mesh.rxVertx().fileSystem().rxOpen(targetFile.getAbsolutePath(), new OpenOptions()).flatMapCompletable(file -> {
				// Now write the stream to the file and handle errors and the final flush.
				return stream
					.doOnNext(file::write)
					.ignoreElements()
					.andThen(file.rxFlush())
					.doOnDispose(file::close)
					.doFinally(file::close);
			});
		});
	}

	/**
	 * Return the absolute path to the binary data for the given uuid.
	 * 
	 * @param binaryUuid
	 * @return
	 */
	public static String getFilePath(String binaryUuid) {
		File folder = new File(Mesh.mesh().getOptions().getUploadOptions().getDirectory(), getSegmentedPath(binaryUuid));
		File binaryFile = new File(folder, binaryUuid + ".bin");
		return binaryFile.getAbsolutePath();
	}

	@Override
	public boolean exists(BinaryGraphField field) {
		String uuid = field.getBinary().getUuid();
		return new File(getFilePath(uuid)).exists();
	}

	@Override
	public Flowable<Buffer> read(String binaryUuid) {
		String path = getFilePath(binaryUuid);
		return RxUtil.openFileBuffer(path, READ_ONLY);
	}

	/**
	 * Generate the segmented path for the given binary uuid.
	 * 
	 * @param binaryUuid
	 * @return
	 */
	public static String getSegmentedPath(String binaryUuid) {
		String partA = binaryUuid.substring(0, 2);
		String partB = binaryUuid.substring(2, 4);
		StringBuffer buffer = new StringBuffer();
		buffer.append(File.separator);
		buffer.append(partA);
		buffer.append(File.separator);
		buffer.append(partB);
		buffer.append(File.separator);
		return buffer.toString();
	}

	@Override
	public Completable delete(String binaryUuid) {
		String path = getFilePath(binaryUuid);
		return FileSystem.newInstance(Mesh.vertx().fileSystem())

			.rxDelete(path)
			// Don't fail if the file is not even in the local storage
			.onErrorComplete(e -> {
				Throwable cause = e.getCause();
				if (cause != null) {
					return cause instanceof NoSuchFileException;
				} else {
					return e instanceof NoSuchFileException;
				}
			});
	}

}
