package Model.Database;

import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
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
    private final HikariDataSource dataSource;

    public JooqDataManager(String path_to_database) throws DataManagerException {
        logger.info("Initializing JooqDataManager with connection pooling: {}", path_to_database);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + path_to_database);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(60000);

        this.dataSource = new HikariDataSource(config);
    }

    @FunctionalInterface
    private interface DSLContextConsumer<T> {
        T execute(DSLContext create) throws Exception;
    }

    private <T> T tryWithDSL(DSLContextConsumer<T> consumer) throws DataManagerException {
        try (Connection connection = dataSource.getConnection()) {
            DSLContext create = DSL.using(connection, SQLDialect.SQLITE);
            return consumer.execute(create);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            throw new DataManagerException("Database operation failed: " + e.getMessage());
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
        return tryWithDSL(create -> {
            logger.info("Fetching appointment with ID: {}", appointmentId);

            Record record = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .fetchOne();

            if (record == null) {
                logger.warn("No appointment found in database for ID: {}", appointmentId);
                return Optional.empty();
            }

            logger.debug("Successfully fetched appointment");
            return Optional.of(mapToAppointment(record));
        });
    }

    public List<Tag> getTagsByAppointmentId(int appointmentId) throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Fetching Tags assigned to Appointment: {}", appointmentId);
            Result<?> result = create.select()
                    .from(APPOINTMENTTAG)
                    .join(TAG).on(APPOINTMENTTAG.TAGID.eq(TAG.TAGID))
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .fetch();

            return result.isEmpty() ? new ArrayList<>() : result.stream()
                    .map(record -> new Tag(
                            record.getValue(TAG.TAGID),
                            record.getValue(TAG.NAME),
                            record.getValue(TAG.COLOR)
                    ))
                    .collect(Collectors.toList());
        });
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date, DateFilter dateFilter) throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Fetching appointments on date: {} with filter: {}", date, dateFilter);
            String datePrefix = date.toString() + "T";

            Result<?> result = switch (dateFilter) {
                case STARTDATE ->
                        create.select().from(APPOINTMENT).where(APPOINTMENT.STARTDATE.like(datePrefix + "%")).fetch();
                case ENDDATE ->
                        create.select().from(APPOINTMENT).where(APPOINTMENT.ENDDATE.like(datePrefix + "%")).fetch();
            };

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
        });
    }

    public List<Appointment> getUpcomingAppointments(LocalDateTime date, int amount) throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Fetching the next {} upcoming appointments after {}", amount, date);

            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.ENDDATE.greaterThan(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .orderBy(APPOINTMENT.STARTDATE.asc())
                    .limit(amount)
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No appointments found after {}", date);
                return new ArrayList<>();
            }

            if (result.size() < amount) {
                logger.warn("Only {}/{} appointments found after {}", result.size(), amount, date);
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

            logger.debug("Successfully fetched {} upcoming appointments after {}", appointmentList.size(), date);
            return appointmentList;
        });
    }

    public List<Appointment> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DataManagerException {
        return tryWithDSL(create -> {
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
        });
    }

    public Optional<Tag> getTagById(int tagId) throws DataManagerException {
        return tryWithDSL(create -> {
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
        });
    }

    public int addAppointment(Appointment appointment) throws DataManagerException {
        try {
            return tryWithDSL(create ->
                    create.transactionResult(configuration -> {
                        DSLContext ctx = DSL.using(configuration);
                        logger.info("Adding new appointment in transaction: {}", appointment);

                        LocalDateTime startDate = appointment.getStartDate();
                        LocalDateTime endDate = appointment.getEndDate();
                        String title = appointment.getTitle();
                        String description = appointment.getDescription();
                        List<Tag> tags = appointment.getTags();

                        Record record = ctx.insertInto(APPOINTMENT, APPOINTMENT.STARTDATE, APPOINTMENT.ENDDATE,
                                        APPOINTMENT.TITLE, APPOINTMENT.DESCRIPTION)
                                .values(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                        endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                        title, description)
                                .returning(APPOINTMENT.APPOINTMENTID)
                                .fetchOne();

                        if (record == null) {
                            throw new DataManagerException("Failed to insert appointment. No ID returned.");
                        }

                        int insertedId = record.getValue(APPOINTMENT.APPOINTMENTID);

                        if (tags != null && !tags.isEmpty()) {
                            for (Tag tag : tags) {
                                boolean exists = ctx.fetchExists(
                                        ctx.selectOne()
                                                .from(APPOINTMENTTAG)
                                                .where(APPOINTMENTTAG.APPOINTMENTID.eq(insertedId)
                                                        .and(APPOINTMENTTAG.TAGID.eq(tag.getTagId())))
                                );
                                if (!exists) {
                                    logger.info("Adding Tag {} to Appointment ID: {}", tag, insertedId);
                                    ctx.insertInto(APPOINTMENTTAG, APPOINTMENTTAG.APPOINTMENTID, APPOINTMENTTAG.TAGID)
                                            .values(insertedId, tag.getTagId())
                                            .execute();
                                }
                            }
                        }

                        logger.info("Successfully added appointment with ID: {}", insertedId);
                        return insertedId;
                    })
            );
        } catch (org.jooq.exception.IntegrityConstraintViolationException e) {
            logger.error("Integrity constraint violation while adding appointment: {}", e.getMessage());
            throw new DataManagerException("Integrity constraint violation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while adding appointment: {}", e.getMessage(), e);
            throw new DataManagerException("Error adding appointment: " + e.getMessage());
        }
    }

    public void removeAppointmentById(int appointmentId) throws DataManagerException {
        tryWithDSL(create -> {
            logger.info("Removing appointment with ID: {}", appointmentId);

            create.deleteFrom(APPOINTMENTTAG)
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .execute();

            create.deleteFrom(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .execute();

            logger.info("Successfully removed appointment with ID: {}", appointmentId);
            return null;
        });
    }

    public void removeAppointment(Appointment appointment) throws DataManagerException {
        logger.info("Removing appointment: {}", appointment);
        removeAppointmentById(appointment.getAppointmentId());
    }

    public void removeTagByTagId(int tagId) throws DataManagerException {
        tryWithDSL(create ->
                create.transactionResult(configuration -> {
                    DSLContext ctx = DSL.using(configuration);
                    logger.info("Removing tag with ID: {}", tagId);

                    ctx.deleteFrom(APPOINTMENTTAG)
                            .where(APPOINTMENTTAG.TAGID.eq(tagId))
                            .execute();

                    int rowsDeleted = ctx.deleteFrom(TAG)
                            .where(TAG.TAGID.eq(tagId))
                            .execute();

                    if (rowsDeleted == 0) {
                        throw new DataManagerException("No tag found with ID: " + tagId);
                    }

                    logger.info("Successfully removed tag with ID: {}", tagId);
                    return true;
                })
        );
    }

    public void removeTag(Tag tag) throws DataManagerException {
        logger.info("Removing tag: {}", tag);
        removeTagByTagId(tag.getTagId());
    }

    public int addTag(Tag tag) throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Adding new tag: {}", tag);

            Record record = create.insertInto(TAG, TAG.NAME, TAG.COLOR)
                    .values(tag.getName(), tag.getColor())
                    .returning(TAG.TAGID)
                    .fetchOne();

            if (record == null) {
                throw new DataManagerException("Failed to insert tag. No ID returned.");
            }

            int insertedId = record.getValue(TAG.TAGID);
            logger.info("Successfully added tag with ID: {}", insertedId);
            return insertedId;
        });
    }

    @Override
    public List<Tag> getAllTags() throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Fetching all tags from the database");

            List<Tag> tags = create.select()
                    .from(TAG)
                    .fetchInto(Tag.class);

            logger.debug("Successfully fetched {} tags from the database", tags.size());
            return tags;
        });
    }

    @Override
    public void updateAppointment(Appointment appointment) throws DataManagerException {
        tryWithDSL(create -> {
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
            return null;
        });
    }

    @Override
    public List<Appointment> getAppointmentsByTitle(String title) throws DataManagerException {
        return tryWithDSL(create -> {
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
        });
    }

    public Optional<Tag> getTagByName(String title) throws DataManagerException {
        //get tag by title
        return tryWithDSL(create -> {
            logger.info("Fetching tag by title: {}", title);

            Optional<Tag> tag = create.select()
                    .from(TAG)
                    .where(TAG.NAME.eq(title))
                    .fetchOptionalInto(Tag.class);

            if (tag.isEmpty()) {
                logger.warn("No tag found with title: {}", title);
            } else {
                logger.debug("Successfully fetched tag with title: {}", title);
            }

            return tag;
        });
    }

    @Override
    public void updateTag(Tag tag) throws DataManagerException {
        tryWithDSL(create -> {
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
                    () -> logger.warn("No tag found with ID: {}", tag.getTagId())
            );

            return null;
        });
    }

    @Override
    public void removeAllAppointments() throws DataManagerException {
        tryWithDSL(create -> {
            logger.info("Removing all appointments from the database");

            create.deleteFrom(APPOINTMENTTAG).execute();
            logger.debug("Successfully removed all appointment");

            create.deleteFrom(APPOINTMENT).execute();
            logger.debug("Removed all appointments");

            return null;
        });
    }

    @Override
    public void removeAllTags() throws DataManagerException {
        tryWithDSL(create -> {
            logger.info("Removing all tags from the database");

            create.deleteFrom(APPOINTMENTTAG).execute();
            logger.debug("Successfully removed all appointment tags");

            create.deleteFrom(TAG).execute();
            logger.debug("Successfully removed all tags");

            return null;
        });
    }

    public List<Appointment> getUpcomingAppointmentsByTag(LocalDateTime date, int amount, String tagName) throws DataManagerException {
        return tryWithDSL(create -> {
            logger.info("Fetching the next {} upcoming appointments after {} with the tag {}", amount, date, tagName);

            Result<?> result = create.select()
                    .from(APPOINTMENT).naturalJoin(APPOINTMENTTAG).naturalJoin(TAG)
                    .where(APPOINTMENT.ENDDATE.greaterThan(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .and(DSL.lower(TAG.NAME).eq(tagName.toLowerCase()))
                    .orderBy(APPOINTMENT.STARTDATE.asc())
                    .limit(amount)
                    .fetch();

            if (result.isEmpty()) {
                logger.warn("No appointments with tag {} found after {}", tagName, date);
                return new ArrayList<>();
            }

            if (result.size() < amount) {
                logger.warn("Only {}/{} appointments with tag {} found after {}", result.size(), amount, tagName, date);
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

            logger.debug("Successfully fetched {} upcoming appointments after {} with tag {}", appointmentList.size(), date, tagName);
            return appointmentList;
        });
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}