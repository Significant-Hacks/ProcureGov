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
                <h2>Manage Tenders</h2>
                <a href="${pageContext.request.contextPath}/create-tender" class="btn btn-primary">+ Create New Tender</a>
            </div>

            <div class="card">
                <div class="card-header">All Tenders</div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Est. Value</th>
                                <th>Deadline</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tender" items="${tenders}">
                                <tr>
                                    <td>${tender.referenceNumber}</td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td>M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td>
                                    <td><span class="badge badge-${fn:replace(tender.status, ' ', '-')}">${tender.status}</span></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/tender-status?id=${tender.id}" class="btn btn-sm">Manage</a>
                                        <c:if test="${tender.status eq 'Under Evaluation'}">
                                            <a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${tender.id}" class="btn btn-sm btn-primary">Score Bids</a>
                                        </c:if>
                                        <c:if test="${tender.status eq 'Evaluated'}">
                                            <a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-sm btn-success">Award Contract</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty tenders}">
                                <tr><td colspan="7" class="text-center text-muted">No tenders found. Create your first tender!</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <c:if test="${not empty underEvaluationTenders}">
            <div class="card">
                <div class="card-header">Tenders Under Evaluation <span class="badge badge-Under-Evaluation">Evaluator Role</span></div>
                <p class="text-muted">As an evaluation committee member, you can score bids on these tenders.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Deadline</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tender" items="${underEvaluationTenders}">
                                <tr>
                                    <td>${tender.referenceNumber}</td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td>
                                    <td><span class="badge badge-Under-Evaluation">${tender.status}</span></td>
                                    <td><a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${tender.id}" class="btn btn-sm btn-primary">Score Bids</a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            </c:if>

            <c:if test="${not empty evaluatedTenders}">
            <div class="card">
                <div class="card-header">Evaluated Tenders - Ready for Award <span class="badge badge-Evaluated">Officer Role</span></div>
                <p class="text-muted">All evaluators have scored these tenders. Review the ranked bids and award the contract.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Est. Value</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tender" items="${evaluatedTenders}">
                                <tr>
                                    <td>${tender.referenceNumber}</td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td>M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td><span class="badge badge-Evaluated">${tender.status}</span></td>
                                    <td><a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-sm btn-success">Award Contract</a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            </c:if>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
