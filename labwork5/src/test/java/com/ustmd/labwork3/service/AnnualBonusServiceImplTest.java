package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Positions;
import java.time.Year;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnualBonusServiceImplTest {

    @Test
    void calculate() {

        // given
        Positions position = Positions.HR;
        double bonus = 2.0;
        int workDays = 243;
        double salary = 100000.00;

        // when
        double result = new AnnualBonusServiceImpl().calculate(position, salary, bonus, workDays);

        // then
        double expected = Year.now().isLeap() ? 361481.48148148146 : 360493.8271604938;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void calculateQuarterlyBonus() {

        // given
        Positions position = Positions.TL;
        double bonus = 2.0;
        int workDays = 243;
        double salary = 100000.00;

        // when
        double result = new AnnualBonusServiceImpl().calculateQuarterlyBonus(position, salary, bonus, workDays);

        // then
        double expected = new AnnualBonusServiceImpl().calculate(position, salary, bonus, workDays) / 4;
        assertThat(result).isEqualTo(expected);
        assertThatThrownBy(() -> new AnnualBonusServiceImpl().calculateQuarterlyBonus(Positions.DEV, salary, bonus, workDays))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
