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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jooq.generated.Tables.*;

public class JooqDataManager implements DataManager {

    private static final Logger logger = LoggerFactory.getLogger(JooqDataManager.class);
    private final String PATH_TO_DATABASE;
    private final Connection connection;
    private final DSLContext create;

    public JooqDataManager(String path_to_database) throws DataManagerException {
        logger.info("Initializing new JooqDataManager to: {}", path_to_database);
        this.PATH_TO_DATABASE = path_to_database;
        try {
            this.connection = DriverManager.getConnection(PATH_TO_DATABASE);
            this.create = DSL.using(connection, SQLDialect.SQLITE);
            logger.info("Successfully connected to database: {}", path_to_database);
        } catch (SQLException e) {
            logger.error("Connection to Database failed: {}", e.getMessage());
            throw new DataManagerException("Failed to connect to database: " + path_to_database, e);
        }
    }

    public enum DateFilter {
        STARTDATE,
        ENDDATE
    }

    private Appointment mapToAppointment(Record record) throws DataManagerException {
        LocalDateTime startDate = LocalDateTime.parse(record.getValue(APPOINTMENT.STARTDATE), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDate = LocalDateTime.parse(record.getValue(APPOINTMENT.ENDDATE), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        List<Tag> tags = getTagsByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID));
        return new Appointment(
                record.getValue(APPOINTMENT.APPOINTMENTID),
                startDate,
                endDate,
                record.getValue(APPOINTMENT.TITLE),
                record.getValue(APPOINTMENT.DESCRIPTION),
                tags
        );
    }

    public Optional<Appointment> getAppointmentById(int appointmentId) throws DataManagerException {
        try {
            logger.info("Fetching appointment with ID: {}", appointmentId);
            Record record = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .fetchOne();

            if (record == null) {
                logger.warn("No appointment found with ID: {}", appointmentId);
                return Optional.empty();
            }

            Appointment appointment = mapToAppointment(record);
            logger.debug("Successfully fetched appointment: {}", appointment);
            return Optional.of(appointment);

        } catch (Exception e) {
            logger.error("Error occurred while fetching appointment by ID: {}", appointmentId, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public List<Tag> getTagsByAppointmentId(int appointmentId) throws DataManagerException {
        try {
            logger.info("Fetching Tags assigned to Appointment: {}", appointmentId);
            Result<?> result = create.select()
                    .from(APPOINTMENTTAG)
                    .join(TAG).on(APPOINTMENTTAG.TAGID.eq(TAG.TAGID))
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No Tags found for Appointment: {}", appointmentId);
                return new ArrayList<>();
            }

            List<Tag> tagList = result.stream()
                    .map(record -> new Tag(
                            record.getValue(TAG.TAGID),
                            record.getValue(TAG.NAME),
                            record.getValue(TAG.COLOR)
                    ))
                    .collect(Collectors.toList());

            logger.debug("Successfully fetched tags: {}", tagList);
            return tagList;

        } catch (Exception e) {
            logger.error("Error occurred while fetching tags for appointment: {}", appointmentId, e);
            throw new DataManagerException(e);
        }
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date, DateFilter dateFilter) throws DataManagerException {
        try {
            logger.info("Fetching appointments on date: {} with filter: {}", date, dateFilter);
            String datePrefix = date.toString() + "T";

            Result<?> result;

            switch (dateFilter) {
                case STARTDATE:
                    logger.info("Fetching Appointments with StartDate: {}", date);
                    result = create.select()
                            .from(APPOINTMENT)
                            .where(APPOINTMENT.STARTDATE.like(datePrefix + "%"))
                            .fetch();
                    break;
                case ENDDATE:
                    logger.info("Fetching Appointments with EndDate: {}", date);
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
                logger.warn("No appointments found matching the date: {} with filter: {}", date, dateFilter);
                return new ArrayList<>();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        try {
                            return mapToAppointment(record);
                        } catch (DataManagerException e) {
                            logger.error("Error mapping record to appointment: {}", record, e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            logger.debug("Successfully fetched {} appointments for date: {}", appointmentList.size(), date);
            return appointmentList;

        } catch (Exception e) {
            logger.error("Error occurred while fetching appointments by date: {}", date, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public List<Appointment> getUpcomingAppointments(LocalDateTime date, int amount) throws DataManagerException{
        try {
            logger.info("Fetching the next {} upcoming appointments", amount);
            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.STARTDATE.greaterOrEqual(
                        date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .limit(amount)
                    .fetch();
            if(result.isEmpty()){
                logger.warn("No appointments found after {}", date);
                return new ArrayList<>();
            }
            if(result.size() < amount){
                logger.warn("There where only {}/{} appointments found after {}", result.size(), amount, date);
            }

            List<Appointment> appointmentList = result.stream()
                .map(record -> {
                    try {
                        return mapToAppointment(record);
                    } catch (DataManagerException e) {
                        logger.error("Error mapping record to appointment: {}", record, e);
                        throw new RuntimeException(e);
                    }
                })
                .toList();
            return appointmentList;
        }catch (Exception e){
            logger.error("Error occurred while fetching upcoming appointments after {}", date, e);
            throw new DataManagerException("Failed to fetch upcoming appointments after " + date);
        }
    }

    public List<Appointment> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DataManagerException {
        try {
            logger.info("Fetching appointments between {} and {}", startDateTime, endDateTime);

            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.STARTDATE.between(
                            startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No appointments found between {} and {}", startDateTime, endDateTime);
                return new ArrayList<>();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        try {
                            return mapToAppointment(record);
                        } catch (DataManagerException e) {
                            logger.error("Error mapping record to appointment: {}", record, e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            logger.debug("Successfully fetched {} appointments between {} and {}", appointmentList.size(), startDateTime, endDateTime);
            return appointmentList;

        } catch (Exception e) {
            logger.error("Error occurred while fetching appointments between {} and {}", startDateTime, endDateTime, e);
            throw new DataManagerException("Failed to fetch appointments in the given range.", e);
        }
    }

    public Optional<Tag> getTagById(int tagId) throws DataManagerException {
        try {
            logger.info("Fetching Tag with ID: {}", tagId);
            Record record = create.select().from(TAG).where(TAG.TAGID.eq(tagId)).fetchOne();

            if (record == null) {
                logger.warn("No Tag found with ID: {}", tagId);
                return Optional.empty();
            }

            Tag tag = new Tag(
                    record.getValue(TAG.TAGID),
                    record.getValue(TAG.NAME),
                    record.getValue(TAG.COLOR)
            );

            logger.debug("Successfully fetched Tag with ID: {}", tagId);
            return Optional.of(tag);

        } catch (Exception e) {
            logger.error("Error occurred while fetching Tag with ID: {}", tagId, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public List<Appointment> getAppointmentsByTagId(int tagId) throws DataManagerException {
        try {
            logger.info("Fetching appointments for Tag ID: {}", tagId);
            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .join(APPOINTMENTTAG).on(APPOINTMENT.APPOINTMENTID.eq(APPOINTMENTTAG.APPOINTMENTID))
                    .where(APPOINTMENTTAG.TAGID.eq(tagId))
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No appointments found for Tag ID: {}", tagId);
                return new ArrayList<>();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        try {
                            return mapToAppointment(record);
                        } catch (DataManagerException e) {
                            logger.error("Error mapping record to appointment for Tag ID: {}", tagId, e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            logger.debug("Successfully fetched {} appointments for Tag ID: {}", appointmentList.size(), tagId);
            return appointmentList;

        } catch (Exception e) {
            logger.error("Error occurred while fetching appointments for Tag ID: {}", tagId, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public int addAppointment(Appointment appointment) throws DataManagerException {
        try {
            logger.info("Adding new appointment: {}", appointment);
            LocalDateTime startDate = appointment.getStartDate();
            LocalDateTime endDate = appointment.getEndDate();
            String title = appointment.getTitle();
            String description = appointment.getDescription();
            List<Tag> tags = appointment.getTags();

            int insertedId = create.insertInto(APPOINTMENT, APPOINTMENT.STARTDATE, APPOINTMENT.ENDDATE,
                            APPOINTMENT.TITLE, APPOINTMENT.DESCRIPTION)
                    .values(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), title, description)
                    .returning(APPOINTMENT.APPOINTMENTID)
                    .fetchOne()
                    .getValue(APPOINTMENT.APPOINTMENTID);

            if (tags != null && !tags.isEmpty()) {
                for (Tag tag : tags) {
                    boolean exists = create.fetchExists(
                            create.selectOne()
                                    .from(APPOINTMENTTAG)
                                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(insertedId)
                                            .and(APPOINTMENTTAG.TAGID.eq(tag.getTagId())))
                    );
                    if (!exists) {
                        logger.info("Adding Tag {} to Appointment ID: {}", tag, insertedId);
                        create.insertInto(APPOINTMENTTAG, APPOINTMENTTAG.APPOINTMENTID, APPOINTMENTTAG.TAGID)
                                .values(insertedId, tag.getTagId())
                                .execute();
                    }
                }
            }

            logger.info("Successfully added appointment with ID: {}", insertedId);
            return insertedId;
        } catch (org.jooq.exception.IntegrityConstraintViolationException e) {
            logger.error("Integrity constraint violation while adding appointment: {}", e.getMessage());
            throw new DataManagerException("Integrity constraint violation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while adding appointment: {}", e.getMessage());
            throw new DataManagerException("Error adding appointment: " + e.getMessage());
        }
    }

    public void removeAppointmentById(int appointmentId) throws DataManagerException {
        try {
            logger.info("Removing appointment with ID: {}", appointmentId);
            create.deleteFrom(APPOINTMENTTAG)
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .execute();

            create.delete(APPOINTMENT).where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .execute();

            logger.info("Successfully removed appointment with ID: {}", appointmentId);
        } catch (Exception e) {
            logger.error("Error occurred while removing appointment with ID: {}", appointmentId, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public void removeAppointment(Appointment appointment) throws DataManagerException {
        logger.info("Removing appointment: {}", appointment);
        removeAppointmentById(appointment.getAppointmentId());
    }

    public boolean removeTagByTagId(int tagId) throws DataManagerException {
        try {
            logger.info("Removing tag with ID: {}", tagId);
            create.deleteFrom(TAG).where(TAG.TAGID.eq(tagId)).execute();
            create.deleteFrom(APPOINTMENTTAG).where(APPOINTMENTTAG.TAGID.eq(tagId)).execute();
            logger.info("Successfully removed tag with ID: {}", tagId);
            return true;

        } catch (Exception e) {
            logger.error("Error occurred while removing tag with ID: {}", tagId, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public boolean removeTag(Tag tag) throws DataManagerException {
        logger.info("Removing tag: {}", tag);
        return removeTagByTagId(tag.getTagId());
    }

    public int addTag(Tag tag) throws DataManagerException {
        try {
            logger.info("Adding new tag: {}", tag);
            String name = tag.getName();
            String color = tag.getColor();

            int insertedId = create.insertInto(TAG, TAG.NAME, TAG.COLOR)
                    .values(name, color)
                    .returning(TAG.TAGID)
                    .fetchOne()
                    .getValue(TAG.TAGID);

            logger.info("Successfully added tag with ID: {}", insertedId);
            return insertedId;

        } catch (Exception e) {
            logger.error("Error occurred while adding tag: {}", tag, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    @Override
    public List<Tag> getAllTags() throws DataManagerException {
        try {
            logger.info("Fetching all tags from the database");

            return create.select()
                    .from(TAG)
                    .fetchInto(Tag.class);

        } catch (Exception e) {
            logger.error("Error fetching tags from the database", e);
            throw new DataManagerException("Failed to fetch tags: " + e.getMessage());
        }
    }

    @Override
    public void updateAppointment(Appointment appointment) throws DataManagerException {
        try {
            logger.info("Updating appointment: {}", appointment);

            //check if appointment is in database
            if (getAppointmentById(appointment.getAppointmentId()).isEmpty()) {
                logger.warn("No appointment found with ID: {}", appointment.getAppointmentId());
                throw new DataManagerException("No appointment found with ID: " + appointment.getAppointmentId());
            }

            create.update(APPOINTMENT)
                    .set(APPOINTMENT.STARTDATE, appointment.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .set(APPOINTMENT.ENDDATE, appointment.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .set(APPOINTMENT.TITLE, appointment.getTitle())
                    .set(APPOINTMENT.DESCRIPTION, appointment.getDescription())
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointment.getAppointmentId()))
                    .execute();

            logger.info("Successfully updated basic appointment data for ID: {}", appointment.getAppointmentId());

            create.deleteFrom(APPOINTMENTTAG)
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointment.getAppointmentId()))
                    .execute();

            logger.info("Deleted existing tags for appointment ID: {}", appointment.getAppointmentId());

            for (Tag tag : appointment.getTags()) {
                create.insertInto(APPOINTMENTTAG, APPOINTMENTTAG.APPOINTMENTID, APPOINTMENTTAG.TAGID)
                        .values(appointment.getAppointmentId(), tag.getTagId())
                        .execute();
            }

            logger.info("Successfully updated tags for appointment ID: {}", appointment.getAppointmentId());

        } catch (Exception e) {
            logger.error("Error updating appointment: {}", e.getMessage());
            throw new DataManagerException("Failed to update appointment: " + e.getMessage());
        }
    }

    @Override
    public List<Appointment> getAppointmentsByTitle(String title) throws DataManagerException {
        try {
            logger.info("Fetching appointments with title: {}", title);

            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.TITLE.eq(title))
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No appointments found with title: {}", title);
                return new ArrayList<>();
            }

            List<Appointment> appointmentList = result.stream()
                    .map(record -> {
                        try {
                            return mapToAppointment(record);
                        } catch (DataManagerException e) {
                            logger.error("Error mapping record to appointment: {}", record, e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            logger.debug("Successfully fetched {} appointments with title: {}", appointmentList.size(), title);
            return appointmentList;

        } catch (Exception e) {
            logger.error("Error occurred while fetching appointments by title: {}", title, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    public Optional<Tag> getTagByName(String title) throws DataManagerException {
        //get tag by title
        try {
            logger.info("Fetching tag by title: {}", title);
            return create.select()
                    .from(TAG)
                    .where(TAG.NAME.eq(title))
                    .fetchOptionalInto(Tag.class);
        } catch (Exception e) {
            logger.error("Error occurred while fetching tag by title: {}", title, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    @Override
    public void updateTag(Tag tag) throws DataManagerException {
        try {
            logger.info("Updating tag: {}", tag);
            getTagById(tag.getTagId()).ifPresentOrElse(
                    existingTag -> {
                        create.update(TAG)
                                .set(TAG.NAME, tag.getName())
                                .set(TAG.COLOR, tag.getColor())
                                .where(TAG.TAGID.eq(existingTag.getTagId()))
                                .execute();
                        logger.info("Successfully updated tag with ID: {}", existingTag.getTagId());
                    },
                    () -> {
                        logger.warn("No tag found with title: {}", tag.getName());
                    }
            );
        } catch (Exception e) {
            logger.error("Error occurred while updating tag: {}", tag, e);
            throw new DataManagerException(e.getMessage());
        }
    }

    @Override
    public void removeAllAppointments() throws DataManagerException {
        try {
            logger.info("Removing all appointments from the database");
            create.delete(APPOINTMENT).execute();
            logger.debug("Successfully removed all appointments");
            create.delete(APPOINTMENTTAG).execute();
            logger.debug("Successfully removed all appointment tags");

        } catch(Exception e) {
            logger.error("Error occurred while removing all appointments: {}", e.getMessage());
            throw new DataManagerException(e.getMessage());
        }
    }

    @Override
    public void removeAllTags() throws DataManagerException {
        try {
            logger.info("Removing all tags from the database");
            create.delete(TAG).execute();
            logger.debug("Successfully removed all tags");
            create.delete(APPOINTMENTTAG).execute();
            logger.debug("Successfully removed all appointment tags");

        } catch(Exception e) {
            logger.error("Error occurred while removing all tags: {}", e.getMessage());
            throw new DataManagerException(e.getMessage());
        }
    }
}