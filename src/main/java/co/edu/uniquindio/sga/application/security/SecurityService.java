package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;

/** Puerto de autenticación (Guía 12) */
public interface SecurityService {

    TokenResponse login(LoginRequest request);

    /** Registro público (7.2): crea un usuario con rol HUESPED. Lanza ReglaDominioException si el correo o el documento ya existen. */
    void registrarHuesped(RegistrarHuespedRequest request);
}