create DATABASE IF NOT EXISTS PilgrimManagement;
USE PilgrimManagement;

CREATE TABLE Admin (
    AdminID INT PRIMARY KEY,
    AdminName VARCHAR(50) NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    Email VARCHAR(100) NOT NULL,
    SupervisorID INT,
    FOREIGN KEY (SupervisorID) REFERENCES Admin(AdminID)
);

CREATE TABLE Permit (
    PermitID INT PRIMARY KEY,
    Name VARCHAR(50) NOT NULL,
    ServiceType VARCHAR(50) NOT NULL,
    Location VARCHAR(100) NOT NULL
);

CREATE TABLE Pilgrim (
    PilgrimID INT PRIMARY KEY,
    PilgrimName VARCHAR(50) NOT NULL,
    SpecialNeed TEXT,
    PilgrimAge INT NOT NULL,
    AdminID INT,
    PermitID INT,
    FOREIGN KEY (AdminID) REFERENCES Admin(AdminID),
    FOREIGN KEY (PermitID) REFERENCES Permit(PermitID)
);

CREATE TABLE MedicalProfile (
    PilgrimID INT PRIMARY KEY,
    BloodType VARCHAR(5) NOT NULL,
    Medications TEXT,
    MedicalHistory TEXT,
    AdminID INT NOT NULL,
    FOREIGN KEY (AdminID) REFERENCES Admin(AdminID),
    FOREIGN KEY (PilgrimID) REFERENCES Pilgrim(PilgrimID)
);

CREATE TABLE PilgrimAllergy (
    PilgrimID INT,
    Allergy VARCHAR(50),
    PRIMARY KEY (PilgrimID, Allergy),
    FOREIGN KEY (PilgrimID) REFERENCES Pilgrim(PilgrimID)
);

CREATE TABLE Accommodation (
    AccommodationID INT PRIMARY KEY,
    HotelName VARCHAR(100) NOT NULL,
    Street VARCHAR(100),
    City VARCHAR(50),
    PostalCode VARCHAR(10),
    Capacity INT NOT NULL
);

CREATE TABLE PilgrimAccommodation (
    PilgrimID INT,
    AccommodationID INT,
    RoomType VARCHAR(30) NOT NULL,
    PRIMARY KEY (PilgrimID, AccommodationID),
    FOREIGN KEY (PilgrimID) REFERENCES Pilgrim(PilgrimID),
    FOREIGN KEY (AccommodationID) REFERENCES Accommodation(AccommodationID)
);

CREATE TABLE TransportSchedule (
    ScheduleID INT PRIMARY KEY,
    DepartureTime TIME NOT NULL,
    ArrivalTime TIME NOT NULL,
    TransportType VARCHAR(30) NOT NULL,
    Route VARCHAR(100) NOT NULL
);

CREATE TABLE PilgrimTransport (
    PilgrimID INT,
    ScheduleID INT,
    PRIMARY KEY (PilgrimID, ScheduleID),
    FOREIGN KEY (PilgrimID) REFERENCES Pilgrim(PilgrimID),
    FOREIGN KEY (ScheduleID) REFERENCES TransportSchedule(ScheduleID)
);

CREATE TABLE Feedback (
    FeedbackID INT PRIMARY KEY,
    PilgrimID INT NOT NULL,
    Content TEXT NOT NULL,
    Rating INT NOT NULL,
    Date DATE NOT NULL,
    FOREIGN KEY (PilgrimID) REFERENCES Pilgrim(PilgrimID)
);

INSERT INTO Admin VALUES 
(1, 'Yara Alqahtani', '0551234567', 'Yory@admin.com', NULL),
(2, 'Mona Saleh', '0552345678', 'mona@admin.com', 1),
(3, 'Fahad Nasser', '0553456789', 'fahad@admin.com', 2),
(4, 'Sarah Ali', '0554567890', 'sarah@admin.com', 2),
(5, 'Nora Mohammed', '0555678901', 'nora@admin.com', 3),
(6, 'Khalid Alharbi', '0556789012', 'khalid@admin.com', 3),
(7, 'Reem Saad', '0557890123', 'reem@admin.com', 4),
(8, 'Hani Omar', '0558901234', 'hani@admin.com', 4),
(9, 'Amal Hassan', '0559012345', 'amal@admin.com', 5),
(10, 'Yasser Sami', '0550123456', 'yasser@admin.com', 5);

INSERT INTO Permit VALUES 
(1, 'Umrah', 'Religious', 'Makkah'),
(2, 'Hajj', 'Religious', 'Mina'),
(3, 'Transport', 'Service', 'Jeddah'),
(4, 'Medical', 'Health', 'Makkah Hospital'),
(5, 'Guidance', 'Support', 'Makkah Center'),
(6, 'Security', 'Control', 'Haram'),
(7, 'Volunteer', 'Support', 'Mina Camp'),
(8, 'Sanitation', 'Service', 'Arafat'),
(9, 'Medical 2', 'Health', 'Arafat Clinic'),
(10, 'Transportation 2', 'Service', 'Jeddah');

INSERT INTO Pilgrim VALUES 
(1, 'Ali Hassan', 'Wheelchair', 65, 1, 1),
(2, 'Salim Omar', NULL, 40, 1, 2),
(3, 'Layla Nader', 'Vision impairment', 50, 2, 3),
(4, 'Mazen Talal', NULL, 38, 2, 4),
(5, 'Noura Majed', NULL, 30, 3, 5),
(6, 'Huda Saleh', 'Wheelchair', 70, 4, 6),
(7, 'Abdullah Faleh', NULL, 55, 5, 7),
(8, 'Sami Jaber', 'Crutches', 47, 6, 8),
(9, 'Rania Sami', NULL, 43, 7, 9),
(10, 'Maha Khalid', 'Hearing aid', 34, 8, 10);

INSERT INTO MedicalProfile VALUES 
(1, 'A+', 'Panadol', 'Diabetes', 1),
(2, 'O-', 'Ibuprofen', 'Asthma', 1),
(3, 'B+', NULL, 'High BP', 2),
(4, 'AB-', 'Aspirin', 'Heart Issue', 2),
(5, 'O+', 'Vitamin D', NULL, 3),
(6, 'A-', 'Insulin', 'Diabetes', 4),
(7, 'B-', NULL, NULL, 5),
(8, 'A+', 'Antibiotics', 'Bronchitis', 6),
(9, 'O+', NULL, NULL, 7),
(10, 'AB+', NULL, 'Migraines', 8);

INSERT INTO PilgrimAllergy VALUES 
(2, 'Peanuts'),
(4, 'Penicillin'),
(5, 'Lactose');

INSERT INTO Accommodation VALUES 
(1, 'Hilton Makkah', 'Ajyad Street', 'Makkah', '24242', 300),
(2, 'Pullman ZamZam', 'Haram Area', 'Makkah', '24243', 250),
(3, 'Al Shohada Hotel', 'Ibrahim Al Khalil St', 'Makkah', '24244', 200),
(4, 'Swissotel', 'Abraj Al Bait', 'Makkah', '24245', 280),
(5, 'Makkah Towers', 'Ajyad District', 'Makkah', '24246', 230),
(6, 'Raffles Makkah', 'King Abdul Aziz Road', 'Makkah', '24247', 220),
(7, 'Hyatt Regency', 'Jabal Omar', 'Makkah', '24248', 210),
(8, 'Fairmont Makkah', 'Clock Tower', 'Makkah', '24249', 260),
(9, 'Marriott Makkah', 'Al Aziziyah', 'Makkah', '24250', 240),
(10, 'Le Meridien', 'Al Hijrah Rd', 'Makkah', '24251', 190);

INSERT INTO PilgrimAccommodation VALUES 
(1, 1, 'Double'),
(2, 1, 'Double'),
(3, 2, 'Single'),
(4, 3, 'Suite'),
(5, 4, 'Double'),
(6, 5, 'Single'),
(7, 6, 'Double'),
(8, 7, 'Suite'),
(9, 8, 'Single'),
(10, 9, 'Double');

INSERT INTO TransportSchedule VALUES 
(1, '10:00:00', '12:00:00', 'Bus', 'Jeddah - Makkah'),
(2, '13:00:00', '15:30:00', 'Train', 'Makkah - Mina'),
(3, '16:00:00', '17:00:00', 'Car', 'Mina - Arafat'),
(4, '08:00:00', '09:30:00', 'Bus', 'Arafat - Muzdalifah'),
(5, '11:00:00', '13:00:00', 'Van', 'Muzdalifah - Mina'),
(6, '09:00:00', '10:00:00', 'Bus', 'Mina - Jamarat'),
(7, '14:00:00', '16:00:00', 'Train', 'Mina - Makkah'),
(8, '06:00:00', '08:00:00', 'Helicopter', 'Emergency Route'),
(9, '17:00:00', '19:00:00', 'Bus', 'Makkah - Jeddah'),
(10, '12:00:00', '14:00:00', 'Taxi', 'Hotel - Haram');

INSERT INTO PilgrimTransport VALUES 
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10);

INSERT INTO Feedback VALUES 
(1, 1, 'Great service!', 5, '2025-01-01'),
(2, 2, 'Very satisfied', 4, '2025-01-02'),
(3, 3, 'Room was clean', 4, '2025-01-03'),
(4, 4, 'Transport delay', 3, '2025-01-04'),
(5, 5, 'Excellent staff', 5, '2025-01-05'),
(6, 6, 'Good experience', 4, '2025-01-06'),
(7, 7, 'Needs improvement', 2, '2025-01-07'),
(8, 8, 'Very comfortable', 5, '2025-01-08'),
(9, 9, 'No issues', 4, '2025-01-09'),
(10, 10, 'Best journey', 5, '2025-01-10');

UPDATE Accommodation SET Street = 'New Ajyad Street' WHERE AccommodationID = 1;
DELETE FROM Feedback WHERE FeedbackID = 7;
INSERT INTO PilgrimAccommodation VALUES (2, 3, 'Suite');
UPDATE Permit SET ServiceType = 'Updated Religious' WHERE PermitID = 1;
DELETE FROM PilgrimTransport WHERE PilgrimID = 5;

SELECT PilgrimName, PilgrimAge FROM Pilgrim;
SELECT HotelName, Capacity FROM Accommodation;
SELECT PilgrimName, SpecialNeed FROM Pilgrim WHERE SpecialNeed IS NOT NULL;
SELECT PilgrimName, Rating FROM Feedback JOIN Pilgrim USING (PilgrimID);
SELECT * FROM TransportSchedule;
SELECT * FROM Pilgrim WHERE PilgrimName LIKE 'A%';
SELECT * FROM Pilgrim WHERE PilgrimAge BETWEEN 30 AND 50;
SELECT * FROM Pilgrim WHERE PilgrimID IN (1, 3, 5);
SELECT PilgrimName FROM Pilgrim ORDER BY PilgrimAge DESC;
SELECT PilgrimName FROM Pilgrim WHERE SpecialNeed IS NULL;

(SELECT PilgrimName FROM Pilgrim) UNION (SELECT HotelName FROM Accommodation);
SELECT TransportType, COUNT(*) AS Total FROM TransportSchedule GROUP BY TransportType HAVING Total > 2;
SELECT * FROM Pilgrim NATURAL JOIN MedicalProfile;
SELECT A.HotelName, PA.RoomType FROM Accommodation A RIGHT JOIN PilgrimAccommodation PA ON A.AccommodationID = PA.AccommodationID;
SELECT PilgrimName FROM Pilgrim P WHERE EXISTS (SELECT 1 FROM MedicalProfile M WHERE M.PilgrimID = P.PilgrimID AND M.BloodType = 'A+');
SELECT PilgrimName FROM Pilgrim WHERE PilgrimID IN (SELECT PilgrimID FROM MedicalProfile WHERE BloodType = 'O-');
SELECT PilgrimName FROM Pilgrim WHERE PilgrimAge > ALL ( SELECT AVG(PilgrimAge) FROM Pilgrim);


DELIMITER //
CREATE FUNCTION GetPilgrimAge(pid INT) RETURNS INT DETERMINISTIC
BEGIN 
    DECLARE age INT; 
    SELECT PilgrimAge INTO age FROM Pilgrim WHERE PilgrimID = pid; 
    RETURN age; 
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE GetPilgrimDetails(IN pid INT)
BEGIN
    SELECT * FROM Pilgrim WHERE PilgrimID = pid;
END //
DELIMITER ;

CALL GetPilgrimDetails(1);
DELIMITER //
CREATE TRIGGER AfterNewPilgrim AFTER INSERT ON Pilgrim FOR EACH ROW
BEGIN
    INSERT INTO Feedback (FeedbackID, PilgrimID, Content, Rating, Date) 
    VALUES ((SELECT IFNULL(MAX(FeedbackID),0)+1 FROM Feedback), NEW.PilgrimID, 'Auto feedback', 5, CURDATE());
END //
DELIMITER ;

CREATE VIEW PilgrimPermitView AS 
SELECT PilgrimName, PilgrimAge, Name AS PermitName, ServiceType 
FROM Pilgrim JOIN Permit USING (PermitID);

