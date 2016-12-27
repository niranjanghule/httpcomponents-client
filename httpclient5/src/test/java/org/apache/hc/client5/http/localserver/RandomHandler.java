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

package org.apache.hc.client5.http.localserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * A handler that generates random data.
 */
public class RandomHandler implements HttpRequestHandler {

    /**
     * Handles a request by generating random data.
     * The length of the response can be specified in the request URI
     * as a number after the last /. For example /random/whatever/20
     * will generate 20 random bytes in the printable ASCII range.
     * If the request URI ends with /, a random number of random bytes
     * is generated, but at least one.
     *
     * @param request   the request
     * @param response  the response
     * @param context   the context
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    @Override
    public void handle(final ClassicHttpRequest request,
                       final ClassicHttpResponse response,
                       final HttpContext context)
        throws HttpException, IOException {

        final String method = request.getMethod().toUpperCase(Locale.ROOT);
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            throw new MethodNotSupportedException
                (method + " not supported by " + getClass().getName());
        }

        final String uri = request.getRequestUri();
        final int  slash = uri.lastIndexOf('/');
        int length = -1;
        if (slash < uri.length()-1) {
            try {
                // no more than Integer, 2 GB ought to be enough for anybody
                length = Integer.parseInt(uri.substring(slash+1));

                if (length < 0) {
                    response.setCode(HttpStatus.SC_BAD_REQUEST);
                    response.setReasonPhrase("LENGTH " + length);
                }
            } catch (final NumberFormatException nfx) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setReasonPhrase(nfx.toString());
            }
        } else {
            // random length, but make sure at least something is sent
            length = 1 + (int)(Math.random() * 79.0);
        }

        if (length >= 0) {

            response.setCode(HttpStatus.SC_OK);

            if (!"HEAD".equals(method)) {
                final RandomEntity entity = new RandomEntity(length);
                entity.setContentType("text/plain; charset=US-ASCII");
                response.setEntity(entity);
            } else {
                response.setHeader("Content-Type",
                                   "text/plain; charset=US-ASCII");
                response.setHeader("Content-Length",
                                   String.valueOf(length));
            }
        }

    } // handle


    /**
     * An entity that generates random data.
     * This is an outgoing entity, it supports {@link #writeTo writeTo}
     * but not {@link #getContent getContent}.
     */
    public static class RandomEntity extends AbstractHttpEntity {

        /** The range from which to generate random data. */
        private final static byte[] RANGE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                .getBytes(StandardCharsets.US_ASCII);

        /** The length of the random data to generate. */
        protected final long length;


        /**
         * Creates a new entity generating the given amount of data.
         *
         * @param len   the number of random bytes to generate,
         *              0 to maxint
         */
        public RandomEntity(final long len) {
            length = len;
        }

        /**
         * Tells that this entity is not streaming.
         *
         * @return      false
         */
        @Override
        public final boolean isStreaming() {
            return false;
        }

        /**
         * Tells that this entity is repeatable, in a way.
         * Repetitions will generate different random data,
         * unless perchance the same random data is generated twice.
         *
         * @return      {@code true}
         */
        @Override
        public boolean isRepeatable() {
            return true;
        }

        /**
         * Obtains the size of the random data.
         *
         * @return      the number of random bytes to generate
         */
        @Override
        public long getContentLength() {
            return length;
        }


        /**
         * Not supported.
         * This method throws an exception.
         *
         * @return      never anything
         */
        @Override
        public InputStream getContent() {
            throw new UnsupportedOperationException();
        }


        /**
         * Generates the random content.
         *
         * @param out   where to write the content to
         */
        @Override
        public void writeTo(final OutputStream out) throws IOException {

            final int blocksize = 2048;
            int       remaining = (int) length; // range checked in constructor
            final byte[]         data = new byte[Math.min(remaining, blocksize)];

            while (remaining > 0) {
                final int end = Math.min(remaining, data.length);

                double value = 0.0;
                for (int i = 0; i < end; i++) {
                    // we get 5 random characters out of one random value
                    if (i%5 == 0) {
                        value = Math.random();
                    }
                    value = value * RANGE.length;
                    final int d = (int) value;
                    value = value - d;
                    data[i] = RANGE[d];
                }
                out.write(data, 0, end);
                out.flush();

                remaining = remaining - end;
            }
            out.close();

        }

        @Override
        public void close() throws IOException {
        }

    }

}
