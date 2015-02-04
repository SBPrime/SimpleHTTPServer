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

package org.primesoft.simplehttpserver.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.primesoft.simplehttpserver.SimpleHTTPServerMain;

/**
 * Reflection GET and SET operations.
 *
 * @author SBPrime
 */
public class Reflection {

    private static void log(String m) {
        SimpleHTTPServerMain.log(m);
    }

    public static <T> T invoke(Object instance, Class<T> resultClass,
            Method method, String message, Object... args) {
        try {
            method.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }
            try {
                return resultClass.cast(method.invoke(instance, args));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (InvocationTargetException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, unable to cast result.");
        }
        return null;
    }

    public static <T> T create(Class<T> resultClass,
            Constructor<?> ctor, String message, Object... args) {
        try {
            ctor.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }
            try {
                return resultClass.cast(ctor.newInstance(args));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (InstantiationException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (InvocationTargetException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, unable to cast result.");
        }
        return null;
    }

    public static <T> T get(Class<?> sourceClass, Class<T> fieldClass,
            String fieldName, String message) {
        return get(sourceClass, fieldClass, null, fieldName, message);
    }

    public static <T> T get(Object instance, Class<T> fieldClass,
            String fieldName, String message) {
        return get(instance.getClass(), fieldClass, instance, fieldName, message);
    }

    public static <T> T get(Class<?> sourceClass, Class<T> fieldClass,
            Object instance, String fieldName,
            String message) {
        try {
            Field field = sourceClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return fieldClass.cast(field.get(instance));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, field " + fieldName
                    + " not found.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, unable to cast result.");
        }

        return null;
    }

    public static void set(Object instance, String fieldName, Object value,
            String message) {
        set(instance.getClass(), instance, fieldName, value, message);
    }

    public static void set(Class<?> sourceClass, String fieldName, Object value,
            String message) {
        set(sourceClass, null, fieldName, value, message);
    }

    public static void set(Class<?> sourceClass,
            Object instance, String fieldName, Object value,
            String message) {
        try {
            Field field = sourceClass.getDeclaredField(fieldName);
            boolean accessible = field.isAccessible();

            //field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            int modifiers = modifiersField.getModifiers();
            boolean isFinal = (modifiers & Modifier.FINAL) == Modifier.FINAL;

            if (!accessible) {
                field.setAccessible(true);
            }
            if (isFinal) {
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            }
            try {
                field.set(instance, value);
            } finally {
                if (isFinal) {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                }
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported WorldEdit version, field " + fieldName
                    + " not found.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        }
    }

    public static void set(Object instance, Field field, Object value,
            String message) {
        try {
            boolean accessible = field.isAccessible();

            //field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            int modifiers = modifiersField.getModifiers();
            boolean isFinal = (modifiers & Modifier.FINAL) == Modifier.FINAL;

            if (!accessible) {
                field.setAccessible(true);
            }
            if (isFinal) {
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            }
            try {
                field.set(instance, value);
            } finally {
                if (isFinal) {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                }
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version.");
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, field modifiers not found.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        }
    }

    public static Constructor<?> findConstructor(Class<?> c, String message, Class<?>... paramTypes) {
        try {
            return c.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            ExceptionHelper.printException(ex, message + ": unsupported version, constructor not found.");
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, message + ": security exception.");
        }
        return null;
    }
}
