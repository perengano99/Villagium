/*
 * PLogger - Sistema de logging avanzado para mods NeoForge/Minecraft
 * Autor: perengano99
 * Descripción: Logger modular, extensible y eficiente para debugging, performance y trazabilidad en entornos de tick crítico.
 * @since 2026-01-05
 */
package com.perengano99.villagium.core.util.logging;

import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logger avanzado para Minecraft 26.1+ (NeoForge 2.0.137, Java 25).
 * Provee utilidades para bloques de log, cronómetros, stacktraces enriquecidos y contexto dinámico.
 * @author perengano99
 * @since 2026-01-05
 */
public class PLogger implements Logger {
    final org.slf4j.Logger lgr;

    /**
     * Constructor principal.
     * @param lgr Logger SLF4J subyacente.
     */
    public PLogger(org.slf4j.Logger lgr) {
        if (lgr == null) throw new IllegalArgumentException("Logger cannot be null");
        this.lgr = lgr;
    }

    /**
     * Loguea información con formato y argumentos.
     */
    @Override
    public void info(String message, Object... args) {
        if (args != null && args.length > 0)
            lgr.info(message, args);
        else
            lgr.info(message);
    }

    /**
     * Loguea información desde un objeto.
     */
    @Override
    public void info(Object message, Object... args) {
        if (args != null && args.length > 0)
            lgr.info(message.toString(), args);
        else
            lgr.info(message.toString());
    }

    /**
     * Loguea error con formato y argumentos.
     */
    @Override
    public void error(String message, Object... args) {
        if (args != null && args.length > 0)
            lgr.error(message, args);
        else
            lgr.error(message);
    }

    /**
     * Loguea error desde un objeto.
     */
    @Override
    public void error(Object message, Object... args) {
        if (args != null && args.length > 0)
            lgr.error(message.toString(), args);
        else
            lgr.error(message.toString());
    }

    /**
     * Loguea error y stacktrace parcial.
     * @param traceSize Cantidad de elementos del stacktrace (-1 para todos).
     */
    @Override
    public void error(int traceSize, Object message, Object... args) {
        error(message, args);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int count = traceSize != -1 ? traceSize + 2 : stackTrace.length;
        for (int i = 2; i < count; i++) {
            if (stackElementAt(stackTrace, i, "⟝", "    "))
                break;
        }
    }

    /**
     * Loguea error con excepción.
     */
    @Override
    public void error(Throwable throwable, String message, Object... args) {
        if (args != null && args.length > 0)
            lgr.error(message, args, throwable);
        else
            lgr.error(message, throwable);
    }

    /**
     * Loguea advertencia.
     */
    @Override
    public void warn(String message, Object... args) {
        if (args != null && args.length > 0)
            lgr.warn(message, args);
        else
            lgr.warn(message);
    }

    /**
     * Loguea advertencia desde objeto.
     */
    @Override
    public void warn(Object message, Object... args) {
        if (args != null && args.length > 0)
            lgr.warn(message.toString(), args);
        else
            lgr.warn(message.toString());
    }

    /**
     * Loguea stacktrace parcial para debugging.
     * @param traceSize Cantidad de elementos del stacktrace (-1 para todos).
     * @param prefix Prefijo visual.
     * @param indent Indentación visual.
     */
    @Override
    public void trace(int traceSize, String prefix, String indent) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int count = traceSize != -1 ? traceSize + 2 : stackTrace.length;
        for (int i = 2; i < count; i++) {
            if (stackElementAt(stackTrace, i, prefix, indent))
                break;
        }
    }

    /**
     * Loguea debug con contexto de stacktrace.
     */
    @Override
    public void debug(String message) {
        sendDebug(message, (Object[]) null);
    }

    @Override
    public void debug(String message, Object... args) {
        sendDebug(message, args);
    }

    @Override
    public void debug(Object message) {
        sendDebug(message.toString(), (Object[]) null);
    }

    @Override
    public void debug(Object message, Object... args) {
        sendDebug(message.toString(), args);
    }

    /**
     * Inicia un bloque de log.
     */
    @Override
    public LogBlock startBlock(String title) {
        return new BlockBuilder(lgr, title);
    }

    @Override
    public LogBlock startBlock(String title, Level level) {
        return new BlockBuilder(lgr, title, level);
    }

    /**
     * Inicia un cronómetro de log.
     */
    @Override
    public Cronometer startCronometer() {
        return new LogCronometer();
    }

    /**
     * Loguea un elemento del stacktrace usando SLF4J (no System.out).
     * @param stackTrace Stacktrace actual.
     * @param trace Índice del elemento.
     * @param prefix Prefijo visual.
     * @param indent Indentación visual.
     * @return true si termina el recorrido.
     */
    private boolean stackElementAt(StackTraceElement[] stackTrace, int trace, String prefix, String indent) {
        if (trace >= stackTrace.length)
            return true;
        StackTraceElement ste = stackTrace[trace];
        lgr.atLevel(Level.DEBUG).log(indent + (prefix + " at").trim() + " " + ste.getClassName() + "." + ste.getMethodName() + " (" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
        return false;
    }

    /**
     * Loguea debug con stacktrace contextual.
     * @param message Mensaje.
     * @param args Argumentos.
     */
    private void sendDebug(String message, @Nullable Object... args) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // El índice 4 asume que sendDebug es llamado desde un método público de PLogger
        StackTraceElement ste = stackTrace.length > 4 ? stackTrace[4] : stackTrace[stackTrace.length - 1];
        String content = message.trim() + "\n    ⟝ at " + ste.getClassName() + "." + ste.getMethodName() + " (" + ste.getFileName() + ":" + ste.getLineNumber() + ")\n";
        if (args != null && args.length > 0)
            lgr.debug(content, args);
        else
            lgr.debug(content);
    }

    /**
     * Añade contexto dinámico al log (ejemplo: jugador, mundo, tick).
     * @param context Map<String, String> con claves y valores de contexto.
     */
    public void withContext(Map<String, String> context, Runnable logAction) {
        if (context != null) {
            context.forEach(MDC::put);
            try {
                logAction.run();
            } finally {
                context.keySet().forEach(MDC::remove);
            }
        } else {
            logAction.run();
        }
    }

    /**
     * Loguea evento con tags de contexto.
     * @param event Evento textual.
     * @param tags Tags de contexto.
     */
    public void event(String event, String... tags) {
        lgr.info("[EVENT] {} | Tags: {}", event, String.join(", ", tags));
    }

    /**
     * Loguea advertencia con sugerencia.
     * @param warning Mensaje de advertencia.
     * @param suggestion Sugerencia de acción.
     */
    public void warnWithSuggestion(String warning, String suggestion) {
        lgr.warn("{} | Sugerencia: {}", warning, suggestion);
    }

    /**
     * Loguea performance (tiempo en ms).
     * @param operation Operación.
     * @param elapsedMs Tiempo en milisegundos.
     */
    public void performance(String operation, long elapsedMs) {
        lgr.info("[PERF] {} | {} ms", operation, elapsedMs);
    }

    /**
     * Loguea excepción con contexto adicional.
     * @param throwable Excepción.
     * @param context Contexto textual.
     */
    public void exception(Throwable throwable, String context) {
        lgr.error("[EXCEPTION] {}", context, throwable);
    }

    /**
     * Representa una entrada de log con formato y argumentos.
     * @param messagePattern Patrón de mensaje.
     * @param arguments Argumentos.
     */
    private record LogEntry(String messagePattern, @Nullable Object... arguments) {}

    /**
     * Bloque de log estructurado para debugging y agrupación de mensajes.
     */
    static class BlockBuilder implements LogBlock {
        private static final String BLOCK_SEPARATOR = "--- Block End ---";
        private static final String BLOCK_TITLE_PREFIX = "--- ";
        private static final String BLOCK_TITLE_SUFFIX = " ---";
        private static final String LINE_PREFIX = "  -> ";
        private static final String EXCEPTION_MARKER = "  ↓↓↓ Exception Details ↓↓↓";

        final org.slf4j.Logger lgr;
        private final String title;
        private final List<LogEntry> logEntries = new ArrayList<>();
        private Level currentLevel;
        private Throwable currentException = null;
        private boolean isPrinted = false;

        BlockBuilder(org.slf4j.Logger lgr, String title) {
            this(lgr, title, Level.INFO);
        }

        BlockBuilder(org.slf4j.Logger lgr, String title, Level level) {
            if (lgr == null)
                throw new IllegalArgumentException("Logger cannot be null");
            if (title == null || title.trim().isEmpty())
                throw new IllegalArgumentException("Title cannot be null or empty");
            if (level == null)
                throw new IllegalArgumentException("Initial level cannot be null");

            this.lgr     = lgr;
            this.title   = title;
            currentLevel = level;
        }

        private boolean disallowModificationIfPrinted(String methodName) {
            if (isPrinted) {
                lgr.warn("Attempted to call {}() on an already printed BlockBuilder instance for title '{}'. " + "Reset the block to modify or print again.", methodName, title);
                return true;
            }
            return false;
        }

        @Override
        public LogBlock addLine(Object obj, @Nullable Object... args) {
            if (disallowModificationIfPrinted("addLine"))
                return this;

            String pattern = (obj == null) ? "null" : obj.toString();
            logEntries.add(new LogEntry(pattern, args));
            return this;
        }

        @Override
        public LogBlock addLine(String text, @Nullable Object... args) {
            if (disallowModificationIfPrinted("addLine"))
                return this;

            String pattern = (text == null) ? "null" : text;
            logEntries.add(new LogEntry(pattern, args));
            return this;
        }

        @Override
        public LogBlock modifyLine(int index, String text, @Nullable Object... args) {
            if (disallowModificationIfPrinted("modifyLine"))
                return this;

            if (index >= 0 && index < logEntries.size()) {
                String pattern = (text == null) ? "null" : text;
                logEntries.set(index, new LogEntry(pattern, args));
            } else
                lgr.warn("Attempted to modify line at invalid index {} for title '{}'. Index out of bounds (0-{}).", index, title, !logEntries.isEmpty() ? logEntries.size() - 1
                        : 0);
            return this;
        }

        @Override
        public LogBlock removeLine(int index) {
            if (disallowModificationIfPrinted("removeLine"))
                return this;

            if (index >= 0 && index < logEntries.size())
                logEntries.remove(index);
            else
                lgr.warn("Attempted to remove line at invalid index {} for title '{}'. Index out of bounds (0-{}).", index, title, !logEntries.isEmpty() ? logEntries.size() - 1
                        : 0);
            return this;
        }

        @Override
        public LogBlock setException(Throwable exception) {
            if (disallowModificationIfPrinted("setException"))
                return this;
            this.currentException = exception;
            return this;
        }

        @Override
        public LogBlock setLevel(Level level) {
            if (disallowModificationIfPrinted("setLevel"))
                return this;
            if (level == null)
                throw new IllegalArgumentException("Level cannot be null");
            this.currentLevel = level;
            return this;
        }

        @Override
        public LogBlock reset() {
            logEntries.clear();
            currentException = null;
            isPrinted        = false;
//            lgr.debug("BlockBuilder for title '{}' has been reset.", title);
            return this;
        }

        @Override
        public void print() {
            if (isPrinted) {
                lgr.warn("BlockBuilder for title '{}' has already been printed. Call reset() to print again.", title);
                return;
            }
            if (logEntries.isEmpty() && currentException == null) {
                lgr.debug("BlockBuilder for title '{}' is empty, nothing to print.", title);
                isPrinted = true;
                return;
            }

            String formattedTitle = BLOCK_TITLE_PREFIX + title + BLOCK_TITLE_SUFFIX;
            lgr.atLevel(currentLevel).log(formattedTitle);

            for (LogEntry entry : logEntries) {
                // SLF4J handles null or empty arguments array correctly
                lgr.atLevel(currentLevel).log(LINE_PREFIX + entry.messagePattern(), entry.arguments());
            }

            if (currentException != null)
                lgr.atLevel(currentLevel).log(EXCEPTION_MARKER, currentException);

            lgr.atLevel(currentLevel).log(BLOCK_SEPARATOR);
            isPrinted = true;
        }

        @Override
        public boolean isEmpty() {
            return logEntries.isEmpty() && currentException == null;
        }

        @Override
        public boolean isPrinted() {
            return isPrinted;
        }
    }

    /**
     * Cronómetro de alta precisión para profiling y debugging.
     */
    static class LogCronometer implements Cronometer {
        private long startTimeNano;
        private long stopTimeNano;
        private boolean running;

        /**
         * Inicia el cronómetro.
         */
        LogCronometer() {
            this.startTimeNano = System.nanoTime();
            this.running       = true;
        }

        /**
         * Obtiene el tiempo transcurrido en el formato solicitado.
         */
        @Override
        public long getElapsedTime(CronometerOutputType outputType) {
            long currentNano = running ? System.nanoTime() : stopTimeNano;
            long elapsed = currentNano - startTimeNano;
            return switch (outputType) {
                case MILLISECONDS -> elapsed / 1_000_000L;
                case SECONDS -> elapsed / 1_000_000_000L;
                case MINUTES -> elapsed / 60_000_000_000L;
            };
        }

        /**
         * Reinicia el cronómetro.
         */
        @Override
        public void reset() {
            this.startTimeNano = System.nanoTime();
            this.stopTimeNano  = 0;
            this.running       = true;
        }

        /**
         * Detiene el cronómetro y retorna el tiempo transcurrido en ms.
         */
        @Override
        public long stop() {
            if (running) {
                this.stopTimeNano = System.nanoTime();
                this.running      = false;
            }
            return (stopTimeNano - startTimeNano) / 1_000_000L;
        }

        /**
         * Retorna el tiempo transcurrido en formato legible.
         */
        @Override
        public String getFormattedElapsedTime() {
            long elapsedMs = getElapsedTime(CronometerOutputType.MILLISECONDS);
            if (!running) {
                elapsedMs = (stopTimeNano - startTimeNano) / 1_000_000L;
            }

            if (elapsedMs < 1000) {
                return elapsedMs + " ms";
            } else if (elapsedMs < 60000) {
                return String.format("%.3f s", elapsedMs / 1000.0);
            } else {
                long totalSeconds = elapsedMs / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                return String.format("%d min %d s (%.3f s)", minutes, seconds, elapsedMs / 1000.0);
            }
        }
    }
}
