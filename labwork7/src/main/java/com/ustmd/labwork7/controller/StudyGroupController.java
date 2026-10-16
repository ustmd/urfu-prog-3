package com.ustmd.labwork7.controller;

import com.ustmd.labwork7.dao.StudyGroupRepository;
import com.ustmd.labwork7.entity.StudyGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Slf4j
@RestController
public class StudyGroupController {

    private final StudyGroupRepository studyGroupRepository;

    @Autowired
    public StudyGroupController(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    @GetMapping("/groups")
    public ModelAndView getAllStudyGroups() {
        ModelAndView mav = new ModelAndView("list-study-groups");
        mav.addObject("studyGroups", studyGroupRepository.findAll());
        return mav;
    }

    @GetMapping("/addStudyGroupForm")
    public ModelAndView addStudyGroupForm() {
        ModelAndView mav = new ModelAndView("add-study-group-form");
        StudyGroup studyGroup = new StudyGroup();
        mav.addObject("studyGroup", studyGroup);
        return mav;
    }

    @PostMapping("/saveStudyGroup")
    public RedirectView saveStudyGroup(@ModelAttribute StudyGroup studyGroup) {
        studyGroupRepository.save(studyGroup);
        return new RedirectView("groups");
    }

    @GetMapping("/showGroupUpdateForm")
    public ModelAndView showGroupUpdateForm(@RequestParam Integer groupId) {
        ModelAndView mav = new ModelAndView("add-study-group-form");
        Optional<StudyGroup> optionalStudyGroup = studyGroupRepository.findById(groupId);
        StudyGroup studyGroup = new StudyGroup();
        if (optionalStudyGroup.isPresent()) {
            studyGroup = optionalStudyGroup.get();
        }
        mav.addObject("studyGroup", studyGroup);
        return mav;
    }

    @GetMapping("/deleteStudyGroup")
    public RedirectView deleteStudyGroup(@RequestParam Integer groupId) {
        studyGroupRepository.deleteById(groupId);
        return new RedirectView("groups");
    }
}
