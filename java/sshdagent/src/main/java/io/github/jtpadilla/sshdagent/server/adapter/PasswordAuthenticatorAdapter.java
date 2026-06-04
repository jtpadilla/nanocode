package io.github.jtpadilla.sshdagent.server.adapter;

import io.github.jtpadilla.sshdagent.service.auth.CredentialService;
import io.helidon.service.registry.Service;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * {@link PasswordAuthenticator} de Apache MINA SSHD que delega la verificación
 * en un {@link CredentialService} inyectado. Desacopla la autenticación del
 * canal SSH del almacén concreto de credenciales: cambiar el origen de usuarios
 * (demo, LDAP, BD, servicio remoto) no requiere tocar esta clase.
 */
@Service.Singleton
public class PasswordAuthenticatorAdapter implements PasswordAuthenticator {

    private final CredentialService credentials;

    @Service.Inject
    public PasswordAuthenticatorAdapter(CredentialService credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        return credentials.authenticate(username, password);
    }

}
