package com.frontendlovers.sanmarcos.user.service;

import com.frontendlovers.sanmarcos.auth.dto.UsuarioResponse;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.exception.DuplicateResourceException;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.user.dto.UserCreateRequest;
import com.frontendlovers.sanmarcos.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        return UsuarioResponse.from(getUsuario(id));
    }

    @Transactional
    public UsuarioResponse create(UserCreateRequest request) {
        if (usuarioRepository.existsByRun(request.run())) {
            throw new DuplicateResourceException("El RUN ya estǭ registrado");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya estǭ registrado");
        }

        Rol rol = getRol(request.rol());

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
                .rol(rol)
                .activo(request.activo() != null ? request.activo() : Boolean.TRUE)
                .build();

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(Long id, UserUpdateRequest request) {
        Usuario usuario = getUsuario(id);

        if (request.run() != null && !request.run().equals(usuario.getRun())) {
            if (usuarioRepository.existsByRun(request.run())) {
                throw new DuplicateResourceException("El RUN ya estǭ registrado");
            }
            usuario.setRun(request.run());
        }
        if (request.email() != null && !request.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("El email ya estǭ registrado");
            }
            usuario.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.nombre() != null) usuario.setNombre(request.nombre());
        if (request.apellido() != null) usuario.setApellido(request.apellido());
        if (request.fechaNacimiento() != null) usuario.setFechaNacimiento(request.fechaNacimiento());
        if (request.telefono() != null) usuario.setTelefono(request.telefono());
        if (request.direccion() != null) usuario.setDireccion(request.direccion());
        if (request.region() != null) usuario.setRegion(request.region());
        if (request.comuna() != null) usuario.setComuna(request.comuna());
        if (request.rol() != null) usuario.setRol(getRol(request.rol()));
        if (request.activo() != null) usuario.setActivo(request.activo());

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deactivate(Long id) {
        Usuario usuario = getUsuario(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Usuario getUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Rol getRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    }
}
