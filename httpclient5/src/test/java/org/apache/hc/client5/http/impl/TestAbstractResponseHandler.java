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

package org.apache.hc.client5.http.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.client5.http.impl.sync.AbstractResponseHandler;
import org.apache.hc.client5.http.protocol.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link BasicResponseHandler}.
 */
public class TestAbstractResponseHandler {

    @Test
    public void testSuccessfulResponse() throws Exception {
        final ClassicHttpResponse response = Mockito.mock(ClassicHttpResponse.class);
        final HttpEntity entity = new StringEntity("42");
        Mockito.when(response.getCode()).thenReturn(200);
        Mockito.when(response.getEntity()).thenReturn(entity);

        final AbstractResponseHandler<Integer> handler = new AbstractResponseHandler<Integer>() {

          @Override
          public Integer handleEntity(final HttpEntity entity) throws IOException {
            return Integer.valueOf(new String(EntityUtils.toByteArray(entity)));
          }
        };
        final Integer number = handler.handleResponse(response);
        Assert.assertEquals(42, number.intValue());
    }

    @SuppressWarnings("boxing")
    @Test
    public void testUnsuccessfulResponse() throws Exception {
        final InputStream instream = Mockito.mock(InputStream.class);
        final HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(entity.isStreaming()).thenReturn(true);
        Mockito.when(entity.getContent()).thenReturn(instream);
        final ClassicHttpResponse response = Mockito.mock(ClassicHttpResponse.class);
        Mockito.when(response.getCode()).thenReturn(404);
        Mockito.when(response.getEntity()).thenReturn(entity);

        final BasicResponseHandler handler = new BasicResponseHandler();
        try {
            handler.handleResponse(response);
            Assert.fail("HttpResponseException expected");
        } catch (final HttpResponseException ex) {
            Assert.assertEquals(404, ex.getStatusCode());
        }
        Mockito.verify(entity).getContent();
        Mockito.verify(instream).close();
    }

}
