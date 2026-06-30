<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .dash-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card { background: var(--card-bg); border-radius: var(--radius-lg); padding: 20px; box-shadow: var(--shadow); border-left: 4px solid var(--border-color); transition: var(--transition); }
        .stat-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }
        .stat-card .stat-icon { font-size: 28px; margin-bottom: 8px; opacity: 0.7; }
        .stat-card .stat-value { font-size: 32px; font-weight: 800; line-height: 1; }
        .stat-card .stat-label { font-size: 13px; color: var(--text-muted); margin-top: 4px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-card.stat-assigned { border-left-color: var(--primary-color); }
        .stat-card.stat-assigned .stat-value { color: var(--primary-color); }
        .stat-card.stat-pending { border-left-color: #d97706; }
        .stat-card.stat-pending .stat-value { color: #d97706; }
        .stat-card.stat-eval { border-left-color: #2563eb; }
        .stat-card.stat-eval .stat-value { color: #2563eb; }
        .stat-card.stat-completed { border-left-color: #16a34a; }
        .stat-card.stat-completed .stat-value { color: #16a34a; }
        .stat-card.stat-evaluated { border-left-color: #7c3aed; }
        .stat-card.stat-evaluated .stat-value { color: #7c3aed; }
        .stat-card.stat-awarded { border-left-color: var(--accent-color); }
        .stat-card.stat-awarded .stat-value { color: var(--accent-color); }

        .action-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .action-card { background: var(--card-bg); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow); transition: var(--transition); cursor: pointer; text-decoration: none; color: inherit; display: block; }
        .action-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); text-decoration: none; color: inherit; }
        .action-card .action-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-bottom: 14px; }
        .action-card .action-title { font-size: 16px; font-weight: 700; color: var(--text-dark); margin-bottom: 4px; }
        .action-card .action-desc { font-size: 13px; color: var(--text-muted); line-height: 1.4; }
        .action-icon.icon-score { background: #dbeafe; color: #2563eb; }
        .action-icon.icon-pending { background: #fef3c7; color: #d97706; }
        .action-icon.icon-results { background: #f3e8ff; color: #7c3aed; }
        .action-icon.icon-awarded { background: #dcfce7; color: #16a34a; }
        .action-icon.icon-profile { background: #e0f2fe; color: #0284c7; }

        .attention-section { margin-bottom: 28px; }
        .attention-section h3 { font-size: 18px; font-weight: 700; color: var(--primary-color); margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
        .attention-section h3 .attn-badge { background: #fef3c7; color: #92400e; font-size: 12px; padding: 2px 10px; border-radius: 20px; font-weight: 700; }
        .attn-list { display: flex; flex-direction: column; gap: 8px; }
        .attn-item { display: flex; align-items: center; justify-content: space-between; background: var(--card-bg); border-radius: var(--radius); padding: 12px 16px; box-shadow: var(--shadow); border-left: 3px solid #d97706; }
        .attn-item.attn-start { border-left-color: #d97706; }
        .attn-item.attn-continue { border-left-color: #2563eb; }
        .attn-item .attn-info { flex: 1; }
        .attn-item .attn-ref { font-weight: 700; font-size: 14px; color: var(--text-dark); }
        .attn-item .attn-title { font-size: 13px; color: var(--text-muted); }
        .attn-item .attn-progress { margin: 0 12px; font-size: 13px; color: var(--text-muted); }
        .attn-item .attn-action { white-space: nowrap; }

        .recent-table { width: 100%; }
        .recent-table td, .recent-table th { padding: 10px 12px; font-size: 13px; }
        .recent-empty { text-align: center; color: var(--text-muted); padding: 24px; font-style: italic; }

        @media (max-width: 768px) {
            .dash-grid { grid-template-columns: repeat(2, 1fr); }
            .action-grid { grid-template-columns: 1fr; }
            .attn-item { flex-direction: column; align-items: flex-start; gap: 8px; }
            .attn-item .attn-progress { margin: 0; }
        }
        @media (max-width: 480px) {
            .dash-grid { grid-template-columns: 1fr 1fr; gap: 10px; }
            .stat-card .stat-value { font-size: 24px; }
            .stat-card .stat-icon { font-size: 22px; }
            .stat-card { padding: 14px; }
            .action-grid { grid-template-columns: 1fr; }
            .action-card { padding: 16px; }
            .action-card .action-icon { width: 40px; height: 40px; font-size: 18px; }
        }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <h2 class="mb-4">Evaluation Committee Dashboard</h2>

            <!-- Stats Overview -->
            <div class="dash-grid">
                <div class="stat-card stat-assigned">
                    <div class="stat-icon">&#128203;</div>
                    <div class="stat-value">${totalAssigned}</div>
                    <div class="stat-label">Assigned Tenders</div>
                </div>
                <div class="stat-card stat-pending">
                    <div class="stat-icon">&#9203;</div>
                    <div class="stat-value">${pendingCount}</div>
                    <div class="stat-label">Pending Scoring</div>
                </div>
                <div class="stat-card stat-eval">
                    <div class="stat-icon">&#128270;</div>
                    <div class="stat-value">${underEvalCount}</div>
                    <div class="stat-label">Under Evaluation</div>
                </div>
                <div class="stat-card stat-completed">
                    <div class="stat-icon">&#9989;</div>
                    <div class="stat-value">${completedCount}</div>
                    <div class="stat-label">Scores Submitted</div>
                </div>
                <div class="stat-card stat-evaluated">
                    <div class="stat-icon">&#128202;</div>
                    <div class="stat-value">${evaluatedCount}</div>
                    <div class="stat-label">Evaluated</div>
                </div>
                <div class="stat-card stat-awarded">
                    <div class="stat-icon">&#127942;</div>
                    <div class="stat-value">${awardedCount}</div>
                    <div class="stat-label">Awarded</div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="action-grid">
                <c:if test="${not empty underEvaluationTenders}">
                <a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${underEvaluationTenders[0].id}" class="action-card">
                    <div class="action-icon icon-score">&#9733;</div>
                    <div class="action-title">Score Bids</div>
                    <div class="action-desc">${underEvalCount} tender(s) under evaluation. Start scoring submitted bids now.</div>
                </a>
                </c:if>
                <c:if test="${empty underEvaluationTenders}">
                <a href="${pageContext.request.contextPath}/evaluations" class="action-card" style="opacity:0.6;cursor:default;" onclick="event.preventDefault();">
                    <div class="action-icon icon-score">&#9733;</div>
                    <div class="action-title">Score Bids</div>
                    <div class="action-desc">No tenders currently under evaluation.</div>
                </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/evaluations" class="action-card">
                    <div class="action-icon icon-pending">&#9203;</div>
                    <div class="action-title">Pending Evaluations</div>
                    <div class="action-desc">${pendingCount} tender(s) awaiting your scores. Complete your evaluations.</div>
                </a>
                <c:if test="${not empty evaluatedTenders}">
                <a href="${pageContext.request.contextPath}/evaluations" class="action-card">
                    <div class="action-icon icon-results">&#128202;</div>
                    <div class="action-title">Consolidated Results</div>
                    <div class="action-desc">${evaluatedCount} tender(s) with final ranked results ready for review.</div>
                </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/profile" class="action-card">
                    <div class="action-icon icon-profile">&#128100;</div>
                    <div class="action-title">My Profile</div>
                    <div class="action-desc">View and update your account settings and password.</div>
                </a>
            </div>

            <!-- Pending Evaluations - Items Needing Attention -->
            <c:if test="${not empty pendingEvaluations}">
            <div class="attention-section">
                <h3>&#9888; Pending Evaluations <span class="attn-badge">${pendingCount} awaiting</span></h3>
                <div class="attn-list">
                    <c:forEach var="pending" items="${pendingEvaluations}">
                        <div class="attn-item ${pending.technicalScore == 0 ? 'attn-start' : 'attn-continue'}">
                            <div class="attn-info">
                                <div class="attn-ref">${pending.tenderRefNumber}</div>
                                <div class="attn-title">${pending.tenderTitle}</div>
                            </div>
                            <div class="attn-progress">
                                <c:choose>
                                    <c:when test="${pending.technicalScore == 0}">Not started</c:when>
                                    <c:otherwise>${pending.technicalScore} / ${pending.bidAmount} scored</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="attn-action">
                                <a href="${pageContext.request.contextPath}/evaluation-panel?tenderId=${pending.tenderId}" class="btn btn-sm ${pending.technicalScore == 0 ? 'btn-primary' : ''}">
                                    <c:choose><c:when test="${pending.technicalScore == 0}">Start Evaluation</c:when><c:otherwise>Continue Scoring</c:otherwise></c:choose>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
            </c:if>

            <!-- Tenders Under Evaluation -->
            <c:if test="${not empty underEvaluationTenders}">
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
                        </tbody>
                    </table>
                </div>
            </div>
            </c:if>

            <!-- Completed Evaluations -->
            <div class="card">
                <div class="card-header">My Completed Scores</div>
                <c:choose>
                    <c:when test="${not empty myEvaluations}">
                        <div class="table-responsive">
                            <table class="recent-table">
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
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="recent-empty">No completed evaluations yet.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Evaluated Tenders - Consolidated Results -->
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
