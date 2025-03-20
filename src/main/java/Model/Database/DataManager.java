package Model.Database;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DataManager {

    Optional<Appointment> getAppointmentById(int appointmentId) throws DataManagerException;

    List<Tag> getTagsByAppointmentId(int appointmentId) throws DataManagerException;

    List<Appointment> getAppointmentsByDate(LocalDate date, JooqDataManager.DateFilter dateFilter) throws DataManagerException;

    List<Appointment> getUpcomingAppointments(LocalDateTime date, int amount) throws DataManagerException;

    List<Appointment> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DataManagerException;

    Optional<Tag> getTagById(int tagId) throws DataManagerException;


    int addAppointment(Appointment appointment) throws DataManagerException;

    void removeAppointmentById(int appointmentId) throws DataManagerException;

    void removeTagByTagId(int tagId) throws DataManagerException;

    int addTag(Tag tag) throws DataManagerException;

    void removeAppointment(Appointment appointment) throws DataManagerException;

    void removeTag(Tag tag) throws DataManagerException;

    List<Tag> getAllTags() throws DataManagerException;

    void updateAppointment(Appointment appointment) throws DataManagerException;

    List<Appointment> getAppointmentsByTitle(String title) throws DataManagerException;

    Optional<Tag> getTagByName(String name) throws DataManagerException;

    void updateTag(Tag tag) throws DataManagerException;

    void removeAllAppointments() throws DataManagerException;

    void removeAllTags() throws DataManagerException;

    List<Appointment> getUpcomingAppointmentsByTag(LocalDateTime date, int amount, String tagName) throws DataManagerException;
}
