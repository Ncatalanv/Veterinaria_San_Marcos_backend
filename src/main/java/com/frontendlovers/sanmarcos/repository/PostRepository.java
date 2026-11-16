package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPublicadoTrue(Pageable pageable);
    Optional<Post> findBySlugAndPublicadoTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}
