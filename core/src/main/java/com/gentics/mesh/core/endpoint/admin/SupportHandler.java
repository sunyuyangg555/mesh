package com.gentics.mesh.core.endpoint.admin;

import static com.gentics.mesh.core.rest.error.Errors.error;
import static com.gentics.mesh.rest.Messages.message;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.Mesh;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.endpoint.handler.AbstractHandler;
import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.graphdb.spi.Database;

import io.reactivex.Single;

@Singleton
public class SupportHandler extends AbstractHandler {

	private Database db;

	@Inject
	public SupportHandler(Database db) {
		this.db = db;
	}

	public void handleDump(InternalActionContext ac) {
		db.asyncTx(() -> {
			if (!ac.getUser().hasAdminRole()) {
				throw error(FORBIDDEN, "error_admin_permission_required");
			}
			MeshOptions options = Mesh.mesh().getOptions();
			String graphDir = options.getStorageOptions().getDirectory();
			Path path = Paths.get(graphDir);

			try {
				FileOutputStream fos = new FileOutputStream(options.getTempDirectory() + "/dump.zip");
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				try {
					DirectoryStream<Path> stream = Files.newDirectoryStream(path);
					for (Path entry : stream) {
						File file = entry.toFile();
						ZipEntry zipEntry = new ZipEntry(file.getName());
						FileInputStream fis = new FileInputStream(file);
						zipOut.putNextEntry(zipEntry);

						byte[] bytes = new byte[1024];
						int length;
						while ((length = fis.read(bytes)) >= 0) {
							zipOut.write(bytes, 0, length);
						}
						fis.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				zipOut.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// return Single.just(null);
			// MeshStatus oldStatus = Mesh.mesh().getStatus();
			// Mesh.mesh().setStatus(MeshStatus.BACKUP);
			// db.backupGraph(Mesh.mesh().getOptions().getStorageOptions().getBackupDirectory());
			// Mesh.mesh().setStatus(oldStatus);
			return Single.just(message(ac, "backup_finished"));
		}).subscribe(model -> ac.send(model, OK), ac::fail);

	}

}
