package io.github.jtpadilla.sshdagent.server.impl;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.shell.ShellFactory;

/**
 * Parámetros de configuración del servidor SSH embebido. Concentra todo lo que
 * antes vivía disperso en el builder de la instancia; se construye con el
 * builder y se valida en {@link Builder#build()}, de modo que una instancia de
 * {@link SshConsoleConfig} ya es siempre válida.
 */
public final class SshConsoleConfig {

    /**
     * El autenticador de password y el factory de sesiones son obligatorios, de
     * modo que se exigen al abrir el builder. El puerto es opcional ({@link
     * Builder#port(int)}) y por defecto vale 2222.
     */
    public static Builder builder(PasswordAuthenticator passwordAuth, ShellFactory shellFactory) {
        return new Builder(passwordAuth, shellFactory);
    }

    public static final class Builder {

        private final PasswordAuthenticator passwordAuth;
        private final ShellFactory shellFactory;
        private int port = 2222;

        private Builder(PasswordAuthenticator passwordAuth, ShellFactory shellFactory) {
            if (passwordAuth == null) {
                throw new IllegalArgumentException("Configura el autenticador de password.");
            }
            if (shellFactory == null) {
                throw new IllegalArgumentException("Configura el factory de sesiones.");
            }
            this.passwordAuth = passwordAuth;
            this.shellFactory = shellFactory;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public SshConsoleConfig build() {
            return new SshConsoleConfig(port, passwordAuth, shellFactory);
        }
    }

    private final int port;
    private final PasswordAuthenticator passwordAuth;
    private final ShellFactory shellFactory;

    private SshConsoleConfig(int port, PasswordAuthenticator passwordAuth, ShellFactory shellFactory) {
        this.port = port;
        this.passwordAuth = passwordAuth;
        this.shellFactory = shellFactory;
    }

    public int port() {
        return port;
    }

    public PasswordAuthenticator passwordAuth() {
        return passwordAuth;
    }

    public ShellFactory shellFactory() {
        return shellFactory;
    }

}
