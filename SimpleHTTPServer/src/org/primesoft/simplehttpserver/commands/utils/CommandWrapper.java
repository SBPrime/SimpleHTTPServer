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
package org.primesoft.simplehttpserver.commands.utils;

import java.lang.reflect.Method;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.simplehttpserver.SimpleHTTPServerMain;
import org.primesoft.simplehttpserver.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class CommandWrapper extends BaseCommand {

    private static final Object[] s_objectArray = new Object[0];

    private final Command m_command;
    private final Method m_method;
    private final JavaPlugin m_plugin;

    private final int m_argCount;
    private final int m_inGamePos;
    private final int m_argsPos;
    private final int m_pluginPos;
    private final int m_cmdSenderPos;

    public CommandWrapper(JavaPlugin plugin, Method method, Command command) {
        m_command = command;
        m_method = method;
        m_plugin = plugin;

        Class<?>[] params = method.getParameterTypes();

        int inGamePos = -1;
        int argsPos = -1;
        int pluginPos = -1;
        int cmdSenderPos = -1;

        if (params != null && params.length > 0) {
            m_argCount = params.length;
            for (int idx = 0; idx < params.length; idx++) {
                Class<?> cls = params[idx];
                if (cls.isAssignableFrom(CommandSender.class)) {
                    cmdSenderPos = idx;
                } else if (cls.isAssignableFrom(Player.class)) {
                    inGamePos = idx;
                } else if (cls.isAssignableFrom(s_objectArray.getClass())) {
                    argsPos = idx;
                } else if (cls.isAssignableFrom(JavaPlugin.class)) {
                    pluginPos = idx;
                }
            }
        } else {
            m_argCount = 0;
        }

        m_inGamePos = inGamePos;
        m_argsPos = argsPos;
        m_pluginPos = pluginPos;
        m_cmdSenderPos = cmdSenderPos;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String name, String[] args) {
        String perm = m_command.getPermission();
        Player player = (Player) ((cs instanceof Player) ? (Player) cs : null);
        Object[] argList = new Object[m_argCount];

        if (m_inGamePos >= 0) {
            if (player == null) {
                SimpleHTTPServerMain.say(null, "Command available only ingame");
                return true;
            }

            argList[m_inGamePos] = player;
        } else if (m_cmdSenderPos >= 0) {
            argList[m_cmdSenderPos] = cs;
        }

        if (m_argsPos >= 0) {
            if (args == null) {
                return false;
            }

            argList[m_argsPos] = args;
        }

        if (m_pluginPos >= 0) {
            argList[m_pluginPos] = m_plugin;
        }

        if (perm == null || perm.isEmpty() || cs.isOp() || cs.hasPermission(perm)) {
            return Reflection.invoke(name, Boolean.class, m_method,
                    "Unable to invoke command", argList);
        }

        return false;
    }
}
