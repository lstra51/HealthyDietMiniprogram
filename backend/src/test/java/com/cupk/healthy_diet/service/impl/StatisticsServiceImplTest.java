package com.cupk.healthy_diet.service.impl;

import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.mapper.DietRecordMapper;
import com.cupk.healthy_diet.vo.StatisticsVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticsServiceImplTest {

    @Test
    void statisticsMultiplySingleServingNutritionByPortionOnce() {
        DietRecordMapper mapper = mock(DietRecordMapper.class);
        StatisticsServiceImpl service = new StatisticsServiceImpl(mapper);

        DietRecord record = new DietRecord();
        record.setUserId(1);
        record.setRecordDate(LocalDate.now());
        record.setPortion(2.0);
        record.setCalories(100);
        record.setProtein(10.0);
        record.setCarbs(20.0);
        record.setFat(5.0);

        when(mapper.selectList(any())).thenReturn(List.of(record));

        StatisticsVO statistics = service.getWeeklyStatistics(1);

        assertEquals(200.0, statistics.getAvgCalories(), 0.001);
        assertEquals(20.0, statistics.getAvgProtein(), 0.001);
        assertEquals(40.0, statistics.getAvgCarbs(), 0.001);
        assertEquals(10.0, statistics.getAvgFat(), 0.001);
        assertEquals(200, statistics.getTrend().getCalories().get(statistics.getTrend().getCalories().size() - 1));
    }
}
