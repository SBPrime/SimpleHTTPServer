/*
 * SimpleHTTPServer a plugin that allows you to run a simple HTTP server
 * directly from Spigot.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) SimpleHTTPServer contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.simplehttpserver.implementation;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.primesoft.simplehttpserver.api.IHeaders;
import org.primesoft.simplehttpserver.api.IHttpRequest;

/**
 *
 * @author SBPrime
 */
public class HttpRequest implements IHttpRequest {

    /**
     * The http request source
     */
    private final HttpExchange m_parent;

    /**
     * The request uri
     */
    private final URI m_requestUri;

    /**
     * The request method
     */
    private final String m_method;

    /**
     * The request header
     */
    private final IHeaders m_requestHeader;

    /**
     * The response header
     */
    private final IHeaders m_responseHeader;

    /**
     * Indicates that the response was already send
     */
    private boolean m_responseSend;

    public HttpRequest(HttpExchange p) {
        m_parent = p;
        m_requestUri = p.getRequestURI();
        m_method = p.getRequestMethod();

        m_requestHeader = new HeadersWrapper(p.getRequestHeaders());
        m_responseHeader = new HeadersWrapper(p.getResponseHeaders());
    }

    @Override
    public URI getUri() {
        return m_requestUri;
    }

    @Override
    public String getMethod() {
        return m_method;
    }

    @Override
    public IHeaders getRequestHeader() {
        return m_requestHeader;
    }

    @Override
    public IHeaders getResponseHeader() {
        return m_responseHeader;
    }

    @Override
    public InputStream getRequestBody() {
        return m_parent.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        return m_parent.getResponseBody();
    }

    @Override
    public void sendResponse(int httpCode, int bodySize) throws IOException {
        if (m_responseSend) {
            throw new IllegalStateException("Response already send");
        }

        m_responseSend = true;
        m_parent.sendResponseHeaders(httpCode, bodySize);
    }

    public boolean isResponseSend() {
        return m_responseSend;
    }
}
