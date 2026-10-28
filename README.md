# Pothole Go

Civic non-profit platform. Milestone 1: Donation module (repair campaigns).

- **Backend:** Spring Boot (Java 17+), port 8080
- **Frontend:** React + Vite, port 5173
- **Database:** the team's Supabase Postgres, through the `DatabaseConnection` singleton

## First-time setup

1. **Database settings.** make sure the variables are in `.env` file.
   Project Settings → Database → Connection string → JDBC.
2. **Open in IntelliJ.** Use File → Open and select this folder's `pom.xml` → Open as Project.
   Wait for the Maven download to finish. No Maven install is needed: the project uses the
   Maven wrapper (`mvnw`), pinned to Maven 3.9.9, which IntelliJ 2024.2 can import.
3. **Frontend packages.** In a terminal:

   ```bash
   cd frontend
   npm install
   ```

## Run (every time)

Start both, in this order, and keep both running while you test.

1. **Backend.** In IntelliJ, open `src/com/community/PotholeGoApplication.java` and click ▶ Run.
   Wait for `Started PotholeGoApplication` in the Run panel.
   From a terminal in this folder you can use `./mvnw spring-boot:run` instead, but not both at once.
2. **Frontend.** In a second terminal:

   ```bash
   cd frontend
   npm run dev
   ```

3. Open http://localhost:5173. The UI forwards `/api` calls to the backend.

**Stop:** ■ Stop in IntelliJ for the backend, Ctrl+C in the terminal for the frontend.

**After code changes:** restart the backend for Java changes. React changes reload in the browser by themselves.

## Accounts

On first start against an empty database the app adds a demo admin and three demo campaigns.

- **Demo admin** (seeded in `UserRepository`): `admin@potholego.org` / `admin123`
- **Anyone** can sign up from the login page. New accounts get the Resident role.

Logins are kept in server memory, so everyone has to log in again after the backend restarts. The data itself is saved.

## What to test

| Area | Steps |
|---|---|
| Landing | Total raised and featured campaigns are shown |
| Sign up / log in | Log in → Sign up, then log out and log in again |
| Donate | Campaigns → Donate, with Online, Check and In-kind. A receipt appears |
| My Panel | Your donations, receipts and profile editing |
| Admin Panel | Log in as the demo admin: stats, campaign create/edit/delete, remove donations, change roles |
| Persistence | Restart the backend: everything is still there (also visible in Supabase's Table Editor) |

## Troubleshooting

| Problem | Fix |
|---|---|
| `Port 8080 was already in use` | The backend is already running (often from IntelliJ). Use that one, or stop it first. |
| `Configure DATABASE_USERNAME, DATABASE_PASSWORD, and DATABASE_URL` | `.env` is missing or empty, or the backend was not started from this folder. In IntelliJ: Run → Edit Configurations → Working directory = this folder. |
| Red "Cannot resolve" errors in IntelliJ | Maven tab → ⟳ Reload All Maven Projects. If that fails: File → Invalidate Caches → Invalidate and Restart. |
| Using Supabase's transaction pooler (port 6543) | Add `?prepareThreshold=0` to the end of `DATABASE_URL`. |

## Database tables used

| Data | Tables |
|---|---|
| Users | `users`, plus `admins` or `residents` to match the role |
| Campaigns | `campaigns` (the model's `title` is the `name` column) |
| Donations | `donations` plus `online_donations`, `check_donations` or `in_kind_donations` |
| Lookups | `roles`, `donation_types` |

## API

| Controller | Method | Path | Access |
|---|---|---|---|
| AuthController | POST | `/api/auth/register`, `/api/auth/login`, `/api/auth/logout` | Public |
| | GET | `/api/auth/me` | Public (204 if logged out) |
| DonationController | GET | `/api/campaigns`, `/api/campaigns/{id}` | Public |
| | POST | `/api/donations` (`type`: Online, Check, InKind) | Logged in |
| | GET | `/api/donations/history` | Logged in |
| UserPanelController | GET | `/api/user/donations` | Logged in |
| | GET | `/api/user/donations/{id}/receipt` | Owner or Admin |
| | PUT | `/api/user/profile` | Logged in |
| AdminPanelController | GET | `/api/admin/statistics`, `/api/admin/users` | Admin |
| | POST/PUT/DELETE | `/api/admin/campaigns[/{id}]` | Admin |
| | DELETE | `/api/admin/donations/{id}` | Admin |
| | PUT | `/api/admin/users/{id}/role` | Admin (roles: Admin, Resident) |
