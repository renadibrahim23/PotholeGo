package com.community.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.community.database.DatabaseConnection;
import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;

// campaigns table (the model's title is the table's name column).
// Campaigns are loaded together with their donations, so raised amount and progress are always current.
@Repository
public class CampaignRepository {
    private static final String SELECT_CAMPAIGN = "SELECT id, name, goal_amount FROM campaigns ";

    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final DonationRepository donations;

    public CampaignRepository(DonationRepository donations) {
        this.donations = donations;
        if (findAll().isEmpty()) {
            // demo data for an empty database
            create("Fix the potholes at the campus gate", 50000);
            create("Repair the broken streetlights on University St.", 30000);
            create("Rebuild the sidewalk near the metro station", 80000);
        }
    }

    public Campaign create(String title, double goalAmount) {
        synchronized (db) {
            // ids have no default in the schema, so the next one is computed here
            try (ResultSet rs = db.query(
                    "INSERT INTO campaigns (id, name, goal_amount, start_date) "
                            + "SELECT COALESCE(MAX(id), 0) + 1, ?, ?, ? FROM campaigns RETURNING id",
                    title, goalAmount, Date.valueOf(LocalDate.now()))) {
                rs.next();
                return toCampaign(rs.getInt(1), title, goalAmount);
            } catch (SQLException e) {
                throw new IllegalStateException("Could not create campaign", e);
            }
        }
    }

    public Campaign update(Campaign c) {
        synchronized (db) {
            try {
                db.execute("UPDATE campaigns SET name = ?, goal_amount = ? WHERE id = ?",
                        c.getTitle(), c.getGoalAmount(), c.getId());
                return c;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not update campaign", e);
            }
        }
    }

    public boolean deleteById(int id) {
        synchronized (db) {
            try {
                return db.execute("DELETE FROM campaigns WHERE id = ?", id) > 0;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not delete campaign", e);
            }
        }
    }

    public List<Campaign> findAll() {
        Map<Integer, Campaign> byId = load(SELECT_CAMPAIGN + "ORDER BY id");
        donations.attachTo(byId, null);
        return new ArrayList<>(byId.values());
    }

    public Optional<Campaign> findById(int id) {
        Map<Integer, Campaign> byId = load(SELECT_CAMPAIGN + "WHERE id = ?", id);
        donations.attachTo(byId, id);
        return byId.values().stream().findFirst();
    }

    public List<Donation> findAllDonations() {
        List<Donation> all = new ArrayList<>();
        for (Campaign c : findAll()) {
            all.addAll(c.getDonations());
        }
        return all;
    }

    public Optional<Donation> findDonationById(int id) {
        return donations.findCampaignIdOf(id)
                .flatMap(this::findById)
                .flatMap(c -> c.getDonations().stream().filter(d -> d.getId() == id).findFirst());
    }

    private Map<Integer, Campaign> load(String sql, Object... params) {
        synchronized (db) {
            try (ResultSet rs = db.query(sql, params)) {
                Map<Integer, Campaign> byId = new LinkedHashMap<>();
                while (rs.next()) {
                    byId.put(rs.getInt("id"),
                            toCampaign(rs.getInt("id"), rs.getString("name"), rs.getDouble("goal_amount")));
                }
                return byId;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load campaigns", e);
            }
        }
    }

    private static Campaign toCampaign(int id, String title, double goal) {
        Campaign c = new Campaign(title, goal, null);
        c.setTitle(title); // Campaign's constructor does not set the title yet
        c.setId(id);
        return c;
    }
}
