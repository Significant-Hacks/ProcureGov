<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/WEB-INF/fragments/sidebar.jsp"/>
    <div class="app-main">
        <jsp:include page="/WEB-INF/fragments/top-header.jsp"/>

        <div class="app-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Edit Tender (Draft)</h2>
                <a href="${pageContext.request.contextPath}/tender-status?id=${tender.id}" class="btn btn-outline">Cancel</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">Tender Details - ${tender.referenceNumber}</div>
                <form action="${pageContext.request.contextPath}/edit-tender" method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="id" value="${tender.id}">
                    
                    <div class="form-group">
                        <label>Reference Number</label>
                        <input type="text" class="form-control" value="${tender.referenceNumber}" disabled>
                        <p class="form-hint">System-generated, cannot be changed</p>
                    </div>

                    <div class="form-group">
                        <label>Tender Title <span class="required">*</span></label>
                        <input type="text" name="title" class="form-control" value="${tender.title}" required>
                    </div>

                    <div class="form-group">
                        <label>Category <span class="required">*</span></label>
                        <select name="category" class="form-control" required>
                            <option value="Construction" ${tender.category eq 'Construction' ? 'selected' : ''}>Construction</option>
                            <option value="Roads" ${tender.category eq 'Roads' ? 'selected' : ''}>Roads</option>
                            <option value="Electrical" ${tender.category eq 'Electrical' ? 'selected' : ''}>Electrical</option>
                            <option value="Plumbing" ${tender.category eq 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                            <option value="General Services" ${tender.category eq 'General Services' ? 'selected' : ''}>General Services</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Estimated Value (Maloti) <span class="required">*</span></label>
                        <input type="number" name="value" class="form-control" step="0.01" min="1" 
                               value="${tender.estimatedValue}" required>
                    </div>

                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="showEstimatedValue" ${tender.showEstimatedValue ? 'checked' : ''}>
                            Show Estimated Value to Bidders
                        </label>
                        <p class="form-hint">If unchecked, bidders will not see the estimated budget for this tender.</p>
                    </div>

                    <div class="form-group">
                        <label>Submission Deadline <span class="required">*</span></label>
                        <fmt:formatDate value="${tender.deadline}" pattern="yyyy-MM-dd'T'HH:mm" var="deadlineFormatted"/>
                        <input type="datetime-local" name="deadline" class="form-control" 
                               value="${deadlineFormatted}" required>
                    </div>

                    <div class="form-group">
                        <label>Description <span class="required">*</span></label>
                        <textarea name="description" class="form-control" rows="5" required><c:out value="${tender.description}"/></textarea>
                    </div>

                    <div class="form-group">
                        <label>Tender Notice Document (PDF only, max 5MB)</label>
                        <input type="file" name="notice" class="form-control" accept="application/pdf">
                        <p class="form-hint">Upload new PDF to replace existing document, or leave blank to keep current.</p>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/tender-status?id=${tender.id}" class="btn btn-outline">Cancel</a>
                        <button type="submit" class="btn btn-primary">Save Changes</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
