package io.github.jtpadilla.sshdagent.service.command;

/**
 * Resultado de ejecutar un comando. Inmutable, sin dependencias de framework.
 */
public record CommandResult(String output, boolean exitSession) {

    public static CommandResult of(String output) {
        return new CommandResult(output, false);
    }

    public static CommandResult quit(String farewell) {
        return new CommandResult(farewell, true);
    }

}