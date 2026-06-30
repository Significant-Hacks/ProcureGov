<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .bid-status-chip { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 12px; font-size: 0.78rem; font-weight: 600; }
        .bid-status-chip.submitted { background: #e8f5e9; color: #2e7d32; }
        .bid-status-chip.not-submitted { background: #fff3e0; color: #e65100; }
        .tab-nav { display: flex; gap: 0; border-bottom: 2px solid #dee2e6; margin-bottom: 16px; flex-wrap: wrap; }
        .tab-btn { padding: 10px 24px; border: none; background: transparent; font-size: 0.95rem; font-weight: 600; color: #666; cursor: pointer; border-bottom: 3px solid transparent; margin-bottom: -2px; transition: color 0.2s, border-color 0.2s; white-space: nowrap; }
        .tab-btn:hover { color: #1B4332; }
        .tab-btn.active { color: #1B4332; border-bottom-color: #1B4332; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        @media (max-width: 480px) {
            .tab-btn { padding: 8px 14px; font-size: 0.85rem; }
        }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <h2 class="mb-4">Tenders</h2>

            <div class="card">
                <div class="card-header">Browse & Bid on Tenders</div>
                <div class="tab-nav">
                    <button class="tab-btn active" onclick="switchTab('open')">Open Tenders</button>
                    <button class="tab-btn" onclick="switchTab('closed')">Closed Tenders</button>
                </div>

                <!-- Open Tenders Tab -->
                <div id="tab-open" class="tab-content active">
                    <p class="text-muted" style="margin-bottom: 12px;">All currently open tenders. Submit your bid or view/edit an existing one.</p>
                    <div class="table-responsive">
                        <table>
                            <thead>
                                <tr>
                                    <th>Reference</th>
                                    <th>Title</th>
                                    <th>Category</th>
                                    <th>Est. Value</th>
                                    <th>Deadline</th>
                                    <th>Your Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="tender" items="${openTenders}">
                                    <tr>
                                        <td>${tender.referenceNumber}</td>
                                        <td>${tender.title}</td>
                                        <td>${tender.category}</td>
                                        <td><c:choose><c:when test="${tender.showEstimatedValue}">M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></c:when><c:otherwise><span class="text-muted">Not disclosed</span></c:otherwise></c:choose></td>
                                        <td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${openTenderBidStatus[tender.id]}">
                                                    <span class="bid-status-chip submitted">Bid Submitted</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="bid-status-chip not-submitted">Not Yet Bid</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${openTenderBidStatus[tender.id]}">
                                                    <a href="${pageContext.request.contextPath}/tender-detail?id=${tender.id}" class="btn btn-sm btn-outline">View / Edit Bid</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/tender-detail?id=${tender.id}" class="btn btn-sm btn-primary">View & Bid</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty openTenders}">
                                    <tr><td colspan="7" class="text-center text-muted">No open tenders available at this time.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Closed Tenders Tab -->
                <div id="tab-closed" class="tab-content">
                    <p class="text-muted" style="margin-bottom: 12px;">Closed tenders you submitted a bid for. Awaiting evaluation or award decision.</p>
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
                                <c:forEach var="tender" items="${closedTendersBidOn}">
                                    <tr>
                                        <td>${tender.referenceNumber}</td>
                                        <td>${tender.title}</td>
                                        <td>${tender.category}</td>
                                        <td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td>
                                        <td><span class="badge badge-${fn:replace(tender.status, ' ', '-')}">${tender.status}</span></td>
                                        <td><a href="${pageContext.request.contextPath}/tender-detail?id=${tender.id}" class="btn btn-sm btn-outline">View Bid</a></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty closedTendersBidOn}">
                                    <tr><td colspan="6" class="text-center text-muted">No closed tenders with your bids.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

<script>
    function switchTab(tabName) {
        var contents = document.querySelectorAll('.tab-content');
        for (var i = 0; i < contents.length; i++) contents[i].classList.remove('active');
        var btns = document.querySelectorAll('.tab-btn');
        for (var i = 0; i < btns.length; i++) btns[i].classList.remove('active');
        document.getElementById('tab-' + tabName).classList.add('active');
        for (var i = 0; i < btns.length; i++) {
            if (btns[i].textContent.toLowerCase().indexOf(tabName) !== -1) btns[i].classList.add('active');
        }
    }
</script>
</body>
</html>
