package com.ggreiff.utils;

import com.ggreiff.p6.XlsxWbsActivity;
import com.primavera.common.value.*;
import com.primavera.integration.client.bo.enm.UDFIndicator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused", "ConstantConditions"})
public class Utils {

    final static Logger P6logger =  LogManager.getLogger(Utils.class);

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Boolean equalYearAndMonth(Date date1, Date date2) {
        return equalYears(date1, date2) && (equalMonths(date1, date2));
    }

    public static Boolean equalYears(Date date1, Date date2) {
        return getYear(date1).equals(getYear(date2));
    }

    public static Boolean equalMonths(Date date1, Date date2) {
        return getMonth(date1).equals(getMonth(date2));
    }

    public static Boolean isLessThan(Date date1, Date date2) {
        if (date1 == null) return true;
        return date2.getTime() < date1.getTime();
    }

    public static Boolean isGreaterThan(Date date1, Date date2) {
        if (date1 == null) return true;
        return date2.getTime() > date1.getTime();
    }

    public static Date getFirstDayOfMonth(Date date) {
        LocalDate localDate = asLocalDate(date);
        return Utils.asDate(localDate.with(TemporalAdjusters.firstDayOfMonth()));
    }

    public static Date getLastDayOfMonth(Date date) {
        LocalDate localDate = asLocalDate(date);
        return Utils.asDate(localDate.with(TemporalAdjusters.lastDayOfMonth()));
    }

    public static Integer getYear(Date date) {
        LocalDate localDate = asLocalDate(date);
        return localDate.getYear();
    }

    public static Integer getMonth(Date date) {
        LocalDate localDate = asLocalDate(date);
        return localDate.getMonthValue();
    }


    public static Double getDoubleFromString(String stringParameter) {
        try {
            return Double.parseDouble(stringParameter);
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return 0.0;
    }

    public static Date getExcelDate(String number) {
        try {
            if (number == null || number.isEmpty()) number = "0.0";
            return DateUtil.getJavaDate(getDoubleFromString(number));
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    public static Integer getIntegerFromString(String stringParameter) {
        return getDoubleFromString(stringParameter).intValue();
    }

    public static Cost getCostFromString(String stringParameter) {
        try {
            return new Cost(getDoubleFromString(stringParameter));
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return new Cost(0.0);
    }

    public static BeginDate getStartDateFromString(String stringParameter) {
        try {
            return new BeginDate(getExcelDate(stringParameter));
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return new BeginDate(new Date());
    }

    public static EndDate getEndDateFromString(String stringParameter) {
        try {
            return new EndDate(getExcelDate(stringParameter));
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return new EndDate(new Date());
    }

    public static UDFIndicator getIndicatorFromString(String stringParameter) {
        if (stringParameter.equalsIgnoreCase("Green")) return UDFIndicator.GREEN;
        if (stringParameter.equalsIgnoreCase("Blue")) return UDFIndicator.BLUE;
        if (stringParameter.equalsIgnoreCase("Red")) return UDFIndicator.RED;
        if (stringParameter.equalsIgnoreCase("Yellow")) return UDFIndicator.YELLOW;
        return UDFIndicator.NONE;
    }
}