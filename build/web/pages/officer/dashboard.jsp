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
        .stat-card { background: var(--card-bg); border-radius: var(--radius-lg); padding: 20px; box-shadow: var(--shadow); border-left: 4px solid var(--border-color); transition: var(--transition); position: relative; overflow: hidden; }
        .stat-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }
        .stat-card .stat-icon { font-size: 28px; margin-bottom: 8px; opacity: 0.7; }
        .stat-card .stat-value { font-size: 32px; font-weight: 800; color: var(--primary-color); line-height: 1; }
        .stat-card .stat-label { font-size: 13px; color: var(--text-muted); margin-top: 4px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-card.stat-total { border-left-color: var(--primary-color); }
        .stat-card.stat-draft { border-left-color: #6b7280; }
        .stat-card.stat-closed { border-left-color: #d97706; }
        .stat-card.stat-evaluation { border-left-color: #2563eb; }
        .stat-card.stat-evaluated { border-left-color: #7c3aed; }
        .stat-card.stat-awarded { border-left-color: var(--accent-color); }
        .stat-card.stat-draft .stat-value { color: #6b7280; }
        .stat-card.stat-open .stat-value { color: #16a34a; }
        .stat-card.stat-closed .stat-value { color: #d97706; }
        .stat-card.stat-evaluation .stat-value { color: #2563eb; }
        .stat-card.stat-evaluated .stat-value { color: #7c3aed; }
        .stat-card.stat-awarded .stat-value { color: var(--accent-color); }

        .action-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .action-card { background: var(--card-bg); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow); transition: var(--transition); cursor: pointer; text-decoration: none; color: inherit; display: block; }
        .action-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); text-decoration: none; color: inherit; }
        .action-card .action-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-bottom: 14px; }
        .action-card .action-title { font-size: 16px; font-weight: 700; color: var(--text-dark); margin-bottom: 4px; }
        .action-card .action-desc { font-size: 13px; color: var(--text-muted); line-height: 1.4; }
        .action-icon.icon-create { background: #dcfce7; color: #16a34a; }
        .action-icon.icon-manage { background: #dbeafe; color: #2563eb; }
        .action-icon.icon-evaluate { background: #f3e8ff; color: #7c3aed; }
        .action-icon.icon-award { background: #fef3c7; color: #d97706; }
        .action-icon.icon-report { background: #fce7f3; color: #db2777; }
        .action-icon.icon-profile { background: #e0f2fe; color: #0284c7; }

        .attention-section { margin-bottom: 28px; }
        .attention-section h3 { font-size: 18px; font-weight: 700; color: var(--primary-color); margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
        .attention-section h3 .attn-badge { background: #fef3c7; color: #92400e; font-size: 12px; padding: 2px 10px; border-radius: 20px; font-weight: 700; }
        .attn-list { display: flex; flex-direction: column; gap: 8px; }
        .attn-item { display: flex; align-items: center; justify-content: space-between; background: var(--card-bg); border-radius: var(--radius); padding: 12px 16px; box-shadow: var(--shadow); border-left: 3px solid var(--accent-color); }
        .attn-item.attn-draft { border-left-color: #6b7280; }
        .attn-item.attn-closed { border-left-color: #d97706; }
        .attn-item.attn-evaluated { border-left-color: #7c3aed; }
        .attn-item .attn-info { flex: 1; }
        .attn-item .attn-ref { font-weight: 700; font-size: 14px; color: var(--text-dark); }
        .attn-item .attn-title { font-size: 13px; color: var(--text-muted); }
        .attn-item .attn-status { margin: 0 12px; }
        .attn-item .attn-action { white-space: nowrap; }

        .recent-table { width: 100%; }
        .recent-table td, .recent-table th { padding: 10px 12px; font-size: 13px; }
        .recent-empty { text-align: center; color: var(--text-muted); padding: 24px; font-style: italic; }
        @media (max-width: 768px) {
            .dash-grid { grid-template-columns: repeat(2, 1fr); }
            .action-grid { grid-template-columns: 1fr; }
            .attn-item { flex-direction: column; align-items: flex-start; gap: 8px; }
            .attn-item .attn-status { margin: 0; }
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
            <h2 class="mb-4">Procurement Officer Dashboard</h2>

            <!-- Stats Overview -->
            <div class="dash-grid">
                <div class="stat-card stat-total">
                    <div class="stat-icon">&#128202;</div>
                    <div class="stat-value">${totalTenders}</div>
                    <div class="stat-label">Total Tenders</div>
                </div>
                <div class="stat-card stat-draft">
                    <div class="stat-icon">&#9997;</div>
                    <div class="stat-value">${draftCount}</div>
                    <div class="stat-label">Drafts</div>
                </div>
                <div class="stat-card stat-open">
                    <div class="stat-icon">&#128194;</div>
                    <div class="stat-value">${openCount}</div>
                    <div class="stat-label">Open</div>
                </div>
                <div class="stat-card stat-closed">
                    <div class="stat-icon">&#128274;</div>
                    <div class="stat-value">${closedCount}</div>
                    <div class="stat-label">Closed</div>
                </div>
                <div class="stat-card stat-evaluation">
                    <div class="stat-icon">&#128270;</div>
                    <div class="stat-value">${evaluationCount}</div>
                    <div class="stat-label">Under Evaluation</div>
                </div>
                <div class="stat-card stat-evaluated">
                    <div class="stat-icon">&#9989;</div>
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
                <a href="${pageContext.request.contextPath}/create-tender" class="action-card">
                    <div class="action-icon icon-create">&#10010;</div>
                    <div class="action-title">Create Tender</div>
                    <div class="action-desc">Draft a new procurement tender with criteria and deadlines.</div>
                </a>
                <a href="${pageContext.request.contextPath}/manage-tenders" class="action-card">
                    <div class="action-icon icon-manage">&#9776;</div>
                    <div class="action-title">Manage Tenders</div>
                    <div class="action-desc">View all tenders, change statuses, and manage the procurement lifecycle.</div>
                </a>
                <a href="${pageContext.request.contextPath}/evaluator-dashboard" class="action-card">
                    <div class="action-icon icon-evaluate">&#9733;</div>
                    <div class="action-title">Evaluation Panel</div>
                    <div class="action-desc">Score bids for tenders under evaluation as a committee member.</div>
                </a>
                <c:if test="${not empty evaluatedTenders}">
                <a href="${pageContext.request.contextPath}/manage-tenders" class="action-card">
                    <div class="action-icon icon-award">&#127942;</div>
                    <div class="action-title">Award Contracts</div>
                    <div class="action-desc">${evaluatedCount} tender(s) evaluated and ready for contract award.</div>
                </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/profile" class="action-card">
                    <div class="action-icon icon-profile">&#128100;</div>
                    <div class="action-title">My Profile</div>
                    <div class="action-desc">View and update your account settings and password.</div>
                </a>
            </div>

            <!-- Items Needing Attention -->
            <c:if test="${not empty draftTenders or not empty closedTenders or not empty evaluatedTenders}">
            <div class="attention-section">
                <h3>&#9888; Items Needing Attention <span class="attn-badge">${draftTenders.size() + closedTenders.size() + evaluatedTenders.size()} pending</span></h3>
                <div class="attn-list">
                    <c:forEach var="t" items="${draftTenders}">
                        <div class="attn-item attn-draft">
                            <div class="attn-info">
                                <div class="attn-ref">${t.referenceNumber}</div>
                                <div class="attn-title">${t.title}</div>
                            </div>
                            <span class="badge badge-Draft attn-status">Draft</span>
                            <div class="attn-action">
                                <a href="${pageContext.request.contextPath}/tender-status?id=${t.id}" class="btn btn-sm">Edit &amp; Publish</a>
                            </div>
                        </div>
                    </c:forEach>
                    <c:forEach var="t" items="${closedTenders}">
                        <div class="attn-item attn-closed">
                            <div class="attn-info">
                                <div class="attn-ref">${t.referenceNumber}</div>
                                <div class="attn-title">${t.title}</div>
                            </div>
                            <span class="badge badge-Closed attn-status">Closed</span>
                            <div class="attn-action">
                                <a href="${pageContext.request.contextPath}/tender-status?id=${t.id}" class="btn btn-sm btn-primary">Start Evaluation</a>
                            </div>
                        </div>
                    </c:forEach>
                    <c:forEach var="t" items="${evaluatedTenders}">
                        <div class="attn-item attn-evaluated">
                            <div class="attn-info">
                                <div class="attn-ref">${t.referenceNumber}</div>
                                <div class="attn-title">${t.title}</div>
                            </div>
                            <span class="badge badge-Evaluated attn-status">Evaluated</span>
                            <div class="attn-action">
                                <a href="${pageContext.request.contextPath}/award-contract?id=${t.id}" class="btn btn-sm btn-success">Award Contract</a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
            </c:if>

            <!-- Recent Tenders -->
            <div class="card">
                <div class="card-header">Recent Tenders</div>
                <c:choose>
                    <c:when test="${not empty recentTenders}">
                        <div class="table-responsive">
                            <table class="recent-table">
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
                                    <c:forEach var="tender" items="${recentTenders}">
                                        <tr>
                                            <td>${tender.referenceNumber}</td>
                                            <td>${tender.title}</td>
                                            <td>${tender.category}</td>
                                            <td>M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></td>
                                            <td><span class="badge badge-${fn:replace(tender.status, ' ', '-')}">${tender.status}</span></td>
                                            <td><a href="${pageContext.request.contextPath}/tender-status?id=${tender.id}" class="btn btn-sm">Manage</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="recent-empty">No tenders yet. Create your first tender to get started!</p>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>