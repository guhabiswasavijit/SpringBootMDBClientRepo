package org.example;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.text.ParseException;

public class DateUtil {
    private static DateTimeFormatter df = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
    public static final java.sql.Date parseDate(String dateString) throws ParseException{
        DateTime dateTime = DateTime.parse(dateString, df);
        return new java.sql.Date(dateTime.getMillis());
    }
    public static void main(String[] args){
        try {
            java.sql.Date date = parseDate("11/12/2020 12:55:32");
            System.out.println(date);
        }catch(ParseException ex){
            ex.printStackTrace();
        }
    }
}
