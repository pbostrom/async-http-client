/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.filter;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AbstractBasicTest;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.extra.ThrottleRequestFilter;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

public class FilterTest extends AbstractBasicTest {

    private static class BasicHandler extends AbstractHandler {

        public void handle(String s, org.eclipse.jetty.server.Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            Enumeration<?> e = httpRequest.getHeaderNames();
            String param;
            while (e.hasMoreElements()) {
                param = e.nextElement().toString();
                httpResponse.addHeader(param, httpRequest.getHeader(param));
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new BasicHandler();
    }

    public String getTargetUrl() {
        return String.format("http://127.0.0.1:%d/foo/test", port1);
    }

    @Test(groups = { "standalone", "default_provider" })
    public void basicTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.addRequestFilter(new ThrottleRequestFilter(100));

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            Response response = c.preparePost(getTargetUrl()).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void loadThrottleTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.addRequestFilter(new ThrottleRequestFilter(10));

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            List<Future<Response>> futures = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                futures.add(c.preparePost(getTargetUrl()).execute());
            }

            for (Future<Response> f : futures) {
                Response r = f.get();
                assertNotNull(f.get());
                assertEquals(r.getStatusCode(), 200);
            }
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void maxConnectionsText() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.addRequestFilter(new ThrottleRequestFilter(0, 1000));

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            c.preparePost(getTargetUrl()).execute().get();
            fail("Should have timed out");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof FilterException);
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void basicResponseFilterTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.addResponseFilter(new ResponseFilter() {

            @Override
            public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
                return ctx;
            }

        });

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            Response response = c.preparePost(getTargetUrl()).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void replayResponseFilterTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        final AtomicBoolean replay = new AtomicBoolean(true);

        b.addResponseFilter(new ResponseFilter() {

            public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {

                if (replay.getAndSet(false)) {
                    Request request = new RequestBuilder(ctx.getRequest()).addHeader("X-Replay", "true").build();
                    return new FilterContext.FilterContextBuilder<T>().asyncHandler(ctx.getAsyncHandler()).request(request).replayRequest(true).build();
                }
                return ctx;
            }

        });

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            Response response = c.preparePost(getTargetUrl()).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("X-Replay"), "true");
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void replayStatusCodeResponseFilterTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        final AtomicBoolean replay = new AtomicBoolean(true);

        b.addResponseFilter(new ResponseFilter() {

            public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {

                if (ctx.getResponseStatus() != null && ctx.getResponseStatus().getStatusCode() == 200 && replay.getAndSet(false)) {
                    Request request = new RequestBuilder(ctx.getRequest()).addHeader("X-Replay", "true").build();
                    return new FilterContext.FilterContextBuilder<T>().asyncHandler(ctx.getAsyncHandler()).request(request).replayRequest(true).build();
                }
                return ctx;
            }

        });

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            Response response = c.preparePost(getTargetUrl()).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("X-Replay"), "true");
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void replayHeaderResponseFilterTest() throws Exception {
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        final AtomicBoolean replay = new AtomicBoolean(true);

        b.addResponseFilter(new ResponseFilter() {

            public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {

                if (ctx.getResponseHeaders() != null && ctx.getResponseHeaders().getHeaders().get("Ping").equals("Pong") && replay.getAndSet(false)) {

                    Request request = new RequestBuilder(ctx.getRequest()).addHeader("Ping", "Pong").build();
                    return new FilterContext.FilterContextBuilder<T>().asyncHandler(ctx.getAsyncHandler()).request(request).replayRequest(true).build();
                }
                return ctx;
            }

        });

        try (AsyncHttpClient c = new DefaultAsyncHttpClient(b.build())) {
            Response response = c.preparePost(getTargetUrl()).addHeader("Ping", "Pong").execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("Ping"), "Pong");
        }
    }
}
