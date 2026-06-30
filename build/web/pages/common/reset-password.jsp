<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
</head>
<body>
<div class="page-wrapper">
    <jsp:include page="/WEB-INF/fragments/navbar.jsp"/>
    <main class="auth-page">
        <div class="auth-card">
            <h2>Reset Password</h2>
            <p class="auth-subtitle">Enter the 6-digit code from your email and your new password</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success"><c:out value="${success}"/></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/reset-password" method="POST">
                <div class="form-group">
                    <label>Reset Code <span class="required">*</span></label>
                    <input type="text" name="token" class="form-control" maxlength="6" pattern="[0-9]{6}"
                           placeholder="e.g. 123456"
                           value="${not empty token ? token : (not empty param.token ? param.token : '')}" required
                           style="font-size:24px;letter-spacing:6px;text-align:center;font-family:monospace;">
                    <p class="form-hint">Enter the 6-digit code sent to your email</p>
                </div>
                <div class="form-group">
                    <label>New Password <span class="required">*</span></label>
                    <input type="password" name="newPassword" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Confirm New Password <span class="required">*</span></label>
                    <input type="password" name="confirmPassword" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Reset Password</button>
            </form>

            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/forgot-password">Request a New Code</a>
                &nbsp;|&nbsp;
                <a href="${pageContext.request.contextPath}/login">Back to Login</a>
            </div>
        </div>
    </main>
    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
</div>
</body>
</html>
