package com.perengano99.villagium.core.util.logging;

import org.jetbrains.annotations.Nullable;

/**
 * Interfaz para bloques de log estructurados.
 * Permite agrupar, modificar y gestionar mensajes de log en bloques para debugging avanzado.
 * Implementación recomendada: {@link PLogger.BlockBuilder}
 * @author perengano99
 * @since 2026-01-05
 */
public interface LogBlock {
    /** Añade una línea al bloque de log. */
    LogBlock addLine(Object obj, @Nullable Object... args);
    /** Añade una línea textual al bloque de log. */
    LogBlock addLine(String text, @Nullable Object... args);
    /** Modifica una línea existente por índice. */
    LogBlock modifyLine(int index, String text, @Nullable Object... args);
    /** Elimina una línea por índice. */
    LogBlock removeLine(int index);
    /** Asocia una excepción al bloque de log. */
    LogBlock setException(Throwable exception);
    /** Cambia el nivel de log del bloque. */
    LogBlock setLevel(org.slf4j.event.Level level);
    /** Reinicia el bloque para reutilización. */
    LogBlock reset();
    /** Imprime el bloque en el logger asociado. */
    void print();
    /** Indica si el bloque está vacío. */
    boolean isEmpty();
    /** Indica si el bloque ya fue impreso. */
    boolean isPrinted();
}
