package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.model.User;
import com.procuregov.util.PasswordUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RegisterServlet handles supplier self-registration.
 * Exam requirements: company name, auto-generated registration number,
 * email, physical address, contact number, password. All fields mandatory.
 * Ministry staff accounts are created via seed script - no self-registration.
 */
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    /**
     * Displays the registration form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
    }

    /**
     * Processes supplier registration form submission via POST.
     * Auto-generates registration number in format SUP-YYYY-NNNN.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String companyName = request.getParameter("companyName");
        String email = request.getParameter("email");
        String physicalAddress = request.getParameter("physicalAddress");
        String contactNumber = request.getParameter("contactNumber");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate all fields are present (all mandatory per exam)
        if (companyName == null || companyName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            physicalAddress == null || physicalAddress.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "All fields are mandatory.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
            return;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
            return;
        }

        // Check if email already exists
        if (userDAO.emailExists(email.trim())) {
            request.setAttribute("error", "An account with this email already exists.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
            return;
        }

        // Verify email was verified via code during registration
        Boolean emailVerified = (Boolean) request.getSession().getAttribute("emailVerified");
        String emailVerifiedAddress = (String) request.getSession().getAttribute("emailVerifiedAddress");
        if (emailVerified == null || !emailVerified) {
            request.setAttribute("error", "Please verify your email address before creating an account.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
            return;
        }
        if (emailVerifiedAddress == null || !emailVerifiedAddress.equalsIgnoreCase(email.trim())) {
            request.setAttribute("error", "The verified email does not match the entered email. Please verify again.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
            return;
        }

        // Auto-generate registration number: SUP-YYYY-NNNN
        String registrationNumber = generateRegistrationNumber();

        // Create User JavaBean and hash password with SHA-256
        User user = new User();
        user.setEmail(email.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setCompanyName(companyName.trim());
        user.setRegistrationNumber(registrationNumber);
        user.setPhysicalAddress(physicalAddress.trim());
        user.setContactNumber(contactNumber.trim());

        // Register supplier (inserts into users + suppliers tables)
        boolean success = userDAO.registerSupplier(user);

        if (success) {
            // Clear email verification session attributes
            request.getSession().removeAttribute("emailVerified");
            request.getSession().removeAttribute("emailVerifiedAddress");
            request.getSession().removeAttribute("emailVerifyCode");
            request.getSession().removeAttribute("emailVerifyTarget");
            request.getSession().removeAttribute("emailVerifyCodeTime");
            request.setAttribute("success", "Registration successful! You can now login with your email and password.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Registration failed. Please try again.");
            preserveFormValues(request, companyName, email, physicalAddress, contactNumber);
            request.getRequestDispatcher("/pages/common/register.jsp").forward(request, response);
        }
    }

    /**
     * Generates a unique registration number in format SUP-YYYY-NNNN.
     * Queries database to ensure uniqueness even after server restart.
     */
    private String generateRegistrationNumber() {
        int year = java.time.LocalDate.now().getYear();
        String prefix = "SUP-" + year + "-";
        int count = userDAO.countSuppliersByRegNumberPrefix(prefix);
        int next = count + 1;
        return String.format("SUP-%d-%04d", year, next);
    }

    /**
     * Preserves form values so the user doesn't have to retype after validation errors.
     */
    private void preserveFormValues(HttpServletRequest request, String companyName, String email, String physicalAddress, String contactNumber) {
        request.setAttribute("companyName", companyName);
        request.setAttribute("email", email);
        request.setAttribute("physicalAddress", physicalAddress);
        request.setAttribute("contactNumber", contactNumber);
    }
}
