package com.example.sshconsole.server;

import com.example.sshconsole.command.CommandHandler;
import com.example.sshconsole.shell.InteractiveShell;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;

public class ServiceShellFactory implements ShellFactory {

    final private CommandHandler commandHandler;

    public ServiceShellFactory(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public Command createShell(ChannelSession channelSession) throws IOException {
        return new InteractiveShell(commandHandler);
    }

}
