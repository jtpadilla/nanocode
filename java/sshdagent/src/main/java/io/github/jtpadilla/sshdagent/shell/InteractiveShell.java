package io.github.jtpadilla.sshdagent.shell;

import io.github.jtpadilla.sshdagent.command.CommandHandler;
import io.github.jtpadilla.sshdagent.command.CommandResult;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Un comando-shell interactivo por sesión SSH. SSHD inyecta los streams del
 * canal; JLine se monta encima para dar edición de línea, historial y
 * autocompletado.
 */
public class InteractiveShell implements Command, Runnable {

    private final CommandHandler handler;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;
    private Environment environment;
    private Thread thread;
    private volatile boolean running = true;

    public InteractiveShell(CommandHandler handler) {
        this.handler = handler;
    }

    @Override public void setInputStream(InputStream in)   { this.in = in; }
    @Override public void setOutputStream(OutputStream out) { this.out = out; }
    @Override public void setErrorStream(OutputStream err)  { this.err = err; }
    @Override public void setExitCallback(ExitCallback cb)  { this.exitCallback = cb; }

    @Override
    public void start(ChannelSession channel, Environment env) {
        this.environment = env;
        this.thread = new Thread(this, "ssh-shell-" + channel.getSession().getUsername());
        this.thread.start();
    }

    @Override
    public void destroy(ChannelSession channel) {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        String username = String.valueOf(
                environment.getEnv().getOrDefault("USER", "user"));

        // Terminal JLine atado a los streams del canal SSH.
        // type "xterm" -> usa la info del PTY que negoció SSHD (flechas, etc.).
        try (Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(in, out)
                .encoding(StandardCharsets.UTF_8)
                .type(environment.getEnv().getOrDefault("TERM", "xterm-256color"))
                .build()) {

            // Sin esto el terminal queda en 0x0 columnas (el PTY es remoto, no
            // hay tty local que consultar) y JLine no puede posicionar el cursor.
            applyPtySize(terminal);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new StringsCompleter(handler.commandNames()))
                    .build();

            // Estos atajos vienen "gratis" con JLine:
            //   ↑/↓ historial · ←/→ mover cursor · Ctrl-A/E inicio/fin
            //   Ctrl-K borrar a fin · Ctrl-W borrar palabra · TAB completar

            terminal.writer().println("Bienvenido, " + username + ". Escribe 'help'.");
            terminal.writer().flush();

            String prompt = "[32m" + username + "@app>[0m ";

            while (running) {
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

            exitCallback.onExit(0);

        } catch (IOException e) {
            try {
                exitCallback.onExit(1, e.getMessage());
            } catch (Exception ignored) {
                // canal ya cerrado
            }
        }
    }

    /**
     * SSHD negocia el tamaño del PTY y lo expone en COLUMNS/LINES del entorno,
     * pero el terminal JLine creado sobre los streams del canal no lo consulta
     * (no hay PTY local real que interrogar). Sin tamaño se queda en 0x0 y el
     * editor de línea no sabe posicionar el cursor: el prompt se dibuja mal y
     * se apila. Lo fijamos a mano y nos suscribimos a WINCH para refrescarlo
     * cuando el cliente redimensione la ventana.
     */
    private void applyPtySize(Terminal terminal) {
        terminal.setSize(sizeFromEnv());
        environment.addSignalListener(
                (channel, signal) -> terminal.setSize(sizeFromEnv()),
                Signal.WINCH);
    }

    private Size sizeFromEnv() {
        int cols = parsePositive(environment.getEnv().get("COLUMNS"), 80);
        int rows = parsePositive(environment.getEnv().get("LINES"), 24);
        return new Size(cols, rows);
    }

    private static int parsePositive(String value, int fallback) {
        try {
            int n = Integer.parseInt(value);
            return n > 0 ? n : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
