package com.procuregov.controller.supplier;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OpenTendersServlet extends HttpServlet {

    private TenderDAO tenderDAO;
    private BidDAO bidDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "SUPPLIER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Auto-close tenders whose deadline has passed
        tenderDAO.autoCloseExpiredTenders();

        int userId = SessionUtil.getUserId(request);
        int supplierId = getSupplierId(userId);

        // This supplier's bids
        List<Bid> myBids = bidDAO.getBySupplierId(supplierId);

        // Set of tender IDs the supplier has bid on
        Set<Integer> biddedTenderIds = new HashSet<>();
        for (Bid b : myBids) {
            biddedTenderIds.add(b.getTenderId());
        }

        // Open tenders - ALL (both bidded and unbidded)
        List<Tender> openTenders = tenderDAO.getOpenTenders();
        request.setAttribute("openTenders", openTenders);

        // For each open tender, whether supplier has bid
        Map<Integer, Boolean> openTenderBidStatus = new HashMap<>();
        for (Tender t : openTenders) {
            openTenderBidStatus.put(t.getId(), biddedTenderIds.contains(t.getId()));
        }
        request.setAttribute("openTenderBidStatus", openTenderBidStatus);

        // Closed tenders that the supplier bid on
        List<Tender> closedTendersBidOn = new ArrayList<>();
        List<Tender> closedOrEvalTenders = new ArrayList<>();
        closedOrEvalTenders.addAll(tenderDAO.getByStatus("Closed"));
        closedOrEvalTenders.addAll(tenderDAO.getByStatus("Under Evaluation"));
        closedOrEvalTenders.addAll(tenderDAO.getByStatus("Evaluated"));
        for (Tender t : closedOrEvalTenders) {
            if (biddedTenderIds.contains(t.getId())) {
                closedTendersBidOn.add(t);
            }
        }
        request.setAttribute("closedTendersBidOn", closedTendersBidOn);

        request.setAttribute("supplierId", supplierId);
        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/supplier/open-tenders.jsp").forward(request, response);
    }

    private int getSupplierId(int userId) {
        String sql = "SELECT id FROM suppliers WHERE user_id = ?";
        try (java.sql.Connection conn = com.procuregov.util.DBConnectionUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(OpenTendersServlet.class.getName())
                .log(java.util.logging.Level.SEVERE, "Error getting supplier id", e);
        }
        return -1;
    }
}
