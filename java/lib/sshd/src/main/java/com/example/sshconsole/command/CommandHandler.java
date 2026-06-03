package com.example.sshconsole.command;

import io.helidon.service.registry.Service;

import java.util.List;

/**
 * Contrato del intérprete de comandos. Una implementación por aplicación;
 * `InteractiveShell` la invoca para cada línea editada por el usuario.
 *
 * <p>{@code @Service.Contract} lo hace inyectable desde el Service Registry:
 * cualquier {@code @Service.Singleton} que lo implemente queda disponible bajo
 * este tipo.
 */
@Service.Contract
public interface CommandHandler {

    /**
     * Ejecuta una línea introducida por {@code who} y devuelve el resultado.
     * Nunca debe lanzar excepciones de control de flujo: usa
     * {@link CommandResult#quit(String)} para cerrar la sesión.
     */
    com.example.sshconsole.command.CommandResult execute(String who, String line);

    /** Nombres de comandos disponibles, usados por JLine para autocompletar. */
    List<String> commandNames();
}
