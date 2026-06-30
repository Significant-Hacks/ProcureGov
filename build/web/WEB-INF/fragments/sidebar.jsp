<%-- Sidebar fragment: included on authenticated pages --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <img src="${pageContext.request.contextPath}/assets/images/ca.png" alt="Coat of Arms" class="sidebar-emblem-img"/>
        <div class="sidebar-brand-text">
            <span class="sidebar-brand-name">Public Procurement</span>
            <span class="sidebar-brand-sub">Khotso &bull; Pula &bull; Nala</span>
        </div>
    </div>

    <nav class="sidebar-nav">
        <c:choose>
            <c:when test="${sessionScope.userRole eq 'OFFICER'}">
                <div class="sidebar-section-title"><span>Procurement</span></div>
                <a href="${pageContext.request.contextPath}/officer-dashboard" class="sidebar-link">
                    <span class="sidebar-icon">&#9632;</span>
                    <span class="sidebar-label">Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/create-tender" class="sidebar-link">
                    <span class="sidebar-icon">&#10010;</span>
                    <span class="sidebar-label">Create Tender</span>
                </a>
                <a href="${pageContext.request.contextPath}/manage-tenders" class="sidebar-link">
                    <span class="sidebar-icon">&#9776;</span>
                    <span class="sidebar-label">Manage Tenders</span>
                </a>
                <div class="sidebar-section-title"><span>Evaluation</span></div>
                <a href="${pageContext.request.contextPath}/evaluations" class="sidebar-link">
                    <span class="sidebar-icon">&#9733;</span>
                    <span class="sidebar-label">Evaluation Panel</span>
                </a>
            </c:when>
            <c:when test="${sessionScope.userRole eq 'SUPPLIER'}">
                <div class="sidebar-section-title"><span>Tenders</span></div>
                <a href="${pageContext.request.contextPath}/supplier-dashboard" class="sidebar-link">
                    <span class="sidebar-icon">&#9632;</span>
                    <span class="sidebar-label">Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/open-tenders" class="sidebar-link">
                    <span class="sidebar-icon">&#128196;</span>
                    <span class="sidebar-label">Open Tenders</span>
                </a>
                <div class="sidebar-section-title"><span>My Bids</span></div>
                <a href="${pageContext.request.contextPath}/supplier-dashboard#my-bids" class="sidebar-link">
                    <span class="sidebar-icon">&#128203;</span>
                    <span class="sidebar-label">Bid Status</span>
                </a>
            </c:when>
            <c:when test="${sessionScope.userRole eq 'EVALUATOR'}">
                <div class="sidebar-section-title"><span>Evaluation</span></div>
                <a href="${pageContext.request.contextPath}/evaluator-dashboard" class="sidebar-link">
                    <span class="sidebar-icon">&#9632;</span>
                    <span class="sidebar-label">Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/evaluations" class="sidebar-link">
                    <span class="sidebar-icon">&#9733;</span>
                    <span class="sidebar-label">Evaluation Panel</span>
                </a>
            </c:when>
        </c:choose>

        <div class="sidebar-section-title"><span>Account</span></div>
        <a href="${pageContext.request.contextPath}/profile" class="sidebar-link">
            <span class="sidebar-icon">&#128100;</span>
            <span class="sidebar-label">My Profile</span>
        </a>
    </nav>

    <div class="sidebar-footer">
        <img src="${pageContext.request.contextPath}/assets/images/ca.png" alt="Coat of Arms" class="sidebar-coat-of-arms"/>
        <div class="sidebar-footer-text">Khotso &bull; Pula &bull; Nala</div>
    </div>
</aside>
