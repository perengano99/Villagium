package com.perengano99.villagium.core.util.logging;

import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

/**
 * Interfaz principal para logging avanzado en mods NeoForge/Minecraft.
 * Provee métodos para logging estructurado, bloques, cronómetros y trazabilidad.
 * Implementación recomendada: {@link PLogger}
 * @author perengano99
 * @since 2026-01-05
 */
public interface Logger {
    /** StackWalker para obtener la clase llamante en tiempo de ejecución. */
    StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Obtiene una instancia de Logger asociada a la clase llamante.
     * @return Logger contextualizado.
     */
    static Logger getLogger() {
        return new PLogger(org.slf4j.LoggerFactory.getLogger(STACK_WALKER.getCallerClass()));
    }

    /** Loguea información (nivel INFO) con formato y argumentos. */
    void info(String message, Object... args);
    /** Loguea información (nivel INFO) desde un objeto. */
    void info(Object message, Object... args);
    /** Loguea error (nivel ERROR) con formato y argumentos. */
    void error(String message, Object... args);
    /** Loguea error (nivel ERROR) desde un objeto. */
    void error(Object message, @Nullable Object... args);
    /** Loguea error con stacktrace parcial. */
    void error(int traceSize, Object message, @Nullable Object... args);
    /** Loguea error con excepción. */
    void error(Throwable throwable, String message, @Nullable Object... args);
    /** Loguea advertencia (nivel WARN) con formato y argumentos. */
    void warn(String message, @Nullable Object... args);
    /** Loguea advertencia (nivel WARN) desde un objeto. */
    void warn(Object message, @Nullable Object... args);
    /** Loguea stacktrace parcial para debugging. */
    void trace(int traceSize, String prefix, String indent);
    /** Loguea debug (nivel DEBUG) con formato y argumentos. */
    void debug(String message);
    void debug(String message, @Nullable Object... args);
    void debug(Object message);
    void debug(Object message, @Nullable Object... args);
    /** Inicia un bloque de log estructurado. */
    LogBlock startBlock(String title);
    /** Inicia un bloque de log con nivel personalizado. */
    LogBlock startBlock(String title, org.slf4j.event.Level level);
    /** Inicia un cronómetro de alta precisión para profiling. */
    Cronometer startCronometer();
}
