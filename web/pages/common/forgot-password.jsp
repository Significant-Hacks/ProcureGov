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
            <h2>Forgot Password</h2>
            <p class="auth-subtitle">Enter your email to receive a 6-digit reset code</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success"><c:out value="${success}"/></div>
            </c:if>

            <c:if test="${not empty resetCode}">
                <div class="token-display">
                    <strong>Reset Code (email not configured - fallback):</strong><br>
                    <span style="font-size:28px;font-weight:bold;letter-spacing:6px;color:#1B4332;font-family:monospace;"><c:out value="${resetCode}"/></span>
                </div>
                <p class="form-hint mb-20">Enter this code on the <a href="${pageContext.request.contextPath}/reset-password">Reset Password page</a> along with your new password.</p>
            </c:if>

            <c:if test="${empty resetCode and empty success}">
                <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                    <div class="form-group">
                        <label>Email Address <span class="required">*</span></label>
                        <input type="email" name="email" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Send Reset Code</button>
                </form>
            </c:if>

            <c:if test="${not empty success and empty resetCode}">
                <a href="${pageContext.request.contextPath}/reset-password" class="btn btn-primary btn-block" style="margin-top:15px;">Go to Reset Password</a>
            </c:if>

            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/login">Back to Login</a>
            </div>
        </div>
    </main>
    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
</div>
</body>
</html>
