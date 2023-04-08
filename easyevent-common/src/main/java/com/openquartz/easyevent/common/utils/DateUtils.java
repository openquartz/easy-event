package com.openquartz.easyevent.common.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * DateUtils
 *
 * @author svnee
 **/
@Slf4j
public final class DateUtils {

    private DateUtils() {
    }

    /**
     * @param date 指定日期
     * @return 指定日期的开始时间
     */
    public static LocalDateTime getStartTimeByDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    /**
     * @param date 指定日期
     * @return 指定日期的结束时间
     */
    public static LocalDateTime getEndTimeByDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDateTime.of(localDate, LocalTime.of(23, 59, 59));
    }

    /**
     * get date
     */
    public static Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return floorDay(calendar.getTime());
    }

    /**
     * 抹平时间零头,返回当天零时
     *
     * @param date 时间
     */
    public static Date floorHour(Date date) {
        return floorDay(date);
    }

    /**
     * 抹平时间零头,返回当天零时
     *
     * @param date 时间
     */
    public static Date floorDay(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return Date.from(date2LocalDateTime(date).with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取指定日期的结束时间
     *
     * @param date 时间
     */
    public static Date getEndTime(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return Date.from(date2LocalDateTime(date).with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 计算时间秒跨度
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return long 时间秒差
     */
    public static long durationOfSeconds(Date start, Date end) {
        Duration duration = Duration.between(date2LocalDateTime(start), date2LocalDateTime(end));
        return duration.getSeconds();
    }

    /**
     * 时间加指定描述
     *
     * @param date 时间
     * @param plusSeconds 秒数
     */
    public static Date addSeconds(Date date, Long plusSeconds) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusSeconds(plusSeconds);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 时间加上指点天数
     *
     * @param date 时间
     * @param days 天数
     * @return 加上指定天数的时间
     */
    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static Date addHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * 抹平周零头, 返回当周一
     */
    public static LocalDate floorWeek(LocalDate localDate) {
        return localDate.minusDays((long) localDate.getDayOfWeek().getValue() - 1L);
    }

    /**
     * 抹平月零头, 返回当月1号
     */
    public static LocalDate floorMonth(LocalDate localDate) {
        return localDate.minusDays((long) localDate.getDayOfMonth() - 1L);
    }

    /**
     * 抹平年零头, 返回当年1月1号
     */
    public static LocalDate floorYear(LocalDate localDate) {
        return localDate.minusDays((long) localDate.getDayOfYear() - 1L);
    }

    /**
     * 计算日差
     */
    public static long diffDay(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }

    /**
     * 计算周差
     */
    public static long diffWeek(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.WEEKS.between(fromDate, toDate);
    }

    /**
     * 计算月差
     */
    public static long diffMonth(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.MONTHS.between(fromDate, toDate);
    }

    /**
     * 计算年差
     */
    public static long diffYear(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.YEARS.between(fromDate, toDate);
    }

    /**
     * 抹平周零头，计算周差
     * 注：
     * 日期1 = K周最后一天
     * 日期2 = K+1周第一天
     * 日期1和日期2周差为1
     */
    public static long floorAndDiffWeek(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorWeek(fromDate);
        LocalDate floorTo = floorWeek(toDate);
        return diffWeek(floorFrom, floorTo);
    }

    /**
     * 抹平月零头，计算月差
     * 注：
     * 日期1 = K月最后一天
     * 日期2 = K+1月第一天
     * 日期1和日期2月差为1
     */
    public static long floorAndDiffMonth(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorMonth(fromDate);
        LocalDate floorTo = floorMonth(toDate);
        return diffMonth(floorFrom, floorTo);
    }

    public static long floorAndDiffYear(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorYear(fromDate);
        LocalDate floorTo = floorYear(toDate);
        return diffYear(floorFrom, floorTo);
    }

    /**
     * 将毫秒时间戳转换为Date
     *
     * @param millis 毫秒时间戳
     * @return Date对象
     */
    public static Date millis2Date(Long millis) {
        if (null == millis) {
            return null;
        }
        if (millis < Integer.MAX_VALUE) {
            throw new IllegalArgumentException("millis 为毫秒时间戳");
        }
        return new Date(millis);
    }

    /**
     * Date转换为LocalDateTime
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();//An instantaneous point on the time-line.(时间线上的一个瞬时点。)
        ZoneId zoneId = ZoneId.systemDefault();//A time-zone ID, such as {@code Europe/Paris}.(时区)
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * LocalDateTime转换为Date
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime
            .atZone(zoneId);//Combines this date-time with a time-zone to create a  ZonedDateTime.
        return Date.from(zdt.toInstant());
    }

    /**
     * Date转LocalDate
     */
    public static LocalDate date2LocalDate(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * local date 转 date
     */
    public static Date localDate2Date(LocalDate localDate) {
        ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }
}
