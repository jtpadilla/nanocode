package io.github.jtpadilla.sshdagent.shell;

import io.github.jtpadilla.sshdagent.service.command.CommandHandler;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Adaptador de un shell interactivo a la API {@link Command} de Apache MINA
 * SSHD: gestiona el ciclo de vida del canal (streams, hilo, cierre) y delega el
 * trabajo real en dos colaboradores. {@link SshTerminalSession} monta JLine
 * sobre los streams del canal y {@link Repl} corre el bucle read-eval-print
 * apoyándose en el {@link CommandHandler} inyectado.
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

        final String username = String.valueOf(environment.getEnv().getOrDefault("USER", "user"));

        try (SshTerminalSession session = SshTerminalSession.open(in, out, environment, handler.commandNames())) {
            new Repl(handler, session, username).run(() -> running);
            exitCallback.onExit(0);
        } catch (IOException e) {
            try {
                exitCallback.onExit(1, e.getMessage());
            } catch (Exception ignored) {
                // canal ya cerrado
            }
        }
    }
}
