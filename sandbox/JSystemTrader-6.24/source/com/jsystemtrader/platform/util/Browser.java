package com.jsystemtrader.platform.util;

import java.io.IOException;
import java.lang.reflect.*;


public class Browser {
    private static final String ERR_MSG = "Couldn't launch web browser";

    private static void openURLDefault(final String url)
        throws BrowserUnavailableException, CannotFindBrowserException {
        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
        String selectedBrowser = null;

        try {
            for (String browser : browsers) {
                String[] command = new String[]{"which", browser};
                if (Runtime.getRuntime().exec(command).waitFor() == 0) {
                    selectedBrowser = browser;
                    break;
                }
            }

            if (selectedBrowser == null) {
                throw new CannotFindBrowserException();
            } else {
                Runtime.getRuntime().exec(new String[]{selectedBrowser, url});
            }
        } catch(IOException e) {
            throw new BrowserUnavailableException(e);
        } catch(InterruptedException e) {
            throw new BrowserUnavailableException(e);
        }
    }

    private static void openURLMac(final String url)
        throws BrowserUnavailableException {
        try {
            Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
            openURL.invoke(null, url);
        } catch(ClassNotFoundException e) {
            throw new BrowserUnavailableException(e);
        } catch(IllegalAccessException e) {
            throw new BrowserUnavailableException(e);
        } catch(InvocationTargetException e) {
            throw new BrowserUnavailableException(e);
        } catch(NoSuchMethodException e) {
            throw new BrowserUnavailableException(e);
        }
    }

    private static void openURLWindows(final String url)
        throws BrowserUnavailableException {
        try {
            Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);
        } catch(IOException e) {
            throw new BrowserUnavailableException(e);
        }
    }

    public static void openURL(final String url)
        throws BrowserUnavailableException, CannotFindBrowserException {
        final String osName = System.getProperty("os.name");

        if (osName.startsWith("Mac OS")) {
            openURLMac(url);
        } else if (osName.startsWith("Windows")) {
            openURLWindows(url);
        } else { //assume Unix or Linux
            openURLDefault(url);
        }
    }

    public static class BrowserUnavailableException extends Exception {
        private         BrowserUnavailableException() {
            super(ERR_MSG);
        }

        private         BrowserUnavailableException(final Throwable cause) {
            super(ERR_MSG, cause);
        }
    }

    public static class CannotFindBrowserException extends BrowserUnavailableException {
        private         CannotFindBrowserException() {
            super();
        }
    }
}
