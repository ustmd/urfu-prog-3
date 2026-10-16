package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Positions;
import org.springframework.stereotype.Service;

@Service
public interface AnnualBonusService {

    double calculate(Positions positions, double salary, double bonus, int workDays);

    double calculateQuarterlyBonus(Positions positions, double salary, double bonus, int workDays);
}
