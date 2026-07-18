package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.ProhibitedWord;
import com.nitax.valueplusbackend.repository.ProhibitedWordRepository;
import com.nitax.valueplusbackend.service.AdminService;
import com.nitax.valueplusbackend.service.ProhibitedWordService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ProhibitedWordServiceImpl implements ProhibitedWordService {
    private final ProhibitedWordRepository prohibitedWordRepository;
    private final AdminService adminService;
    @Override
    public String getProhibitedWords() {
        return prohibitedWordRepository.findAll().stream()
                .map(ProhibitedWord::getWord)
                .reduce((word1, word2) -> word1 + ", " + word2)
                .orElse("");
    }

    @Override
    public ProhibitedWord addNewKeyWord(String keyword) {
        Admin admin =  getCurrentAdmin();
        ProhibitedWord newWord =  new ProhibitedWord();
        newWord.setWord(keyword);
        newWord.setAddedBy(admin);
        newWord.setDateAdded(new Date());
        return prohibitedWordRepository.save(newWord);
    }

    @Override
    public void deleteProhibitedWordById(long id) {
        prohibitedWordRepository.deleteById(id);
    }

    @Override
    public List<ProhibitedWord> getAllProhibitedWord(int page, int size) {
        Page<ProhibitedWord> prohibitedWordPage = prohibitedWordRepository.findAll(PageRequest.of(page, size));
        if (prohibitedWordPage.hasContent()) {
            return prohibitedWordPage.getContent();
        }
        return List.of();
    }


    private Admin getCurrentAdmin(){
        String adminEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            adminEmail =  userDetails.getEmail();
        }
        return adminService.getAdminByEmail(adminEmail);
    }
}
