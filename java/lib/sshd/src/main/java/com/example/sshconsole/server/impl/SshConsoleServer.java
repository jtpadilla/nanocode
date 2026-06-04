package com.example.sshconsole.server.impl;

import com.example.sshconsole.command.CommandHandler;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;

/**
 * Servidor SSH embebido. Construye con el builder para configurarlo
 * programáticamente; sin anotaciones ni magia de framework.
 */
public class SshConsoleServer implements AutoCloseable {

    public static Builder builder(CommandHandler handler) {
        return new Builder(handler);
    }

    public static final class Builder {

        private final CommandHandler handler;
        private int port = 2222;
        private PasswordAuthenticator passwordAuth;
        private ShellFactory shellFactory;

        private Builder(CommandHandler handler) {
            this.handler = handler;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Autenticación por usuario/contraseña delegada en un
         * {@link PasswordAuthenticator} externo (p. ej. uno que consulte un
         * servicio de credenciales inyectado).
         */
        public Builder setPasswordAuth(PasswordAuthenticator auth) {
            this.passwordAuth = auth;
            return this;
        }

        public Builder setServiceShellFactory(ShellFactory shellFactory) {
            this.shellFactory = shellFactory;
            return this;
        }

        public SshConsoleServer build() {

            // Se crea un SshServer con los valores por defecto.
            SshServer sshd = SshServer.setUpDefaultServer();

            // Se configura el puerto por donde escuchara
            sshd.setPort(port);

            // Se instala el verificador de credenciales
            if (passwordAuth == null) {
                throw new IllegalStateException("Configura el autenticador de password.");
            }
            sshd.setPasswordAuthenticator(passwordAuth);

            // Clave de host fija, embebida como recurso del classpath (hostkey.pem).
            // Así el fingerprint es estable entre ejecuciones de `bazel run`.
            // NOTA: la clave privada va en el repo/jar; vale para demo, no para producción.
            final KeyPairProvider hostKeys = new ClassLoadableResourceKeyPairProvider(
                    SshConsoleServer.class.getClassLoader(),
                    "hostkey.pem"
            );
            sshd.setKeyPairProvider(hostKeys);

            // Se instala el factory de sesiones
            if (shellFactory == null) {
                throw new IllegalStateException("Configura el factory de sesiones.");
            }
            sshd.setShellFactory(shellFactory);

            return new SshConsoleServer(sshd);
        }
    }

    private final SshServer sshd;

    private SshConsoleServer(SshServer sshd) {
        this.sshd = sshd;
    }

    public void start() throws IOException {
        sshd.start();
    }

    public int port() {
        return sshd.getPort();
    }

    @Override
    public void close() throws IOException {
        sshd.stop(true);
    }

}