package com.example.sshconsole.server;

import com.example.sshconsole.command.CommandHandler;
import io.helidon.service.registry.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Servicio gestionado por el Service Registry de Helidon. El registro controla
 * su ciclo de vida: lo instancia de forma eager al arrancar (por el
 * {@code @Service.RunLevel(SERVER)}), arranca el servidor SSH en el
 * {@code @Service.PostConstruct} y lo detiene en el {@code @Service.PreDestroy}
 * cuando el registro se apaga.
 *
 * <p>El {@link CommandHandler} llega por inyección de constructor desde el
 * registro (lo aporta {@code DemoCommandHandler}).
 */
@Service.Singleton
@Service.RunLevel(Service.RunLevel.SERVER)
class SshServerService {

    private final SshConsoleServer server;

    @Service.Inject
    SshServerService(CommandHandler handler) {
        this.server = SshConsoleServer.builder(handler)
                .port(2222)
                .passwordAuth(Map.of(
                        "admin", "secret",
                        "juan", "rambla"))
                .build();
    }

    @Service.PostConstruct
    void start() {
        try {
            server.start();
            System.out.println("SSH escuchando en el puerto " + server.port());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo arrancar el servidor SSH", e);
        }
    }

    @Service.PreDestroy
    void stop() {
        try {
            server.close();
            System.out.println("Servidor SSH detenido.");
        } catch (IOException e) {
            throw new UncheckedIOException("Error al detener el servidor SSH", e);
        }
    }
}
