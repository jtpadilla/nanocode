package io.github.jtpadilla.sshdagent.shell;

import io.github.jtpadilla.sshdagent.service.command.CommandHandler;
import io.github.jtpadilla.sshdagent.service.command.CommandResult;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import java.util.function.BooleanSupplier;

/**
 * El bucle read-eval-print. Trabaja sobre un {@link LineReader} ya montado
 * ({@link SshTerminalSession}) y delega cada línea en el {@link CommandHandler},
 * sin conocer nada del transporte SSH ni del bootstrap de JLine.
 *
 * <p>Atajos que vienen "gratis" con JLine:
 * ↑/↓ historial · ←/→ mover cursor · Ctrl-A/E inicio/fin · Ctrl-K borrar a fin
 * · Ctrl-W borrar palabra · TAB completar.
 */
final class Repl {

    private final CommandHandler handler;
    private final Terminal terminal;
    private final LineReader reader;
    private final String username;

    Repl(CommandHandler handler, SshTerminalSession session, String username) {
        this.handler = handler;
        this.terminal = session.terminal();
        this.reader = session.reader();
        this.username = username;
    }

    /**
     * Corre el bucle hasta que {@code running} sea falso (sesión cerrada desde
     * fuera), el usuario pulse Ctrl-D, o un comando pida cerrar la sesión.
     */
    void run(BooleanSupplier running) {

        terminal.writer().println("Bienvenido, " + username + ". Escribe 'help'.");
        terminal.writer().flush();

        String prompt = "[32m" + username + "@app>[0m ";

        while (running.getAsBoolean()) {
            String line;
            try {
                line = reader.readLine(prompt);
            } catch (UserInterruptException e) {   // Ctrl-C
                terminal.writer().println("^C");
                terminal.writer().flush();
                continue;
            } catch (EndOfFileException e) {        // Ctrl-D
                break;
            }

            CommandResult result = handler.execute(username, line);
            if (result.output() != null && !result.output().isEmpty()) {
                terminal.writer().println(result.output());
                terminal.writer().flush();
            }
            if (result.exitSession()) {
                break;
            }
        }
    }
}
