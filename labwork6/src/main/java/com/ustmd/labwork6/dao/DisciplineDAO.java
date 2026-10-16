package com.ustmd.labwork6.dao;

import org.springframework.stereotype.Repository;
import com.ustmd.labwork6.entity.Discipline;

import java.util.List;

@Repository
public interface DisciplineDAO {

    List<Discipline> getAllDisciplines();

    Discipline saveDiscipline(Discipline discipline);

    Discipline getDiscipline(int id);

    void deleteDiscipline(int id);

}
