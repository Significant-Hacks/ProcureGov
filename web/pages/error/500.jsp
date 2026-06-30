<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - System Error | ProcureGov</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #F5F1EB; color: #1A1A2E; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .error-container { text-align: center; padding: 40px; max-width: 600px; }
        .error-code { font-size: 6rem; font-weight: 700; color: #B91C1C; line-height: 1; margin-bottom: 8px; }
        .error-title { font-size: 1.5rem; font-weight: 600; color: #B91C1C; margin-bottom: 12px; }
        .error-message { font-size: 1rem; color: #666; margin-bottom: 24px; line-height: 1.6; }
        .error-divider { width: 60px; height: 4px; background: #C9A227; margin: 0 auto 24px; border-radius: 2px; }
        .ministry-badge { display: inline-block; background: #1B4332; color: white; padding: 6px 16px; border-radius: 4px; font-size: 0.8rem; font-weight: 600; letter-spacing: 0.5px; margin-bottom: 20px; }
        .btn-home { display: inline-block; background: #C9A227; color: #0F2B1F; padding: 12px 32px; border-radius: 6px; text-decoration: none; font-weight: 700; font-size: 0.95rem; transition: background 0.2s; }
        .btn-home:hover { background: #A6851E; }
    </style>
</head>
<body>
    <div class="error-container">
        <span class="ministry-badge">MINISTRY OF PUBLIC WORKS</span>
        <div class="error-code">500</div>
        <div class="error-title">Internal Server Error</div>
        <div class="error-divider"></div>
        <p class="error-message">A technical problem has occurred on the ProcureGov portal. Please try again later or contact the ICT Directorate for assistance.</p>
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn-home">Return to Portal</a>
    </div>
</body>
</html>
