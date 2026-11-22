# Publishing Bahá'í Calendar to Google Play (`ajkiefte@gmail.com`)

This repository is bridged to Google Play via **GitHub Actions** so you can keep iterating in Google AI Studio / GitHub while building signed `.aab` bundles and publishing directly to your Google Play Developer account (`ajkiefte@gmail.com`).

- **Application ID (permanent):** `eu.kiefte.bahaicalendar`
- **Privacy Policy URL:** `https://lapingvino.github.io/Bahai-Calendar/privacy-policy.html`
- **Workflow:** `.github/workflows/release.yml`

---

## Phase 1: One-Time Manual First Upload (Required by Google Play)

Google's Play Publishing API **cannot create a new app** or accept the very first bundle upload. You must do this once in the browser:

1. **Download the signed `.aab`** from GitHub Actions:
   - Go to **GitHub → Actions → Build, Release & Publish to Google Play → latest green run → Artifacts → `app-release-...`** and unzip `app-release.aab`.
2. **Create the app in Google Play Console** (signed in as `ajkiefte@gmail.com`):
   - Visit [Google Play Console](https://play.google.com/console) → **Create app**.
   - App name: `Bahá'í Calendar` (or your preferred display name).
   - Default language, App (not Game), Free.
3. **Upload the first `.aab` to Internal testing**:
   - In the left menu: **Test and release → Testing → Internal testing → Create new release**.
   - Opt in to **Play App Signing** (recommended & required for new apps — Google protects your app signing key while GitHub Actions signs uploads with your upload keystore).
   - Upload `app-release.aab`, save, and roll out to Internal testing.
4. **Complete required Play Console declarations** (under **Policy and programmes → App content**):
   - **Privacy policy URL:** `https://lapingvino.github.io/Bahai-Calendar/privacy-policy.html`
   - **Data safety:** No data is collected or shared with any developer server or third-party SDK (Calendar & Location are processed strictly on-device; `INTERNET` permission and Firebase/reCAPTCHA SDKs have been removed).
   - **Ads:** No.
   - **App access:** All functionality is available without special login credentials.
   - **Content rating / Target audience / Store listing:** Add app icon (512×512 PNG), feature graphic (1024×500 PNG), and at least 2 phone screenshots.

> **Note for brand-new personal Google Play Developer accounts:**  
> Before Production release unlocks, Google requires a **Closed testing** track with at least **12 testers opted-in continuously for 14 days**.

---

## Phase 2: Enable Fully Automatic Uploads from GitHub Actions

Once Phase 1 is complete (your app exists in Play Console with at least one uploaded `.aab`), connect a Google Cloud Service Account so future releases publish automatically:

1. **Create a Google Cloud Project & Service Account** (signed in as `ajkiefte@gmail.com`):
   - Open [Google Cloud Console](https://console.cloud.google.com/) and create (or select) a project (e.g., `bahai-calendar-play`).
   - Go to **APIs & Services → Library**, search for **Google Play Android Developer API**, and click **Enable**.
   - Go to **IAM & Admin → Service Accounts → Create Service Account**:
     - Name: `github-play-publisher`
     - Skip optional IAM roles and click **Done**.
   - Open the new service account → **Keys → Add Key → Create new key → JSON**. Download the `.json` file and copy its **email address** (e.g., `github-play-publisher@bahai-calendar-play.iam.gserviceaccount.com`).
2. **Grant the Service Account access in Google Play Console**:
   - In [Google Play Console](https://play.google.com/console), go to **Users and permissions → Invite new users**.
   - Paste the service account email address.
   - Under **App permissions**, add **Bahá'í Calendar** (`eu.kiefte.bahaicalendar`) and grant:
     - **View app information and download bulk reports (read-only)**
     - **Create, edit, and delete draft apps**
     - **Release to testing tracks** (and **Release to production** if you want to promote to production via CI).
   - Click **Invite user → Send invite** (service accounts accept automatically; propagation can take 5–30 minutes).
3. **Add the JSON key as a GitHub Secret in the `google-play` environment**:
   - Run locally (or paste in **GitHub → Settings → Environments → `google-play` → Environment secrets → Add environment secret**):
     ```bash
     gh secret set PLAY_SERVICE_ACCOUNT_JSON --env google-play --repo lapingvino/Bahai-Calendar < /path/to/service-account.json
     ```
   - Delete the downloaded `.json` key from your Downloads folder once stored safely.

---

## Phase 3: Day-to-Day Publishing

- **Push to `main` (including from AI Studio sync):** Runs `build-debug` only (verifies the build without ever touching signing or Play secrets).
- **Create a version tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`):**
  - Runs `release-and-publish` inside the isolated `google-play` environment.
  - Builds signed `.aab` + `.apk` with `versionName = 1.0.0` and auto-incremented `versionCode`.
  - Creates a GitHub Release with the signed binaries attached.
  - Uploads the `.aab` to Google Play's `internal` track (once `PLAY_SERVICE_ACCOUNT_JSON` is set).
- **Manual trigger (GitHub → Actions → Run workflow):** Lets you build a signed `.aab` on demand and pick the exact track (`internal`, `alpha`, `beta`, `production`, or `none`) and status (`draft` or `completed`).
- **Once your app leaves Google Play's initial Draft state:** Set an Environment Variable `PLAY_RELEASE_STATUS` to `completed` (in **Settings → Environments → `google-play` → Environment variables**) so tag-triggered uploads roll out directly to testers without needing manual approval in Play Console.
