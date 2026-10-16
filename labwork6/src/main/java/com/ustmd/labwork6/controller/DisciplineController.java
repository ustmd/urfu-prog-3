package com.ustmd.labwork6.controller;

import com.ustmd.labwork6.entity.Discipline;
import com.ustmd.labwork6.service.DisciplineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DisciplineController {

    @Autowired
    private DisciplineService disciplineService;

    @GetMapping("/disciplines")
    public ResponseEntity<ApiResponse<List<Discipline>>> allDisciplines() {
        List<Discipline> allDisciplines = disciplineService.getAllDisciplines();
        return ResponseEntity.ok(new ApiResponse<>(true, "Список дисциплин получен", allDisciplines));
    }

    @GetMapping("/disciplines/{id}")
    public ResponseEntity<ApiResponse<Discipline>> getDiscipline(@PathVariable("id") int id) {
        Discipline discipline = disciplineService.getDiscipline(id);
        if (discipline == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Дисциплина с id=" + id + " не найдена", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Дисциплина найдена", discipline));
    }

    @PostMapping("/disciplines")
    public ResponseEntity<ApiResponse<Discipline>> saveDiscipline(@RequestBody Discipline discipline) {
        Discipline saved = disciplineService.saveDiscipline(discipline);
        return ResponseEntity.ok(new ApiResponse<>(true, "Дисциплина добавлена", saved));
    }

    @PutMapping("/disciplines")
    public ResponseEntity<ApiResponse<Discipline>> updateDiscipline(@RequestBody Discipline discipline) {
        if (disciplineService.getDiscipline(discipline.getId()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Дисциплина с id=" + discipline.getId() + " не найдена", null));
        }
        Discipline updated = disciplineService.saveDiscipline(discipline);
        return ResponseEntity.ok(new ApiResponse<>(true, "Дисциплина изменена", updated));
    }

    @DeleteMapping("/disciplines/{id}")
    public ResponseEntity<ApiResponse<Integer>> deleteDiscipline(@PathVariable("id") int id) {
        if (disciplineService.getDiscipline(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Дисциплина с id=" + id + " не найдена", null));
        }
        disciplineService.deleteDiscipline(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Дисциплина удалена", id));
    }
}
