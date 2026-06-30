/**
 * Layout Management Script
 * Handles footer and sidebar positioning with zoom support
 */

document.addEventListener('DOMContentLoaded', function() {
    const ZOOM = 0.8;

    const adjustLayout = function() {
        const sidebar = document.querySelector('.sidebar');
        const footer = document.querySelector('.footer');
        const pageWrapper = document.querySelector('.page-wrapper');

        // Real visual viewport height divided by zoom = CSS px needed to fill screen
        var cssHeight = Math.ceil(window.innerHeight / ZOOM);

        if (sidebar) {
            sidebar.style.height = cssHeight + 'px';
        }

        if (pageWrapper) {
            pageWrapper.style.minHeight = cssHeight + 'px';
            pageWrapper.style.display = 'flex';
            pageWrapper.style.flexDirection = 'column';
        }

        if (footer) {
            footer.style.marginTop = 'auto';
            footer.style.flexShrink = '0';
        }
    };

    // Call on load and resize
    adjustLayout();
    window.addEventListener('resize', adjustLayout);
    window.addEventListener('orientationchange', adjustLayout);
});
