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

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import org.primesoft.simplehttpserver.SimpleHTTPServerMain;
import org.primesoft.simplehttpserver.api.IHttpServer;
import org.primesoft.simplehttpserver.api.IService;
import org.primesoft.simplehttpserver.utils.ExceptionHelper;
import org.primesoft.simplehttpserver.utils.Pair;

/**
 *
 * @author SBPrime
 */
public class SunHttpServer implements IHttpServer {

    /**
     * Log a message
     *
     * @param msg
     */
    private static void log(String msg) {
        SimpleHTTPServerMain.log(msg);
    }

    /**
     * MTA access mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The Server
     */
    private HttpServer m_server;

    /**
     * List of all registered contexts
     */
    private final HashMap<String, IService> m_registeredHandlers = new HashMap<String, IService>();

    /**
     * Map of all service wrappers (optimalization)
     */
    private final HashMap<IService, Pair<Integer, ServiceWrapper>> m_serviceWrappers = new HashMap<IService, Pair<Integer, ServiceWrapper>>();

    @Override
    public boolean startServer(int port) {
        synchronized (m_mutex) {
            if (m_server != null) {
                stopServer();
            }

            HttpServer server;

            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                server.setExecutor(null);
                server.start();

                m_server = server;

                log("Server started");

                restoreServices();
                return true;
            } catch (IOException ex) {
                ExceptionHelper.printException(ex, "Unable to create the HttpServer");

                return false;
            }
        }
    }

    private boolean stopServer(boolean clean) {
        synchronized (m_mutex) {
            if (m_server == null) {
                log("Server not running");
                return false;
            }

            m_server.stop(0);
            m_server = null;

            if (clean) {
                m_registeredHandlers.clear();
                m_serviceWrappers.clear();
            }

            log("Server stopped");
            return true;
        }
    }

    @Override
    public boolean stopServer() {
        return stopServer(true);
    }

    @Override
    public void registerService(String context, IService service) {
        synchronized (m_mutex) {
            if (m_registeredHandlers.containsKey(context)) {
                log("Service for " + context + " already registered.");
                return;
            }

            m_registeredHandlers.put(context, service);

            if (m_server != null) {
                ServiceWrapper wrapper = getWrapper(service);
                m_server.createContext(context, wrapper);
            }

            log("Service for " + context + " registered.");
        }
    }

    @Override
    public void unregisterService(String context) {
        synchronized (m_mutex) {
            if (!m_registeredHandlers.containsKey(context)) {
                log("Service for " + context + " notregistered.");
                return;
            }

            IService service = m_registeredHandlers.get(context);
            m_registeredHandlers.remove(context);

            if (!m_serviceWrappers.containsKey(service)) {
                return;
            }
            
            Pair<Integer, ServiceWrapper> p = m_serviceWrappers.get(service);
            m_serviceWrappers.remove(service);
            
            ServiceWrapper wrapper = p.getX2();
            
            if (m_server != null) {
                m_server.createContext(context, wrapper);
            }
            
            if (p.getX1() > 1) {
                m_serviceWrappers.put(service, new Pair<Integer, ServiceWrapper>(p.getX1() - 1, wrapper));
            }

        }
    }

    @Override
    public boolean restart(int port) {
        synchronized (m_mutex) {
            stopServer(false);
            return startServer(port);
        }
    }

    private void restoreServices() {
        synchronized (m_mutex) {
            if (m_server == null || m_registeredHandlers.isEmpty()) {
                return;
            }

            log("Restoring services...");

            m_serviceWrappers.clear();
            for (String context : m_registeredHandlers.keySet()) {
                IService service = m_registeredHandlers.get(context);
                ServiceWrapper wrapper = getWrapper(service);
                m_server.createContext(context, wrapper);
            }
        }
    }

    private ServiceWrapper getWrapper(IService service) {
        ServiceWrapper result;
        synchronized (m_mutex) {
            if (!m_serviceWrappers.containsKey(service)) {
                result = new ServiceWrapper(service);
                m_serviceWrappers.put(service, new Pair<Integer, ServiceWrapper>(1, result));
            } else {
                Pair<Integer, ServiceWrapper> p = m_serviceWrappers.get(service);
                m_serviceWrappers.remove(service);

                result = p.getX2();
                m_serviceWrappers.put(service, new Pair<Integer, ServiceWrapper>(p.getX1() + 1, result));
            }
        }

        return result;
    }

}
