<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .criteria-row { border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; margin-bottom: 12px; background: #fafafa; }
        .criteria-row .form-group { margin-bottom: 8px; }
        .criteria-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; flex-wrap: wrap; gap: 8px; }
        .criteria-header h4 { margin: 0; font-size: 0.95rem; color: #555; }
        .btn-remove-criteria { background: #dc3545; color: white; border: none; border-radius: 4px; padding: 4px 10px; cursor: pointer; font-size: 0.8rem; }
        .btn-remove-criteria:hover { background: #c82333; }
        .criterion-type-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
        @media (max-width: 480px) {
            .criteria-row { padding: 12px; }
            .criteria-header { flex-direction: column; align-items: flex-start; }
        }
        .criterion-type-badge.Equipment { background: #e3f2fd; color: #1565c0; }
        .criterion-type-badge.Certifications { background: #f3e5f5; color: #7b1fa2; }
        .criterion-type-badge.Experience { background: #e8f5e9; color: #2e7d32; }
        .criterion-type-badge.QualityStandards { background: #fff3e0; color: #e65100; }
        .criterion-type-badge.Methodology { background: #e0f2f1; color: #00695c; }
        .criterion-type-badge.Personnel { background: #fce4ec; color: #c62828; }
        .criterion-type-badge.Other { background: #f5f5f5; color: #616161; }
        .custom-name-field { display: none; margin-top: 6px; }
        .section-hint { color: #666; font-size: 0.85rem; margin-bottom: 12px; }
        .existing-evidence { font-size: 0.8rem; color: #17a2b8; margin-top: 4px; }
        .criteria-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; margin-top: 12px; }
        .criterion-card { background: #fff; border: 1px solid #e0e0e0; border-radius: 10px; padding: 16px; transition: box-shadow 0.2s, transform 0.15s; position: relative; overflow: hidden; }
        .criterion-card:hover { box-shadow: 0 4px 14px rgba(0,0,0,0.08); transform: translateY(-2px); }
        .criterion-card .card-type-strip { position: absolute; top: 0; left: 0; right: 0; height: 4px; }
        .criterion-card .card-type-strip.Equipment { background: #1565c0; }
        .criterion-card .card-type-strip.Certifications { background: #7b1fa2; }
        .criterion-card .card-type-strip.Experience { background: #2e7d32; }
        .criterion-card .card-type-strip.QualityStandards { background: #e65100; }
        .criterion-card .card-type-strip.Methodology { background: #00695c; }
        .criterion-card .card-type-strip.Personnel { background: #c62828; }
        .criterion-card .card-type-strip.Other { background: #757575; }
        .criterion-card .card-body { margin-top: 4px; }
        .criterion-card .card-body h5 { margin: 0 0 4px 0; font-size: 0.95rem; font-weight: 600; color: #333; }
        .criterion-card .card-body p { margin: 0 0 8px 0; color: #555; font-size: 0.88rem; line-height: 1.45; }
        .criterion-card .card-evidence { font-size: 0.8rem; color: #17a2b8; display: inline-flex; align-items: center; gap: 4px; text-decoration: none; }
        .criterion-card .card-evidence:hover { text-decoration: underline; }
        .bid-read-only { background: #f8f9fa; border-radius: 8px; padding: 16px; }
        .bid-read-only table { margin-bottom: 0; }
        .edit-toggle-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Tender Details</h2>
                <a href="${pageContext.request.contextPath}/supplier-dashboard" class="btn btn-outline">Back to Dashboard</a>
            </div>

            <c:if test="${param.success eq 'true'}">
                <div class="alert alert-success">Your bid has been submitted successfully!</div>
            </c:if>
            <c:if test="${param.success eq 'edit'}">
                <div class="alert alert-success">Your bid has been updated successfully!</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">
                    ${tender.referenceNumber} - ${tender.title}
                    <span class="badge badge-${fn:replace(tender.status, ' ', '-')}" style="float:right">${tender.status}</span>
                </div>
                <table>
                    <tr><th>Field</th><th>Value</th></tr>
                    <tr><td>Reference</td><td>${tender.referenceNumber}</td></tr>
                    <tr><td>Title</td><td>${tender.title}</td></tr>
                    <tr><td>Category</td><td>${tender.category}</td></tr>
                    <tr><td>Description</td><td>${tender.description}</td></tr>
                    <c:if test="${tender.showEstimatedValue}">
                        <tr><td>Estimated Value</td><td>M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></td></tr>
                    </c:if>
                    <tr><td>Deadline</td><td><fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></td></tr>
                    <tr><td>Notice Document</td>
                        <td><c:if test="${not empty tender.noticeDocumentPath}">
                            <a href="${pageContext.request.contextPath}/download?file=${tender.noticeDocumentPath}" class="btn btn-sm">Download PDF</a>
                        </c:if>
                        <c:if test="${empty tender.noticeDocumentPath}">No document uploaded</c:if>
                        </td>
                    </tr>
                </table>
            </div>

            <c:choose>
                <%-- Case 1: Tender is Open and supplier has NOT bid yet --%>
                <c:when test="${tender.status eq 'Open' && not hasBid}">
                    <div class="card">
                        <div class="card-header">Submit Your Bid</div>
                        <form action="${pageContext.request.contextPath}/submit-bid" method="POST" enctype="multipart/form-data" id="newBidForm">
                            <input type="hidden" name="tenderId" value="${tender.id}">

                            <div class="form-row" style="display:flex; gap:16px;">
                                <div class="form-group" style="flex:1;">
                                    <label>Bid Amount (Maloti) <span class="required">*</span></label>
                                    <input type="number" name="amount" class="form-control" step="0.01" min="1" required>
                                </div>
                                <div class="form-group" style="flex:1;">
                                    <label>Proposed Delivery Timeline (Days) <span class="required">*</span></label>
                                    <input type="number" name="timeline" class="form-control" min="1" required>
                                </div>
                            </div>

                            <div class="form-group">
                                <label>Technical Compliance Statement (Max 600 characters) <span class="required">*</span></label>
                                <textarea name="technicalCompliance" class="form-control" rows="4" maxlength="600" required></textarea>
                                <p class="form-hint">Provide a brief overview of how you comply with the tender requirements.</p>
                            </div>

                            <div class="form-group">
                                <label>Supporting Document (PDF or DOCX, max 10MB)</label>
                                <input type="file" name="document" class="form-control" accept=".pdf,.docx">
                                <p class="form-hint">Upload a supporting document in PDF or DOCX format.</p>
                            </div>

                            <!-- Structured Technical Compliance Criteria -->
                            <div style="margin-top: 20px; border: 1px solid #dee2e6; border-radius: 8px; overflow: hidden;">
                                <div class="card-header" style="background: #f8f9fa;">
                                    <div class="d-flex justify-content-between align-items-center" style="width: 100%;">
                                        <h4 style="margin: 0; font-size: 1rem; font-weight: 600;">Technical Compliance Details</h4>
                                        <button type="button" class="btn btn-sm btn-primary" onclick="addNewCriterion()">+ Add Criterion</button>
                                    </div>
                                </div>
                                <p class="section-hint" style="padding: 8px 16px 0;">Provide detailed technical compliance information. Select a criterion type, describe your compliance, and optionally attach evidence documents. The more detail you provide, the better evaluators can assess your bid.</p>

                                <div id="newCriteriaContainer" style="padding: 0 16px;"></div>

                                <div style="padding: 12px 16px;">
                                    <button type="button" class="btn btn-outline btn-sm" onclick="addNewCriterion()">+ Add Another Criterion</button>
                                </div>
                            </div>

                            <div class="d-flex justify-content-between" style="margin-top: 20px;">
                                <a href="${pageContext.request.contextPath}/supplier-dashboard" class="btn btn-outline">Cancel</a>
                                <button type="submit" class="btn btn-primary">Submit Sealed Bid</button>
                            </div>
                        </form>
                    </div>
                </c:when>

                <%-- Case 2: Supplier HAS bid on this tender --%>
                <c:when test="${hasBid}">
                    <div class="card">
                        <div class="card-header">
                            <div class="edit-toggle-bar">
                                <span>Your Bid</span>
                                <c:choose>
                                    <c:when test="${tender.status eq 'Open'}">
                                        <button type="button" class="btn btn-sm btn-outline" id="toggleEditBtn" onclick="toggleEdit()">Edit Bid</button>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-${fn:replace(tender.status, ' ', '-')}">${tender.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <%-- Read-only view (shown by default) --%>
                        <div id="bidReadOnly" class="bid-read-only">
                            <table>
                                <tr><th>Bid Amount</th><td>M <fmt:formatNumber value="${existingBid.amount}" pattern="#,##0.00"/></td></tr>
                                <tr><th>Proposed Timeline</th><td>${existingBid.proposedTimelineDays} days</td></tr>
                                <tr><th>Technical Compliance</th><td>${existingBid.technicalCompliance}</td></tr>
                                <tr><th>Supporting Document</th>
                                    <td><c:if test="${not empty existingBid.documentPath}">
                                        <a href="${pageContext.request.contextPath}/download?file=${existingBid.documentPath}" class="btn btn-sm">Download</a>
                                    </c:if>
                                    <c:if test="${empty existingBid.documentPath}">None</c:if>
                                    </td>
                                </tr>
                                <tr><th>Submitted</th><td><fmt:formatDate value="${existingBid.submittedAt}" pattern="dd MMM yyyy HH:mm"/></td></tr>
                            </table>

                            <c:if test="${not empty existingCriteria}">
                                <div style="margin-top: 20px;">
                                    <h4 style="font-size: 1rem; margin-bottom: 4px;">Technical Compliance Details</h4>
                                    <p style="font-size: 0.85rem; color: #888; margin-bottom: 12px;">Structured criteria submitted with your bid</p>
                                    <div class="criteria-grid">
                                        <c:forEach var="crit" items="${existingCriteria}">
                                            <div class="criterion-card">
                                                <div class="card-type-strip ${crit.criterionType}"></div>
                                                <div class="card-body">
                                                    <span class="criterion-type-badge ${crit.criterionType}">${crit.criterionType}</span>
                                                    <h5>${crit.criterionName}</h5>
                                                    <p>${crit.criterionValue}</p>
                                                    <c:if test="${not empty crit.evidenceDocumentPath}">
                                                        <a href="${pageContext.request.contextPath}/download?file=${crit.evidenceDocumentPath}" class="card-evidence">&#128206; View Evidence</a>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${tender.status eq 'Closed'}">
                                <div style="margin-top: 12px; padding: 10px 14px; background: #f8f9fa; border-radius: 6px; border-left: 3px solid #6c757d;">
                                    <strong style="color: #495057;">Awaiting Evaluation</strong>
                                    <p style="margin: 2px 0 0; color: #6c757d; font-size: 0.88rem;">This tender has closed. Your bid is being reviewed by the evaluation committee.</p>
                                </div>
                            </c:if>
                            <c:if test="${tender.status eq 'Under Evaluation'}">
                                <div style="margin-top: 12px; padding: 10px 14px; background: #fff8e1; border-radius: 6px; border-left: 3px solid #ffc107;">
                                    <strong style="color: #f57f17;">Under Evaluation</strong>
                                    <p style="margin: 2px 0 0; color: #795548; font-size: 0.88rem;">The evaluation committee is currently scoring bids. You will be notified of the outcome.</p>
                                </div>
                            </c:if>
                            <c:if test="${tender.status eq 'Evaluated'}">
                                <div style="margin-top: 12px; padding: 10px 14px; background: #e8f5e9; border-radius: 6px; border-left: 3px solid #4caf50;">
                                    <strong style="color: #2e7d32;">Evaluation Complete</strong>
                                    <p style="margin: 2px 0 0; color: #388e3c; font-size: 0.88rem;">All evaluations have been submitted. Awaiting award decision from the procurement officer.</p>
                                </div>
                            </c:if>
                        </div>

                        <%-- Editable form (hidden by default, only for Open tenders) --%>
                        <c:if test="${tender.status eq 'Open'}">
                        <div id="bidEditForm" style="display: none;">
                            <form action="${pageContext.request.contextPath}/tender-detail" method="POST" enctype="multipart/form-data" id="bidEditFormEl">
                                <input type="hidden" name="tenderId" value="${tender.id}">

                                <div class="form-row" style="display:flex; gap:16px;">
                                    <div class="form-group" style="flex:1;">
                                        <label>Bid Amount (Maloti) <span class="required">*</span></label>
                                        <input type="number" name="amount" class="form-control" step="0.01" min="1" value="${existingBid.amount}" required>
                                    </div>
                                    <div class="form-group" style="flex:1;">
                                        <label>Proposed Delivery Timeline (Days) <span class="required">*</span></label>
                                        <input type="number" name="timeline" class="form-control" min="1" value="${existingBid.proposedTimelineDays}" required>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label>Technical Compliance Statement (Max 600 characters) <span class="required">*</span></label>
                                    <textarea name="technicalCompliance" class="form-control" rows="4" maxlength="600" required>${existingBid.technicalCompliance}</textarea>
                                    <p class="form-hint">Provide a brief overview of how you comply with the tender requirements.</p>
                                </div>

                                <div class="form-group">
                                    <label>Supporting Document (PDF or DOCX, max 10MB)</label>
                                    <input type="file" name="document" class="form-control" accept=".pdf,.docx">
                                    <c:if test="${not empty existingBid.documentPath}">
                                        <p class="existing-evidence">Current: <a href="${pageContext.request.contextPath}/download?file=${existingBid.documentPath}" target="_blank">${existingBid.documentPath}</a> — upload a new file to replace.</p>
                                    </c:if>
                                </div>

                                <!-- Structured Technical Compliance Criteria -->
                                <div style="margin-top: 20px; border: 1px solid #dee2e6; border-radius: 8px; overflow: hidden;">
                                    <div class="card-header" style="background: #f8f9fa;">
                                        <div class="d-flex justify-content-between align-items-center" style="width: 100%;">
                                            <h4 style="margin: 0; font-size: 1rem; font-weight: 600;">Technical Compliance Details</h4>
                                            <button type="button" class="btn btn-sm btn-primary" onclick="addCriterion()">+ Add Criterion</button>
                                        </div>
                                    </div>
                                    <p class="section-hint" style="padding: 8px 16px 0;">Modify your technical compliance criteria. Upload new evidence to replace existing files.</p>

                                    <div id="criteriaContainer" style="padding: 0 16px;">
                                        <c:forEach var="crit" items="${existingCriteria}" varStatus="loop">
                                            <div class="criteria-row" id="criteriaRow_existing_${loop.index}">
                                                <div class="criteria-header">
                                                    <h4><span class="criterion-type-badge ${crit.criterionType}">${crit.criterionType}</span> ${crit.criterionName}</h4>
                                                    <button type="button" class="btn-remove-criteria" onclick="removeCriterion('criteriaRow_existing_${loop.index}')">Remove</button>
                                                </div>
                                                <input type="hidden" name="criterionType_${loop.index}" value="${crit.criterionType}">
                                                <input type="hidden" name="criterionName_${loop.index}" value="${crit.criterionName}">
                                                <div class="form-group">
                                                    <label>Compliance Details</label>
                                                    <textarea name="criterionValue_${loop.index}" class="form-control" rows="2" required>${crit.criterionValue}</textarea>
                                                </div>
                                                <div class="form-group">
                                                    <label>New Evidence Document (replaces existing)</label>
                                                    <input type="file" name="criterionEvidence_${loop.index}" class="form-control" accept=".pdf,.docx">
                                                    <c:if test="${not empty crit.evidenceDocumentPath}">
                                                        <p class="existing-evidence">Current: <a href="${pageContext.request.contextPath}/download?file=${crit.evidenceDocumentPath}" target="_blank">${crit.evidenceDocumentPath}</a></p>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>

                                    <div style="padding: 12px 16px;">
                                        <button type="button" class="btn btn-outline btn-sm" onclick="addCriterion()">+ Add New Criterion</button>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-between" style="margin-top: 20px;">
                                    <button type="button" class="btn btn-outline" onclick="toggleEdit()">Cancel</button>
                                    <button type="submit" class="btn btn-primary">Save Changes</button>
                                </div>
                            </form>
                        </div>
                        </c:if>
                    </div>
                </c:when>

                <%-- Case 3: Tender is not Open and supplier has not bid --%>
                <c:otherwise>
                    <div class="card">
                        <c:choose>
                            <c:when test="${tender.status eq 'Closed' or tender.status eq 'Under Evaluation' or tender.status eq 'Evaluated' or tender.status eq 'Awarded'}">
                                <div style="padding: 16px; text-align: center;">
                                    <span class="badge badge-${fn:replace(tender.status, ' ', '-')}" style="font-size: 0.9rem;">${tender.status}</span>
                                    <p style="margin-top: 12px; color: #666;">This tender is no longer accepting bids. You did not submit a bid for this tender.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted">This tender is not currently available for bidding (status: ${tender.status}).</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

<script>
    var criterionIndex = ${not empty existingCriteria ? fn:length(existingCriteria) : 0};
    var newCriterionIndex = 0;
    var criterionTypes = {
        'Equipment': { label: 'Equipment & Technology', placeholder: 'e.g. 2x 50-ton cranes, 1x pile driver' },
        'Certifications': { label: 'Certifications & Licences', placeholder: 'e.g. ISO 9001:2015 Certified' },
        'Experience': { label: 'Experience & Track Record', placeholder: 'e.g. 3 bridge projects completed in last 5 years' },
        'QualityStandards': { label: 'Quality Standards & QMS', placeholder: 'e.g. Full QMS with independent inspection regime' },
        'Methodology': { label: 'Methodology & Approach', placeholder: 'e.g. Incremental launch method with temporary falsework' },
        'Personnel': { label: 'Personnel & Qualifications', placeholder: 'e.g. 4 registered structural engineers, 2 site managers' },
        'Other': { label: 'Other (specify name)', placeholder: 'Describe your compliance for this criterion' }
    };

    function toggleEdit() {
        var readOnly = document.getElementById('bidReadOnly');
        var editForm = document.getElementById('bidEditForm');
        var toggleBtn = document.getElementById('toggleEditBtn');
        if (editForm.style.display === 'none') {
            readOnly.style.display = 'none';
            editForm.style.display = 'block';
            toggleBtn.textContent = 'Cancel Edit';
            toggleBtn.className = 'btn btn-sm btn-danger';
        } else {
            readOnly.style.display = 'block';
            editForm.style.display = 'none';
            toggleBtn.textContent = 'Edit Bid';
            toggleBtn.className = 'btn btn-sm btn-outline';
        }
    }

    function addCriterion() {
        addCriterionToContainer('criteriaContainer', 'criteriaRow_', criterionIndex++);
    }

    function addNewCriterion() {
        addCriterionToContainer('newCriteriaContainer', 'newCriteriaRow_', newCriterionIndex++);
    }

    function addCriterionToContainer(containerId, rowPrefix, idx) {
        var container = document.getElementById(containerId);

        var row = document.createElement('div');
        row.className = 'criteria-row';
        row.id = rowPrefix + idx;

        var typeOptions = '';
        for (var key in criterionTypes) {
            typeOptions += '<option value="' + key + '">' + criterionTypes[key].label + '</option>';
        }

        row.innerHTML =
            '<div class="criteria-header">' +
                '<h4><span class="criterion-type-badge" id="badge_' + rowPrefix + idx + '">New Criterion</span></h4>' +
                '<button type="button" class="btn-remove-criteria" onclick="removeCriterion(\'' + rowPrefix + idx + '\')">Remove</button>' +
            '</div>' +
            '<div class="form-group">' +
                '<label>Criterion Type <span class="required">*</span></label>' +
                '<select name="criterionType_' + idx + '" class="form-control" onchange="onTypeChange(\'' + rowPrefix + idx + '\', this.value, ' + idx + ')" required>' +
                    '<option value="">-- Select criterion type --</option>' +
                    typeOptions +
                '</select>' +
            '</div>' +
            '<div class="form-group custom-name-field" id="customNameField_' + rowPrefix + idx + '">' +
                '<label>Custom Criterion Name <span class="required">*</span></label>' +
                '<input type="text" name="criterionName_' + idx + '" class="form-control" placeholder="e.g. Environmental Compliance">' +
            '</div>' +
            '<div class="form-group">' +
                '<label>Compliance Details <span class="required">*</span></label>' +
                '<textarea name="criterionValue_' + idx + '" class="form-control" rows="2" placeholder="Describe how you meet this criterion..." required></textarea>' +
            '</div>' +
            '<div class="form-group">' +
                '<label>Evidence Document (PDF or DOCX, max 10MB) <span style="color:#888; font-weight:normal;">- Optional</span></label>' +
                '<input type="file" name="criterionEvidence_' + idx + '" class="form-control" accept=".pdf,.docx">' +
            '</div>';

        container.appendChild(row);
    }

    function onTypeChange(rowId, value, idx) {
        var customField = document.getElementById('customNameField_' + rowId);
        var badge = document.getElementById('badge_' + rowId);
        var textarea = document.querySelector('#' + rowId + ' textarea[name="criterionValue_' + idx + '"]');

        if (value === 'Other') {
            customField.style.display = 'block';
        } else {
            customField.style.display = 'none';
        }

        if (value && criterionTypes[value]) {
            badge.textContent = criterionTypes[value].label;
            badge.className = 'criterion-type-badge ' + value;
        }

        if (value && criterionTypes[value] && textarea) {
            textarea.placeholder = criterionTypes[value].placeholder;
        }
    }

    function removeCriterion(elementId) {
        var row = document.getElementById(elementId);
        if (row) row.remove();
    }

    // Auto-start: add one criterion row to the new bid form if it exists
    var newContainer = document.getElementById('newCriteriaContainer');
    if (newContainer) {
        addNewCriterion();
    }
</script>
</body>
</html>
