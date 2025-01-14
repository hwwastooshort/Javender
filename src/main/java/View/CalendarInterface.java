package View;

import java.time.*;

public class CalendarInterface {

    public String printMonth(LocalDate date){
        StringBuilder monthString = new StringBuilder();
        String dateHeader = date.getMonth().toString() + "\t" + date.getYear() + "\n";
        String days = "MO TU WE TH FR SA SU\n";
        monthString.append(dateHeader).append(days);
        int offset = getDayOffset(date);
        for(int i = 0; i < offset; i++){
            monthString.append("   ");
        }
        DayOfWeek startDay = DayOfWeek.of(offset + 1);
        int dayPosition = offset;
        for(int day = 1; day <= date.getMonth().length(date.isLeapYear()); day++){
            monthString.append(String.format("%2d ", day));

            dayPosition = (dayPosition + 1) % 7;
            if(dayPosition == 0){
                monthString.append("\n");
            }
        }
        return monthString.toString();
    }

    private int getDayOffset(LocalDate date){
        int offset = 0;
        DayOfWeek firstDayOfMonth = date.minusDays(date.getDayOfMonth()-1).getDayOfWeek();
        offset = firstDayOfMonth.getValue() - 1;
        return offset;
    }

}
