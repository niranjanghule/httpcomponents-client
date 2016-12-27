/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.impl.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.localserver.LocalServerTestBase;
import org.apache.hc.client5.http.localserver.RequestBasicAuth;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClientAuthenticationFallBack extends LocalServerTestBase {

    public class ResponseBasicUnauthorized implements HttpResponseInterceptor {

        @Override
        public void process(
                final HttpResponse response,
                final EntityDetails entityDetails,
                final HttpContext context) throws HttpException, IOException {
            if (response.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Digest realm=\"test realm\" invalid");
                response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"test realm\"");
            }
        }

    }

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        final HttpProcessor httpproc = HttpProcessors.customServer(null)
            .add(new RequestBasicAuth())
            .add(new ResponseBasicUnauthorized()).build();
        this.serverBootstrap.setHttpProcessor(httpproc);
    }

    static class AuthHandler implements HttpRequestHandler {

        @Override
        public void handle(
                final ClassicHttpRequest request,
                final ClassicHttpResponse response,
                final HttpContext context) throws HttpException, IOException {
            final String creds = (String) context.getAttribute("creds");
            if (creds == null || !creds.equals("test:test")) {
                response.setCode(HttpStatus.SC_UNAUTHORIZED);
            } else {
                response.setCode(HttpStatus.SC_OK);
                final StringEntity entity = new StringEntity("success", StandardCharsets.US_ASCII);
                response.setEntity(entity);
            }
        }

    }

    static class TestCredentialsProvider implements CredentialsProvider {

        private final Credentials creds;
        private AuthScope authscope;

        TestCredentialsProvider(final Credentials creds) {
            super();
            this.creds = creds;
        }

        @Override
        public Credentials getCredentials(final AuthScope authscope, final HttpContext context) {
            this.authscope = authscope;
            return this.creds;
        }

        public AuthScope getAuthScope() {
            return this.authscope;
        }

    }

    @Test
    public void testBasicAuthenticationSuccess() throws Exception {
        this.serverBootstrap.registerHandler("*", new AuthHandler());

        final HttpHost target = start();

        final HttpClientContext context = HttpClientContext.create();
        final TestCredentialsProvider credsProvider = new TestCredentialsProvider(
                new UsernamePasswordCredentials("test", "test".toCharArray()));
        context.setCredentialsProvider(credsProvider);
        final HttpGet httpget = new HttpGet("/");

        final ClassicHttpResponse response = this.httpclient.execute(target, httpget, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertEquals(HttpStatus.SC_OK, response.getCode());
        Assert.assertNotNull(entity);
        EntityUtils.consume(entity);
        final AuthScope authscope = credsProvider.getAuthScope();
        Assert.assertNotNull(authscope);
        Assert.assertEquals("test realm", authscope.getRealm());
    }

}
