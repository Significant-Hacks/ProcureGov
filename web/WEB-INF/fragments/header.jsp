<%-- Header fragment: included at the top of every JSP page --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
    html { overflow-y: auto !important; overflow-x: hidden !important; }
    body { overflow-y: auto !important; overflow-x: hidden !important; min-height: 125vh !important; }
    ::-webkit-scrollbar { width: 0 !important; height: 0 !important; display: none !important; }
    * { scrollbar-width: none !important; -ms-overflow-style: none !important; }
</style>
<title>ProcureGov - ${param.title ne null ? param.title : 'Tender Management'}</title>
<script>
function toggleSidebar() {
    var sidebar = document.getElementById('sidebar');
    var main = document.querySelector('.app-main');
    var overlay = document.getElementById('sidebarOverlay');
    if (!sidebar) return;
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('mobile-open');
        if (overlay) overlay.classList.toggle('show');
    } else {
        sidebar.classList.toggle('collapsed');
        if (main) main.classList.toggle('sidebar-collapsed');
    }
}
function toggleProfileMenu() {
    var menu = document.getElementById('profileMenu');
    if (menu) menu.classList.toggle('show');
}
document.addEventListener('click', function(e) {
    var menu = document.getElementById('profileMenu');
    if (menu && menu.classList.contains('show')) {
        var btn = e.target.closest('.profile-btn');
        var m = e.target.closest('.profile-menu');
        if (!btn && !m) menu.classList.remove('show');
    }
});
// Force hide all scrollbars via JS (most reliable cross-browser)
(function() {
    var s = document.createElement('style');
    s.textContent = 'html{overflow-y:auto!important;overflow-x:hidden!important}body{overflow-y:auto!important;overflow-x:hidden!important;min-height:125vh!important;scrollbar-width:none!important;-ms-overflow-style:none!important}::-webkit-scrollbar{width:0!important;height:0!important;display:none!important}*{scrollbar-width:none!important;-ms-overflow-style:none!important}*::-webkit-scrollbar{width:0!important;height:0!important;display:none!important}';
    document.head.appendChild(s);
})();
</script>
<script src="${pageContext.request.contextPath}/assets/js/layout.js"></script>