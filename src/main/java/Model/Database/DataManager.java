package Model.Database;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DataManager {

    public Optional<Appointment> getAppointmentById(int appointmentId) throws DataManagerException;

    public List<Tag> getTagsByAppointmentId(int appointmentId) throws DataManagerException;

    public List<Appointment> getAppointmentsByDate(LocalDate date, JooqDataManager.DateFilter dateFilter) throws DataManagerException;

    public List<Appointment> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DataManagerException;

    public Optional<Tag> getTagById(int tagId) throws DataManagerException;

    public List<Appointment> getAppointmentsByTagId(int tagId) throws DataManagerException;

    public int addAppointment(Appointment appointment) throws DataManagerException;

    public void removeAppointmentById(int appointmentId) throws DataManagerException;

    public boolean removeTagByTagId(int tagId) throws DataManagerException;

    public int addTag(Tag tag) throws DataManagerException;

    public void removeAppointment(Appointment appointment) throws DataManagerException;

    public boolean removeTag(Tag tag) throws DataManagerException;

    public List<Tag> getAllTags() throws DataManagerException;

    public void updateAppointment(Appointment appointment) throws DataManagerException;

    }
