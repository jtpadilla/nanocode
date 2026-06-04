package io.github.jtpadilla.sshdagent.server.impl;

import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;

import java.io.IOException;

/**
 * Servidor SSH embebido ya arrancado. Solo existe en estado «en marcha»: se
 * obtiene con {@link #start(SshConsoleConfig)}, que construye el servidor a
 * partir del config y lo arranca, y se termina con {@link #shutdown()}.
 */
public class SshConsoleInstance {

    /**
     * Construye el servidor a partir del config y lo arranca. La instancia
     * devuelta ya está escuchando.
     */
    public static SshConsoleInstance start(SshConsoleConfig config) throws IOException {

        // Se crea un SshServer con los valores por defecto.
        SshServer sshd = SshServer.setUpDefaultServer();

        // Se configura el puerto por donde escuchara
        sshd.setPort(config.port());

        // Se instala el verificador de credenciales
        sshd.setPasswordAuthenticator(config.passwordAuth());

        // Se instala el proveedro de claves
        sshd.setKeyPairProvider(config.getKeyPairProvider());

        // Se instala el factory de sesiones
        sshd.setShellFactory(config.shellFactory());

        // Se arranca el servidor antes de exponer la instancia.
        sshd.start();

        return new SshConsoleInstance(sshd);
    }

    private final SshServer sshd;

    private SshConsoleInstance(SshServer sshd) {
        this.sshd = sshd;
    }

    public int port() {
        return sshd.getPort();
    }

    /**
     * Detiene el servidor. Tras llamarla la instancia queda inutilizable.
     */
    public void shutdown() throws IOException {
        sshd.stop(true);
    }

}
