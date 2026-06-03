package com.example.sshconsole;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Map;

/**
 * Servidor SSH embebido. Construye con el builder para configurarlo
 * programáticamente; sin anotaciones ni magia de framework.
 */
public class SshConsoleServer implements AutoCloseable {

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

    public static Builder builder(CommandHandler handler) {
        return new Builder(handler);
    }

    public static final class Builder {
        private final CommandHandler handler;
        private int port = 2222;
        private Path hostKeyPath = Path.of("hostkey.ser");
        private PasswordAuthenticator passwordAuth;
        private PublickeyAuthenticator publicKeyAuth;

        private Builder(CommandHandler handler) {
            this.handler = handler;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder hostKey(Path path) {
            this.hostKeyPath = path;
            return this;
        }

        /** Autenticación por usuario/contraseña. */
        public Builder passwordAuth(Map<String, String> userPasswords) {
            this.passwordAuth = (user, password, session) ->
                    password != null && password.equals(userPasswords.get(user));
            return this;
        }

        /** Autenticación por clave pública. Tú decides la política. */
        public Builder publicKeyAuth(PublickeyAuthenticator auth) {
            this.publicKeyAuth = auth;
            return this;
        }

        public SshConsoleServer build() {
            SshServer sshd = SshServer.setUpDefaultServer();
            sshd.setPort(port);

            // Clave de host: en producción usa una persistente y versiónala fuera del repo.
            KeyPairProvider hostKeys = new SimpleGeneratorHostKeyProvider(hostKeyPath);
            sshd.setKeyPairProvider(hostKeys);

            if (passwordAuth != null) {
                sshd.setPasswordAuthenticator(passwordAuth);
            }
            if (publicKeyAuth != null) {
                sshd.setPublickeyAuthenticator(publicKeyAuth);
            }
            if (passwordAuth == null && publicKeyAuth == null) {
                throw new IllegalStateException(
                        "Configura al menos un autenticador (password o publicKey).");
            }

            // Una sesión interactiva por canal de shell.
            ShellFactory shellFactory = channel -> new InteractiveShell(handler);
            sshd.setShellFactory(shellFactory);

            return new SshConsoleServer(sshd);
        }
    }
}