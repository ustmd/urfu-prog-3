package com.ustmd.labwork6.controller;

import com.ustmd.labwork6.entity.Student;
import com.ustmd.labwork6.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MyController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> allStudents() {
        List<Student> allStudents = studentService.getAllStudents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Список студентов получен", allStudents));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudent(@PathVariable("id") int id) {
        Student student = studentService.getStudent(id);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Студент с id=" + id + " не найден", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Студент найден", student));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<Student>> saveStudent(@RequestBody Student student) {
        Student saved = studentService.saveStudent(student);
        return ResponseEntity.ok(new ApiResponse<>(true, "Студент добавлен", saved));
    }

    @PutMapping("/students")
    public ResponseEntity<ApiResponse<Student>> updateStudent(@RequestBody Student student) {
        if (studentService.getStudent(student.getId()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Студент с id=" + student.getId() + " не найден", null));
        }
        Student updated = studentService.saveStudent(student);
        return ResponseEntity.ok(new ApiResponse<>(true, "Студент изменён", updated));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Integer>> deleteStudent(@PathVariable("id") int id) {
        if (studentService.getStudent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Студент с id=" + id + " не найден", null));
        }
        studentService.deleteStudent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Студент удалён", id));
    }
}
