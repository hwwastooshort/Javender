package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;



public class CalendarInterface implements UserInterface{

    private Scanner scanner = new Scanner(System.in);

    public String getMonth(LocalDate date) {
        StringBuilder monthString = new StringBuilder();

        String days = "MO TU WE TH FR SA SU";

        String dateHeader = date.getMonth().toString() + "\t" + date.getYear();
        int dateHeaderMargin = dateHeader.length() + (days.length() - dateHeader.length()) / 2;

        String dateHeaderCentered = String.format("%" + dateHeaderMargin + "s\n", dateHeader);

        monthString.append(dateHeaderCentered).append(days).append("\n");
        int offset = getDayOffset(date);
        for (int i = 0; i < offset; i++) {
            monthString.append("   ");
        }

        int dayPosition = offset;
        for (int day = 1; day <= date.getMonth().length(date.isLeapYear()); day++) {
            monthString.append(String.format("%2d ", day));

            dayPosition = (dayPosition + 1) % 7;
            if (dayPosition == 0) {
                monthString.append("\n");
            }
        }
        return monthString.toString();
    }

    /**
     * @param date any day in the month that is supposed to be displayed
     * @return amount of days that need to be skipped in order for the first day
     * of the month to be aligned with the corresponding day of the week in the CLI
     * **/
    private int getDayOffset(LocalDate date) {
        int offset = 0;
        DayOfWeek firstDayOfMonth = date.minusDays(date.getDayOfMonth() - 1).getDayOfWeek();
        offset = firstDayOfMonth.getValue() - 1;
        return offset;
    }

    public void startAppointmentCreation(){
        System.out.println("Enter the details of your appointment:\n");
    }

    public String getTitle(){
        System.out.print("Title: ");
        return scanner.next();
    }

    public LocalDate getStartDate(){
        System.out.print("Start Date (e.g: YYYY-MM-DD): ");
        String startDateString = scanner.next();
        return LocalDate.parse(startDateString);
    }

    public LocalDate getEndDate(){
            System.out.print("End Date (e.g: YYYY-MM-DD): ");
            String endDateString = scanner.next();
            return LocalDate.parse(endDateString);
    }

    public LocalTime getStartTime(){
        System.out.print("Start Time (e.g hh:mm): ");
        String startTimeString = scanner.next();
        return LocalTime.parse(startTimeString);
    }

    public LocalTime getEndTime(){
        System.out.print("End Time (e.g hh:mm): ");
        String endTimeString = scanner.next();
        return LocalTime.parse(endTimeString);
    }

    public String getDescription(){
        System.out.println("Description: ");
        return scanner.nextLine(); //TODO nextLine Problem lösen
    }

    public List<Tag> getTags(){
        //TODO implement getTags() functionality
        return null;
    }

    public void printError(String prompt){
        System.out.println(prompt);
    }

}