/**
 * The HajjGuide application provides a comprehensive system for managing pilgrim information
 * during the Hajj season. It includes interfaces for both pilgrims and administrators,
 * with features for personal information, medical records, accommodation, transportation,
 * and permits.
 */


package hajjguide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 * The DBConnection class manages the database connection for the Hajj Guide application.
 * It provides methods to establish and close a connection to the MySQL database.
 * This class implements the Singleton pattern to ensure only one database connection
 * is active throughout the application lifecycle.
 */
class DBConnection {
    /** The JDBC URL for connecting to the MySQL database */
    private static final String DB_URL = "jdbc:mysql://localhost:3306/PilgrimSystem?useSSL=false";
    
    /** The database username */
    private static final String DB_USER = "root";
    
    /** The database password */
    private static final String DB_PASSWORD = "&Ghadeer123&";
    
    /** The static Connection instance (Singleton pattern) */
    private static Connection connection = null;
    
    /**
     * Gets the database connection instance. If no connection exists,
     * it creates a new one using the configured URL, username and password.
     * Implements lazy initialization for the Singleton pattern.
     * 
     * @return The active database Connection object
     * @throws ClassNotFoundException if the JDBC driver class is not found
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return connection;
    }
    
    /**
     * Closes the current database connection if it exists.
     * Sets the connection instance to null to allow garbage collection.
     * This method handles any SQLException that might occur during connection closing.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * The PilgrimDAO class provides data access operations for Pilgrim entities.
 * It handles CRUD (Create, Read, Update, Delete) operations for pilgrims in the database.
 * This class follows the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class PilgrimDAO {
    
    /**
     * Creates a new pilgrim record in the database.
     * 
     * @param pilgrimName The full name of the pilgrim
     * @param pilgrimID The unique ID of the pilgrim
     * @param phone The contact phone number of the pilgrim
     * @param nationality The nationality of the pilgrim
     * @param specialNeed Any special needs or requirements of the pilgrim
     * @param allergies Any allergies the pilgrim may have
     * @param pilgrimAge The age of the pilgrim
     * @return true if the operation was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean createPilgrim(String pilgrimName, int pilgrimID, String phone, 
            String nationality, String specialNeed, String allergies, int pilgrimAge) {
        String sql = "INSERT INTO Pilgrim (PilgrimName, PilgrimID, Phone, Nationality, specialNeed, allergies, pilgrimAge) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, pilgrimName);
            stmt.setInt(2, pilgrimID);
            stmt.setString(3, phone);
            stmt.setString(4, nationality);
            stmt.setString(5, specialNeed);
            stmt.setString(6, allergies);
            stmt.setInt(7, pilgrimAge);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating pilgrim: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves all pilgrims from the database.
     * 
     * @return A List of String arrays where each array represents a pilgrim record.
     *         Each array contains: [PilgrimID, PilgrimName, Phone, Nationality, specialNeed, allergies, pilgrimAge]
     * @throws SQLException if a database access error occurs
     */
    public static List<String[]> getAllPilgrims() {
        List<String[]> pilgrims = new ArrayList<>();
        String sql = "SELECT PilgrimID, PilgrimName, Phone, Nationality, specialNeed, allergies, pilgrimAge FROM Pilgrim";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String[] pilgrim = new String[7];
                pilgrim[0] = String.valueOf(rs.getInt("PilgrimID"));
                pilgrim[1] = rs.getString("PilgrimName");
                pilgrim[2] = rs.getString("Phone");
                pilgrim[3] = rs.getString("Nationality");
                pilgrim[4] = rs.getString("specialNeed");
                pilgrim[5] = rs.getString("allergies");
                pilgrim[6] = String.valueOf(rs.getInt("pilgrimAge"));
                pilgrims.add(pilgrim);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving pilgrims: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return pilgrims;
    }
    
    /**
     * Updates an existing pilgrim record in the database.
     * 
     * @param pilgrimID The ID of the pilgrim to update
     * @param pilgrimName The updated name of the pilgrim
     * @param phone The updated phone number of the pilgrim
     * @param nationality The updated nationality of the pilgrim
     * @param specialNeed The updated special needs of the pilgrim
     * @param allergies The updated allergies of the pilgrim
     * @param pilgrimAge The updated age of the pilgrim
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean updatePilgrim(int pilgrimID, String pilgrimName, String phone, 
            String nationality, String specialNeed, String allergies, int pilgrimAge) {
        String sql = "UPDATE Pilgrim SET PilgrimName = ?, Phone = ?, Nationality = ?, specialNeed = ?, allergies = ?, pilgrimAge = ? "
                   + "WHERE PilgrimID = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, pilgrimName);
            stmt.setString(2, phone);
            stmt.setString(3, nationality);
            stmt.setString(4, specialNeed);
            stmt.setString(5, allergies);
            stmt.setInt(6, pilgrimAge);
            stmt.setInt(7, pilgrimID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating pilgrim: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Deletes a pilgrim record from the database.
     * 
     * @param pilgrimID The ID of the pilgrim to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean deletePilgrim(int pilgrimID) {
        String sql = "DELETE FROM Pilgrim WHERE PilgrimID = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, pilgrimID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting pilgrim: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves a specific pilgrim by their ID.
     * 
     * @param pilgrimID The ID of the pilgrim to retrieve
     * @return A String array containing the pilgrim's information:
     *         [PilgrimID, PilgrimName, Phone, Nationality, specialNeed, allergies, pilgrimAge]
     *         Returns an array with null elements if pilgrim not found
     * @throws SQLException if a database access error occurs
     */
    public static String[] getPilgrimById(int pilgrimID) {
        String sql = "SELECT PilgrimID, PilgrimName, Phone, Nationality, specialNeed, allergies, pilgrimAge FROM Pilgrim WHERE PilgrimID = ?";
        String[] pilgrim = new String[7];
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pilgrimID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                pilgrim[0] = String.valueOf(rs.getInt("PilgrimID"));
                pilgrim[1] = rs.getString("PilgrimName");
                pilgrim[2] = rs.getString("Phone");
                pilgrim[3] = rs.getString("Nationality");
                pilgrim[4] = rs.getString("specialNeed");
                pilgrim[5] = rs.getString("allergies");
                pilgrim[6] = String.valueOf(rs.getInt("pilgrimAge"));
            }
            
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving pilgrim: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return pilgrim;
    }
}

/**
 * The MedicalProfileDAO class provides data access operations for medical profile records.
 * It handles CRUD operations for medical profiles associated with pilgrims in the database.
 * This class follows the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class MedicalProfileDAO {
    
    /**
     * Creates a new medical profile record in the database.
     * 
     * @param profileID The unique identifier for the medical profile
     * @param bloodType The blood type of the pilgrim (e.g., "A+", "O-")
     * @param medications Current medications the pilgrim is taking
     * @param medicalHistory Relevant medical history of the pilgrim
     * @param pilgrimID The ID of the pilgrim this profile belongs to
     * @param adminID The ID of the administrator creating this profile
     * @return true if the profile was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean createMedicalProfile(int profileID, String bloodType, String medications, 
            String medicalHistory, int pilgrimID, int adminID) {
        String sql = "INSERT INTO MedicalProfile (ProfileID, bloodType, medications, Medical_History, PilgrimID, AdminID) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, profileID);
            stmt.setString(2, bloodType);
            stmt.setString(3, medications);
            stmt.setString(4, medicalHistory);
            stmt.setInt(5, pilgrimID);
            stmt.setInt(6, adminID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating medical profile: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves a medical profile for a specific pilgrim.
     * 
     * @param pilgrimID The ID of the pilgrim whose medical profile to retrieve
     * @return A String array containing the medical profile information:
     *         [ProfileID, bloodType, medications, Medical_History, PilgrimID, AdminID]
     *         Returns an array with null elements if no profile is found
     * @throws SQLException if a database access error occurs
     */
    public static String[] getMedicalProfileByPilgrimId(int pilgrimID) {
        String sql = "SELECT ProfileID, bloodType, medications, Medical_History, PilgrimID, AdminID FROM MedicalProfile WHERE PilgrimID = ?";
        String[] profile = new String[6];
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pilgrimID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                profile[0] = String.valueOf(rs.getInt("ProfileID"));
                profile[1] = rs.getString("bloodType");
                profile[2] = rs.getString("medications");
                profile[3] = rs.getString("Medical_History");
                profile[4] = String.valueOf(rs.getInt("PilgrimID"));
                profile[5] = String.valueOf(rs.getInt("AdminID"));
            }
            
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving medical profile: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return profile;
    }
    
    /**
     * Updates an existing medical profile in the database.
     * 
     * @param profileID The ID of the medical profile to update
     * @param bloodType The updated blood type information
     * @param medications The updated medications information
     * @param medicalHistory The updated medical history
     * @param adminID The ID of the administrator making the update
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean updateMedicalProfile(int profileID, String bloodType, String medications, 
            String medicalHistory, int adminID) {
        String sql = "UPDATE MedicalProfile SET bloodType = ?, medications = ?, Medical_History = ?, AdminID = ? "
                   + "WHERE ProfileID = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, bloodType);
            stmt.setString(2, medications);
            stmt.setString(3, medicalHistory);
            stmt.setInt(4, adminID);
            stmt.setInt(5, profileID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating medical profile: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}




/**
 * The TransportScheduleDAO class provides data access operations for transport schedules.
 * It handles the creation, retrieval, and assignment of transport schedules for pilgrims.
 * This class implements the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class TransportScheduleDAO {
    
    /**
     * Creates a new transport schedule in the database.
     * 
     * @param scheduleID The unique identifier for the transport schedule
     * @param departureTime The scheduled departure time (format: "HH:MM" or "YYYY-MM-DD HH:MM:SS")
     * @param arrivalTime The scheduled arrival time (format: "HH:MM" or "YYYY-MM-DD HH:MM:SS")
     * @param route The route description (e.g., "Mina to Arafat")
     * @param transportType The type of transport (e.g., "Bus", "Train")
     * @param adminID The ID of the administrator creating this schedule
     * @return true if the schedule was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean createTransportSchedule(int scheduleID, String departureTime, 
            String arrivalTime, String route, String transportType, int adminID) {
        String sql = "INSERT INTO TransportSchedule (ScheduleID, departureTime, arrivalTime, route, TransportType, AdminID) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, scheduleID);
            stmt.setString(2, departureTime);
            stmt.setString(3, arrivalTime);
            stmt.setString(4, route);
            stmt.setString(5, transportType);
            stmt.setInt(6, adminID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating transport schedule: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves all transport schedules from the database.
     * 
     * @return A List of String arrays where each array represents a transport schedule:
     *         [ScheduleID, departureTime, arrivalTime, route, TransportType, AdminID]
     *         Returns an empty list if no schedules exist
     * @throws SQLException if a database access error occurs
     */
    public static List<String[]> getAllTransportSchedules() {
        List<String[]> schedules = new ArrayList<>();
        String sql = "SELECT ScheduleID, departureTime, arrivalTime, route, TransportType, AdminID FROM TransportSchedule";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String[] schedule = new String[6];
                schedule[0] = String.valueOf(rs.getInt("ScheduleID"));
                schedule[1] = rs.getString("departureTime");
                schedule[2] = rs.getString("arrivalTime");
                schedule[3] = rs.getString("route");
                schedule[4] = rs.getString("TransportType");
                schedule[5] = String.valueOf(rs.getInt("AdminID"));
                schedules.add(schedule);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving transport schedules: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return schedules;
    }
    
    /**
     * Assigns a pilgrim to a specific transport schedule.
     * 
     * @param pilgrimID The ID of the pilgrim to assign
     * @param scheduleID The ID of the transport schedule
     * @return true if the assignment was successful, false otherwise
     * @throws SQLException if a database access error occurs or if the assignment violates constraints
     */
    public static boolean assignPilgrimToTransport(int pilgrimID, int scheduleID) {
        String sql = "INSERT INTO PilgrimTransport (PilgrimID, ScheduleID) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, pilgrimID);
            stmt.setInt(2, scheduleID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error assigning pilgrim to transport: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * The AccommodationDAO class provides data access operations for pilgrim accommodation records.
 * It handles the creation, retrieval, and assignment of accommodations for pilgrims during Hajj.
 * This class implements the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class AccommodationDAO {
    
    /**
     * Creates a new accommodation record in the database.
     * 
     * @param accommodationID The unique identifier for the accommodation
     * @param hotelName The name of the hotel or accommodation facility
     * @param roomType The type of room (e.g., "Single", "Double", "Suite")
     * @param capacity The maximum number of pilgrims the accommodation can hold
     * @param address The physical address of the accommodation
     * @param adminID The ID of the administrator creating this record
     * @return true if the accommodation was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean createAccommodation(int accommodationID, String hotelName, 
            String roomType, int capacity, String address, int adminID) {
        String sql = "INSERT INTO Accommodation (AccommodationID, HotelName, roomType, capacity, address, AdminID) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, accommodationID);
            stmt.setString(2, hotelName);
            stmt.setString(3, roomType);
            stmt.setInt(4, capacity);
            stmt.setString(5, address);
            stmt.setInt(6, adminID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating accommodation: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves all accommodation records from the database.
     * 
     * @return A List of String arrays where each array represents an accommodation:
     *         [AccommodationID, HotelName, roomType, capacity, address, AdminID]
     *         Returns an empty list if no accommodations exist
     * @throws SQLException if a database access error occurs
     */
    public static List<String[]> getAllAccommodations() {
        List<String[]> accommodations = new ArrayList<>();
        String sql = "SELECT AccommodationID, HotelName, roomType, capacity, address, AdminID FROM Accommodation";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String[] accommodation = new String[6];
                accommodation[0] = String.valueOf(rs.getInt("AccommodationID"));
                accommodation[1] = rs.getString("HotelName");
                accommodation[2] = rs.getString("roomType");
                accommodation[3] = String.valueOf(rs.getInt("capacity"));
                accommodation[4] = rs.getString("address");
                accommodation[5] = String.valueOf(rs.getInt("AdminID"));
                accommodations.add(accommodation);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving accommodations: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return accommodations;
    }
    
    /**
     * Assigns a pilgrim to a specific accommodation.
     * 
     * @param pilgrimID The ID of the pilgrim to assign
     * @param accommodationID The ID of the accommodation
     * @return true if the assignment was successful, false otherwise
     * @throws SQLException if a database access error occurs or if the assignment violates constraints
     */
    public static boolean assignPilgrimToAccommodation(int pilgrimID, int accommodationID) {
        String sql = "INSERT INTO PilgrimAccommodation (PilgrimID, AccommodationID) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, pilgrimID);
            stmt.setInt(2, accommodationID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error assigning pilgrim to accommodation: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * The PermitDAO class provides data access operations for Hajj permit records.
 * It handles the creation, retrieval, and assignment of permits to pilgrims.
 * This class implements the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class PermitDAO {
    
    /**
     * Creates a new permit record in the database.
     * 
     * @param permitID The unique identifier for the permit
     * @param name The name/description of the permit (e.g., "Arafat Access Permit")
     * @param location The location this permit grants access to
     * @param serviceType The type of service the permit provides (e.g., "Transport", "Food")
     * @return true if the permit was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean createPermit(int permitID, String name, String location, String serviceType) {
        String sql = "INSERT INTO Permit (PermitID, Name, location, serviceType) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, permitID);
            stmt.setString(2, name);
            stmt.setString(3, location);
            stmt.setString(4, serviceType);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating permit: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves all permit records from the database.
     * 
     * @return A List of String arrays where each array represents a permit:
     *         [PermitID, Name, location, serviceType]
     *         Returns an empty list if no permits exist
     * @throws SQLException if a database access error occurs
     */
    public static List<String[]> getAllPermits() {
        List<String[]> permits = new ArrayList<>();
        String sql = "SELECT PermitID, Name, location, serviceType FROM Permit";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String[] permit = new String[4];
                permit[0] = String.valueOf(rs.getInt("PermitID"));
                permit[1] = rs.getString("Name");
                permit[2] = rs.getString("location");
                permit[3] = rs.getString("serviceType");
                permits.add(permit);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving permits: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return permits;
    }
    
    /**
     * Assigns a permit to a specific pilgrim.
     * 
     * @param pilgrimID The ID of the pilgrim to assign the permit to
     * @param permitID The ID of the permit to assign
     * @return true if the assignment was successful, false otherwise
     * @throws SQLException if a database access error occurs or if the assignment violates constraints
     */
    public static boolean assignPermitToPilgrim(int pilgrimID, int permitID) {
        String sql = "INSERT INTO PilgrimPermit (PilgrimID, PermitID) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, pilgrimID);
            stmt.setInt(2, permitID);
            
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error assigning permit to pilgrim: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * The AdminDAO class provides data access operations for administrator accounts.
 * It handles authentication and retrieval of administrator information.
 * This class implements the Data Access Object (DAO) pattern to separate database operations
 * from business logic.
 */
class AdminDAO {
    
    /**
     * Validates administrator credentials against the database.
     * 
     * @param adminID The unique identifier of the administrator
     * @param password The password to validate (in plaintext)
     * @return true if the credentials are valid, false otherwise
     * @throws SQLException if a database access error occurs
     * 
     * @implNote This method performs a direct password comparison. In production environments,
     *           consider using password hashing and salting for security.
     */
    public static boolean validateAdmin(int adminID, String password) {
        String sql = "SELECT * FROM Admin WHERE AdminID = ? AND Password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminID);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            boolean isValid = rs.next();
            rs.close();
            return isValid;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error validating admin: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Retrieves administrator information by ID.
     * 
     * @param adminID The unique identifier of the administrator
     * @return A String array containing administrator details:
     *         [AdminID, AdminName, phone, Email]
     *         Returns an array with null elements if admin not found
     * @throws SQLException if a database access error occurs
     * 
     * @see #validateAdmin For authentication before retrieving details
     */
    public static String[] getAdminById(int adminID) {
        String sql = "SELECT AdminID, AdminName, phone, Email FROM Admin WHERE AdminID = ?";
        String[] admin = new String[4];
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                admin[0] = String.valueOf(rs.getInt("AdminID"));
                admin[1] = rs.getString("AdminName");
                admin[2] = rs.getString("phone");
                admin[3] = rs.getString("Email");
            }
            
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving admin: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return admin;
    }
}



/**
 * The main class for the Hajj Guide application that serves as the entry point
 * and provides the main menu interface.
 */
public class HajjGuide {
    JFrame frame;
    
    /**
     * Constructs the main HajjGuide application window with a menu interface.
     */
    public HajjGuide() {
        frame = new JFrame("Hajj Guide - Main Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(680, 650);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout()); // مركزية

        frame.getContentPane().setBackground(new Color(230, 240, 255)); // خلفية خفيفة

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(480, 400));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setOpaque(true);
        card.setFocusable(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // ===== Title =====
        JLabel titleLabel = new JLabel("Hajj Guide", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titleLabel.setForeground(new Color(0, 102, 153));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== Slogan =====
        JLabel sloganLabel = new JLabel("Your Journey with Faith and Ease", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sloganLabel.setForeground(new Color(100, 100, 100));
        sloganLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== Buttons =====
        JButton pilgrimButton = new JButton("PILGRIM");
        JButton adminButton = new JButton("ADMIN");

        styleFancyButton(pilgrimButton, new Color(0, 153, 76));
        styleFancyButton(adminButton, new Color(30, 144, 255));

        pilgrimButton.addActionListener(e -> openPilgrimLogin());
        adminButton.addActionListener(e -> openAdminLogin());

        // ===== Add Components to Card =====
        card.add(titleLabel);
        card.add(sloganLabel);
        card.add(pilgrimButton);
        card.add(Box.createVerticalStrut(20)); // فراغ بسيط
        card.add(adminButton);

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Styles a button with consistent visual properties.
     * @param button The button to style
     * @param bgColor The background color for the button
     */
    private void styleFancyButton(JButton button, Color bgColor) {
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    /**
     * Opens the pilgrim login interface.
     */
    private void openPilgrimLogin() {
        frame.dispose();
        new PilgrimLogin(); // تأكد من وجودها
    }

    /**
     * Opens the admin login interface.
     */
    private void openAdminLogin() {
        frame.dispose();
        new AdminLogin(); // تأكد من وجودها
    }

    /**
     * The main entry point for the HajjGuide application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        new HajjGuide();
    }
}

/**
 * Provides the login interface for pilgrims.
 */
class PilgrimLogin {
    JFrame frame;
    JTextField usernameField;
    JPasswordField passwordField;

    /**
     * Constructs the pilgrim login interface.
     */
    public PilgrimLogin() {
        frame = new JFrame("Pilgrim Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(680, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(450, 300));

        JLabel title = new JLabel("Pilgrim Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 153));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        card.add(title);

        // ===== Form Panel (Labels on Left, Fields on Right) =====
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        styleInputField(usernameField);
        styleInputField(passwordField);

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);

        card.add(formPanel);
        card.add(Box.createVerticalStrut(30));

        // ===== Buttons =====
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        styleButton(loginButton, new Color(0, 153, 76));
        styleButton(registerButton, new Color(30, 144, 255));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginButton);
        btnPanel.add(registerButton);
        card.add(btnPanel);

        // ===== Button Actions =====
        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            try {
                int pilgrimID = Integer.parseInt(user);
                String[] pilgrim = PilgrimDAO.getPilgrimById(pilgrimID);
        
                if (pilgrim[0] != null) { // Pilgrim exist
                    // In a real app, you would verify the password against a stored hash
                    frame.dispose();
                    new PilgrimDashboard(pilgrimID);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid ID format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            frame.dispose();
            new PilgrimRegister(); // تأكد أنه موجود
        });

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Styles an input field with consistent visual properties.
     * @param field The text field to style
     */
    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 40));
    }

    /**
     * Styles a button with consistent visual properties.
     * @param button The button to style
     * @param bg The background color for the button
     */
    private void styleButton(JButton button, Color bg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(120, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
    }
}

/**
 * Provides the registration interface for new pilgrims.
 */
class PilgrimRegister {
    JFrame frame;
    JTextField nameField, idField, phoneField, nationalityField, emailField;
    JPasswordField passwordField, confirmPasswordField;
    private DBConnection dbManager;

    /**
     * Constructs the pilgrim registration interface.
     */
    public PilgrimRegister() {
        dbManager = new DBConnection();
        frame = new JFrame("Pilgrim Registration");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(240, 248, 255));
        frame.setLayout(new BorderLayout());

        // ===== Title =====
        JLabel title = new JLabel("✦ Pilgrim Registration ✦", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 204));
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        frame.add(title, BorderLayout.NORTH);

        // ===== Form Panel =====
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 15, 15));
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 60));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);

        nameField = createModernField();
        idField = createModernField();
        phoneField = createModernField();
        nationalityField = createModernField();
        emailField = createModernField();
        passwordField = createModernPasswordField();
        confirmPasswordField = createModernPasswordField();

        formPanel.add(createLabel("Full Name:", labelFont));
        formPanel.add(nameField);
        formPanel.add(createLabel("ID Number:", labelFont));
        formPanel.add(idField);
        formPanel.add(createLabel("Phone Number:", labelFont));
        formPanel.add(phoneField);
        formPanel.add(createLabel("Nationality:", labelFont));
        formPanel.add(nationalityField);
        formPanel.add(createLabel("Email:", labelFont));
        formPanel.add(emailField);
        formPanel.add(createLabel("Password:", labelFont));
        formPanel.add(passwordField);
        formPanel.add(createLabel("Confirm Password:", labelFont));
        formPanel.add(confirmPasswordField);

        frame.add(formPanel, BorderLayout.CENTER);

        // ===== Buttons =====
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back");
        JButton exitBtn = new JButton("Exit");

        styleButton(registerBtn, new Color(0, 153, 76), Color.WHITE);
        styleButton(backBtn, new Color(30, 144, 255), Color.WHITE);
        styleButton(exitBtn, new Color(255, 77, 77), Color.WHITE);

        registerBtn.addActionListener(e -> {
    String pass = new String(passwordField.getPassword());
    String confirm = new String(confirmPasswordField.getPassword());
    
    if (!pass.equals(confirm)) {
        JOptionPane.showMessageDialog(frame, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        try {
            int pilgrimID = Integer.parseInt(idField.getText());
            String pilgrimName = nameField.getText();
            String phone = phoneField.getText();
            String nationality = nationalityField.getText();
            
            // Call with correct parameters
            boolean success = PilgrimDAO.createPilgrim(
                pilgrimName, 
                pilgrimID, 
                phone, 
                nationality, 
                "", // specialNeed (empty for now)
                "", // allergies (empty for now)
                0   // age (0 for now)
            );
            
            if (success) {
                JOptionPane.showMessageDialog(frame, "Registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                new PilgrimDashboard(pilgrimID);
            } else {
                JOptionPane.showMessageDialog(frame, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid ID format!", "Error", JOptionPane.ERROR_MESSAGE);
        }    
    }
});
        

        backBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        exitBtn.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.add(registerBtn);
        buttonPanel.add(backBtn);
        buttonPanel.add(exitBtn);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /**
     * Creates a label with consistent styling.
     * @param text The label text
     * @param font The font to use for the label
     * @return The created JLabel
     */
    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    /**
     * Creates a styled text input field.
     * @return The created JTextField
     */
    private JTextField createModernField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(50, 50, 50));
        field.setBackground(Color.WHITE);
        field.setCaretColor(new Color(30, 144, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 160, 160), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return field;
    }

    /**
     * Creates a styled password input field.
     * @return The created JPasswordField
     */
    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(50, 50, 50));
        field.setBackground(Color.WHITE);
        field.setCaretColor(new Color(30, 144, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 160, 160), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return field;
    }

    /**
     * Styles a button with consistent visual properties.
     * @param button The button to style
     * @param bg The background color
     * @param fg The foreground (text) color
     */
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
    }
}

/**
 * Provides the main dashboard interface for pilgrims after login.
 */
class PilgrimDashboard {
    JFrame frame;
    private int pilgrimID;

    /**
     * Constructs the pilgrim dashboard interface.
     */
    public PilgrimDashboard(int pilgrimID) {
        this.pilgrimID = pilgrimID;
        
        frame = new JFrame("🕋 Pilgrim Dashboard");
        frame.setSize(680, 650); // Same size as Main
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Disable resizing
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(245, 250, 255));
        frame.setLayout(new BorderLayout());

        // ===== Title Label =====
        JLabel title = new JLabel("🧳 Pilgrim Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 153));
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));
        frame.add(title, BorderLayout.NORTH);

        // ===== Buttons Panel =====
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        buttonPanel.setBackground(new Color(245, 250, 255));

        // ===== Buttons =====
        JButton personalBtn = createButton("🙍‍♂️ Personal Information", new Color(0, 153, 76));
        JButton medicalBtn = createButton("🩺 Medical File", new Color(0, 153, 76));
        JButton accommodationBtn = createButton("🏨 Accommodation", new Color(0, 153, 76));
        JButton transportBtn = createButton("🚌 Transport", new Color(0, 153, 76));
        JButton permitBtn = createButton("📄 Permit", new Color(0, 153, 76));
        JButton homeBtn = createButton("🏠 Go to Home Page", new Color(30, 144, 255));
        JButton exitBtn = createButton("❌ Exit", new Color(204, 0, 0));

        // ===== Actions =====
        personalBtn.addActionListener(e -> new PilgrimPersonalInfo(pilgrimID));
        medicalBtn.addActionListener(e -> new PilgrimMedicalFile());
        accommodationBtn.addActionListener(e -> new PilgrimAccommodation());
        transportBtn.addActionListener(e -> new PilgrimTransport());
        permitBtn.addActionListener(e -> new PilgrimPermit());

        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        exitBtn.addActionListener(e -> frame.dispose());

        // ===== Add Buttons to Panel =====
        buttonPanel.add(personalBtn);
        buttonPanel.add(medicalBtn);
        buttonPanel.add(accommodationBtn);
        buttonPanel.add(transportBtn);
        buttonPanel.add(permitBtn);
        buttonPanel.add(homeBtn);
        buttonPanel.add(exitBtn);

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}

/**
 * Provides the personal information viewing interface for pilgrims.
 */
class PilgrimPersonalInfo {
    JFrame frame;

    /**
     * Constructs the personal information interface.
     */
    public PilgrimPersonalInfo(int pilgrimID) {
        frame = new JFrame("🧕 Personal Information");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255)); // Soft background

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new GridLayout(6, 2, 12, 12));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 360));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(35, 35, 35, 35)
        ));

        String[] pilgrim = PilgrimDAO.getPilgrimById(pilgrimID);
        
        // ===== Input Fields =====
        
        if (pilgrim[0] != null) {
        card.add(createLabel("👤 Full Name:"));
        card.add(createStyledTextField(pilgrim[1]));

        card.add(createLabel("🆔 ID Number:"));
        card.add(createStyledTextField(pilgrim[0]));

        card.add(createLabel("📱 Phone Number:"));
        card.add(createStyledTextField("0501234567"));

        card.add(createLabel("🌍 Nationality:"));
        card.add(createStyledTextField("Saudi"));

        card.add(createLabel("✉️ Email:"));
        card.add(createStyledTextField("john@example.com"));
        }

        // ===== Buttons Panel =====
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton homeBtn = createEmojiButton("🏠 Go to Home Page", new Color(30, 144, 255));
        JButton exitBtn = createEmojiButton("❌ Exit", new Color(220, 53, 69));

        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        exitBtn.addActionListener(e -> frame.dispose());

        buttonPanel.add(homeBtn);
        buttonPanel.add(exitBtn);

        // ===== Layout Assembly =====
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        frame.add(card, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(25, 0, 0, 0);
        frame.add(buttonPanel, gbc);

        frame.setVisible(true);
    }

    /**
     * Creates a label with consistent styling.
     * @param text The label text
     * @return The created JLabel
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
        return label;
    }

    /**
     * Creates a styled text field with a default value.
     * @param value The default text value
     * @return The created JTextField
     */
    private JTextField createStyledTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Creates a styled button with an emoji.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createEmojiButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}

/**
 * Provides the medical file viewing interface for pilgrims.
 */
class PilgrimMedicalFile {
    JFrame frame;
    private int pilgrimID;
    
    


    /**
     * Constructs the medical file interface.
     */
    public PilgrimMedicalFile() {
        frame = new JFrame("🩺 Medical File");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255)); // soft background

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new GridLayout(5, 2, 12, 12));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 300));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(35, 35, 35, 35)
        ));

        String[] medicalProfile = MedicalProfileDAO.getMedicalProfileByPilgrimId(pilgrimID);
        
        // ===== Input Fields =====
        if (medicalProfile[0] != null) {
        card.add(createLabel("🩸 Blood Type:"));
        card.add(createStyledTextField(medicalProfile[1]));

        card.add(createLabel("🌾 Allergies:"));
        card.add(createStyledTextField("None"));

        card.add(createLabel("🧬 Medical Conditions:"));
        card.add(createStyledTextField("None"));

        card.add(createLabel("👨‍👩‍👦 Emergency Contact Name:"));
        card.add(createStyledTextField("Ahmed"));

        card.add(createLabel("📞 Emergency Contact Phone:"));
        card.add(createStyledTextField("0509876543"));
        }

        // ===== Buttons Panel =====
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton homeBtn = createEmojiButton("🏠 Go to Home Page", new Color(30, 144, 255));
        JButton exitBtn = createEmojiButton("❌ Exit", new Color(220, 53, 69));

        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        exitBtn.addActionListener(e -> frame.dispose());

        buttonPanel.add(homeBtn);
        buttonPanel.add(exitBtn);

        // ===== Layout Assembly =====
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        frame.add(card, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(25, 0, 0, 0);
        frame.add(buttonPanel, gbc);

        frame.setVisible(true);
    }

    /**
     * Creates a label with consistent styling.
     * @param text The label text
     * @return The created JLabel
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
        return label;
    }

    /**
     * Creates a styled text field with a default value.
     * @param value The default text value
     * @return The created JTextField
     */
    private JTextField createStyledTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Creates a styled button with an emoji.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createEmojiButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}

/**
 * Provides the accommodation management interface for pilgrims.
 */
class PilgrimAccommodation {
    JFrame frame;

    /**
     * Constructs the accommodation interface.
     */
    public PilgrimAccommodation() {
        frame = new JFrame("🏨 Accommodation");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255)); // Soft background

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 260));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // ===== Buttons =====
        JButton bookBtn = createEmojiButton("📦 Book Accommodation", new Color(60, 179, 113));
        JButton viewBtn = createEmojiButton("🔍 View Booking", new Color(100, 149, 237));
        JButton homeBtn = createEmojiButton("🏠 Go to Home Page", new Color(30, 144, 255));
        JButton exitBtn = createEmojiButton("❌ Exit", new Color(220, 53, 69));

        // ===== Button Actions =====
        bookBtn.addActionListener(e -> bookAccommodation());
        viewBtn.addActionListener(e -> viewBooking());
        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });
        exitBtn.addActionListener(e -> frame.dispose());

        // ===== Add Buttons to Card =====
        card.add(bookBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(viewBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(homeBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(exitBtn);

        // ===== Add to Frame =====
        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Displays booking confirmation information.
     */
    private void bookAccommodation() {
        String[][] data = {
            {"🕋 Arafat Tent", "Booked"},
            {"🏨 Hotel", "5-Star Hotel"},
            {"🛏️ Room", "Single"},
            {"🍱 Food Package", "Premium"},
            {"💰 Cost", "10100.0"}
        };
        String[] columns = {"Category", "Details"};

        showTableDialog("✅ Booking Confirmation", data, columns);
    }

    /**
     * Displays existing booking details.
     */
    private void viewBooking() {
        String[][] data = {
            {"🔢 Booking Number", "23546"},
            {"🏨 Hotel", "5-Star Hotel"},
            {"🛏️ Room", "Single"},
            {"🍱 Food Package", "Premium"},
            {"💰 Cost", "10100.0"}
        };
        String[] columns = {"Category", "Details"};

        showTableDialog("📄 Booking Details", data, columns);
    }

    /**
     * Displays a table dialog with specified data.
     * @param title The dialog title
     * @param data The 2D array of data to display
     * @param columns The column headers
     */
    private void showTableDialog(String title, String[][] data, String[] columns) {
        JTable table = new JTable(data, columns);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        table.setEnabled(false);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(450, 160));

        JOptionPane.showMessageDialog(frame, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a styled button with an emoji.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createEmojiButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}


/**
 * Provides the transportation management interface for pilgrims.
 */
class PilgrimTransport {
    JFrame frame;

    /**
     * Constructs the transportation interface.
     */
    public PilgrimTransport() {
        frame = new JFrame("🚌 Transport");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255)); // Soft background

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 450));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // ===== Unified Button Colors =====
        Color mainColor = new Color(0, 153, 76); // Green
        Color homeColor = new Color(30, 144, 255); // Blue
        Color exitColor = new Color(220, 53, 69); // Red

        // ===== Buttons =====
        JButton viewOptionsBtn = createStyledButton("🗺️ View Transportation Options", mainColor);
        JButton bookTicketBtn = createStyledButton("🎫 Book a Ticket", mainColor);
        JButton viewTicketBtn = createStyledButton("🔍 View Your Ticket", mainColor);
        JButton cancelTicketBtn = createStyledButton("❌ Cancel Your Ticket", mainColor);
        JButton homeBtn = createStyledButton("🏠 Go to Home Page", homeColor);
        JButton exitBtn = createStyledButton("🚪 Exit", exitColor);

        // ===== Actions =====
        viewOptionsBtn.addActionListener(e -> viewOptions());
        bookTicketBtn.addActionListener(e -> bookTicket());
        viewTicketBtn.addActionListener(e -> viewTicket());
        cancelTicketBtn.addActionListener(e -> cancelTicket());
        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });
        exitBtn.addActionListener(e -> frame.dispose());

        // ===== Add Buttons =====
        card.add(viewOptionsBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(bookTicketBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(viewTicketBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(cancelTicketBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(homeBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(exitBtn);

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Displays available transportation options.
     */
    private void viewOptions() {
        JOptionPane.showMessageDialog(frame,
                "🚌 Available Bus Locations:\n- 🕋 Jabal Alnoor\n- 🏕️ Mina Camp\n- 🏞️ Arafat Plain\n- 🌌 Muzdalifah");
    }

    /**
     * Handles the ticket booking process.
     */
    private void bookTicket() {
        JOptionPane.showMessageDialog(frame,
                "✅ Ticket booked successfully!\n🎫 Ticket Number: 1000\n🛣️ Route: Mina Camp to Al Haram at 08:00 AM");
    }

    /**
     * Displays ticket information.
     */
    private void viewTicket() {
        String ticket = JOptionPane.showInputDialog(frame, "🔎 Enter your ticket number:");
        if (ticket != null && !ticket.trim().isEmpty()) {
            if (ticket.equals("1000")) {
                String[][] data = {
                        {"🎫 Ticket Number", ticket},
                        {"🛣️ Route", "Jabal Alnoor to Al Haram"},
                        {"🕒 Time", "08:00 AM"}
                };
                String[] columns = {"Category", "Details"};
                showTableDialog("📄 Ticket Details", data, columns);
            } else {
                JOptionPane.showMessageDialog(frame, "⚠️ Ticket number not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the ticket cancellation process.
     */
    private void cancelTicket() {
        String ticket = JOptionPane.showInputDialog(frame, "❌ Enter your ticket number to cancel:");
        if (ticket != null && !ticket.trim().isEmpty()) {
            if (ticket.equals("1000")) {
                String[][] data = {
                        {"🗑️ Ticket Number", ticket},
                        {"🚫 Status", "Canceled"}
                };
                String[] columns = {"Category", "Details"};
                showTableDialog("❌ Ticket Cancellation", data, columns);
            } else {
                JOptionPane.showMessageDialog(frame, "⚠️ Ticket number not found or already canceled!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays a table dialog with specified data.
     * @param title The dialog title
     * @param data The 2D array of data to display
     * @param columns The column headers
     */
    private void showTableDialog(String title, String[][] data, String[] columns) {
        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(30);
        table.setEnabled(false);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, table.getRowHeight() * data.length + 40));

        JOptionPane.showMessageDialog(frame, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}

/**
 * Provides the permit management interface for pilgrims.
 */
 class PilgrimPermit {
    JFrame frame;

    /**
     * Constructs the permit interface.
     */
    public PilgrimPermit() {
        frame = new JFrame("🛂 Hajj Permit");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255)); // Soft background

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 300));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // ===== Buttons =====
        Color primaryColor = new Color(0, 153, 76); // Unified main color (green)
        JButton bookPermitBtn = createStyledButton("📝 Book Permit", primaryColor);
        JButton viewPermitBtn = createStyledButton("🔍 View Permit", primaryColor);
        JButton homeBtn = createStyledButton("🏠 Go to Home Page", new Color(30, 144, 255)); // Blue
        JButton exitBtn = createStyledButton("🚪 Exit", new Color(220, 53, 69)); // Red

        // ===== Button Actions =====
        bookPermitBtn.addActionListener(e -> bookPermit());
        viewPermitBtn.addActionListener(e -> viewPermit());
        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });
        exitBtn.addActionListener(e -> frame.dispose());

        // ===== Add Buttons to Card =====
        card.add(bookPermitBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(viewPermitBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(homeBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(exitBtn);

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Handles the permit booking process.
     */
    private void bookPermit() {
        String[][] data = {
                {"🆔 Permit Number", "HUP-1247654527"},
                {"📌 Type", "International Hajj"},
                {"📅 Issue Date", "2025-12-14"},
                {"📅 Expiry Date", "2025-12-21"}
        };
        String[] columns = {"Category", "Details"};
        showTableDialog("📄 Permit Generated", data, columns);
    }

    /**
     * Displays permit information.
     */
    private void viewPermit() {
        String[][] data = {
                {"🆔 Permit Number", "HUP-1247654527"},
                {"📅 Issue Date", "2025-12-14"},
                {"📅 Expiry Date", "2025-12-21"}
        };
        String[] columns = {"Category", "Details"};
        showTableDialog("🔍 Permit Details", data, columns);
    }

    /**
     * Displays a table dialog with specified data.
     * @param title The dialog title
     * @param data The 2D array of data to display
     * @param columns The column headers
     */
    private void showTableDialog(String title, String[][] data, String[] columns) {
        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(30);
        table.setEnabled(false);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, table.getRowHeight() * data.length + 40));

        JOptionPane.showMessageDialog(frame, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}

/**
 * Provides the login interface for administrators.
 */
class AdminLogin {
    JFrame frame;
    JTextField usernameField;
    JPasswordField passwordField;

    /**
     * Constructs the admin login interface.
     */
    public AdminLogin() {
        frame = new JFrame("Admin Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(680, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(230, 240, 255));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(450, 300));

        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(153, 0, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        card.add(title);

        // ===== Form Panel =====
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        styleInputField(usernameField);
        styleInputField(passwordField);

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);

        card.add(formPanel);
        card.add(Box.createVerticalStrut(30));

        // ===== Buttons =====
        JButton loginBtn = new JButton("Login");
        JButton homeBtn = new JButton("Home Page");

        styleButton(loginBtn, new Color(204, 0, 0));
        styleButton(homeBtn, new Color(30, 144, 255));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(homeBtn);
        card.add(btnPanel);

        // ===== Button Actions =====
        loginBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            
            try {
                int adminID = Integer.parseInt(user);
                boolean isValid = AdminDAO.validateAdmin(adminID, pass);
                
                if (isValid) {
                    frame.dispose();
                    new AdminDashboard();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid ID format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        homeBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Styles an input field with consistent visual properties.
     * @param field The text field to style
     */
    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 40));
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private void styleButton(JButton button, Color bg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(120, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
    }
}

/**
 * Provides the data viewing interface for administrators.
 */
class ViewDashboard {
    JFrame frame;

    /**
     * Constructs the view dashboard interface.
     */
    public ViewDashboard() {
        frame = new JFrame("🔍 View Dashboard");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(8, 1, 15, 15));
        frame.getContentPane().setBackground(new Color(245, 250, 255));

        Color mainColor = new Color(0, 153, 76);
        Color backColor = new Color(30, 144, 255);
        Color exitColor = new Color(204, 0, 0);

        JLabel title = new JLabel("View Pilgrim Data", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JButton pilgrimBtn = createButton("🙍‍♂️ Pilgrims", mainColor);
        JButton medicalBtn = createButton("🩺 Medical Files", mainColor);
        JButton accommodationBtn = createButton("🏨 Accommodations", mainColor);
        JButton transportBtn = createButton("🚌 Transport", mainColor);
        JButton permitBtn = createButton("📄 Permits", mainColor);
        JButton backBtn = createButton("🔙 Back", backColor);
        JButton exitBtn = createButton("❌ Exit", exitColor);

        backBtn.addActionListener(e -> {
            frame.dispose();
            new AdminDashboard();
        });

        exitBtn.addActionListener(e -> System.exit(0));

        frame.add(title);
        frame.add(pilgrimBtn);
        frame.add(medicalBtn);
        frame.add(accommodationBtn);
        frame.add(transportBtn);
        frame.add(permitBtn);
        frame.add(backBtn);
        frame.add(exitBtn);

        frame.setVisible(true);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        return btn;
    }
}

/**
 * Provides the data editing interface for administrators.
 */
class EditDashboard {
    JFrame frame;

    /**
     * Constructs the edit dashboard interface.
     */
    public EditDashboard() {
        frame = new JFrame("✏️ Edit Dashboard");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(8, 1, 15, 15));
        frame.getContentPane().setBackground(new Color(245, 250, 255));

        Color mainColor = new Color(0, 153, 76);
        Color backColor = new Color(30, 144, 255);
        Color exitColor = new Color(204, 0, 0);

        JLabel title = new JLabel("Edit Pilgrim Data", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JButton pilgrimBtn = createButton("🙍‍♂️ Pilgrims", mainColor);
        JButton medicalBtn = createButton("🩺 Medical Files", mainColor);
        JButton accommodationBtn = createButton("🏨 Accommodations", mainColor);
        JButton transportBtn = createButton("🚌 Transport", mainColor);
        JButton permitBtn = createButton("📄 Permits", mainColor);
        JButton backBtn = createButton("🔙 Back", backColor);
        JButton exitBtn = createButton("❌ Exit", exitColor);

        backBtn.addActionListener(e -> {
            frame.dispose();
            new AdminDashboard();
        });

        exitBtn.addActionListener(e -> System.exit(0));

        frame.add(title);
        frame.add(pilgrimBtn);
        frame.add(medicalBtn);
        frame.add(accommodationBtn);
        frame.add(transportBtn);
        frame.add(permitBtn);
        frame.add(backBtn);
        frame.add(exitBtn);

        frame.setVisible(true);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        return btn;
    }
}

/**
 * Provides the main dashboard interface for administrators after login.
 */
class AdminDashboard {
    JFrame frame;

    /**
     * Constructs the admin dashboard interface.
     */
    public AdminDashboard() {
        frame = new JFrame("🧑‍💼 Admin Dashboard");
        frame.setSize(680, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(240, 248, 255));
        frame.setLayout(new GridBagLayout());

        // ===== Card Panel =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(500, 360));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // ===== Title =====
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(153, 0, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(30));

        // ===== Unified Colors =====
        Color mainColor = new Color(0, 153, 76);
        Color backColor = new Color(30, 144, 255);
        Color exitColor = new Color(204, 0, 0);

        // ===== Buttons =====
        JButton viewBtn = createStyledButton("🔍 View Pilgrim Data", mainColor);
        JButton editBtn = createStyledButton("✏️ Edit Pilgrim Data", mainColor);
        JButton backBtn = createStyledButton("🔙 Back to Home", backColor);
        JButton exitBtn = createStyledButton("❌ Exit", exitColor);

        // ===== Actions =====
        viewBtn.addActionListener(e -> {
            frame.dispose();
            new ViewDashboard();
        });

        editBtn.addActionListener(e -> {
            frame.dispose();
            new EditDashboard();
        });

        backBtn.addActionListener(e -> {
            frame.dispose();
            new HajjGuide();
        });

        exitBtn.addActionListener(e -> System.exit(0));

        // ===== Add Buttons =====
        card.add(viewBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(editBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(backBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(exitBtn);

        frame.add(card);
        frame.setVisible(true);
    }

    /**
     * Creates a styled button for the dashboard.
     * @param text The button text
     * @param bgColor The background color
     * @return The created JButton
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}