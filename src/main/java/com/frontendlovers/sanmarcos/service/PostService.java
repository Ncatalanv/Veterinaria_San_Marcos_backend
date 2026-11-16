package com.frontendlovers.sanmarcos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.dto.PostRequest;
import com.frontendlovers.sanmarcos.dto.PostResponse;
import com.frontendlovers.sanmarcos.entity.Post;
import com.frontendlovers.sanmarcos.exception.DuplicateResourceException;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    public PostService(PostRepository postRepository, ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> listarPublicados(Pageable pageable) {
        return postRepository.findByPublicadoTrue(pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse obtenerPorSlug(String slug) {
        Post post = postRepository.findBySlugAndPublicadoTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> listarTodos(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse obtenerPorId(Long id) {
        return PostResponse.from(getPost(id));
    }

    @Transactional
    public PostResponse crear(PostRequest request) {
        if (postRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("El slug ya estǭ registrado");
        }
        Post post = Post.builder()
                .slug(request.slug())
                .titulo(request.titulo())
                .categoria(request.categoria())
                .extracto(request.extracto())
                .imagen(request.imagen())
                .alt(request.alt())
                .fecha(request.fecha())
                .contenido(writeContenido(request))
                .publicado(request.publicado() != null ? request.publicado() : Boolean.TRUE)
                .build();
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse actualizar(Long id, PostRequest request) {
        Post post = getPost(id);
        if (request.slug() != null && !request.slug().equals(post.getSlug())) {
            if (postRepository.existsBySlugAndIdNot(request.slug(), id)) {
                throw new DuplicateResourceException("El slug ya estǭ registrado");
            }
            post.setSlug(request.slug());
        }
        if (request.titulo() != null) post.setTitulo(request.titulo());
        post.setCategoria(request.categoria());
        post.setExtracto(request.extracto());
        post.setImagen(request.imagen());
        post.setAlt(request.alt());
        if (request.fecha() != null) post.setFecha(request.fecha());
        if (request.contenido() != null) post.setContenido(writeContenido(request));
        if (request.publicado() != null) post.setPublicado(request.publicado());
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public void despublicar(Long id) {
        Post post = getPost(id);
        post.setPublicado(false);
        postRepository.save(post);
    }

    private Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
    }

    private String writeContenido(PostRequest request) {
        try {
            return objectMapper.writeValueAsString(request.contenido());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Contenido JSON invǭlido", e);
        }
    }
}
