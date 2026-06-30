<%-- Top header fragment: included on authenticated pages with sidebar layout --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<header class="top-header">
    <div class="header-left">
        <button class="hamburger-btn" onclick="toggleSidebar()" title="Toggle sidebar">&#9776;</button>
        <a href="${pageContext.request.contextPath}/" class="header-brand">
            <div class="brand-emblem">P</div>
            <div class="brand-text">
                <span class="brand-name">ProcureGov</span>
                <span class="brand-subtitle">Ministry of Public Works</span>
            </div>
        </a>
    </div>
    <div class="header-right">
        <div class="header-gov-badge">
            <img src="${pageContext.request.contextPath}/assets/images/flag.png" alt="Lesotho Flag" class="flag-img"/>
            Kingdom of Lesotho
        </div>
        <div class="profile-dropdown">
            <button class="profile-btn" onclick="toggleProfileMenu()">
                <div class="profile-avatar">
                    <c:out value="${fn:substring(sessionScope.userName, 0, 1)}"/>
                </div>
                <div class="profile-info">
                    <span class="profile-name"><c:out value="${sessionScope.userName}"/></span>
                    <span class="profile-role"><c:out value="${sessionScope.userRole}"/></span>
                </div>
            </button>
            <div class="profile-menu" id="profileMenu">
                <a href="${pageContext.request.contextPath}/profile">&#128100; My Profile</a>
                <a href="${pageContext.request.contextPath}/profile" class="menu-divider">&#9881; Settings</a>
                <a href="${pageContext.request.contextPath}/logout" class="logout-link">&#10140; Logout</a>
            </div>
        </div>
    </div>
</header>

<div class="sidebar-overlay" id="sidebarOverlay" onclick="toggleSidebar()"></div>
