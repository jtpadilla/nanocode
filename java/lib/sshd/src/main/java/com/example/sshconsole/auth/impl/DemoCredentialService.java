package com.example.sshconsole.auth.impl;

import com.example.sshconsole.auth.CredentialService;
import io.helidon.service.registry.Service;

import java.util.Map;

/**
 * Implementación demo de {@link CredentialService} con un almacén de usuarios
 * en memoria. Sustituye al {@code Map} antes incrustado en {@code SshServerService}.
 *
 * <p>Para conectar un origen real de credenciales basta con sustituir este
 * {@code @Service.Singleton} por otro que implemente {@link CredentialService}.
 */
@Service.Singleton
public class DemoCredentialService implements CredentialService {

    private static final Map<String, String> USUARIOS = Map.of(
            "admin", "secret",
            "juan", "rambla");

    @Override
    public boolean authenticate(String user, String password) {
        return password != null && password.equals(USUARIOS.get(user));
    }
}
