package com.frontendlovers.sanmarcos.auth.service;

import com.frontendlovers.sanmarcos.auth.dto.AuthResponse;
import com.frontendlovers.sanmarcos.auth.dto.LoginRequest;
import com.frontendlovers.sanmarcos.auth.dto.RegisterRequest;
import com.frontendlovers.sanmarcos.auth.dto.UsuarioResponse;
import com.frontendlovers.sanmarcos.entity.Mascota;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.exception.DuplicateResourceException;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.MascotaRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.security.JwtService;
import com.frontendlovers.sanmarcos.security.UsuarioUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final MascotaRepository mascotaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioUserDetailsService usuarioUserDetailsService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       MascotaRepository mascotaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UsuarioUserDetailsService usuarioUserDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.mascotaRepository = mascotaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioUserDetailsService = usuarioUserDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByRun(request.run())) {
            throw new DuplicateResourceException("El RUN ya está registrado");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no encontrado en la base de datos"));

        Usuario usuario = Usuario.builder()
                .run(request.run())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .apellido(request.apellido())
                .fechaNacimiento(request.fechaNacimiento())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .region(request.region())
                .comuna(request.comuna())
                .rol(rolCliente)
                .build();

        usuarioRepository.save(usuario);

        if (request.mascota() != null) {
            Mascota mascota = Mascota.builder()
                    .nombre(request.mascota().nombre())
                    .especie(request.mascota().especie())
                    .raza(request.mascota().raza())
                    .usuario(usuario)
                    .build();
            mascotaRepository.save(mascota);
        }

        return buildAuthResponse(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), UsuarioResponse.from(usuario));
    }

    public UsuarioResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return UsuarioResponse.from(usuario);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        UserDetails userDetails = usuarioUserDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), UsuarioResponse.from(usuario));
    }
}
