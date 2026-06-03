package com.example.sshconsole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DemoCommandHandler implements CommandHandler {

    private static final List<String> COMMANDS =
            List.of("help", "echo", "time", "whoami", "quit");

    @Override
    public CommandResult execute(String who, String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return CommandResult.of("");
        }

        String[] parts = trimmed.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        return switch (cmd) {
            case "help" -> CommandResult.of(
                    "Comandos: " + String.join(", ", COMMANDS));
            case "echo" -> CommandResult.of(args);
            case "time" -> CommandResult.of(
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            case "whoami" -> CommandResult.of(who);
            case "quit", "exit" -> CommandResult.quit("Hasta luego.");
            default -> CommandResult.of(
                    "Comando desconocido: '" + cmd + "'. Escribe 'help'.");
        };
    }

    @Override
    public List<String> commandNames() {
        return COMMANDS;
    }
}