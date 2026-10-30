package com.homeservice.ui.customer;

import com.homeservice.dao.ReviewDAO;
import com.homeservice.model.Booking;
import com.homeservice.model.Review;
import com.homeservice.ui.common.StarRatingInput;
import com.homeservice.ui.common.ToastNotification;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddReviewDialog extends JDialog {
    private final Booking booking;
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private boolean reviewSubmitted = false;

    private StarRatingInput starInput;
    private JTextArea txtComment;

    public AddReviewDialog(Frame parent, Booking booking) {
        super(parent, "Submit Rating & Review - Booking #" + booking.getId(), true);
        this.booking = booking;
        setSize(500, 470);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.getCardBg());

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Rate & Review Your Service");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(booking.getCategoryName() + " by " + booking.getProviderName());
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(224, 231, 255));
        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.getCardBg());
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel lblStar = new JLabel("Your Overall Experience Rating (Click to rate):");
        lblStar.setFont(UITheme.FONT_BOLD);
        lblStar.setForeground(UITheme.getTextDark());
        form.add(lblStar);
        form.add(Box.createVerticalStrut(8));

        // Suggestion #5: Interactive Star Rating Input
        starInput = new StarRatingInput(5);
        form.add(starInput);
        form.add(Box.createVerticalStrut(18));

        JLabel lblFeedback = new JLabel("Write Detailed Feedback / Comments:");
        lblFeedback.setFont(UITheme.FONT_BOLD);
        lblFeedback.setForeground(UITheme.getTextDark());
        form.add(lblFeedback);
        form.add(Box.createVerticalStrut(6));

        txtComment = new JTextArea(4, 20);
        txtComment.setFont(UITheme.FONT_BODY);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.putClientProperty("JTextField.placeholderText", "Describe the quality, punctuality, and professionalism of the service...");
        JScrollPane sp = new JScrollPane(txtComment);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        form.add(sp);

        root.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UITheme.getCardBg());
        footer.setBorder(new EmptyBorder(4, 16, 12, 16));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Submit Review");
        btnSubmit.addActionListener(e -> submitReview());

        footer.add(btnCancel);
        footer.add(btnSubmit);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void submitReview() {
        String comment = txtComment.getText().trim();
        if (comment.isEmpty()) {
            ToastNotification.showWarning(this, "Please write a brief comment describing your experience.");
            return;
        }

        int rating = starInput.getRating();

        Review r = new Review();
        r.setBookingId(booking.getId());
        r.setCustomerId(booking.getCustomerId());
        r.setProviderId(booking.getProviderId());
        r.setRating(rating);
        r.setComment(comment);

        int id = reviewDAO.createReview(r);
        if (id > 0) {
            reviewSubmitted = true;
            ToastNotification.showSuccess(getOwner(), "✓ Rating & review published successfully!");
            dispose();
        } else {
            ToastNotification.showError(this, "✕ A review might already exist for this booking.");
        }
    }

    public boolean isReviewSubmitted() {
        return reviewSubmitted;
    }
}
