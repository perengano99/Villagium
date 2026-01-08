package com.perengano99.villagium.core.util.logging;

/**
 * Interfaz para cronómetros de alta precisión en logging y profiling.
 * Permite medir tiempos de ejecución y obtener formatos legibles para debugging y performance.
 * Implementación recomendada: {@link PLogger.LogCronometer}
 * @author perengano99
 * @since 2026-01-05
 */
public interface Cronometer {
    /** Tipos de salida para el cronómetro (minutos, segundos, milisegundos). */
    enum CronometerOutputType {
        MINUTES, SECONDS, MILLISECONDS
    }

    /** Obtiene el tiempo transcurrido en el formato solicitado. */
    default long getElapsedTime() {
        return getElapsedTime(CronometerOutputType.MILLISECONDS);
    }

    long getElapsedTime(CronometerOutputType outputType);

    /** Reinicia el cronómetro. */
    void reset();

    /** Detiene el cronómetro y retorna el tiempo en ms. */
    long stop();

    /** Retorna el tiempo transcurrido en formato legible. */
    String getFormattedElapsedTime();
}
