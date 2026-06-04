package io.github.jtpadilla.sshdagent.server;

import io.github.jtpadilla.sshdagent.service.command.CommandHandler;
import io.github.jtpadilla.sshdagent.server.impl.SshConsoleConfig;
import io.github.jtpadilla.sshdagent.server.impl.SshConsoleInstance;
import io.github.jtpadilla.sshdagent.server.localservice.ServicePasswordAuthenticator;
import io.github.jtpadilla.sshdagent.server.localservice.ServiceShellFactory;
import io.helidon.service.registry.Service;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Servicio gestionado por el Service Registry de Helidon. El registro controla
 * su ciclo de vida: lo instancia de forma eager al arrancar (por el
 * {@code @Service.RunLevel(SERVER)}), arranca el servidor SSH en el
 * {@code @Service.PostConstruct} y lo detiene en el {@code @Service.PreDestroy}
 * cuando el registro se apaga.
 *
 * <p>El {@link CommandHandler} y el {@link ServicePasswordAuthenticator} llegan
 * por inyección de constructor desde el registro (los aportan
 * {@code DemoCommandHandler} y {@code DemoCredentialService} respectivamente).
 */
@Service.Singleton
@Service.RunLevel(Service.RunLevel.SERVER)
class SshServerService {

    private final SshConsoleConfig config;
    private SshConsoleInstance server;

    @Service.Inject
    SshServerService(ServicePasswordAuthenticator passwordAuth, ServiceShellFactory serviceShellFactory) {
        this.config = SshConsoleConfig.builder(passwordAuth, serviceShellFactory)
                .port(2222)
                .build();
    }

    @Service.PostConstruct
    void start() {
        try {
            server = SshConsoleInstance.start(config);
            System.out.println("SSH escuchando en el puerto " + server.port());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo arrancar el servidor SSH", e);
        }
    }

    @Service.PreDestroy
    void stop() {
        try {
            server.shutdown();
            System.out.println("Servidor SSH detenido.");
        } catch (IOException e) {
            throw new UncheckedIOException("Error al detener el servidor SSH", e);
        }
    }

}
