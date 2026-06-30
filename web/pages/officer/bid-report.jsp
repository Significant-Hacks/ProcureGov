<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .report-section { margin-bottom: 24px; }
        .report-section h3 { border-bottom: 2px solid #dee2e6; padding-bottom: 8px; margin-bottom: 12px; }
        .detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:12px; }
        .detail-item { padding:8px 12px; background:#f8f9fa; border-radius:6px; }
        .detail-item label { display:block; font-size:0.85em; color:#666; margin-bottom:2px; }
        .detail-item span { font-weight:600; }
        .rank-badge { display:inline-flex; align-items:center; justify-content:center; width:36px; height:36px; border-radius:50%; font-weight:700; font-size:1.1em; }
        .rank-1 { background:#ffd700; color:#333; }
        .rank-2 { background:#c0c0c0; color:#333; }
        .rank-3 { background:#cd7f32; color:#fff; }
        .rank-other { background:#e9ecef; color:#495057; }
        .criterion-card { background:#f8f9fa; border:1px solid #e9ecef; border-radius:8px; padding:12px 16px; margin-bottom:8px; }
        @media (max-width: 480px) {
            .detail-grid { grid-template-columns: 1fr; }
            .criterion-card { padding: 10px 12px; }
        }
        .criterion-type-badge { display:inline-block; padding:2px 8px; border-radius:4px; font-size:0.8em; font-weight:600; color:#fff; }
        .Equipment { background:#2563eb; }
        .Certifications { background:#7c3aed; }
        .Experience { background:#059669; }
        .QualityStandards { background:#d97706; }
        .Methodology { background:#dc2626; }
        .Personnel { background:#0891b2; }
        .Other { background:#6b7280; }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Bid Evaluation Report</h2>
                <a href="${pageContext.request.contextPath}/award-contract?id=${tender.id}" class="btn btn-outline">Back to Leaderboard</a>
            </div>

            <!-- Tender Info -->
            <div class="card">
                <div class="card-header">${tender.referenceNumber}: ${tender.title}</div>
                <p style="padding:0 16px;">Estimated Value: M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/> &nbsp;|&nbsp; Status: <span class="badge badge-${tender.status}">${tender.status}</span></p>
            </div>

            <!-- Bidder Details -->
            <div class="card report-section">
                <h3 style="padding:0 16px;">Bidder Details</h3>
                <div class="detail-grid" style="padding:0 16px 16px;">
                    <div class="detail-item">
                        <label>Company Name</label>
                        <span>${bid.companyName}</span>
                    </div>
                    <div class="detail-item">
                        <label>Rank</label>
                        <span>
                            <c:choose>
                                <c:when test="${rank eq 1}"><span class="rank-badge rank-1">1</span></c:when>
                                <c:when test="${rank eq 2}"><span class="rank-badge rank-2">2</span></c:when>
                                <c:when test="${rank eq 3}"><span class="rank-badge rank-3">3</span></c:when>
                                <c:otherwise><span class="rank-badge rank-other">${rank}</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Bid Summary -->
            <div class="card report-section">
                <h3 style="padding:0 16px;">Bid Summary</h3>
                <div class="detail-grid" style="padding:0 16px 16px;">
                    <div class="detail-item">
                        <label>Bid Amount</label>
                        <span>M <fmt:formatNumber value="${bid.amount}" pattern="#,##0.00"/></span>
                    </div>
                    <div class="detail-item">
                        <label>Proposed Timeline</label>
                        <span>${bid.proposedTimelineDays} days</span>
                    </div>
                    <div class="detail-item" style="grid-column:1/3;">
                        <label>Technical Compliance Statement</label>
                        <span>${bid.technicalCompliance}</span>
                    </div>
                    <div class="detail-item">
                        <label>Submitted At</label>
                        <span><fmt:formatDate value="${bid.submittedAt}" pattern="dd MMM yyyy HH:mm"/></span>
                    </div>
                    <div class="detail-item">
                        <label>Supporting Document</label>
                        <span>
                            <c:choose>
                                <c:when test="${not empty bid.documentPath}">
                                    <a href="${pageContext.request.contextPath}/download?file=${bid.documentPath}" target="_blank">Download Document</a>
                                </c:when>
                                <c:otherwise>No document uploaded</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Technical Compliance Criteria -->
            <c:if test="${not empty criteria}">
            <div class="card report-section">
                <h3 style="padding:0 16px;">Technical Compliance Details</h3>
                <div style="padding:0 16px 16px;">
                    <c:forEach var="c" items="${criteria}">
                        <div class="criterion-card">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                                <strong>${c.criterionName}</strong>
                                <span class="criterion-type-badge ${c.criterionType}">${c.criterionType}</span>
                            </div>
                            <p style="margin:0 0 4px 0;">${c.criterionValue}</p>
                            <c:if test="${not empty c.evidenceDocumentPath}">
                                <a href="${pageContext.request.contextPath}/download?file=${c.evidenceDocumentPath}" target="_blank" style="font-size:0.85em;">Download Evidence</a>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </div>
            </c:if>

            <!-- Evaluator Scores -->
            <div class="card report-section">
                <h3 style="padding:0 16px;">Evaluator Scores</h3>
                <div class="table-responsive" style="padding:0 16px 16px;">
                    <table>
                        <thead>
                            <tr>
                                <th>Evaluator</th>
                                <th>Technical</th>
                                <th>Price</th>
                                <th>Timeline</th>
                                <th>Weighted Total</th>
                                <th>Submitted</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="es" items="${evaluatorScores}">
                                <tr>
                                    <td><strong>${es.evaluatorName}</strong></td>
                                    <td><fmt:formatNumber value="${es.technicalScore}" pattern="0.00"/></td>
                                    <td><fmt:formatNumber value="${es.priceScore}" pattern="0.00"/></td>
                                    <td><fmt:formatNumber value="${es.timelineScore}" pattern="0.00"/></td>
                                    <td><strong><fmt:formatNumber value="${es.weightedTotal}" pattern="0.00"/></strong></td>
                                    <td><fmt:formatDate value="${es.submittedAt}" pattern="dd MMM yyyy"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                        <tfoot>
                            <tr style="background:#f0f4f8; font-weight:700;">
                                <td>Average (Final Score)</td>
                                <td colspan="3"></td>
                                <td><fmt:formatNumber value="${finalScore}" pattern="0.00"/></td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>

        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
