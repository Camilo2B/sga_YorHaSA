package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarHuespedRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioInternoRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioInternoResponse;
import co.edu.uniquindio.sga.application.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controller REST de autenticación (Guía 12) */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión, registro de huéspedes y gestión de cuentas internas")
public class SecurityController {

    private final SecurityService securityService;

    /** POST -> /api/auth/login */
    @PostMapping("/login")
    @SecurityRequirements    // este endpoint es público: no exige token en Swagger
    @Operation(summary = "Iniciar sesión", description = "Devuelve un JWT para usar como Bearer token")
    @ApiResponse(responseCode = "200", description = "Token emitido")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(securityService.login(request));
    }

    /** POST -> /api/auth/registro */
    @PostMapping("/registro")
    @SecurityRequirements    // este endpoint es público: no exige token en Swagger
    @Operation(summary = "Registrar un huésped", description = "Registro público (7.2): crea un usuario con rol HUESPED")
    @ApiResponse(responseCode = "201", description = "Huésped registrado")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "409", description = "El correo o el documento ya están registrados")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistrarHuespedRequest request) {
        securityService.registrarHuesped(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** GET -> /api/auth/usuarios-internos: solo el administrador puede consultar el personal interno. */
    @GetMapping("/usuarios-internos")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios internos", description = "Lista las cuentas de recepción y personal de servicio")
    public ResponseEntity<List<UsuarioInternoResponse>> listarUsuariosInternos() {
        return ResponseEntity.ok(securityService.listarUsuariosInternos());
    }

    /** POST -> /api/auth/usuarios-internos: solo el administrador puede crear cuentas internas. */
    @PostMapping("/usuarios-internos")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario interno", description = "Crea una cuenta de RECEPCIONISTA o PERSONAL_SERVICIO")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede crear cuentas internas")
    @ApiResponse(responseCode = "409", description = "El correo o documento ya están registrados")
    public ResponseEntity<UsuarioInternoResponse> crearUsuarioInterno(
            @Valid @RequestBody CrearUsuarioInternoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(securityService.crearUsuarioInterno(request));
    }

}
