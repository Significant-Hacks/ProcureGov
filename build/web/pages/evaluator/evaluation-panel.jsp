<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .criteria-panel { background: #f8f9fa; border: 1px solid #e0e0e0; border-radius: 6px; padding: 12px; margin-top: 8px; }
        .criteria-panel h5 { margin: 0 0 8px 0; font-size: 0.9rem; color: #555; }
        .criterion-item { display: flex; padding: 4px 0; border-bottom: 1px solid #eee; font-size: 0.85rem; }
        .criterion-item:last-child { border-bottom: none; }
        .criterion-type { display: inline-block; padding: 1px 6px; border-radius: 3px; font-size: 0.75rem; font-weight: 600; margin-right: 8px; min-width: 100px; text-align: center; }
        .criterion-type.Equipment { background: #e3f2fd; color: #1565c0; }
        .criterion-type.Certifications { background: #f3e5f5; color: #7b1fa2; }
        .criterion-type.Experience { background: #e8f5e9; color: #2e7d32; }
        .criterion-type.QualityStandards { background: #fff3e0; color: #e65100; }
        .criterion-type.Methodology { background: #e0f2f1; color: #00695c; }
        .criterion-type.Personnel { background: #fce4ec; color: #c62828; }
        .criterion-type.Other { background: #f5f5f5; color: #616161; }
        .criterion-value { flex: 1; }
        .criterion-evidence { margin-left: 8px; }
        .bid-row td { vertical-align: top; }
        .expand-criteria { cursor: pointer; color: #2D6A4F; font-size: 0.85rem; }
        .expand-criteria:hover { text-decoration: underline; }
        @media (max-width: 480px) {
            .criterion-item { flex-wrap: wrap; }
            .criterion-type { min-width: auto; margin-bottom: 4px; }
            .criterion-evidence { margin-left: 0; margin-top: 4px; }
            .criteria-panel { padding: 8px; }
        }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Evaluation Panel</h2>
                <c:choose>
                    <c:when test="${sessionScope.userRole eq 'OFFICER'}">
                        <a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Back to Dashboard</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/evaluator-dashboard" class="btn btn-outline">Back to Dashboard</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="card">
                <div class="card-header">Tender: ${tender.referenceNumber} - ${tender.title}
                    <span class="badge badge-${fn:replace(tender.status, ' ', '-')}" style="float:right">${tender.status}</span>
                </div>
                <p>Estimated Value: M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></p>
            </div>

            <c:choose>
                <%-- When tender is Evaluated/Awarded, show the final ranked leaderboard with individual scores --%>
                <c:when test="${tender.status eq 'Evaluated' or tender.status eq 'Awarded'}">
                    <div class="card">
                        <div class="card-header">Final Evaluation Results</div>
                        <p class="text-muted">All evaluators have submitted their scores. Below are the final averaged results with individual evaluator breakdowns.</p>
                        <div class="table-responsive">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Rank</th>
                                        <th>Supplier</th>
                                        <th>Bid Amount</th>
                                        <th>Timeline (Days)</th>
                                        <th>Technical Compliance Details</th>
                                        <th>Evaluator Scores</th>
                                        <th>Final Score</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="rb" items="${rankedBids}" varStatus="status">
                                        <tr class="bid-row" style="${status.count eq 1 ? 'background:#fffdf0;' : ''}">
                                            <td><strong>${status.count}</strong></td>
                                            <td>${rb.companyName}</td>
                                            <td>M <fmt:formatNumber value="${rb.amount}" pattern="#,##0.00"/></td>
                                            <td>${rb.bid.proposedTimelineDays} days</td>
                                            <td>
                                                <c:set var="bidCriteria" value="${criteriaMap[rb.bid.id]}"/>
                                                <c:choose>
                                                    <c:when test="${not empty bidCriteria}">
                                                        <span class="expand-criteria" onclick="toggleCriteria('criteria_results_${rb.bid.id}')">Show ${fn:length(bidCriteria)} criteria</span>
                                                        <div id="criteria_results_${rb.bid.id}" style="display:none;" class="criteria-panel">
                                                            <c:forEach var="crit" items="${bidCriteria}">
                                                                <div class="criterion-item">
                                                                    <span class="criterion-type ${crit.criterionType}">${crit.criterionType}</span>
                                                                    <span class="criterion-value"><strong>${crit.criterionName}:</strong> ${crit.criterionValue}</span>
                                                                    <c:if test="${not empty crit.evidenceDocumentPath}">
                                                                        <a href="${pageContext.request.contextPath}/download?file=${crit.evidenceDocumentPath}" class="criterion-evidence" target="_blank">Evidence</a>
                                                                    </c:if>
                                                                </div>
                                                            </c:forEach>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted" style="font-size:0.85em">No structured criteria submitted</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:forEach var="es" items="${rb.evaluatorScores}" varStatus="esStatus">
                                                    <div style="font-size:0.85em;margin-bottom:2px">
                                                        <strong>${es.evaluatorName}</strong>:
                                                        Tech=<fmt:formatNumber value="${es.technicalScore}" pattern="0.00"/>,
                                                        Price=<fmt:formatNumber value="${es.priceScore}" pattern="0.00"/>,
                                                        Timeline=<fmt:formatNumber value="${es.timelineScore}" pattern="0.00"/>,
                                                        Weighted=<fmt:formatNumber value="${es.weightedTotal}" pattern="0.00"/>
                                                    </div>
                                                </c:forEach>
                                            </td>
                                            <td><strong><fmt:formatNumber value="${rb.finalScore}" pattern="0.00"/></strong></td>
                                            <td>
                                                <div style="display:flex; flex-direction:column; gap:4px;">
                                                    <a href="${pageContext.request.contextPath}/bid-report?tenderId=${tender.id}&bidId=${rb.id}" class="btn btn-outline btn-sm">View Report</a>
                                                    <c:if test="${not empty rb.documentPath}">
                                                        <a href="${pageContext.request.contextPath}/download?file=${rb.documentPath}" class="btn btn-outline btn-sm" target="_blank">Download Document</a>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty rankedBids}">
                                        <tr><td colspan="8" class="text-center text-muted">No evaluation results available.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:when>

                <%-- When tender is Under Evaluation, show scoring form with blind scoring --%>
                <c:otherwise>
                    <div class="card">
                        <div class="card-header">Bids for Evaluation</div>
                        <p class="text-muted">Enter a Technical Compliance Score (0-100) for each bid. Price and Timeline scores are calculated automatically. You cannot see other evaluators' scores until you submit your own. <strong>Review the technical compliance details below to inform your score.</strong></p>
                        <div class="table-responsive">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Supplier</th>
                                        <th>Bid Amount</th>
                                        <th>Timeline (Days)</th>
                                        <th>Technical Compliance Details</th>
                                        <th>Technical Score</th>
                                        <th>Other Evaluators' Scores</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="bid" items="${bids}">
                                        <tr class="bid-row">
                                            <td>${bid.companyName}</td>
                                            <td>M <fmt:formatNumber value="${bid.amount}" pattern="#,##0.00"/></td>
                                            <td>${bid.proposedTimelineDays} days</td>
                                            <td>
                                                <c:set var="bidCriteria" value="${criteriaMap[bid.id]}"/>
                                                <c:choose>
                                                    <c:when test="${not empty bidCriteria}">
                                                        <span class="expand-criteria" onclick="toggleCriteria('criteria_eval_${bid.id}')">Show ${fn:length(bidCriteria)} criteria</span>
                                                        <div id="criteria_eval_${bid.id}" style="display:none;" class="criteria-panel">
                                                            <c:forEach var="crit" items="${bidCriteria}">
                                                                <div class="criterion-item">
                                                                    <span class="criterion-type ${crit.criterionType}">${crit.criterionType}</span>
                                                                    <span class="criterion-value"><strong>${crit.criterionName}:</strong> ${crit.criterionValue}</span>
                                                                    <c:if test="${not empty crit.evidenceDocumentPath}">
                                                                        <a href="${pageContext.request.contextPath}/download?file=${crit.evidenceDocumentPath}" class="criterion-evidence" target="_blank">Evidence</a>
                                                                    </c:if>
                                                                </div>
                                                            </c:forEach>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted" style="font-size:0.85em">No structured criteria</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${requestScope['scored_bid_'.concat(bid.id)]}">
                                                        <span class="badge badge-Awarded">Already Scored</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form action="${pageContext.request.contextPath}/submit-score" method="POST" style="display:inline">
                                                            <input type="hidden" name="bidId" value="${bid.id}">
                                                            <input type="hidden" name="tenderId" value="${tender.id}">
                                                            <input type="number" name="technicalScore" class="form-control" min="0" max="100" step="0.01" placeholder="0-100" required style="width:100px;display:inline-block">
                                                            <button type="submit" class="btn btn-sm btn-primary">Submit Score</button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${requestScope['scored_bid_'.concat(bid.id)]}">
                                                        <c:set var="otherScores" value="${otherScoresMap[bid.id]}"/>
                                                        <c:choose>
                                                            <c:when test="${not empty otherScores}">
                                                                <c:forEach var="os" items="${otherScores}">
                                                                    <div style="font-size:0.85em;margin-bottom:2px">
                                                                        <strong>${os.evaluatorName}</strong>:
                                                                        Tech=<fmt:formatNumber value="${os.technicalScore}" pattern="0.00"/>,
                                                                        Price=<fmt:formatNumber value="${os.priceScore}" pattern="0.00"/>,
                                                                        Timeline=<fmt:formatNumber value="${os.timelineScore}" pattern="0.00"/>,
                                                                        Weighted=<fmt:formatNumber value="${os.weightedTotal}" pattern="0.00"/>
                                                                    </div>
                                                                </c:forEach>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted" style="font-size:0.85em">No other scores yet</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted" style="font-style:italic;font-size:0.85em">Submit your score first</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty bids}">
                                        <tr><td colspan="6" class="text-center text-muted">No bids submitted for this tender.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

<script>
    function toggleCriteria(elementId) {
        var el = document.getElementById(elementId);
        if (el) {
            el.style.display = el.style.display === 'none' ? 'block' : 'none';
        }
    }
</script>
</body>
</html>