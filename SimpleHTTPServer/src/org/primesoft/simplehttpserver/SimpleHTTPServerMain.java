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
package org.primesoft.simplehttpserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.simplehttpserver.api.IApi;
import org.primesoft.simplehttpserver.api.IHttpServer;
import org.primesoft.simplehttpserver.commands.GlobalCommands;
import org.primesoft.simplehttpserver.commands.utils.CommandManager;
import org.primesoft.simplehttpserver.configuration.ConfigProvider;
import org.primesoft.simplehttpserver.implementation.SimpleApi;
import org.primesoft.simplehttpserver.implementation.SunHttpServer;
import org.primesoft.simplehttpserver.metrics.MetricsLite;
import org.primesoft.simplehttpserver.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class SimpleHTTPServerMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.SimpleHTTPServer");

    private static ConsoleCommandSender s_console;

    private static String s_prefix = null;

    private static final String s_logFormat = "%s %s";

    private static SimpleHTTPServerMain s_instance;

    private MetricsLite m_metrics;

    private CommandManager m_commandManager;

    public static SimpleHTTPServerMain getInstance() {
        return s_instance;
    }

    public static void log(String msg) {
        String formated;
        if (msg == null) {
            return;
        }
        if (s_prefix == null || s_logFormat == null) {
            formated = msg;
        } else {
            formated = String.format(s_logFormat, s_prefix, msg);
        }

        if (s_log == null) {
            System.out.println(formated);
            return;
        }

        s_log.log(Level.INFO, formated);
    }

    public static void say(Player player, String msg) {
        if (player == null) {
            s_console.sendRawMessage(msg);
        } else {
            player.sendRawMessage(msg);
        }
    }

    /**
     * Instance of the API
     */
    private IApi m_api;

    /**
     * Get the instance of API
     *
     * @return
     */
    public IApi getAPI() {
        return m_api;
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_console = getServer().getConsoleSender();
        s_instance = this;

        try {
            MetricsLite metrics = new MetricsLite(this);
            if (!metrics.isOptOut()) {
                m_metrics = metrics;
                m_metrics.start();
            }
        } catch (IOException e) {
            ExceptionHelper.printException(e, "Error initializing MCStats");
        }

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
        }

        m_commandManager = new CommandManager(this);
        m_commandManager.initializeCommands(GlobalCommands.class);

        m_api = new SimpleApi(new SunHttpServer());

        log("Enabled");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        IHttpServer server = (m_api != null) ? m_api.getServer() : null;
        if (server != null) {
            server.stopServer();
        }

        log("Disabled");
    }
}
