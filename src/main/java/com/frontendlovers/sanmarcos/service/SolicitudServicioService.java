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
import com.frontendlovers.sanmarcos.exception.SolicitudStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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

    // Asigna o modifica fecha y hora de la cita
    @Transactional
    public SolicitudServicioResponse actualizarCita(Long id, LocalDate fechaCita, LocalTime horaCita) {
        SolicitudServicio solicitud = buscarEntity(id);
        validarEstadoModificable(solicitud);
        solicitud.setFechaCita(fechaCita);
        solicitud.setHoraCita(horaCita);
        return SolicitudServicioResponse.from(solicitudRepository.save(solicitud));
    }

    // Actualiza el comentario de la solicitud
    @Transactional
    public SolicitudServicioResponse actualizarComentario(Long id, String comentario) {
        SolicitudServicio solicitud = buscarEntity(id);
        validarEstadoModificable(solicitud);
        solicitud.setComentario(comentario);
        return SolicitudServicioResponse.from(solicitudRepository.save(solicitud));
    }

    // Cambia el estado validando transiciones permitidas
    @Transactional
    public SolicitudServicioResponse cambiarEstado(Long id, EstadoSolicitud nuevoEstado) {
        SolicitudServicio solicitud = buscarEntity(id);

        if (solicitud.getEstado() == nuevoEstado) {
            return SolicitudServicioResponse.from(solicitud);
        }

        boolean valido = switch (solicitud.getEstado()) {
            case PENDIENTE -> nuevoEstado == EstadoSolicitud.CONFIRMADA || nuevoEstado == EstadoSolicitud.CANCELADA;
            case CONFIRMADA -> nuevoEstado == EstadoSolicitud.COMPLETADA || nuevoEstado == EstadoSolicitud.CANCELADA;
            default -> false;
        };

        if (!valido) {
            throw new SolicitudStateException(
                    String.format("Transición de estado no permitida: %s -> %s", solicitud.getEstado(), nuevoEstado));
        }

        solicitud.setEstado(nuevoEstado);
        return SolicitudServicioResponse.from(solicitudRepository.save(solicitud));
    }

    // Cancelación por el cliente solo si está PENDIENTE
    @Transactional
    public SolicitudServicioResponse cancelar(Long id, Long usuarioId) {
        SolicitudServicio solicitud = buscarEntity(id);

        if (!solicitud.getUsuario().getId().equals(usuarioId)) {
            throw new SolicitudStateException("No puedes cancelar una solicitud que no te pertenece");
        }

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudStateException("Solo se pueden cancelar solicitudes pendientes");
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        return SolicitudServicioResponse.from(solicitudRepository.save(solicitud));
    }

    private SolicitudServicio buscarEntity(Long id) {
        return solicitudRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
    }

    private void validarEstadoModificable(SolicitudServicio solicitud) {
        if (solicitud.getEstado() == EstadoSolicitud.CANCELADA || solicitud.getEstado() == EstadoSolicitud.COMPLETADA) {
            throw new SolicitudStateException("No se puede modificar una solicitud cancelada o completada");
        }
    }
}
