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

package org.apache.hc.client5.http.config;

import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;

/**
 *  Immutable class encapsulating request configuration items.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestConfig implements Cloneable {

    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);
    private static final int DEFAULT_CONNECT_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);
    private static final int DEFAULT_SOCKET_TIMEOUT = -1;

    public static final RequestConfig DEFAULT = new Builder().build();

    private final boolean expectContinueEnabled;
    private final HttpHost proxy;
    private final InetAddress localAddress;
    private final String cookieSpec;
    private final boolean redirectsEnabled;
    private final boolean circularRedirectsAllowed;
    private final int maxRedirects;
    private final boolean authenticationEnabled;
    private final Collection<String> targetPreferredAuthSchemes;
    private final Collection<String> proxyPreferredAuthSchemes;
    private final int connectionRequestTimeout;
    private final int connectTimeout;
    private final int socketTimeout;
    private final boolean contentCompressionEnabled;

    /**
     * Intended for CDI compatibility
    */
    protected RequestConfig() {
        this(false, null, null, null, false, false, 0, false, null, null,
                DEFAULT_CONNECTION_REQUEST_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT, false);
    }

    RequestConfig(
            final boolean expectContinueEnabled,
            final HttpHost proxy,
            final InetAddress localAddress,
            final String cookieSpec,
            final boolean redirectsEnabled,
            final boolean circularRedirectsAllowed,
            final int maxRedirects,
            final boolean authenticationEnabled,
            final Collection<String> targetPreferredAuthSchemes,
            final Collection<String> proxyPreferredAuthSchemes,
            final int connectionRequestTimeout,
            final int connectTimeout,
            final int socketTimeout,
            final boolean contentCompressionEnabled) {
        super();
        this.expectContinueEnabled = expectContinueEnabled;
        this.proxy = proxy;
        this.localAddress = localAddress;
        this.cookieSpec = cookieSpec;
        this.redirectsEnabled = redirectsEnabled;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        this.maxRedirects = maxRedirects;
        this.authenticationEnabled = authenticationEnabled;
        this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
        this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.contentCompressionEnabled = contentCompressionEnabled;
    }

    /**
     * Determines whether the 'Expect: 100-Continue' handshake is enabled
     * for entity enclosing methods. The purpose of the 'Expect: 100-Continue'
     * handshake is to allow a client that is sending a request message with
     * a request body to determine if the origin server is willing to
     * accept the request (based on the request headers) before the client
     * sends the request body.
     * <p>
     * The use of the 'Expect: 100-continue' handshake can result in
     * a noticeable performance improvement for entity enclosing requests
     * (such as POST and PUT) that require the target server's
     * authentication.
     * </p>
     * <p>
     * 'Expect: 100-continue' handshake should be used with caution, as it
     * may cause problems with HTTP servers and proxies that do not support
     * HTTP/1.1 protocol.
     * </p>
     * <p>
     * Default: {@code false}
     * </p>
     */
    public boolean isExpectContinueEnabled() {
        return expectContinueEnabled;
    }

    /**
     * Returns HTTP proxy to be used for request execution.
     * <p>
     * Default: {@code null}
     * </p>
     */
    public HttpHost getProxy() {
        return proxy;
    }

    /**
     * Returns local address to be used for request execution.
     * <p>
     * On machines with multiple network interfaces, this parameter
     * can be used to select the network interface from which the
     * connection originates.
     * </p>
     * <p>
     * Default: {@code null}
     * </p>
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Determines the name of the cookie specification to be used for HTTP state
     * management.
     * <p>
     * Default: {@code null}
     * </p>
     */
    public String getCookieSpec() {
        return cookieSpec;
    }

    /**
     * Determines whether redirects should be handled automatically.
     * <p>
     * Default: {@code true}
     * </p>
     */
    public boolean isRedirectsEnabled() {
        return redirectsEnabled;
    }

    /**
     * Determines whether circular redirects (redirects to the same location) should
     * be allowed. The HTTP spec is not sufficiently clear whether circular redirects
     * are permitted, therefore optionally they can be enabled
     * <p>
     * Default: {@code false}
     * </p>
     */
    public boolean isCircularRedirectsAllowed() {
        return circularRedirectsAllowed;
    }

    /**
     * Returns the maximum number of redirects to be followed. The limit on number
     * of redirects is intended to prevent infinite loops.
     * <p>
     * Default: {@code 50}
     * </p>
     */
    public int getMaxRedirects() {
        return maxRedirects;
    }

    /**
     * Determines whether authentication should be handled automatically.
     * <p>
     * Default: {@code true}
     * </p>
     */
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    /**
     * Determines the order of preference for supported authentication schemes
     * when authenticating with the target host.
     * <p>
     * Default: {@code null}
     * </p>
     */
    public Collection<String> getTargetPreferredAuthSchemes() {
        return targetPreferredAuthSchemes;
    }

    /**
     * Determines the order of preference for supported authentication schemes
     * when authenticating with the proxy host.
     * <p>
     * Default: {@code null}
     * </p>
     */
    public Collection<String> getProxyPreferredAuthSchemes() {
        return proxyPreferredAuthSchemes;
    }

    /**
     * Returns the timeout in milliseconds used when requesting a connection
     * from the connection manager. A timeout value of zero is interpreted
     * as an infinite timeout.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: 2 minutes.
     * </p>
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: no timeout
     * </p>
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: 2 minutes.
     * </p>
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Determines whether the target server is requested to compress content.
     * <p>
     * Default: {@code true}
     * </p>
     *
     * @since 4.5
     */
    public boolean isContentCompressionEnabled() {
        return contentCompressionEnabled;
    }

    @Override
    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("expectContinueEnabled=").append(expectContinueEnabled);
        builder.append(", proxy=").append(proxy);
        builder.append(", localAddress=").append(localAddress);
        builder.append(", cookieSpec=").append(cookieSpec);
        builder.append(", redirectsEnabled=").append(redirectsEnabled);
        builder.append(", maxRedirects=").append(maxRedirects);
        builder.append(", circularRedirectsAllowed=").append(circularRedirectsAllowed);
        builder.append(", authenticationEnabled=").append(authenticationEnabled);
        builder.append(", targetPreferredAuthSchemes=").append(targetPreferredAuthSchemes);
        builder.append(", proxyPreferredAuthSchemes=").append(proxyPreferredAuthSchemes);
        builder.append(", connectionRequestTimeout=").append(connectionRequestTimeout);
        builder.append(", connectTimeout=").append(connectTimeout);
        builder.append(", socketTimeout=").append(socketTimeout);
        builder.append(", contentCompressionEnabled=").append(contentCompressionEnabled);
        builder.append("]");
        return builder.toString();
    }

    public static RequestConfig.Builder custom() {
        return new Builder();
    }

    public static RequestConfig.Builder copy(final RequestConfig config) {
        return new Builder()
            .setExpectContinueEnabled(config.isExpectContinueEnabled())
            .setProxy(config.getProxy())
            .setLocalAddress(config.getLocalAddress())
            .setCookieSpec(config.getCookieSpec())
            .setRedirectsEnabled(config.isRedirectsEnabled())
            .setCircularRedirectsAllowed(config.isCircularRedirectsAllowed())
            .setMaxRedirects(config.getMaxRedirects())
            .setAuthenticationEnabled(config.isAuthenticationEnabled())
            .setTargetPreferredAuthSchemes(config.getTargetPreferredAuthSchemes())
            .setProxyPreferredAuthSchemes(config.getProxyPreferredAuthSchemes())
            .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
            .setConnectTimeout(config.getConnectTimeout())
            .setSocketTimeout(config.getSocketTimeout())
            .setContentCompressionEnabled(config.isContentCompressionEnabled());
    }

    public static class Builder {

        private boolean expectContinueEnabled;
        private HttpHost proxy;
        private InetAddress localAddress;
        private String cookieSpec;
        private boolean redirectsEnabled;
        private boolean circularRedirectsAllowed;
        private int maxRedirects;
        private boolean authenticationEnabled;
        private Collection<String> targetPreferredAuthSchemes;
        private Collection<String> proxyPreferredAuthSchemes;
        private int connectionRequestTimeout;
        private int connectTimeout;
        private int socketTimeout;
        private boolean contentCompressionEnabled;

        Builder() {
            super();
            this.redirectsEnabled = true;
            this.maxRedirects = 50;
            this.authenticationEnabled = true;
            this.connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
            this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
            this.socketTimeout = DEFAULT_SOCKET_TIMEOUT;
            this.contentCompressionEnabled = true;
        }

        public Builder setExpectContinueEnabled(final boolean expectContinueEnabled) {
            this.expectContinueEnabled = expectContinueEnabled;
            return this;
        }

        public Builder setProxy(final HttpHost proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder setLocalAddress(final InetAddress localAddress) {
            this.localAddress = localAddress;
            return this;
        }

        public Builder setCookieSpec(final String cookieSpec) {
            this.cookieSpec = cookieSpec;
            return this;
        }

        public Builder setRedirectsEnabled(final boolean redirectsEnabled) {
            this.redirectsEnabled = redirectsEnabled;
            return this;
        }

        public Builder setCircularRedirectsAllowed(final boolean circularRedirectsAllowed) {
            this.circularRedirectsAllowed = circularRedirectsAllowed;
            return this;
        }

        public Builder setMaxRedirects(final int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }

        public Builder setAuthenticationEnabled(final boolean authenticationEnabled) {
            this.authenticationEnabled = authenticationEnabled;
            return this;
        }

        public Builder setTargetPreferredAuthSchemes(final Collection<String> targetPreferredAuthSchemes) {
            this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
            return this;
        }

        public Builder setProxyPreferredAuthSchemes(final Collection<String> proxyPreferredAuthSchemes) {
            this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
            return this;
        }

        public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setConnectTimeout(final int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSocketTimeout(final int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder setContentCompressionEnabled(final boolean contentCompressionEnabled) {
            this.contentCompressionEnabled = contentCompressionEnabled;
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(
                    expectContinueEnabled,
                    proxy,
                    localAddress,
                    cookieSpec,
                    redirectsEnabled,
                    circularRedirectsAllowed,
                    maxRedirects,
                    authenticationEnabled,
                    targetPreferredAuthSchemes,
                    proxyPreferredAuthSchemes,
                    connectionRequestTimeout,
                    connectTimeout,
                    socketTimeout,
                    contentCompressionEnabled);
        }

    }

}
