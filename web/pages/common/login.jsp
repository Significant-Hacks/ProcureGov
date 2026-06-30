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
            <h2>Login</h2>
            <p class="auth-subtitle">Sign in to ProcureGov Tender Management</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success"><c:out value="${success}"/></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="POST">
                <div class="form-group">
                    <label>Email Address <span class="required">*</span></label>
                    <input type="email" name="email" class="form-control"
                           value="${not empty email ? email : ''}" required>
                </div>
                <div class="form-group">
                    <label>Password <span class="required">*</span></label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Login</button>
            </form>

            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/forgot-password">Forgot Password?</a>
                <br><br>
                Don't have an account? <a href="${pageContext.request.contextPath}/register">Register here</a>
            </div>
        </div>
    </main>
    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
</div>
</body>
</html>
