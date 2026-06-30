<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>My Profile</h2>
            </div>

            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card mb-4">
                <div class="card-header">Account Information</div>
                <table>
                    <tr><th>Field</th><th>Value</th></tr>
                    <tr><td>Email</td><td><c:out value="${user.email}"/></td></tr>
                    <tr><td>Role</td><td><span class="badge badge-${user.role}"><c:out value="${user.role}"/></span></td></tr>
                    <c:choose>
                        <c:when test="${user.role eq 'SUPPLIER'}">
                            <tr><td>Company Name</td><td><c:out value="${user.companyName}"/></td></tr>
                            <tr><td>Registration Number</td><td><c:out value="${user.registrationNumber}"/></td></tr>
                            <tr><td>Physical Address</td><td><c:out value="${user.physicalAddress}"/></td></tr>
                            <tr><td>Contact Number</td><td><c:out value="${user.contactNumber}"/></td></tr>
                            <tr><td>Verified</td><td>${user.verified ? 'Yes' : 'Pending Verification'}</td></tr>
                        </c:when>
                        <c:when test="${user.role eq 'OFFICER' || user.role eq 'EVALUATOR'}">
                            <tr><td>Full Name</td><td><c:out value="${user.displayName}"/></td></tr>
                            <tr><td>Department</td><td><c:out value="${user.department}"/></td></tr>
                            <tr><td>Staff ID</td><td><c:out value="${user.staffId}"/></td></tr>
                        </c:when>
                    </c:choose>
                </table>
            </div>

            <div class="card">
                <div class="card-header">Change Password</div>
                <form action="${pageContext.request.contextPath}/profile" method="POST">
                    <div class="form-group">
                        <label>Current Password</label>
                        <input type="password" name="currentPassword" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>New Password</label>
                        <input type="password" name="newPassword" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Confirm New Password</label>
                        <input type="password" name="confirmPassword" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Update Password</button>
                </form>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
