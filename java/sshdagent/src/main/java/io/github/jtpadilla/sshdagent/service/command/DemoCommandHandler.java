package io.github.jtpadilla.sshdagent.service.command;

import io.helidon.service.registry.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service.Singleton
public class DemoCommandHandler implements io.github.jtpadilla.sshdagent.service.command.CommandHandler {

    private static final List<String> COMMANDS =
            List.of("help", "echo", "time", "whoami", "quit");

    @Override
    public io.github.jtpadilla.sshdagent.service.command.CommandResult execute(String who, String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return io.github.jtpadilla.sshdagent.service.command.CommandResult.of("");
        }

        String[] parts = trimmed.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        return switch (cmd) {
            case "help" -> io.github.jtpadilla.sshdagent.service.command.CommandResult.of(
                    "Comandos: " + String.join(", ", COMMANDS));
            case "echo" -> io.github.jtpadilla.sshdagent.service.command.CommandResult.of(args);
            case "time" -> io.github.jtpadilla.sshdagent.service.command.CommandResult.of(
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            case "whoami" -> io.github.jtpadilla.sshdagent.service.command.CommandResult.of(who);
            case "quit", "exit" -> io.github.jtpadilla.sshdagent.service.command.CommandResult.quit("Hasta luego.");
            default -> io.github.jtpadilla.sshdagent.service.command.CommandResult.of(
                    "Comando desconocido: '" + cmd + "'. Escribe 'help'.");
        };
    }

    @Override
    public List<String> commandNames() {
        return COMMANDS;
    }
}