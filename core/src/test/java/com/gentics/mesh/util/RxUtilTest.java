package com.gentics.mesh.util;

import static com.gentics.mesh.util.RxUtil.inputStream;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.tika.io.IOUtils;
import org.junit.Test;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;

public class RxUtilTest {

	@Test
	public void testToInputStream() throws IOException {
		Flowable<Buffer> data = Observable.range(1, 5).map(String::valueOf).map(Buffer::buffer).toFlowable(BackpressureStrategy.BUFFER);
		Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
		Observable<Buffer> buf = Observable.zip(data.toObservable(), interval, (b, i) -> b);
		try (InputStream ins = buf.toFlowable(BackpressureStrategy.BUFFER).to(inputStream(Vertx.vertx()))) {
			String text = IOUtils.toString(ins);
			assertEquals("12345", text);
		}
	}

}