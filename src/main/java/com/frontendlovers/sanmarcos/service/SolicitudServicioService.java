package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.SolicitudServicioRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudServicioResponse;
import com.frontendlovers.sanmarcos.entity.Mascota;
import com.frontendlovers.sanmarcos.entity.Servicio;
import com.frontendlovers.sanmarcos.entity.SolicitudServicio;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoSolicitud;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.MascotaRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import com.frontendlovers.sanmarcos.repository.SolicitudServicioRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudServicioService {

    private final SolicitudServicioRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final MascotaRepository mascotaRepository;

    public SolicitudServicioService(SolicitudServicioRepository solicitudRepository,
                                    UsuarioRepository usuarioRepository,
                                    ServicioRepository servicioRepository,
                                    MascotaRepository mascotaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.mascotaRepository = mascotaRepository;
    }

    // Crea una solicitud de servicio para el usuario autenticado
    @Transactional
    public SolicitudServicioResponse crear(Long usuarioId, SolicitudServicioRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Servicio servicio = servicioRepository.findById(request.servicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new ResourceNotFoundException("Servicio no disponible");
        }

        Mascota mascota = null;
        if (request.mascotaId() != null) {
            mascota = mascotaRepository.findById(request.mascotaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada"));
            if (!mascota.getUsuario().getId().equals(usuarioId)) {
                throw new ResourceNotFoundException("La mascota no pertenece al usuario");
            }
        }

        SolicitudServicio solicitud = SolicitudServicio.builder()
                .usuario(usuario)
                .servicio(servicio)
                .mascota(mascota)
                .mascotaNombre(request.mascotaNombre())
                .especie(request.especie())
                .fechaSolicitada(request.fechaSolicitada())
                .comentario(request.comentario())
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        return SolicitudServicioResponse.from(solicitudRepository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudServicioResponse> listarPorUsuario(Long usuarioId) {
        return solicitudRepository.findByUsuarioId(usuarioId).stream()
                .map(SolicitudServicioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudServicioResponse> listarTodas() {
        return solicitudRepository.findAll().stream()
                .map(SolicitudServicioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudServicioResponse buscarPorId(Long id) {
        return SolicitudServicioResponse.from(solicitudRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada")));
    }
}
