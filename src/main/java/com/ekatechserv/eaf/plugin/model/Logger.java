package com.ekatechserv.eaf.plugin.model;

import com.ekatechserv.eaf.plugin.util.PluginUtil;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Logger {

    public static final int LL_NORMAL = 0;
    public static final int LL_TRACE = 1;
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * This var is initialized during Plugin.perform()
     */
    private PrintStream logger;
    /**
     * Plugin log level (normal or trace)
     */
    private int iLogLevel = LL_NORMAL;

    private static Logger singleInstance;

    public static void init(PrintStream logger, String loglevel) {
        singleInstance = new Logger(logger, loglevel);
    }

    private Logger(PrintStream logger, String loglevel) {
        if (loglevel != null && loglevel.equals("trace")) {
            iLogLevel = LL_TRACE;
        } else {
            iLogLevel = LL_NORMAL;
        }
        this.logger = logger;

    }

    public int getLevel() {
        return this.iLogLevel;
    }

    public void setLevel(int iLogLevel) {
        this.iLogLevel = iLogLevel;
    }

    public static void info(String s) {
        if (singleInstance != null) {
            singleInstance.log(s, LL_NORMAL);
        }
    }

    public static void trace(String s) {
        if (singleInstance != null) {
            singleInstance.log(s, LL_TRACE);
        }
    }

    public static void infoln(String s) {
        if (singleInstance != null) {
            singleInstance.log(s + "\n", LL_NORMAL);
        }
    }

    public static void traceln(String s) {
        if (singleInstance != null) {
            singleInstance.log(s + "\n", LL_TRACE);
        }
    }

    /**
     * Logs exception.
     *
     * @param e exception to log
     */
    public static void exception(Throwable e) {
        if (singleInstance != null) {
            singleInstance.log(e);
        }
    }

    /**
     * Logs string depending on the actual log level. This helper function
     * allows to specify short message for normal logging and extended message
     * for trace
     *
     * @param trace string to log if current log level == LL_TRACE
     * @param normal string to log if current log level == LL_NORMAL
     */
    public static void tori(String trace, String normal) {
        if (singleInstance != null) {
            if (singleInstance.getLevel() == Logger.LL_TRACE) {
                trace(trace);
            } else if (singleInstance.getLevel() == Logger.LL_NORMAL) {
                infoln(normal);
            }
        }
    }

    private void log(String s, int level) {
        if (logger != null) {
            if (level <= iLogLevel) {
                logger.print(df.format(new java.util.Date()) + " " + s);
            }
        }
    }

    private void log(Throwable e) {
        if (logger != null) {
            logger.println(PluginUtil.getTraceAsString(e));
        }
    }
}
