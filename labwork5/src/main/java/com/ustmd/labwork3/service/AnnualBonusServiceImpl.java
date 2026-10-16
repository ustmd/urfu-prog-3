package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Positions;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public class AnnualBonusServiceImpl implements AnnualBonusService {

    @Override
    public double calculate(Positions positions, double salary, double bonus, int workDays) {
        int daysInYear = Year.now().isLeap() ? 366 : 365;
        return salary * bonus * daysInYear * positions.getPositionCoefficient() / workDays;
    }

    @Override
    public double calculateQuarterlyBonus(Positions positions, double salary, double bonus, int workDays) {
        if (!positions.isManager()) {
            throw new IllegalArgumentException("Квартальная премия рассчитывается только для управленцев");
        }
        return calculate(positions, salary, bonus, workDays) / 4;
    }
}
