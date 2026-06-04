package io.github.jtpadilla.sshdagent.server.adapter;

import io.github.jtpadilla.sshdagent.service.command.CommandHandler;
import io.github.jtpadilla.sshdagent.shell.InteractiveShell;
import io.helidon.service.registry.Service;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;

/**
 * {@link ShellFactory} de Apache MINA SSHD gestionado por el Service Registry de
 * Helidon. Abre una sesión interactiva por canal, delegando la lógica de
 * comandos en el {@link CommandHandler} inyectado (lo aporta
 * {@code DemoCommandHandler}).
 */
@Service.Singleton
public class ShellFactoryAdapter implements ShellFactory {

    final private CommandHandler commandHandler;

    @Service.Inject
    public ShellFactoryAdapter(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public Command createShell(ChannelSession channelSession) throws IOException {
        return new InteractiveShell(commandHandler);
    }

}
