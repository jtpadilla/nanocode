package io.github.jtpadilla.sshdagent.shell;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Encapsula el montaje de JLine sobre los streams de un canal SSH: construye el
 * {@link Terminal} atado a esos streams y el {@link LineReader} con
 * autocompletado, y se ocupa del tamaño del PTY (incluido el refresco por
 * WINCH). Oculta así todo el detalle de terminal para que el REPL solo vea un
 * {@code LineReader} listo para usar.
 *
 * <p>Es {@link AutoCloseable}: cerrar la sesión cierra el terminal.
 */
final class SshTerminalSession implements AutoCloseable {

    private final Terminal terminal;
    private final LineReader reader;

    private SshTerminalSession(Terminal terminal, LineReader reader) {
        this.terminal = terminal;
        this.reader = reader;
    }

    /**
     * Abre el terminal JLine sobre los streams del canal y prepara el lector de
     * línea. El {@code type} "xterm" usa la info del PTY que negoció SSHD
     * (flechas, etc.).
     */
    static SshTerminalSession open(InputStream in,
                                   OutputStream out,
                                   Environment environment,
                                   Collection<String> completions) throws IOException {

        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(in, out)
                .encoding(StandardCharsets.UTF_8)
                .type(environment.getEnv().getOrDefault("TERM", "xterm-256color"))
                .build();

        // Sin esto el terminal queda en 0x0 columnas (el PTY es remoto, no hay
        // tty local que consultar) y JLine no puede posicionar el cursor.
        applyPtySize(terminal, environment);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter(completions))
                .build();

        return new SshTerminalSession(terminal, reader);
    }

    Terminal terminal() {
        return terminal;
    }

    LineReader reader() {
        return reader;
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }

    /**
     * SSHD negocia el tamaño del PTY y lo expone en COLUMNS/LINES del entorno,
     * pero el terminal JLine creado sobre los streams del canal no lo consulta
     * (no hay PTY local real que interrogar). Sin tamaño se queda en 0x0 y el
     * editor de línea no sabe posicionar el cursor: el prompt se dibuja mal y
     * se apila. Lo fijamos a mano y nos suscribimos a WINCH para refrescarlo
     * cuando el cliente redimensione la ventana.
     */
    private static void applyPtySize(Terminal terminal, Environment environment) {
        terminal.setSize(sizeFromEnv(environment));
        environment.addSignalListener(
                (channel, signal) -> terminal.setSize(sizeFromEnv(environment)),
                Signal.WINCH);
    }

    private static Size sizeFromEnv(Environment environment) {
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
