package com.example.sshconsole;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        CommandHandler handler = new DemoCommandHandler();

        try (SshConsoleServer server = SshConsoleServer.builder(handler)
                .port(2222)
                .hostKey(Path.of("hostkey.ser"))
                .passwordAuth(Map.of(
                        "admin", "secret",
                        "juan",  "rambla"))
                .build()) {

            server.start();
            System.out.println("SSH escuchando en el puerto " + server.port());
            Thread.currentThread().join();
        }
    }
}