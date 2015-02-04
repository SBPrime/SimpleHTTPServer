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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.simplehttpserver.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class CommandManager {

    /**
     * Instance of the plugin
     */
    private final JavaPlugin m_plugin;

    /**
     * The command map
     */
    private final CommandMap m_commandMap;
    
    
    /**
     * The plugin command creator
     */
    private final Constructor<?> m_commandCtor;

    public CommandManager(JavaPlugin plugin) {
        m_plugin = plugin;

        PluginManager pm = Bukkit.getPluginManager();
        if (pm instanceof SimplePluginManager) {
            m_commandMap = Reflection.get(SimplePluginManager.class, CommandMap.class, 
                    pm, "commandMap", "Unable to get the commandMap");
        } else {
            m_commandMap = null;
        }

        m_commandCtor = Reflection.findConstructor(PluginCommand.class, 
                "Unable to get the pluginCommand constructor", String.class, Plugin.class);
    }

    /**
     * initialise commands
     *
     * @param cls
     */
    public void initializeCommands(Class<?> cls) {
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(CommandDescriptor.class)) {
                installCommand(method, method.getAnnotation(CommandDescriptor.class));
            }
        }
    }

    private void installCommand(Method method, CommandDescriptor cd) {
        if (m_commandCtor == null || m_commandMap == null) {
            return;
        }
        
        PluginCommand command = Reflection.create(PluginCommand.class, m_commandCtor, 
                "Unable to create command", cd.command(), m_plugin);
        String[] aliases = cd.aliases();
        if (aliases != null && aliases.length > 0) {
            command.setAliases(Arrays.asList(aliases));
        }
        
        CommandWrapper cdWrapper = new CommandWrapper(m_plugin, method, command);
        command.setDescription(cd.description());
        command.setExecutor(cdWrapper);
        command.setPermission(cd.permission());
        command.setTabCompleter(cdWrapper);
        command.setUsage(cd.usage());
        
        m_commandMap.register(m_plugin.getDescription().getName(), command);
    }
}
