
DELETE FROM AppointmentTag;
DELETE FROM Appointment;
DELETE FROM Tag;

INSERT INTO Tag (tagId, name, color) VALUES (1, 'Personal', 'red');
INSERT INTO Tag (tagId, name, color) VALUES (2, 'Work', 'blue');

INSERT INTO Appointment (appointmentId, startDate, endDate, title, description) VALUES
    (1, '2025-01-01T09:00:00', '2025-01-01T10:00:00', 'Doctor Appointment', 'Annual checkup');
INSERT INTO Appointment (appointmentId, startDate, endDate, title, description) VALUES
    (2, '2025-01-01T11:00:00', '2025-01-01T12:00:00', 'Team Meeting', 'Monthly progress update');
INSERT INTO Appointment (appointmentId, startDate, endDate, title, description) VALUES
    (3, '2025-01-02T14:00:00', '2025-01-02T15:00:00', 'Client Presentation', 'Present new project proposal');

INSERT INTO AppointmentTag (appointmentId, tagId) VALUES (1, 1);
INSERT INTO AppointmentTag (appointmentId, tagId) VALUES (2, 2);
INSERT INTO AppointmentTag (appointmentId, tagId) VALUES (3, 2);