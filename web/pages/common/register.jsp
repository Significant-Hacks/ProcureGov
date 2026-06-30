<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/fragments/header.jsp"/>
    <style>
        .email-verify-row { display: flex; gap: 10px; align-items: flex-end; }
        .email-verify-row .form-group { flex: 1; margin-bottom: 0; }
        .email-verify-row .btn { white-space: nowrap; margin-bottom: 0; height: 42px; }
        .verify-status { font-size: 13px; margin-top: 4px; min-height: 20px; }
        .verify-status.success { color: #16a34a; }
        .verify-status.error { color: #dc2626; }
        .verify-status.info { color: #2563eb; }
        .code-row { display: flex; gap: 10px; align-items: flex-end; }
        .code-row .form-group { flex: 1; margin-bottom: 0; }
        .code-row .btn { white-space: nowrap; margin-bottom: 0; height: 42px; }
        @media (max-width: 480px) {
            .email-verify-row { flex-direction: column; gap: 0; }
            .email-verify-row .btn { width: 100%; height: auto; padding: 10px; }
            .code-row { flex-direction: column; gap: 0; }
            .code-row .btn { width: 100%; height: auto; padding: 10px; }
        }
        .fallback-code { font-size: 22px; font-weight: bold; letter-spacing: 6px; color: #1B4332; font-family: monospace; background: #f8f9fa; padding: 8px 16px; border-radius: 6px; display: inline-block; margin-top: 4px; }
    </style>
</head>
<body>
<div class="page-wrapper">
    <jsp:include page="/WEB-INF/fragments/navbar.jsp"/>
    <main class="auth-page">
        <div class="auth-card" style="max-width:520px;">
            <h2>Supplier Registration</h2>
            <p class="auth-subtitle">Create your ProcureGov supplier account</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error"><c:out value="${error}"/></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="POST" id="registerForm">
                <div class="form-group">
                    <label>Company Name <span class="required">*</span></label>
                    <input type="text" name="companyName" class="form-control"
                           value="${not empty companyName ? companyName : ''}" required>
                    <p class="form-hint">Your registered business name</p>
                </div>

                <!-- Email with Verify Button -->
                <div class="form-group">
                    <label>Email Address <span class="required">*</span></label>
                    <div class="email-verify-row">
                        <div class="form-group">
                            <input type="email" name="email" id="emailInput" class="form-control"
                                   value="${not empty email ? email : ''}" required>
                        </div>
                        <button type="button" id="sendCodeBtn" class="btn btn-secondary" onclick="sendVerifyCode()">Verify Email</button>
                    </div>
                    <div id="sendCodeStatus" class="verify-status"></div>
                    <div id="fallbackCodeDisplay" style="display:none;">
                        <span class="form-hint">Code (email not configured):</span>
                        <span id="fallbackCode" class="fallback-code"></span>
                    </div>
                </div>

                <!-- Verification Code Input (hidden until code sent) -->
                <div class="form-group" id="codeInputGroup" style="display:none;">
                    <label>Verification Code <span class="required">*</span></label>
                    <div class="code-row">
                        <div class="form-group">
                            <input type="text" id="verifyCodeInput" maxlength="6" pattern="[0-9]{6}"
                                   placeholder="e.g. 123456" class="form-control"
                                   style="font-size:22px;letter-spacing:6px;text-align:center;font-family:monospace;">
                        </div>
                        <button type="button" id="verifyCodeBtn" class="btn btn-secondary" onclick="verifyCode()">Confirm Code</button>
                    </div>
                    <div id="verifyCodeStatus" class="verify-status"></div>
                </div>

                <!-- Verified indicator -->
                <div id="emailVerifiedBadge" style="display:none;margin-bottom:15px;">
                    <span style="color:#16a34a;font-weight:bold;">&#10003; Email Verified</span>
                </div>

                <div class="form-group">
                    <label>Physical Address <span class="required">*</span></label>
                    <textarea name="physicalAddress" class="form-control" rows="2" required><c:out value="${not empty physicalAddress ? physicalAddress : ''}"/></textarea>
                </div>
                <div class="form-group">
                    <label>Contact Number <span class="required">*</span></label>
                    <input type="tel" name="contactNumber" class="form-control"
                           value="${not empty contactNumber ? contactNumber : ''}" required>
                </div>
                <div class="form-group">
                    <label>Password <span class="required">*</span></label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Confirm Password <span class="required">*</span></label>
                    <input type="password" name="confirmPassword" class="form-control" required>
                </div>
                <button type="submit" id="createAccountBtn" class="btn btn-primary btn-block" disabled>Create Account</button>
                <p class="form-hint" style="text-align:center;margin-top:8px;">You must verify your email before creating an account.</p>
            </form>

            <div class="auth-links">
                Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a>
            </div>
        </div>
    </main>
    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>
</div>

<script>
var emailVerified = false;

function sendVerifyCode() {
    var email = document.getElementById('emailInput').value.trim();
    if (!email) {
        document.getElementById('sendCodeStatus').textContent = 'Please enter your email first.';
        document.getElementById('sendCodeStatus').className = 'verify-status error';
        return;
    }

    var btn = document.getElementById('sendCodeBtn');
    btn.disabled = true;
    btn.textContent = 'Sending...';
    document.getElementById('sendCodeStatus').textContent = 'Sending verification code...';
    document.getElementById('sendCodeStatus').className = 'verify-status info';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/email-verify', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onload = function() {
        btn.disabled = false;
        btn.textContent = 'Resend Code';
        try {
            var resp = JSON.parse(xhr.responseText);
            if (resp.success) {
                document.getElementById('sendCodeStatus').textContent = resp.message;
                document.getElementById('sendCodeStatus').className = 'verify-status success';
                document.getElementById('codeInputGroup').style.display = 'block';
                document.getElementById('verifyCodeInput').focus();
                // Show fallback code if email not configured
                if (resp.code) {
                    document.getElementById('fallbackCodeDisplay').style.display = 'block';
                    document.getElementById('fallbackCode').textContent = resp.code;
                } else {
                    document.getElementById('fallbackCodeDisplay').style.display = 'none';
                }
            } else {
                document.getElementById('sendCodeStatus').textContent = resp.message;
                document.getElementById('sendCodeStatus').className = 'verify-status error';
            }
        } catch(e) {
            document.getElementById('sendCodeStatus').textContent = 'Error sending code.';
            document.getElementById('sendCodeStatus').className = 'verify-status error';
        }
    };
    xhr.onerror = function() {
        btn.disabled = false;
        btn.textContent = 'Verify Email';
        document.getElementById('sendCodeStatus').textContent = 'Network error.';
        document.getElementById('sendCodeStatus').className = 'verify-status error';
    };
    xhr.send('action=send&email=' + encodeURIComponent(email));
}

function verifyCode() {
    var email = document.getElementById('emailInput').value.trim();
    var code = document.getElementById('verifyCodeInput').value.trim();
    if (!code) {
        document.getElementById('verifyCodeStatus').textContent = 'Please enter the verification code.';
        document.getElementById('verifyCodeStatus').className = 'verify-status error';
        return;
    }

    var btn = document.getElementById('verifyCodeBtn');
    btn.disabled = true;
    btn.textContent = 'Verifying...';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/email-verify', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onload = function() {
        btn.disabled = false;
        btn.textContent = 'Confirm Code';
        try {
            var resp = JSON.parse(xhr.responseText);
            if (resp.success) {
                emailVerified = true;
                document.getElementById('verifyCodeStatus').textContent = resp.message;
                document.getElementById('verifyCodeStatus').className = 'verify-status success';
                document.getElementById('emailVerifiedBadge').style.display = 'block';
                document.getElementById('codeInputGroup').style.display = 'none';
                document.getElementById('sendCodeBtn').disabled = true;
                document.getElementById('emailInput').readOnly = true;
                document.getElementById('createAccountBtn').disabled = false;
                document.getElementById('fallbackCodeDisplay').style.display = 'none';
            } else {
                document.getElementById('verifyCodeStatus').textContent = resp.message;
                document.getElementById('verifyCodeStatus').className = 'verify-status error';
            }
        } catch(e) {
            document.getElementById('verifyCodeStatus').textContent = 'Error verifying code.';
            document.getElementById('verifyCodeStatus').className = 'verify-status error';
        }
    };
    xhr.onerror = function() {
        btn.disabled = false;
        btn.textContent = 'Confirm Code';
        document.getElementById('verifyCodeStatus').textContent = 'Network error.';
        document.getElementById('verifyCodeStatus').className = 'verify-status error';
    };
    xhr.send('action=verify&email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code));
}
</script>
</body>
</html>
