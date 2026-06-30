<%-- Navbar fragment: included on unauthenticated/landing pages --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<nav class="navbar">
    <div class="navbar-inner">
        <a href="${pageContext.request.contextPath}/" class="navbar-brand">
            <img src="${pageContext.request.contextPath}/assets/images/ca.png" alt="Coat of Arms" class="brand-icon-img"/>
            ProcureGov
        </a>
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <ul class="navbar-nav">
                    <c:choose>
                        <c:when test="${sessionScope.userRole eq 'OFFICER'}">
                            <li><a href="${pageContext.request.contextPath}/officer-dashboard">Dashboard</a></li>
                            <li><a href="${pageContext.request.contextPath}/create-tender">Create Tender</a></li>
                            <li><a href="${pageContext.request.contextPath}/evaluations">Evaluation Panel</a></li>
                        </c:when>
                        <c:when test="${sessionScope.userRole eq 'SUPPLIER'}">
                            <li><a href="${pageContext.request.contextPath}/supplier-dashboard">Dashboard</a></li>
                        </c:when>
                        <c:when test="${sessionScope.userRole eq 'EVALUATOR'}">
                            <li><a href="${pageContext.request.contextPath}/evaluator-dashboard">Dashboard</a></li>
                        </c:when>
                    </c:choose>
                    <li><a href="${pageContext.request.contextPath}/profile">Profile</a></li>
                </ul>
                <div class="navbar-user">
                    <span>Welcome, <c:out value="${sessionScope.userName}"/></span>
                    <span class="user-role"><c:out value="${sessionScope.userRole}"/></span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
                </div>
            </c:when>
            <c:otherwise>
                <ul class="navbar-nav">
                    <li><a href="${pageContext.request.contextPath}/login">Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/register">Register</a></li>
                </ul>
            </c:otherwise>
        </c:choose>
    </div>
</nav>