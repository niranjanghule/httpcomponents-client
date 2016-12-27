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
package org.apache.hc.client5.http.impl.cache;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hc.client5.http.cache.HeaderConstants;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.net.URIBuilder;

/**
 * @since 4.1
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
class CacheKeyGenerator {

    private static final URI BASE_URI = URI.create("http://example.com/");

    private URI normalize(final URI uri) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder(URIUtils.resolve(BASE_URI, uri)) ;
        if (builder.getHost() != null) {
            if (builder.getScheme() == null) {
                builder.setScheme("http");
            }
            if (builder.getPort() == -1) {
                if ("http".equalsIgnoreCase(builder.getScheme())) {
                    builder.setPort(80);
                } else if ("https".equalsIgnoreCase(builder.getScheme())) {
                    builder.setPort(443);
                }
            }
        }
        if (builder.getPath() == null) {
            builder.setPath("/");
        }
        return builder.build();
    }

    /**
     * For a given {@link HttpHost} and {@link HttpRequest} get a URI from the
     * pair that I can use as an identifier KEY into my HttpCache
     *
     * @param host The host for this request
     * @param req the {@link HttpRequest}
     * @return String the extracted URI
     */
    public String generateKey(final HttpHost host, final HttpRequest req) {
        try {
            URI uri = req.getUri();
            if (!uri.isAbsolute()) {
                uri = URIUtils.rewriteURI(uri, host);
            }
            return normalize(uri).toASCIIString();
        } catch (URISyntaxException ex) {
            return req.getRequestUri();
        }
    }

    public String generateKey(final URL url) {
        if (url == null) {
            return null;
        }
        try {
            return normalize(url.toURI()).toASCIIString();
        } catch (URISyntaxException ex) {
            return url.toString();
        }
    }

    protected String getFullHeaderValue(final Header[] headers) {
        if (headers == null) {
            return "";
        }
        final StringBuilder buf = new StringBuilder("");
        for (int i = 0; i < headers.length; i++) {
            final Header hdr = headers[i];
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(hdr.getValue().trim());
        }
        return buf.toString();
    }

    /**
     * For a given {@link HttpHost} and {@link HttpRequest} if the request has a
     * VARY header - I need to get an additional URI from the pair of host and
     * request so that I can also store the variant into my HttpCache.
     *
     * @param host The host for this request
     * @param req the {@link HttpRequest}
     * @param entry the parent entry used to track the variants
     * @return String the extracted variant URI
     */
    public String generateVariantURI(final HttpHost host, final HttpRequest req, final HttpCacheEntry entry) {
        if (!entry.hasVariants()) {
            return generateKey(host, req);
        }
        return generateVariantKey(req, entry) + generateKey(host, req);
    }

    /**
     * Compute a "variant key" from the headers of a given request that are
     * covered by the Vary header of a given cache entry. Any request whose
     * varying headers match those of this request should have the same
     * variant key.
     * @param req originating request
     * @param entry cache entry in question that has variants
     * @return a {@code String} variant key
     */
    public String generateVariantKey(final HttpRequest req, final HttpCacheEntry entry) {
        final List<String> variantHeaderNames = new ArrayList<>();
        final Iterator<HeaderElement> it = MessageSupport.iterate(entry, HeaderConstants.VARY);
        while (it.hasNext()) {
            final HeaderElement elt = it.next();
            variantHeaderNames.add(elt.getName());
        }
        Collections.sort(variantHeaderNames);

        final StringBuilder buf;
        try {
            buf = new StringBuilder("{");
            boolean first = true;
            for (final String headerName : variantHeaderNames) {
                if (!first) {
                    buf.append("&");
                }
                buf.append(URLEncoder.encode(headerName, StandardCharsets.UTF_8.name()));
                buf.append("=");
                buf.append(URLEncoder.encode(getFullHeaderValue(req.getHeaders(headerName)),
                        StandardCharsets.UTF_8.name()));
                first = false;
            }
            buf.append("}");
        } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException("couldn't encode to UTF-8", uee);
        }
        return buf.toString();
    }

}
