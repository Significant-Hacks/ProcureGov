<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .badge-Won { background: #28a745; color: white; }
        .badge-Not-Won { background: #dc3545; color: white; }
        .badge-Pending { background: #ffc107; color: #333; }
        .badge-Bidding { background: #17a2b8; color: white; }
        .badge-Awaiting { background: #6c757d; color: white; }
        .contract-card { border: 1px solid #dee2e6; border-radius: 8px; padding: 20px; margin-bottom: 16px; background: #fff; }
        .contract-card.won { border-left: 4px solid #28a745; }
        .contract-detail { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
        .contract-detail:last-child { border-bottom: none; }
        .contract-detail-label { color: #666; font-size: 0.9rem; }
        .contract-detail-value { font-weight: 600; }
        .btn-download { background: #17a2b8; color: white; border: none; border-radius: 4px; padding: 6px 14px; cursor: pointer; font-size: 0.85rem; text-decoration: none; display: inline-block; }
        .btn-download:hover { background: #138496; color: white; }
        .dash-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card { background: var(--card-bg); border-radius: var(--radius-lg); padding: 20px; box-shadow: var(--shadow); border-left: 4px solid var(--border-color); transition: var(--transition); }
        .stat-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }
        .stat-card .stat-icon { font-size: 28px; margin-bottom: 8px; opacity: 0.7; }
        .stat-card .stat-value { font-size: 32px; font-weight: 800; line-height: 1; }
        .stat-card .stat-label { font-size: 13px; color: var(--text-muted); margin-top: 4px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-card.stat-open { border-left-color: #16a34a; }
        .stat-card.stat-open .stat-value { color: #16a34a; }
        .stat-card.stat-bids { border-left-color: var(--primary-color); }
        .stat-card.stat-bids .stat-value { color: var(--primary-color); }
        .stat-card.stat-pending { border-left-color: #d97706; }
        .stat-card.stat-pending .stat-value { color: #d97706; }
        .stat-card.stat-won { border-left-color: var(--accent-color); }
        .stat-card.stat-won .stat-value { color: var(--accent-color); }
        @media (max-width: 480px) {
            .contract-detail { flex-direction: column; gap: 2px; }
            .contract-card { padding: 14px; }
        }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <h2 class="mb-4">Supplier Dashboard</h2>

            <!-- Stats Overview -->
            <div class="dash-grid">
                <div class="stat-card stat-open">
                    <div class="stat-icon">&#128194;</div>
                    <div class="stat-value">${openTenderCount}</div>
                    <div class="stat-label">Open Tenders</div>
                </div>
                <div class="stat-card stat-bids">
                    <div class="stat-icon">&#128203;</div>
                    <div class="stat-value">${totalBidsCount}</div>
                    <div class="stat-label">Bids Submitted</div>
                </div>
                <div class="stat-card stat-pending">
                    <div class="stat-icon">&#9203;</div>
                    <div class="stat-value">${pendingBidsCount}</div>
                    <div class="stat-label">Awaiting Outcome</div>
                </div>
                <div class="stat-card stat-won">
                    <div class="stat-icon">&#127942;</div>
                    <div class="stat-value">${wonContractsCount}</div>
                    <div class="stat-label">Contracts Won</div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="action-grid">
                <a href="${pageContext.request.contextPath}/open-tenders" class="action-card">
                    <div class="action-icon icon-tenders">&#128196;</div>
                    <div class="action-title">Browse Open Tenders</div>
                    <div class="action-desc">View and bid on available tenders</div>
                </a>
                <a href="#my-bids" class="action-card" onclick="document.getElementById('my-bids').scrollIntoView({behavior:'smooth'});return false;">
                    <div class="action-icon icon-pending">&#128203;</div>
                    <div class="action-title">My Bids</div>
                    <div class="action-desc">Track status of submitted bids</div>
                </a>
                <a href="#contracts" class="action-card" onclick="document.getElementById('contracts').scrollIntoView({behavior:'smooth'});return false;">
                    <div class="action-icon icon-results">&#127942;</div>
                    <div class="action-title">Awarded Contracts</div>
                    <div class="action-desc">View won contracts and documents</div>
                </a>
            </div>

            <!-- My Submitted Bids -->
            <div class="card" id="my-bids">
                <div class="card-header">My Submitted Bids</div>
                <p class="text-muted">Track the status and outcome of all your bids.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Tender</th>
                                <th>Ref Number</th>
                                <th>Bid Amount</th>
                                <th>Timeline</th>
                                <th>Submitted</th>
                                <th>Tender Status</th>
                                <th>Outcome</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="bws" items="${bidsWithStatus}">
                                <tr>
                                    <td>${bws.tenderTitle}</td>
                                    <td>${bws.tenderRefNumber}</td>
                                    <td>M <fmt:formatNumber value="${bws.amount}" pattern="#,##0.00"/></td>
                                    <td>${bws.proposedTimelineDays} days</td>
                                    <td><fmt:formatDate value="${bws.submittedAt}" pattern="dd MMM yyyy"/></td>
                                    <td><span class="badge badge-${fn:replace(bws.tenderStatus, ' ', '-')}">${bws.tenderStatus}</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${bws.tenderStatus == 'Awarded'}">
                                                <c:choose>
                                                    <c:when test="${bws.winner}"><span class="badge badge-Won">Won</span></c:when>
                                                    <c:otherwise><span class="badge badge-Not-Won">Not Won</span><br><span style="font-size:0.8em;color:#666;">Won by: ${bws.winningSupplier}</span></c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${bws.tenderStatus == 'Evaluated'}">
                                                <span class="badge badge-Awaiting">Awaiting Award</span>
                                            </c:when>
                                            <c:when test="${bws.tenderStatus == 'Under Evaluation'}">
                                                <span class="badge badge-Pending">Under Evaluation</span>
                                            </c:when>
                                            <c:when test="${bws.tenderStatus == 'Closed'}">
                                                <span class="badge badge-Awaiting">Awaiting Evaluation</span>
                                            </c:when>
                                            <c:when test="${bws.tenderStatus == 'Open'}">
                                                <span class="badge badge-Bidding">Bidding Open</span>
                                            </c:when>
                                            <c:otherwise><span class="text-muted">${bws.tenderStatus}</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bidsWithStatus}">
                                <tr><td colspan="7" class="text-center text-muted">You have not submitted any bids yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Won Contracts Section -->
            <div class="card" id="contracts">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <span>Awarded Contracts</span>
                        <c:if test="${not empty wonContracts}"><span class="badge badge-Won">${fn:length(wonContracts)} contract${fn:length(wonContracts) > 1 ? 's' : ''}</span></c:if>
                    </div>
                </div>
                <p class="text-muted">Contracts you have been awarded. View details and download confirmation documents.</p>

                <c:choose>
                    <c:when test="${not empty wonContracts}">
                        <c:forEach var="contract" items="${wonContracts}">
                            <div class="contract-card won">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <h4 style="margin:0; color:#28a745;">${contract.tenderTitle}</h4>
                                    <span class="badge badge-Won">Contract Awarded</span>
                                </div>
                                <div class="contract-detail">
                                    <span class="contract-detail-label">Reference</span>
                                    <span class="contract-detail-value">${contract.tenderRefNumber}</span>
                                </div>
                                <div class="contract-detail">
                                    <span class="contract-detail-label">Awarded Value</span>
                                    <span class="contract-detail-value">M <fmt:formatNumber value="${contract.awardedValue}" pattern="#,##0.00"/></span>
                                </div>
                                <div class="contract-detail">
                                    <span class="contract-detail-label">Award Date</span>
                                    <span class="contract-detail-value"><fmt:formatDate value="${contract.awardedAt}" pattern="dd MMM yyyy"/></span>
                                </div>
                                <div class="contract-detail">
                                    <span class="contract-detail-label">Justification</span>
                                    <span class="contract-detail-value" style="max-width: 60%;">${contract.justification}</span>
                                </div>
                                <c:if test="${not empty contract.confirmationDocumentPath}">
                                    <div style="margin-top: 12px;">
                                        <a href="${pageContext.request.contextPath}/download?file=${contract.confirmationDocumentPath}" class="btn-download">Download Confirmation Document</a>
                                    </div>
                                </c:if>
                                <div style="margin-top: 8px;">
                                    <a href="${pageContext.request.contextPath}/award-notice?tender=${contract.tenderId}" class="btn btn-sm btn-outline">View Full Award Notice</a>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p class="text-center text-muted" style="padding: 20px;">You have not been awarded any contracts yet.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Award Notices for all tenders bid on -->
            <div class="card">
                <div class="card-header">Award Notices</div>
                <p class="text-muted">Official award decisions for tenders you bid on.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Tender</th>
                                <th>Reference</th>
                                <th>Winning Supplier</th>
                                <th>Awarded Value</th>
                                <th>Justification</th>
                                <th>Award Date</th>
                                <th>Your Outcome</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="award" items="${myAwards}">
                                <tr>
                                    <td>${award.tenderTitle}</td>
                                    <td>${award.tenderRefNumber}</td>
                                    <td><strong>${award.supplierName}</strong></td>
                                    <td>M <fmt:formatNumber value="${award.awardedValue}" pattern="#,##0.00"/></td>
                                    <td>${award.justification}</td>
                                    <td><fmt:formatDate value="${award.awardedAt}" pattern="dd MMM yyyy"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${award.winningSupplierId == supplierId}"><span class="badge badge-Won">Won</span></c:when>
                                            <c:otherwise><span class="badge badge-Not-Won">Not Won</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myAwards}">
                                <tr><td colspan="7" class="text-center text-muted">No award notices yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

</body>
</html>
