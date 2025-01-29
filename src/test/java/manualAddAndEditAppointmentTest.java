import Controller.CalendarController;

public class manualAddAndEditAppointmentTest {
    public static void main(String[] args) {
        CalendarController cc = new CalendarController();
        cc.addAppointment();
        cc.editAppointment();
        cc.removeAppointment();
    }
}
