<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
                <h2>Manage Tender</h2>
                <a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Back to Dashboard</a>
            </div>

            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">Tender: ${tender.referenceNumber} - ${tender.title}</div>
                <table>
                    <tr><th>Field</th><th>Value</th></tr>
                    <tr><td>Reference</td><td>${tender.referenceNumber}</td></tr>
                    <tr><td>Title</td><td>${tender.title}</td></tr>
                    <tr><td>Category</td><td>${tender.category}</td></tr>
                    <tr><td>Description</td><td>${tender.description}</td></tr>
                    <tr><td>Estimated Value</td><td>M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></td></tr>
                    <tr><td>Deadline</td><td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td></tr>
                    <tr><td>Status</td><td><span class="badge badge-${fn:replace(tender.status, ' ', '-')}">${tender.status}</span></td></tr>
                </table>
            </div>

            <div class="card">
                <div class="card-header">Status Actions</div>
                <c:choose>
                    <c:when test="${tender.status eq 'Draft'}">
                        <p>This tender is in <strong>Draft</strong> status. You can edit all fields or publish it to make it visible to suppliers.</p>
                        <div class="d-flex gap-2">
                            <a href="${pageContext.request.contextPath}/edit-tender?id=${tender.id}" class="btn btn-outline">Edit Tender</a>
                            <form action="${pageContext.request.contextPath}/tender-status" method="POST">
                                <input type="hidden" name="id" value="${tender.id}">
                                <input type="hidden" name="action" value="publish">
                                <button type="submit" class="btn btn-primary">Publish (Draft &rarr; Open)</button>
                            </form>
                        </div>
                    </c:when>
                    <c:when test="${tender.status eq 'Open'}">
                        <p>This tender is <strong>Open</strong> for bidding. It will automatically close when the deadline passes.</p>
                    </c:when>
                    <c:when test="${tender.status eq 'Closed'}">
                        <p>This tender is <strong>Closed</strong>. Bidding has ended. You can start the evaluation process.</p>
                        <form action="${pageContext.request.contextPath}/tender-status" method="POST">
                            <input type="hidden" name="id" value="${tender.id}">
                            <input type="hidden" name="action" value="startEvaluation">
                            <button type="submit" class="btn btn-warning">Start Evaluation (Closed &rarr; Under Evaluation)</button>
                        </form>
                    </c:when>
                    <c:when test="${tender.status eq 'Under Evaluation'}">
                        <p>This tender is <strong>Under Evaluation</strong>. Evaluators and officers are scoring bids. It will automatically move to Evaluated when all scores are submitted.</p>
                        <a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${tender.id}" class="btn btn-primary">Score Bids</a>
                    </c:when>
                    <c:when test="${tender.status eq 'Evaluated'}">
                        <p>This tender has been <strong>Evaluated</strong>. Review the scores and award the contract.</p>
                        <a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-success">Award Contract</a>
                    </c:when>
                    <c:when test="${tender.status eq 'Awarded'}">
                        <p>This tender has been <strong>Awarded</strong>. The contract has been assigned.</p>
                        <a href="${pageContext.request.contextPath}/award-notice?tender=${tender.id}" class="btn btn-primary">View Award Notice</a>
                    </c:when>
                </c:choose>
            </div>
        </div>
        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>