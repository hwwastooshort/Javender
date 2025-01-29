import Controller.CalendarController;

public class manualAddEditRemoveAppointmentTest {
    public static void main(String[] args) {
        CalendarController cc = new CalendarController();
        cc.addAppointment();
        cc.editAppointment();
        cc.deleteAppointment();
    }
}
