package co.gentiac.authcore.application.port.out;

import co.gentiac.authcore.domain.model.EventoSeguridad;

/** Puerto de salida hacia el registro de auditoría de solo inserción (spec 004, RF-060/061). */
public interface PublicadorAuditoria {

    void publicar(EventoSeguridad evento);
}
