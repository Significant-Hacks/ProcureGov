package com.procuregov.controller.supplier;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.AwardDAOImpl;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Award;
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

public class SupplierDashboardServlet extends HttpServlet {

    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private AwardDAO awardDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        awardDAO = new AwardDAOImpl();
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
        request.setAttribute("myBids", myBids);

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

        // Map of tenderId -> award for quick lookup
        Map<Integer, Award> awardMap = new HashMap<>();
        for (Bid bid : myBids) {
            Award award = awardDAO.getByTenderId(bid.getTenderId());
            if (award != null) {
                awardMap.put(bid.getTenderId(), award);
            }
        }
        request.setAttribute("awardMap", awardMap);

        // Enrich bids with tender status and winning supplier info
        List<BidWithStatus> bidsWithStatus = new ArrayList<>();
        for (Bid bid : myBids) {
            Tender t = tenderDAO.getById(bid.getTenderId());
            String tenderStatus = t != null ? t.getStatus() : "Unknown";
            String winningSupplier = null;
            boolean isWinner = false;
            Award award = awardMap.get(bid.getTenderId());
            if (award != null) {
                winningSupplier = award.getSupplierName();
                isWinner = (award.getWinningSupplierId() == supplierId);
            }
            bidsWithStatus.add(new BidWithStatus(bid, tenderStatus, winningSupplier, isWinner));
        }
        request.setAttribute("bidsWithStatus", bidsWithStatus);

        // Award notices for tenders this supplier bid on
        List<Award> myAwards = new ArrayList<>();
        for (Bid bid : myBids) {
            Award award = awardDAO.getByTenderId(bid.getTenderId());
            if (award != null) {
                myAwards.add(award);
            }
        }
        request.setAttribute("myAwards", myAwards);

        // Won contracts: awards where this supplier is the winner
        List<Award> wonContracts = awardDAO.getBySupplierId(supplierId);
        request.setAttribute("wonContracts", wonContracts);

        // Stats for cards
        request.setAttribute("openTenderCount", openTenders.size());
        request.setAttribute("totalBidsCount", myBids.size());
        request.setAttribute("pendingBidsCount", closedTendersBidOn.size());
        request.setAttribute("wonContractsCount", wonContracts.size());

        request.setAttribute("supplierId", supplierId);
        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/supplier/dashboard.jsp").forward(request, response);
    }

    /**
     * Inner class to pair a bid with its tender status and award outcome for display.
     */
    public static class BidWithStatus {
        private Bid bid;
        private String tenderStatus;
        private String winningSupplier;
        private boolean isWinner;

        public BidWithStatus(Bid bid, String tenderStatus, String winningSupplier, boolean isWinner) {
            this.bid = bid;
            this.tenderStatus = tenderStatus;
            this.winningSupplier = winningSupplier;
            this.isWinner = isWinner;
        }

        public Bid getBid() { return bid; }
        public String getTenderStatus() { return tenderStatus; }
        public String getWinningSupplier() { return winningSupplier; }
        public boolean isWinner() { return isWinner; }
        public int getId() { return bid.getId(); }
        public int getTenderId() { return bid.getTenderId(); }
        public String getTenderTitle() { return bid.getTenderTitle(); }
        public String getTenderRefNumber() { return bid.getTenderRefNumber(); }
        public java.math.BigDecimal getAmount() { return bid.getAmount(); }
        public int getProposedTimelineDays() { return bid.getProposedTimelineDays(); }
        public java.util.Date getSubmittedAt() { return bid.getSubmittedAt(); }
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
            java.util.logging.Logger.getLogger(SupplierDashboardServlet.class.getName()).log(java.util.logging.Level.SEVERE, "Error getting supplier id", e);
        }
        return -1;
    }
}
