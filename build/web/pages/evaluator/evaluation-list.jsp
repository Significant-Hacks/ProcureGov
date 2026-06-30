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
                <h2>Evaluation Panel</h2>
            </div>

            <!-- Tenders Under Evaluation -->
            <div class="card">
                <div class="card-header">Tenders Under Evaluation</div>
                <p class="text-muted">Click on a tender to score the submitted bids.</p>
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
                                    <td><span class="badge badge-evaluation">${tender.status}</span></td>
                                    <td><a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${tender.id}" class="btn btn-sm btn-primary">Score Bids</a></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty underEvaluationTenders}">
                                <tr><td colspan="6" class="text-center text-muted">No tenders currently under evaluation, or you have completed scoring all assigned tenders.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Pending Evaluations -->
            <div class="card">
                <div class="card-header">Pending Evaluations</div>
                <p class="text-muted">Tenders assigned to you that still need scoring (includes partially scored tenders).</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Tender Title</th>
                                <th>Progress</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="pending" items="${pendingEvaluations}">
                                <tr>
                                    <td>${pending.tenderRefNumber}</td>
                                    <td>${pending.tenderTitle}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${pending.technicalScore == 0}">Not started</c:when>
                                            <c:otherwise>${pending.technicalScore} / ${pending.bidAmount} bids scored</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${pending.tenderId}" class="btn btn-sm"><c:choose><c:when test="${pending.technicalScore == 0}">Start Evaluation</c:when><c:otherwise>Continue Scoring</c:otherwise></c:choose></a></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty pendingEvaluations}">
                                <tr><td colspan="4" class="text-center text-muted">No pending evaluations. All assigned tenders have been scored.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Completed Evaluations -->
            <div class="card">
                <div class="card-header">Completed Evaluations</div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Tender</th>
                                <th>Supplier</th>
                                <th>Bid Amount</th>
                                <th>Technical</th>
                                <th>Price</th>
                                <th>Timeline</th>
                                <th>Weighted Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="ev" items="${myEvaluations}">
                                <tr>
                                    <td>${ev.tenderRefNumber} - ${ev.tenderTitle}</td>
                                    <td>${ev.supplierName}</td>
                                    <td>M <fmt:formatNumber value="${ev.bidAmount}" pattern="#,##0.00"/></td>
                                    <td><fmt:formatNumber value="${ev.technicalScore}" pattern="0.00"/></td>
                                    <td><fmt:formatNumber value="${ev.priceScore}" pattern="0.00"/></td>
                                    <td><fmt:formatNumber value="${ev.timelineScore}" pattern="0.00"/></td>
                                    <td><strong><fmt:formatNumber value="${ev.weightedTotal}" pattern="0.00"/></strong></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myEvaluations}">
                                <tr><td colspan="7" class="text-center text-muted">No completed evaluations yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Evaluated Tenders -->
            <c:if test="${not empty evaluatedTenders}">
            <div class="card">
                <div class="card-header">Evaluated Tenders - Consolidated Results</div>
                <p class="text-muted">All committee members have scored these tenders. View the final ranked results.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
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
                                    <td><span class="badge badge-Evaluated">${tender.status}</span></td>
                                    <td><a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-sm">View Ranked Results</a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            </c:if>

            <!-- Awarded Tenders -->
            <c:if test="${not empty awardedTenders}">
            <div class="card">
                <div class="card-header">Awarded Tenders</div>
                <p class="text-muted">These tenders have been awarded. View the final results and award details.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tender" items="${awardedTenders}">
                                <tr>
                                    <td>${tender.referenceNumber}</td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td><span class="badge badge-Awarded">${tender.status}</span></td>
                                    <td><a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-sm">View Results</a></td>
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
