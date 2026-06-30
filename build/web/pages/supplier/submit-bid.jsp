<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
        .criteria-actions { margin-top: 12px; }
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
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Submit Bid</h2>
                <a href="${pageContext.request.contextPath}/tender-detail?id=${tender.id}" class="btn btn-outline">Back to Tender</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">Bidding on: ${tender.referenceNumber} - ${tender.title}</div>
                <p><c:if test="${tender.showEstimatedValue}">Estimated Value: M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/> |</c:if>
                   Deadline: <fmt:formatDate value="${tender.deadline}" pattern="dd MMM yyyy HH:mm"/></p>
            </div>

            <div class="card">
                <div class="card-header">Bid Details</div>
                <form action="${pageContext.request.contextPath}/submit-bid" method="POST" enctype="multipart/form-data" id="bidForm">
                    <input type="hidden" name="tenderId" value="${tender.id}">

                    <div class="form-group">
                        <label>Bid Amount (Maloti) <span class="required">*</span></label>
                        <input type="number" name="amount" class="form-control" step="0.01" min="1" required>
                    </div>

                    <div class="form-group">
                        <label>Technical Compliance Statement (Max 600 characters) <span class="required">*</span></label>
                        <textarea name="technicalCompliance" class="form-control" rows="4" maxlength="600" required></textarea>
                        <p class="form-hint">Provide a brief overview of how you comply with the tender requirements.</p>
                    </div>

                    <div class="form-group">
                        <label>Proposed Delivery Timeline (Days) <span class="required">*</span></label>
                        <input type="number" name="timeline" class="form-control" min="1" required>
                    </div>

                    <div class="form-group">
                        <label>Supporting Document (PDF or DOCX, max 10MB)</label>
                        <input type="file" name="document" class="form-control" accept=".pdf,.docx">
                        <p class="form-hint">Upload a supporting document in PDF or DOCX format.</p>
                    </div>

                    <!-- Structured Technical Compliance Criteria -->
                    <div class="card" style="margin-top: 20px;">
                        <div class="card-header">
                            <div class="d-flex justify-content-between align-items-center" style="width: 100%;">
                                <h4 style="margin: 0; font-size: 1rem; font-weight: 600;">Technical Compliance Details</h4>
                                <button type="button" class="btn btn-sm btn-primary" onclick="addCriterion()">+ Add Criterion</button>
                            </div>
                        </div>
                        <p class="section-hint">Provide detailed technical compliance information. Select a criterion type from the dropdown, describe your compliance, and optionally attach evidence documents. The more detail you provide, the better evaluators can assess your bid.</p>

                        <div id="criteriaContainer">
                            <!-- Dynamic criteria rows will be added here -->
                        </div>

                        <div class="criteria-actions" style="padding: 12px;">
                            <button type="button" class="btn btn-outline btn-sm" onclick="addCriterion()">+ Add Another Criterion</button>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between" style="margin-top: 20px;">
                        <a href="${pageContext.request.contextPath}/supplier-dashboard" class="btn btn-outline">Cancel</a>
                        <button type="submit" class="btn btn-primary">Submit Sealed Bid</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>

<script>
    var criterionIndex = 0;
    var criterionTypes = {
        'Equipment': { label: 'Equipment & Technology', placeholder: 'e.g. 2x 50-ton cranes, 1x pile driver' },
        'Certifications': { label: 'Certifications & Licences', placeholder: 'e.g. ISO 9001:2015 Certified' },
        'Experience': { label: 'Experience & Track Record', placeholder: 'e.g. 3 bridge projects completed in last 5 years' },
        'QualityStandards': { label: 'Quality Standards & QMS', placeholder: 'e.g. Full QMS with independent inspection regime' },
        'Methodology': { label: 'Methodology & Approach', placeholder: 'e.g. Incremental launch method with temporary falsework' },
        'Personnel': { label: 'Personnel & Qualifications', placeholder: 'e.g. 4 registered structural engineers, 2 site managers' },
        'Other': { label: 'Other (specify name)', placeholder: 'Describe your compliance for this criterion' }
    };

    function addCriterion() {
        var container = document.getElementById('criteriaContainer');
        var idx = criterionIndex++;

        var row = document.createElement('div');
        row.className = 'criteria-row';
        row.id = 'criteriaRow_' + idx;

        var typeOptions = '';
        for (var key in criterionTypes) {
            typeOptions += '<option value="' + key + '">' + criterionTypes[key].label + '</option>';
        }

        row.innerHTML =
            '<div class="criteria-header">' +
                '<h4><span class="criterion-type-badge" id="badge_' + idx + '">Criterion #' + (idx + 1) + '</span> Technical Criterion</h4>' +
                '<button type="button" class="btn-remove-criteria" onclick="removeCriterion(' + idx + ')">Remove</button>' +
            '</div>' +
            '<div class="form-group">' +
                '<label>Criterion Type <span class="required">*</span></label>' +
                '<select name="criterionType_' + idx + '" class="form-control" onchange="onTypeChange(' + idx + ', this.value)" required>' +
                    '<option value="">-- Select criterion type --</option>' +
                    typeOptions +
                '</select>' +
            '</div>' +
            '<div class="form-group custom-name-field" id="customNameField_' + idx + '">' +
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
                '<p class="form-hint">Attach supporting evidence such as certificates, photos, or reports.</p>' +
            '</div>';

        container.appendChild(row);
    }

    function onTypeChange(idx, value) {
        var customField = document.getElementById('customNameField_' + idx);
        var badge = document.getElementById('badge_' + idx);
        var textarea = document.querySelector('#criteriaRow_' + idx + ' textarea[name="criterionValue_' + idx + '"]');

        if (value === 'Other') {
            customField.style.display = 'block';
        } else {
            customField.style.display = 'none';
        }

        if (value && criterionTypes[value]) {
            badge.textContent = criterionTypes[value].label;
            badge.className = 'criterion-type-badge ' + value;
        }

        if (value && criterionTypes[value]) {
            textarea.placeholder = criterionTypes[value].placeholder;
        }
    }

    function removeCriterion(idx) {
        var row = document.getElementById('criteriaRow_' + idx);
        if (row) row.remove();
    }

    addCriterion();
</script>
</body>
</html>
