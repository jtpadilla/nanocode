package com.example.sshconsole;

import java.util.List;

/**
 * Contrato del intérprete de comandos. Una implementación por aplicación;
 * `InteractiveShell` la invoca para cada línea editada por el usuario.
 */
public interface CommandHandler {

    /**
     * Ejecuta una línea introducida por {@code who} y devuelve el resultado.
     * Nunca debe lanzar excepciones de control de flujo: usa
     * {@link CommandResult#quit(String)} para cerrar la sesión.
     */
    CommandResult execute(String who, String line);

    /** Nombres de comandos disponibles, usados por JLine para autocompletar. */
    List<String> commandNames();
}
