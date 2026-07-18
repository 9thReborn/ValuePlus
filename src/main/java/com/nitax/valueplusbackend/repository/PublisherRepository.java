package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Publisher;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
  boolean existsByEmail(String email);

  boolean existsByPubId(String toString);

  Optional<Publisher> findByPubId(String publisherId);

  Optional<Publisher> findByApiKey(String apiKey);

  Optional<Publisher> findByIdentifier(String identifier);
  Optional<Publisher> findByEmail(String email);

  //Autosuggestion
  @Query(value = "SELECT p.name,p.pub_id FROM publishers p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))",
          nativeQuery = true)
  List<Object[]> findPublishersByQuery(@Param("query") String query);

  List<Publisher> findAllByOrderByCreatedDateDesc();
}
