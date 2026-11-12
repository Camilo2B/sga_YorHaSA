package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.security.SecurityService;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.UsuarioJpaDataRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Implementación del inicio de sesión con Spring Security (Guía 12) */
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioJpaDataRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sga.jwt.expiracion-minutos}")
    private long expiracionMinutos;

    @Override
    public TokenResponse login(LoginRequest request) {

        // 1. Busca el usuario (UserConfig) y compara la contraseña con BCrypt.
        //    Si falla, lanza una AuthenticationException → 401 (sección 6.4)
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.correo(), request.contrasena())
        );

        // 2. Credenciales correctas: se leen el rol y el documento del usuario autenticado
        CustomUserDetails usuario = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 3. Se calcula la vigencia y se firma el token
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

        String token = jwtTokenProvider.generateTokenAsString(
                authentication.getName(), usuario.usuario().getDocumento(), roles, ahora, expira);

        return new TokenResponse(token, "Bearer", expira, roles);
    }

    @Override
    public void registrarHuesped(RegistrarHuespedRequest request) {

        // Las reglas de unicidad (correo y documento) son de negocio: se responden con 409, no con 400
        if (usuarioRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new ReglaDominioException("Ya existe un usuario registrado con ese correo");
        }
        if (usuarioRepository.existsByDocumento(request.documento())) {
            throw new ReglaDominioException("Ya existe un usuario registrado con ese documento");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(request.nombre());
        usuario.setCorreo(request.correo());
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));   // nunca texto plano
        usuario.setTelefono(request.telefono());
        usuario.setDocumento(request.documento());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRol(RolUsuario.HUESPED);   // el registro público solo crea huéspedes (7.1)

        try {
            usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo correo o documento: gana el primero
            throw new ReglaDominioException("Ya existe un usuario registrado con ese correo o documento");
        }
    }
}
