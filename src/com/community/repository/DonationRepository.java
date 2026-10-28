package com.community.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.community.database.DatabaseConnection;
import com.community.model.donation.Campaign;
import com.community.model.donation.CheckDonation;
import com.community.model.donation.Donation;
import com.community.model.donation.InKindDonation;
import com.community.model.donation.OnlineDonation;
import com.community.model.user.User;

// donations table plus one subtype table per donation type
// (online_donations, check_donations, in_kind_donations).
@Repository
public class DonationRepository {
    private static final String SELECT_DONATION = "SELECT d.id, d.campaign_id, d.amount, "
            + "d.donated_at, t.name AS type_name, "
            + "o.transaction_reference, c.check_number, c.bank_name, k.item_description, k.quantity, "
            + UserRepository.USER_COLUMNS + " "
            + "FROM donations d "
            + "JOIN donation_types t ON t.id = d.donation_type_id "
            + "JOIN users u ON u.id = d.donor_id "
            + "JOIN roles r ON r.id = u.role_id "
            + "LEFT JOIN online_donations o ON o.donation_id = d.id "
            + "LEFT JOIN check_donations c ON c.donation_id = d.id "
            + "LEFT JOIN in_kind_donations k ON k.donation_id = d.id ";

    // first part of every insert: the shared donations row. The type is matched by name
    // ignoring case and dashes, so the model's "Inkind" finds the table's "In-Kind".
    private static final String INSERT_DONATION = "WITH d AS (INSERT INTO donations "
            + "(id, donor_id, campaign_id, donation_type_id, amount) "
            + "SELECT COALESCE(MAX(id), 0) + 1, ?, ?, "
            + "(SELECT id FROM donation_types WHERE LOWER(REPLACE(name, '-', '')) = LOWER(?)), ? "
            + "FROM donations RETURNING id) ";

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    // both rows are written in one statement, so a donation is never half-saved
    public Donation save(Donation d) {
        String sql;
        Object[] params;
        Object[] common = { d.getDonor().getId(), d.getCampaign().getId(), d.getType().getName() };
        if (d instanceof OnlineDonation o) {
            sql = INSERT_DONATION + "INSERT INTO online_donations "
                    + "(donation_id, payment_provider, transaction_reference, payment_status) "
                    + "SELECT id, 'Simulated', ?, 'completed' FROM d RETURNING donation_id";
            params = new Object[] { common[0], common[1], common[2], o.getAmount(), o.getTransactionId() };
        } else if (d instanceof CheckDonation c) {
            sql = INSERT_DONATION + "INSERT INTO check_donations (donation_id, check_number, bank_name) "
                    + "SELECT id, ?, ? FROM d RETURNING donation_id";
            params = new Object[] { common[0], common[1], common[2], c.getAmount(), c.getCheckNumber(),
                    c.getBankName() };
        } else if (d instanceof InKindDonation k) {
            sql = INSERT_DONATION + "INSERT INTO in_kind_donations (donation_id, item_description, quantity) "
                    + "SELECT id, ?, ? FROM d RETURNING donation_id";
            params = new Object[] { common[0], common[1], common[2], 0, k.getItemName(), k.getQuantity() };
        } else {
            throw new IllegalArgumentException("Unknown donation type " + d.getClass().getSimpleName());
        }
        synchronized (db) {
            try (ResultSet rs = db.query(sql, params)) {
                rs.next();
                d.setId(rs.getInt(1));
                return d;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not save donation", e);
            }
        }
    }

    // the subtype row is removed by ON DELETE CASCADE
    public boolean deleteById(int id) {
        synchronized (db) {
            try {
                return db.execute("DELETE FROM donations WHERE id = ?", id) > 0;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not delete donation", e);
            }
        }
    }

    public Optional<Integer> findCampaignIdOf(int donationId) {
        synchronized (db) {
            try (ResultSet rs = db.query("SELECT campaign_id FROM donations WHERE id = ?", donationId)) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load donation", e);
            }
        }
    }

    public boolean existsForCampaign(int campaignId) {
        synchronized (db) {
            try (ResultSet rs = db.query("SELECT 1 FROM donations WHERE campaign_id = ? LIMIT 1", campaignId)) {
                return rs.next();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load donations", e);
            }
        }
    }

    // Loads the donations of the given campaigns (all of them when onlyCampaignId is null)
    // and adds each one to its Campaign object, updating the raised amount.
    void attachTo(Map<Integer, Campaign> campaigns, Integer onlyCampaignId) {
        String sql = SELECT_DONATION + (onlyCampaignId == null ? "" : "WHERE d.campaign_id = ? ") + "ORDER BY d.id";
        synchronized (db) {
            try (ResultSet rs = onlyCampaignId == null ? db.query(sql) : db.query(sql, onlyCampaignId)) {
                while (rs.next()) {
                    Campaign c = campaigns.get(rs.getInt("campaign_id"));
                    if (c == null) {
                        continue;
                    }
                    Donation d = fromRow(rs, UserRepository.fromRow(rs), c);
                    c.getDonations().add(d);
                    c.setRaisedAmount(c.getRaisedAmount() + d.getAmount());
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load donations", e);
            }
        }
    }

    private static Donation fromRow(ResultSet rs, User donor, Campaign c) throws SQLException {
        String type = rs.getString("type_name").replace("-", "");
        Donation d;
        if ("Online".equalsIgnoreCase(type)) {
            d = new OnlineDonation(donor, c, rs.getString("transaction_reference"), rs.getDouble("amount"));
        } else if ("Check".equalsIgnoreCase(type)) {
            d = new CheckDonation(donor, c, rs.getString("check_number"), rs.getString("bank_name"),
                    rs.getDouble("amount"));
        } else {
            d = new InKindDonation(donor, c, rs.getString("item_description"), rs.getInt("quantity"));
        }
        d.setId(rs.getInt("id"));
        // donated_at is stored in UTC; show the date in the server's own time zone
        d.setDate(rs.getObject("donated_at", OffsetDateTime.class)
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
        return d;
    }
}
