<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
                <h2>Create New Tender</h2>
                <a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Back to Dashboard</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card">
                <div class="card-header">Tender Details</div>
                <form action="${pageContext.request.contextPath}/create-tender" method="POST" enctype="multipart/form-data">
                    <div class="form-group">
                        <label>Tender Title <span class="required">*</span></label>
                        <input type="text" name="title" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label>Category <span class="required">*</span></label>
                        <select name="category" class="form-control" required>
                            <option value="Construction">Construction</option>
                            <option value="Roads">Roads</option>
                            <option value="Electrical">Electrical</option>
                            <option value="Plumbing">Plumbing</option>
                            <option value="General Services">General Services</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Estimated Value (Maloti) <span class="required">*</span></label>
                        <input type="number" name="value" class="form-control" step="0.01" min="1" required>
                    </div>

                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="showEstimatedValue" checked>
                            Show Estimated Value to Bidders
                        </label>
                        <p class="form-hint">If unchecked, bidders will not see the estimated budget for this tender.</p>
                    </div>

                    <div class="form-group">
                        <label>Submission Deadline <span class="required">*</span></label>
                        <input type="datetime-local" name="deadline" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label>Description <span class="required">*</span></label>
                        <textarea name="description" class="form-control" rows="5" required></textarea>
                    </div>

                    <div class="form-group">
                        <label>Tender Notice Document (PDF only, max 5MB) <span class="required">*</span></label>
                        <input type="file" name="notice" class="form-control" accept="application/pdf" required>
                        <p class="form-hint">Upload the official tender notice document in PDF format.</p>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/officer-dashboard" class="btn btn-outline">Cancel</a>
                        <button type="submit" class="btn btn-primary">Save as Draft</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
    </div>
</div>
</body>
</html>
