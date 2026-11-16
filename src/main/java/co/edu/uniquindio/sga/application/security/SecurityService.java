package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarHuespedRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioInternoRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioInternoResponse;

import java.util.List;

/** Puerto de autenticación (Guía 12) */
public interface SecurityService {

    TokenResponse login(LoginRequest request);

    /** Registro público (7.2): crea un usuario con rol HUESPED. Lanza ReglaDominioException si el correo o el documento ya existen. */
    void registrarHuesped(RegistrarHuespedRequest request);

    /** Crea una cuenta interna; el controlador restringe esta operación al administrador. */
    UsuarioInternoResponse crearUsuarioInterno(CrearUsuarioInternoRequest request);

    /** Lista las cuentas de recepción y personal de servicio, sin datos de autenticación. */
    List<UsuarioInternoResponse> listarUsuariosInternos();
}