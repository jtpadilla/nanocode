package io.github.jtpadilla.sshdagent.auth;

import io.github.jtpadilla.sshdagent.server.localservice.ServicePasswordAuthenticator;
import io.helidon.service.registry.Service;

/**
 * Contrato de verificación de credenciales. Es el seam por el que, en el futuro,
 * la autenticación SSH se resolverá mediante un servicio externo de gestión de
 * usuarios y credenciales (LDAP, base de datos, servicio remoto, etc.) sin tocar
 * ni el {@link ServicePasswordAuthenticator} ni el servidor.
 *
 * <p>{@code @Service.Contract} lo hace inyectable desde el Service Registry:
 * cualquier {@code @Service.Singleton} que lo implemente queda disponible bajo
 * este tipo (hoy lo aporta {@code DemoCredentialService}).
 */
@Service.Contract
public interface CredentialService {

    /**
     * Comprueba si {@code user}/{@code password} son válidos.
     *
     * @return {@code true} si las credenciales son correctas; {@code false} en
     *         caso contrario (usuario desconocido, contraseña errónea o nula).
     */
    boolean authenticate(String user, String password);
}
