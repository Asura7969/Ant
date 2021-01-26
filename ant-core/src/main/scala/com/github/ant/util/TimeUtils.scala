package com.github.ant.util

import java.util.{Calendar, Date}

object TimeUtils {

  /**
   * 获取指定日期为当月第几天
   * @param date 日期参数
   * @return 1 - 31
   */
  def getDaysOfMonth(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

}
