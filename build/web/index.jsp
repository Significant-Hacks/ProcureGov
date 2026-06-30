<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
</head>
<body>
<div class="page-wrapper">
    <jsp:include page="/WEB-INF/fragments/navbar.jsp"/>

    <section class="hero">
        <p class="hero-subtitle">Kingdom of Lesotho Ministry of Public Works</p>
        <h1>ProcureGov</h1>
        <p>Tender Management System &mdash; Transparent, Efficient Public Procurement</p>
        <div class="hero-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.userId}">
                    <a href="${pageContext.request.contextPath}/${sessionScope.userRole eq 'OFFICER' ? 'officer-dashboard' : (sessionScope.userRole eq 'SUPPLIER' ? 'supplier-dashboard' : 'evaluator-dashboard')}" class="btn btn-gold btn-lg">Go to Dashboard</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-gold btn-lg">Login</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-outline btn-lg" style="color:#fff;border-color:rgba(255,255,255,0.5);">Register as Supplier</a>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <main class="main-content">
        <div class="container">
            <div class="card">
                <div class="card-header">About ProcureGov</div>
                <p>ProcureGov is the official tender management system for the Kingdom of Lesotho Ministry of Public Works. We provide a transparent, efficient platform for managing public procurement processes.</p>
                <p><strong>For Officials:</strong> Create tenders, manage the procurement lifecycle, and award contracts to qualified suppliers.</p>
                <p><strong>For Suppliers:</strong> Browse open tenders, submit competitive bids, and track the status of your submissions.</p>
                <p><strong>For Evaluators:</strong> Review bid submissions, score based on technical merit, price, and timeline, and ensure fair evaluation.</p>
            </div>

            <div class="card">
                <div class="card-header">Public Tender Information</div>
                <p class="text-muted">All active tenders are publicly visible. Registered suppliers can submit bids before the deadline.</p>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr><th>Stage</th><th>Description</th><th>Visibility</th></tr>
                        </thead>
                        <tbody>
                            <tr><td><span class="badge badge-open">Open</span></td><td>Tender is accepting bids from registered suppliers</td><td>Public</td></tr>
                            <tr><td><span class="badge badge-closed">Closed</span></td><td>Bid submission deadline has passed</td><td>Public</td></tr>
                            <tr><td><span class="badge badge-evaluation">Under Evaluation</span></td><td>Evaluators are reviewing and scoring bids</td><td>Public</td></tr>
                            <tr><td><span class="badge badge-awarded">Awarded</span></td><td>Contract has been awarded to winning supplier</td><td>Public</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card">
                <div class="card-header">How to Participate</div>
                <div class="steps">
                    <div class="step">
                        <div class="step-number">1</div>
                        <div class="step-content">
                            <h3>Register Your Company</h3>
                            <p>Create a supplier account with your company details, registration number, and contact information.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">2</div>
                        <div class="step-content">
                            <h3>Browse Open Tenders</h3>
                            <p>Review available tenders that match your expertise. Check requirements, deadlines, and estimated values.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">3</div>
                        <div class="step-content">
                            <h3>Submit Your Bid</h3>
                            <p>Prepare and submit your bid with technical proposals, pricing, and timeline before the deadline.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">4</div>
                        <div class="step-content">
                            <h3>Track Your Status</h3>
                            <p>Monitor your bid status through the evaluation process. Winners are notified via email.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
</div>
</body>
</html>