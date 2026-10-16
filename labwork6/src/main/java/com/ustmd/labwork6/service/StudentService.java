package com.ustmd.labwork6.service;

import org.springframework.stereotype.Service;
import com.ustmd.labwork6.entity.Student;

import java.util.List;

@Service
public interface StudentService {
    List<Student> getAllStudents();

    Student saveStudent(Student student);

    Student getStudent(int id);

    void deleteStudent(int id);
}
