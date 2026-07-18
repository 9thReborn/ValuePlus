package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.ProhibitedWord;

import java.util.List;

public interface ProhibitedWordService {
    String getProhibitedWords();

    ProhibitedWord addNewKeyWord(String keyword);

    void deleteProhibitedWordById(long id);


    List<ProhibitedWord> getAllProhibitedWord(int page, int size);
}
