package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        return scanner.nextLine();
    }

    public String getStartDate(){
        System.out.print("Start Date (e.g: YYYY-MM-DD): ");
        String startDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startDate;
    }

    public String getEndDate(){
        System.out.print("End Date (e.g: YYYY-MM-DD): ");
        String endDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endDate;
    }

    public String getStartTime(){
        System.out.print("Start Time (e.g hh:mm): ");
        String startTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startTime;
    }

    public String getEndTime(){
        System.out.print("End Time (e.g hh:mm): ");
        String endTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endTime;
    }

    public String getDescription(){
        System.out.println("Description: ");
        return scanner.nextLine();
    }

    public Optional<Tag> getTag(List<Tag> tags){
        System.out.println("Select the tag you want to add to your appointment:");
        int index = 0;
        for (int i = 0; i < tags.size(); i++) {
            System.out.println(i + 1 + ". " + tags.get(i).getName());
            index = i;
        }
        System.out.println(tags.size() + 1 +". Exit\n");
        int input = scanner.nextInt();
        if(input >= tags.size() +1){
            return Optional.empty();
        }
        return Optional.of(tags.get(input - 1));
    }

    public void printError(String prompt){
        System.out.println(prompt);
    }

}