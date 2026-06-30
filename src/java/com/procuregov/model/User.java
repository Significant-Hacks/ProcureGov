package com.procuregov.model;

import java.io.Serializable;

/**
 * User JavaBean representing a system user.
 * Maps to the users table in the database.
 * Role-specific data is held in separate profile fields.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String email;
    private String passwordHash;
    private String role; // SUPPLIER, OFFICER, EVALUATOR
    private boolean isActive;
    private int failedLoginAttempts;
    private boolean accountLocked;

    // Profile fields (populated from role-specific tables)
    private String displayName;
    private String registrationNumber; // Suppliers only
    private String companyName;        // Suppliers only
    private String physicalAddress;    // Suppliers only
    private String contactNumber;      // Suppliers only
    private boolean isVerified;        // Suppliers only
    private String department;         // Officers & Evaluators
    private String staffId;            // Officers & Evaluators

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public boolean isAccountLocked() { return accountLocked; }
    public void setAccountLocked(boolean accountLocked) { this.accountLocked = accountLocked; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
}
