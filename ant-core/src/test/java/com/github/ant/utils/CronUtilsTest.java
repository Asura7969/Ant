package com.github.ant.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class CronUtilsTest {

    @Test
    public void getNextExecuteTimeTest() {
        long testStartDate = 1612195200000L;
        Date startDate = new Date(testStartDate);
        Date t1 = CronUtils.getNextExecuteTime("0 0 18 * * ?", startDate);
        assert t1.getTime() == 1612260000000L;

        Date t2 = CronUtils.getNextExecuteTime("0 0 0 * * ?", startDate);
        assert t2.getTime() == 1612281600000L;
    }
}
