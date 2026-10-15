package co.gentiac.authcore.application.port.out;

import co.gentiac.authcore.domain.model.ColaboradorId;

import java.util.Optional;

/**
 * Puerto de salida hacia el proveedor de identidad (spec 001, RF-013).
 * Adaptadores previstos: Amazon Cognito, directorio LDAP (AWS Directory Service), almacén local Argon2id.
 *
 * <p>Contrato: devuelve vacío ante cualquier fallo de credenciales, sin distinguir
 * usuario inexistente de contraseña incorrecta (RF-004).</p>
 */
public interface VerificadorCredenciales {

    Optional<ColaboradorId> verificar(String identificador, char[] secreto);
}
