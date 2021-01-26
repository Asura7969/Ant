package com.github.ant.utils;

import org.apache.logging.log4j.core.util.CronExpression;

import java.text.ParseException;
import java.util.Date;

public class CronUtils {

    /**
     * 根据 Cron表达式和开始时间，得到下次执行时间
     *
     * @param cron crontab 表达式
     * @param startDate 指定时间
     * @return 下次执行时间
     */
    public static Date getNextExecuteTime(String cron, Date startDate) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            return cronExpression.getNextValidTimeAfter(startDate == null ? new Date() : startDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("无效的cron表达式:" + cron, e);
        }
    }
}
