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
                <h2>Official Award Notice</h2>
                <c:choose>
                    <c:when test="${sessionScope.userRole eq 'OFFICER'}">
                        <a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Back to Dashboard</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/supplier-dashboard" class="btn btn-outline">Back to Dashboard</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="card">
                <div class="card-header" style="text-align:center">
                    <h3>OFFICIAL AWARD NOTICE</h3>
                    <p>Ministry of Public Works - Kingdom of Lesotho</p>
                </div>
                <table>
                    <tr><th>Field</th><th>Value</th></tr>
                    <tr><td>Tender Reference</td><td><strong>${tender.referenceNumber}</strong></td></tr>
                    <tr><td>Tender Title</td><td>${tender.title}</td></tr>
                    <tr><td>Winning Supplier</td><td><strong>${award.supplierName}</strong></td></tr>
                    <tr><td>Awarded Value</td><td><strong>M <fmt:formatNumber value="${award.awardedValue}" pattern="#,##0.00"/></strong></td></tr>
                    <tr><td>Award Date</td><td><fmt:formatDate value="${award.awardedAt}" pattern="dd MMM yyyy"/></td></tr>
                    <tr><td>Justification</td><td>${award.justification}</td></tr>
                </table>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
