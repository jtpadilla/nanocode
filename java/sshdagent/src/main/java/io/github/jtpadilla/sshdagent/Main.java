package io.github.jtpadilla.sshdagent;

import io.helidon.service.registry.ServiceRegistryManager;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Arranca el Service Registry de Helidon: instancia de forma eager los
        // servicios con @Service.RunLevel. Entre ellos SshServerService, que en
        // su @PostConstruct levanta el servidor SSH.
        ServiceRegistryManager manager = ServiceRegistryManager.start();

        // Parada ordenada ante Ctrl-C / SIGTERM: shutdown() dispara los
        // @PreDestroy (SshServerService detiene el servidor).
        Runtime.getRuntime().addShutdownHook(
                new Thread(manager::shutdown, "registry-shutdown"));

        // El registro y el servidor viven en hilos propios; mantenemos vivo el
        // proceso hasta que llegue la señal de parada.
        Thread.currentThread().join();
    }
}
