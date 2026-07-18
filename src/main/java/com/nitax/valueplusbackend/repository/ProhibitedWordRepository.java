package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.ProhibitedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProhibitedWordRepository extends JpaRepository<ProhibitedWord,Long> {

    @Query("SELECT p FROM ProhibitedWord p ORDER BY p.dateAdded DESC")
    Optional<ProhibitedWord> findLastAddedWord();
}
