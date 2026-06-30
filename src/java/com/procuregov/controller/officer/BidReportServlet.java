package com.procuregov.controller.officer;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidTechnicalCriterionDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.BidTechnicalCriterionDAOImpl;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.BidTechnicalCriterion;
import com.procuregov.model.Evaluation;
import com.procuregov.model.Tender;
import com.procuregov.service.EvaluationService;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling the detailed bid evaluation report view.
 * Shows a single bidder's full evaluation breakdown including
 * technical criteria, all evaluator scores, and final ranking.
 */
public class BidReportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BidReportServlet.class.getName());
    private BidDAO bidDAO;
    private TenderDAO tenderDAO;
    private EvaluationDAO evaluationDAO;
    private BidTechnicalCriterionDAO criterionDAO;
    private EvaluationService evaluationService;

    @Override
    public void init() throws ServletException {
        bidDAO = new BidDAOImpl();
        tenderDAO = new TenderDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        criterionDAO = new BidTechnicalCriterionDAOImpl();
        evaluationService = new EvaluationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"OFFICER".equals(role) && !"EVALUATOR".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            int bidId = Integer.parseInt(request.getParameter("bidId"));

            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                String dashboard = "OFFICER".equals(role) ? "/officer-dashboard" : "/evaluator-dashboard";
                response.sendRedirect(request.getContextPath() + dashboard);
                return;
            }

            Bid bid = bidDAO.getById(bidId);
            if (bid == null || bid.getTenderId() != tenderId) {
                response.sendRedirect(request.getContextPath() + "/award-contract?id=" + tenderId);
                return;
            }

            // Get all evaluator scores for this bid
            List<Evaluation> evaluatorScores = evaluationDAO.getByBidId(bidId);
            double finalScore = evaluationService.getFinalScore(bidId);

            // Get technical criteria for this bid
            List<BidTechnicalCriterion> criteria = criterionDAO.getByBidId(bidId);

            // Compute this bid's rank among all bids for this tender
            int rank = computeRank(tenderId, bidId);

            request.setAttribute("tender", tender);
            request.setAttribute("bid", bid);
            request.setAttribute("evaluatorScores", evaluatorScores);
            request.setAttribute("finalScore", finalScore);
            request.setAttribute("criteria", criteria);
            request.setAttribute("rank", rank);
            request.setAttribute("isOfficer", "OFFICER".equals(role));
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/officer/bid-report.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying bid report", e);
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
        }
    }

    private int computeRank(int tenderId, int targetBidId) {
        List<Bid> allBids = bidDAO.getByTenderId(tenderId);
        // Sort by final score descending
        Collections.sort(allBids, new Comparator<Bid>() {
            @Override
            public int compare(Bid a, Bid b) {
                double scoreA = evaluationService.getFinalScore(a.getId());
                double scoreB = evaluationService.getFinalScore(b.getId());
                return Double.compare(scoreB, scoreA);
            }
        });
        for (int i = 0; i < allBids.size(); i++) {
            if (allBids.get(i).getId() == targetBidId) {
                return i + 1;
            }
        }
        return 0;
    }
}
