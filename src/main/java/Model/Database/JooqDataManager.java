package Model.Database;

import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.*;

public class JooqDataManager implements DataManager {

    private final String PATH_TO_DATABASE;
    private final Connection connection;
    private final DSLContext create;

    public JooqDataManager(String path_to_database) {
        this.PATH_TO_DATABASE = path_to_database;
        try {
            this.connection = DriverManager.getConnection(PATH_TO_DATABASE);
            this.create = DSL.using(connection, SQLDialect.SQLITE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + path_to_database, e);
        }
    }
    public enum DateFilter {
        STARTDATE,
        ENDDATE
    }

    /**
     * Method for fetching data of an Appointment based on the appointmentId
     *
     * @param appointmentId the Appointment we want to look up
     * @return Optional of Appointment, returns empty Optional if the appointmentId doesn't exist
     */
    public Optional<Appointment> getAppointmentById(int appointmentId) throws DataManagerException {
        try {
            Record record = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .fetchOne();

            if (record == null) {
                return Optional.empty();
            }

            String startDateString = record.getValue(APPOINTMENT.STARTDATE);
            String endDateString = record.getValue(APPOINTMENT.ENDDATE);

            LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Appointment appointment = new Appointment(
                    record.getValue(APPOINTMENT.APPOINTMENTID),
                    startDate,
                    endDate,
                    record.getValue(APPOINTMENT.TITLE),
                    record.getValue(APPOINTMENT.DESCRIPTION),
                    getTagsByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
            );

            return Optional.of(appointment);

        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: Logging
        }
    }

    /**
     * Method for fetching Data of Tags, which belong to a unique appointmentId
     *
     * @param appointmentId the appointmentId of the Appointment which is used for Joining
     * @return the list of tags matching the appointmentId as an Optional, returns empty Optional if there are none
     */

    public Optional<List<Tag>> getTagsByAppointmentId(int appointmentId) throws DataManagerException {
        try {
            Result<?> result = create.select()
                    .from(APPOINTMENTTAG)
                    .join(TAG).on(APPOINTMENTTAG.TAGID.eq(TAG.TAGID))
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .fetch();

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Tag> tagList = result.stream()
                    .map(record -> new Tag(
                            record.getValue(TAG.TAGID),
                            record.getValue(TAG.NAME),
                            record.getValue(TAG.COLOR)
                    ))
                    .collect(Collectors.toList());

            return Optional.of(tagList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for fetching a list of appointments, which starts or ends at a specific date
     *
     * @param date       the date to be searched for
     * @param dateFilter the filter option to decide if the given date should match the start- or the end date of an appointment
     * @return the list of appointments matching the date, returns null, if no appointments were found
     */

    public Optional<List<Appointment>> getAppointmentsByDate(LocalDate date, DateFilter dateFilter) throws DataManagerException {
        try {
            String datePrefix = date.toString() + "T"; // example: "2025-01-01T"

            Result<?> result;

            switch (dateFilter) {
                case STARTDATE:
                    result = create.select()
                            .from(APPOINTMENT)
                            .where(APPOINTMENT.STARTDATE.like(datePrefix + "%"))
                            .fetch();
                    break;
                case ENDDATE:
                    result = create.select()
                            .from(APPOINTMENT)
                            .where(APPOINTMENT.ENDDATE.like(datePrefix + "%"))
                            .fetch();
                    break;
                default:
                    result = null;
                    break;
            }

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        String startDateString = record.getValue(APPOINTMENT.STARTDATE);
                        String endDateString = record.getValue(APPOINTMENT.ENDDATE);

                        LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                        try {
                            return new Appointment(
                                    record.getValue(APPOINTMENT.APPOINTMENTID),
                                    startDate,
                                    endDate,
                                    record.getValue(APPOINTMENT.TITLE),
                                    record.getValue(APPOINTMENT.DESCRIPTION),
                                    getTagsByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
                            );
                        } catch (DataManagerException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            return Optional.of(appointmentList);

        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: Logging, individual Exception
        }
    }

    /**
     * Method for fetching Appointments in a given Time Intervall
     *
     * @param startDateTime start of the Intervall
     * @param endDateTime   end of the Intervall
     * @return Optional of a List of Appointments, returns an empty Optional if Query has no matches
     */

    public Optional<List<Appointment>> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DataManagerException {
        try {
            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.STARTDATE.between(
                            startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .fetch();

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        String startDateString = record.getValue(APPOINTMENT.STARTDATE);
                        String endDateString = record.getValue(APPOINTMENT.ENDDATE);

                        LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                        try {
                            return new Appointment(
                                    record.getValue(APPOINTMENT.APPOINTMENTID),
                                    startDate,
                                    endDate,
                                    record.getValue(APPOINTMENT.TITLE),
                                    record.getValue(APPOINTMENT.DESCRIPTION),
                                    getTagsByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
                            );
                        } catch (DataManagerException e) {
                            throw new RuntimeException(e);
                        }

                    })
                    .collect(Collectors.toList());

            return Optional.of(appointmentList);

        } catch (Exception e) {
            throw new DataManagerException(e);
            //TODO: Logging, individual Exception
        }
    }

    /**
     * Method for fetching a Tag based on its tagId
     *
     * @param tagId the given tagId
     * @return Optional of Tag, returns an empty Optional if Query has no matches
     */

    public Optional<Tag> getTagById(int tagId) throws DataManagerException {
        try {
            Record record = create.select().from(TAG).where(TAG.TAGID.eq(tagId)).fetchOne();

            if (record == null) {
                return Optional.empty();
            }

            Tag tag = new Tag(record.getValue(TAG.TAGID),
                    record.getValue(TAG.NAME),
                    record.getValue(TAG.COLOR)
            );

            return Optional.of(tag);

        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: logging, individual Exception
        }
    }

    /**
     * Method for fetching Appointments based on a given tagId
     *
     * @param tagId the given tagId
     * @return Optional of List of Appointment, returns an empty Optional if Query has no matches
     */

    public Optional<List<Appointment>> getAppointmentsByTagId(int tagId) throws DataManagerException {
        try {
            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .join(APPOINTMENTTAG).on(APPOINTMENT.APPOINTMENTID.eq(APPOINTMENTTAG.APPOINTMENTID))
                    .where(APPOINTMENTTAG.TAGID.eq(tagId))
                    .fetch();
            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        try {
                            return new Appointment(
                                    record.getValue(APPOINTMENT.APPOINTMENTID),
                                    LocalDateTime.parse(record.getValue(APPOINTMENT.STARTDATE)),
                                    LocalDateTime.parse(record.getValue(APPOINTMENT.ENDDATE)),
                                    record.getValue(APPOINTMENT.TITLE),
                                    record.getValue(APPOINTMENT.DESCRIPTION),
                                    getTagsByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).get()
                            );
                        } catch (DataManagerException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            return Optional.of(appointmentList);

        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: logging, individual Exception
        }
    }

    public int addAppointment(Appointment appointment) throws DataManagerException {
        try {
            LocalDateTime startDate = appointment.getStartDate();
            LocalDateTime endDate = appointment.getEndDate();
            String title = appointment.getTitle();
            String description = appointment.getDescription();
            List<Tag> tags = appointment.getTags();

            // Füge den Termin ein und erhalte die ID
            int insertedId = create.insertInto(APPOINTMENT, APPOINTMENT.STARTDATE, APPOINTMENT.ENDDATE,
                            APPOINTMENT.TITLE, APPOINTMENT.DESCRIPTION)
                    .values(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), title, description)
                    .returning(APPOINTMENT.APPOINTMENTID)
                    .fetchOne()
                    .getValue(APPOINTMENT.APPOINTMENTID);

            // Wenn Tags vorhanden sind, füge sie hinzu, aber prüfe auf Duplikate
            if (tags != null && !tags.isEmpty()) {
                for (Tag tag : tags) {
                    boolean exists = create.fetchExists(
                            create.selectOne()
                                    .from(APPOINTMENTTAG)
                                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(insertedId)
                                            .and(APPOINTMENTTAG.TAGID.eq(tag.getTagId())))
                    );
                    if (!exists) {
                        create.insertInto(APPOINTMENTTAG, APPOINTMENTTAG.APPOINTMENTID, APPOINTMENTTAG.TAGID)
                                .values(insertedId, tag.getTagId())
                                .execute();
                    }
                }
            }

            return insertedId;
        } catch (org.jooq.exception.IntegrityConstraintViolationException e) {
            throw new DataManagerException("Integrity constraint violation: " + e.getMessage());
        } catch (Exception e) {
            throw new DataManagerException("Error adding appointment: " + e.getMessage());
        }
    }

    public void removeAppointmentById(int appointmentId) throws DataManagerException {
        try {
            create.deleteFrom(APPOINTMENTTAG)
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .execute();

            create.delete(APPOINTMENT).where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .execute();
        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: logging, individual Exception
        }
    }

    public void removeAppointment(Appointment appointment) throws DataManagerException {
        removeAppointmentById(appointment.getAppointmentId());
    }


    public boolean removeTagByTagId(int tagId) throws DataManagerException {
        try {
           create.deleteFrom(TAG).where(TAG.TAGID.eq(tagId)).execute();
           return true;

        } catch (Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: logging, individual Exception
        }
    }

    public boolean removeTag(Tag tag) throws DataManagerException {
        return removeTagByTagId(tag.getTagId());
    }

    public int addTag(Tag tag) throws DataManagerException {
        try {
            String name = tag.getName();
            String color = tag.getColor();

            return create.insertInto(TAG, TAG.NAME, TAG.COLOR)
                    .values(name, color)
                    .returning(TAG.TAGID)
                    .fetchOne()
                    .getValue(TAG.TAGID);

        } catch(Exception e) {
            throw new DataManagerException(e.getMessage());
            //TODO: logging, individual Exception
        }
    }
}