/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.test;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Log4j2Factory;
import com.hazelcast.logging.LogEvent;
import com.hazelcast.logging.LoggerFactorySupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * The factory uses log4j2 internally, however loggers always
 * return true to guards such as `isFinestEnabled()` etc.
 * <p>
 * The real filtering is happening in the log4js once again.
 * Thus it covers branches guarded by is-level-enabled checks
 * yet the real logging is configurable via log4j2.xml
 *
 * It also supports changing logging configuration on-the-fly, see {@link TestLoggerFactory#changeConfigFile}
 * and
 */
public class TestLoggerFactory extends LoggerFactorySupport {

    /**
     * Log4j XML configuration being currently used. Null indicated the default Log4j behavior.
     */
    private URI configFile = null;

    /**
     * Store all the logging context being created, because we need to clear them in order to change the configuration
     * to reload it.
     */
    private final List<LoggerContext> loggerContexts = new ArrayList<LoggerContext>();

    /**
     * Changes the configuration to be used.
     *
     * @param configName The Log4j XML configuration to be used, null for default behavior.
     */
    public void changeConfigFile(String configName) {
        for (LoggerContext context: loggerContexts) {
            LogManager.getFactory().removeContext(context);
        }

        configFile = configName != null ? URI.create(configName) : null;
        clearLoadedLoggers();
        loggerContexts.clear();
    }

    protected ILogger createLogger(String name) {
        LoggerContext loggerContext = LogManager.getContext(null, false, configFile);
        loggerContexts.add(loggerContext);

        return new DelegatingTestLogger(new Log4j2Factory.Log4j2Logger(loggerContext.getLogger(name)));
    }

    private static class DelegatingTestLogger implements ILogger {

        private static final long WARNING_THRESHOLD_NANOS = MILLISECONDS.toNanos(500);

        private ILogger delegate;

        private DelegatingTestLogger(ILogger delegate) {
            this.delegate = delegate;
        }

        @Override
        public void finest(String message) {
            long startTime = System.nanoTime();
            delegate.finest(message);
            logOnSlowLogging(startTime);
        }

        @Override
        public void finest(Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.finest(thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void finest(String message, Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.finest(message, thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public boolean isFinestEnabled() {
            return true;
        }

        @Override
        public void fine(String message) {
            long startTime = System.nanoTime();
            delegate.fine(message);
            logOnSlowLogging(startTime);
        }

        @Override
        public void fine(Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.fine(thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void fine(String message, Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.fine(message, thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public boolean isFineEnabled() {
            return true;
        }

        @Override
        public void info(String message) {
            long startTime = System.nanoTime();
            delegate.info(message);
            logOnSlowLogging(startTime);
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public void warning(String message) {
            long startTime = System.nanoTime();
            delegate.warning(message);
            logOnSlowLogging(startTime);
        }

        @Override
        public void warning(Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.warning(thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void warning(String message, Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.warning(message, thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public boolean isWarningEnabled() {
            return true;
        }

        @Override
        public void severe(String message) {
            long startTime = System.nanoTime();
            delegate.severe(message);
            logOnSlowLogging(startTime);
        }

        @Override
        public void severe(Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.severe(thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void severe(String message, Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.severe(message, thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void log(Level level, String message) {
            long startTime = System.nanoTime();
            delegate.log(level, message);
            logOnSlowLogging(startTime);
        }

        @Override
        public void log(Level level, String message, Throwable thrown) {
            long startTime = System.nanoTime();
            delegate.log(level, message, thrown);
            logOnSlowLogging(startTime);
        }

        @Override
        public void log(LogEvent logEvent) {
            long startTime = System.nanoTime();
            delegate.log(logEvent);
            logOnSlowLogging(startTime);
        }

        @Override
        public Level getLevel() {
            return Level.ALL;
        }

        @Override
        public boolean isLoggable(Level level) {
            return true;
        }

        private void logOnSlowLogging(long startTime) {
            long durationNanos = System.nanoTime() - startTime;
            if (durationNanos > WARNING_THRESHOLD_NANOS) {
                long durationMillis = NANOSECONDS.toMillis(durationNanos);
                delegate.warning("Logging took " + durationMillis + " ms.");
            }
        }
    }
}
