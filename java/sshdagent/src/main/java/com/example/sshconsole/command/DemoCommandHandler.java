package com.example.sshconsole.command;

import io.helidon.service.registry.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service.Singleton
public class DemoCommandHandler implements com.example.sshconsole.command.CommandHandler {

    private static final List<String> COMMANDS =
            List.of("help", "echo", "time", "whoami", "quit");

    @Override
    public com.example.sshconsole.command.CommandResult execute(String who, String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return com.example.sshconsole.command.CommandResult.of("");
        }

        String[] parts = trimmed.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        return switch (cmd) {
            case "help" -> com.example.sshconsole.command.CommandResult.of(
                    "Comandos: " + String.join(", ", COMMANDS));
            case "echo" -> com.example.sshconsole.command.CommandResult.of(args);
            case "time" -> com.example.sshconsole.command.CommandResult.of(
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            case "whoami" -> com.example.sshconsole.command.CommandResult.of(who);
            case "quit", "exit" -> com.example.sshconsole.command.CommandResult.quit("Hasta luego.");
            default -> com.example.sshconsole.command.CommandResult.of(
                    "Comando desconocido: '" + cmd + "'. Escribe 'help'.");
        };
    }

    @Override
    public List<String> commandNames() {
        return COMMANDS;
    }
}