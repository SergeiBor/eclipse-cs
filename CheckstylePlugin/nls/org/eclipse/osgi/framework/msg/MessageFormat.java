/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.osgi.framework.msg;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageFormat {

    // ResourceBundle holding the messages.
    protected ResourceBundle bundle;
    protected Locale locale;

    public MessageFormat(String bundleName) {
        init(bundleName, Locale.getDefault(), this.getClass());
    }

    public MessageFormat(String bundleName, Locale locale) {
        init(bundleName, locale, this.getClass());
    }

    public MessageFormat(String bundleName, Locale locale, Class clazz) {
        init(bundleName, locale, clazz);
    }

    protected void init(final String bundleName, final Locale locale, final Class clazz) {
        bundle = (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader loader = clazz.getClassLoader();

                if (loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }

                try {
                    return ResourceBundle.getBundle(bundleName, locale, loader);
                } catch (MissingResourceException e) {
                    return null;
                }
            }
        });

        this.locale = locale;
    }

    /**
     * Return the Locale object used for this MessageFormat object.
     * @return Locale of this object.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Retrieves a message which has no arguments.
     * @param       msg String
     *                  the key to look up.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg) {
        if (bundle == null) {
            return msg;
        }

        try {
            return bundle.getString(msg);
        } catch (MissingResourceException e) {
            return msg;
        }
    }

    /**
     * Retrieves a message which takes 1 argument.
     * @param       msg String
     *                  the key to look up.
     * @param       arg Object
     *                  the object to insert in the formatted output.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg, Object arg) {
        return getString(msg, new Object[] {arg});
    }

    /**
     * Retrieves a message which takes 1 integer argument.
     * @param       msg String
     *                  the key to look up.
     * @param       arg int
     *                  the integer to insert in the formatted output.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg, int arg) {
        return getString(msg, new Object[] {Integer.toString(arg)});
    }

    /**
     * Retrieves a message which takes 1 character argument.
     * @param       msg String
     *                  the key to look up.
     * @param       arg char
     *                  the character to insert in the formatted output.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg, char arg) {
        return getString(msg, new Object[] {String.valueOf(arg)});
    }

    /**
     * Retrieves a message which takes 2 arguments.
     * @param       msg String
     *                  the key to look up.
     * @param       arg1 Object
     *                  an object to insert in the formatted output.
     * @param       arg2 Object
     *                  another object to insert in the formatted output.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg, Object arg1, Object arg2) {
        return getString(msg, new Object[] {arg1, arg2});
    }

    /**
     * Retrieves a message which takes several arguments.
     * @param       msg String
     *                  the key to look up.
     * @param       args Object[]
     *                  the objects to insert in the formatted output.
     * @return      String
     *                  the message for that key in the system
     *                  message bundle.
     */
    public String getString(String msg, Object[] args) {
        String format = msg;

        if (bundle != null) {
            try {
                format = bundle.getString(msg);
            } catch (MissingResourceException e) {
            }
        }

        return format(format, args);
    }

    /**
     * Generates a formatted text string given a source string
     * containing "argument markers" of the form "{argNum}"
     * where each argNum must be in the range 0..9. The result
     * is generated by inserting the toString of each argument
     * into the position indicated in the string.
     * <p>
     * To insert the "{" character into the output, use a single
     * backslash character to escape it (i.e. "\{"). The "}"
     * character does not need to be escaped.
     * @param       format String
     *                  the format to use when printing.
     * @param       args Object[]
     *                  the arguments to use.
     * @return      String
     *                  the formatted message.
     */
    public static String format(String format, Object[] args) {
        StringBuffer answer = new StringBuffer();
        String[] argStrings = new String[args.length];

        for (int i = 0; i < args.length; ++i) {
            argStrings[i] = args[i] == null ? "<null>" : args[i].toString(); //$NON-NLS-1$
        }

        int lastI = 0;

        for (int i = format.indexOf('{', 0); i >= 0; i = format.indexOf('{', lastI)) {
            if (i != 0 && format.charAt(i - 1) == '\\') {
                // It's escaped, just print and loop.
                if (i != 1) {
                    answer.append(format.substring(lastI, i - 1));
                }
                answer.append('{');
                lastI = i + 1;
            } else {
                // It's a format character.
                if (i > format.length() - 3) {
                    // Bad format, just print and loop.
                    answer.append(format.substring(lastI, format.length()));
                    lastI = format.length();
                } else {
                    int argnum = (byte) Character.digit(format.charAt(i + 1), 10);
                    if (argnum < 0 || format.charAt(i + 2) != '}') {
                        // Bad format, just print and loop.
                        answer.append(format.substring(lastI, i + 1));
                        lastI = i + 1;
                    } else {
                        // Got a good one!
                        answer.append(format.substring(lastI, i));
                        if (argnum >= argStrings.length) {
                            answer.append("<missing argument>"); //$NON-NLS-1$
                        } else {
                            answer.append(argStrings[argnum]);
                        }
                        lastI = i + 3;
                    }
                }
            }
        }

        if (lastI < format.length()) {
            answer.append(format.substring(lastI, format.length()));
        }

        return answer.toString();
    }
}
