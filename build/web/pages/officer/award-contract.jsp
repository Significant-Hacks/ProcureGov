<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .rank-badge { display:inline-flex; align-items:center; justify-content:center; width:36px; height:36px; border-radius:50%; font-weight:700; font-size:1.1em; }
        .rank-1 { background:#ffd700; color:#333; }
        .rank-2 { background:#c0c0c0; color:#333; }
        .rank-3 { background:#cd7f32; color:#fff; }
        .rank-other { background:#e9ecef; color:#495057; }
        .action-links { display:flex; flex-direction:column; gap:6px; }
        .action-links a, .action-links button { text-align:left; font-size:0.9em; }
        .award-form-section { margin-top:24px; }
        .award-form-section .form-row { display:flex; gap:16px; }
        .award-form-section .form-row .form-group { flex:1; }
        .evaluator-score-chip { display:inline-block; font-size:0.8em; background:#f0f4f8; border:1px solid #d0d8e0; border-radius:4px; padding:2px 6px; margin:1px; }
        @media (max-width: 480px) {
            .award-form-section .form-row { flex-direction: column; gap: 0; }
            .action-links { flex-direction: row; flex-wrap: wrap; }
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
                <h2>Tender Evaluation Results</h2>
                <c:choose><c:when test="${isOfficer}"><a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Back to Dashboard</a></c:when><c:otherwise><a href="${pageContext.request.contextPath}/evaluator-dashboard" class="btn btn-outline">Back to Dashboard</a></c:otherwise></c:choose>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">${tender.referenceNumber}: ${tender.title}</div>
                <p>Estimated Value: M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/> &nbsp;|&nbsp; Status: <span class="badge badge-${tender.status}">${tender.status}</span></p>
            </div>

            <!-- Leaderboard -->
            <div class="card">
                <div class="card-header">Ranked Bids (by Final Score)</div>
                <p class="text-muted" style="padding:0 16px;">Review evaluator scores below. <c:if test="${isOfficer and tender.status eq 'Evaluated'}">Click <strong>Award Tender</strong> on the top-ranked bidder to award the contract.</c:if></p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th style="width:60px;">Rank</th>
                                <th>Supplier</th>
                                <th>Bid Amount</th>
                                <th>Timeline</th>
                                <th>Evaluator Scores</th>
                                <th style="width:80px;">Final Score</th>
                                <th style="width:200px;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="rb" items="${rankedBids}" varStatus="status">
                                <tr style="${status.count eq 1 ? 'background:#fffdf0;' : ''}">
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${status.count eq 1}"><span class="rank-badge rank-1">1</span></c:when>
                                            <c:when test="${status.count eq 2}"><span class="rank-badge rank-2">2</span></c:when>
                                            <c:when test="${status.count eq 3}"><span class="rank-badge rank-3">3</span></c:when>
                                            <c:otherwise><span class="rank-badge rank-other">${status.count}</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><strong>${rb.companyName}</strong></td>
                                    <td>M <fmt:formatNumber value="${rb.amount}" pattern="#,##0.00"/></td>
                                    <td>${rb.proposedTimelineDays} days</td>
                                    <td>
                                        <c:forEach var="es" items="${rb.evaluatorScores}">
                                            <span class="evaluator-score-chip">
                                                <strong>${es.evaluatorName}</strong>:
                                                T=<fmt:formatNumber value="${es.technicalScore}" pattern="0.0"/>,
                                                P=<fmt:formatNumber value="${es.priceScore}" pattern="0.0"/>,
                                                TL=<fmt:formatNumber value="${es.timelineScore}" pattern="0.0"/>,
                                                W=<fmt:formatNumber value="${es.weightedTotal}" pattern="0.0"/>
                                            </span>
                                        </c:forEach>
                                    </td>
                                    <td style="text-align:center;font-weight:700;font-size:1.1em;"><fmt:formatNumber value="${rb.finalScore}" pattern="0.00"/></td>
                                    <td>
                                        <div class="action-links">
                                            <!-- Award Tender: only #1 ranked, officer, Evaluated status -->
                                            <c:if test="${status.count eq 1 and isOfficer and tender.status eq 'Evaluated'}">
                                                <button type="button" class="btn btn-success btn-sm" onclick="showAwardForm(${rb.id}, '${rb.companyName}', ${rb.amount})">Award Tender</button>
                                            </c:if>
                                            <c:if test="${status.count eq 1 and isOfficer and tender.status eq 'Awarded'}">
                                                <span class="badge badge-Awarded">Already Awarded</span>
                                            </c:if>
                                            <c:if test="${not isOfficer and status.count eq 1 and tender.status eq 'Awarded'}">
                                                <span class="badge badge-Awarded">Awarded</span>
                                            </c:if>
                                            <!-- View Full Evaluation Report: all bidders -->
                                            <a href="${pageContext.request.contextPath}/bid-report?tenderId=${tender.id}&bidId=${rb.id}" class="btn btn-outline btn-sm">View Report</a>
                                            <!-- Download Supporting Document: all bidders with doc -->
                                            <c:if test="${not empty rb.documentPath}">
                                                <a href="${pageContext.request.contextPath}/download?file=${rb.documentPath}" class="btn btn-outline btn-sm" target="_blank">Download Document</a>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Award Form (hidden by default, shown when Award Tender is clicked) -->
            <c:if test="${isOfficer and tender.status eq 'Evaluated'}">
            <div id="awardFormSection" class="card award-form-section" style="display:none;">
                <div class="card-header">Award Tender</div>
                <p style="padding:0 16px; color:#666;">${tender.referenceNumber}: ${tender.title}</p>
                <form action="${pageContext.request.contextPath}/award-contract" method="POST" enctype="multipart/form-data" style="padding:0 16px 16px;">
                    <input type="hidden" name="tenderId" value="${tender.id}">
                    <input type="hidden" name="bidId" id="awardBidId" value="">

                    <div class="form-row">
                        <div class="form-group">
                            <label>Winning Supplier</label>
                            <input type="text" id="awardSupplierName" class="form-control" readonly>
                        </div>
                        <div class="form-group">
                            <label>Bid Amount</label>
                            <input type="text" id="awardBidAmount" class="form-control" readonly>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Awarded Value (Maloti) <span class="required">*</span></label>
                            <input type="number" name="awardedValue" id="awardedValueInput" class="form-control" step="0.01" min="1" required>
                            <p class="form-hint">Enter the final contract value (must be at least the bid amount).</p>
                        </div>
                        <div class="form-group">
                            <label>Justification <span class="required">*</span></label>
                            <textarea name="justification" class="form-control" rows="3" required placeholder="Enter the award justification note..."></textarea>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between" style="margin-top:12px;">
                        <button type="button" class="btn btn-outline" onclick="hideAwardForm()">Cancel</button>
                        <button type="submit" class="btn btn-primary">Award &amp; Generate Notice</button>
                    </div>
                </form>
            </div>
            </c:if>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

<script>
    function showAwardForm(bidId, supplierName, bidAmount) {
        document.getElementById('awardBidId').value = bidId;
        document.getElementById('awardSupplierName').value = supplierName;
        document.getElementById('awardBidAmount').value = 'M ' + Number(bidAmount).toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
        document.getElementById('awardedValueInput').min = bidAmount;
        var section = document.getElementById('awardFormSection');
        section.style.display = 'block';
        section.scrollIntoView({behavior:'smooth'});
    }
    function hideAwardForm() {
        document.getElementById('awardFormSection').style.display = 'none';
    }
</script>
</body>
</html>
