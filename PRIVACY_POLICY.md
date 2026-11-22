# Privacy Policy for Bahá'í Calendar

**Effective Date:** November 22, 2026  
**App Package ID:** `eu.kiefte.bahaicalendar`  
**Developer Contact:** ajkiefte@gmail.com

Bahá'í Calendar ("the App") is designed with privacy first. The App operates locally on your device, does not require user accounts, contains no advertisements, includes no analytics or tracking SDKs, and does not transmit your personal data to any developer-controlled server.

## Permissions & Data Usage

### 1. Calendar (`READ_CALENDAR`, `WRITE_CALENDAR`)
- **Purpose:** Used strictly on your device to display your existing Android/Google Calendar appointments alongside Badí' dates, Holy Days, and Feast schedules, and to let you add or edit calendar appointments directly from the App and home screen widgets.
- **Handling:** All calendar data is read from and written directly to Android's local Calendar Provider (`CalendarContract`). The App never uploads, shares, or sells your calendar data.

### 2. Location (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- **Purpose:** Used optionally to calculate accurate local sunrise and sunset times (since the Badí' day begins at sunset), fasting times, and the Qiblih compass direction.
- **Handling:** Location coordinates are stored only in private local app preferences (`SharedPreferences`) on your device. You may also select a preset city manually without granting location permission. When automatic location detection is used, the App uses Android's built-in system `Geocoder` service to resolve your coordinates into a city/country name; that lookup is handled by your device's operating system.

### 3. Vibration (`VIBRATE`)
- **Purpose:** Used only for optional tactile feedback (such as the chanting counter).

## Network Access & Third-Party Services
- The App does **not** request the `INTERNET` permission and makes **no** direct network requests.
- The App includes **no** third-party advertising, analytics, crash-reporting, or tracking libraries.

## Data Retention & Deletion
- Because all app settings and saved location preferences are stored exclusively on your device, you can delete all stored app data at any time by clearing the App's storage in Android Settings or uninstalling the App.

## Children's Privacy
- The App does not knowingly collect any personal information from anyone, including children under 13.

## Changes to This Policy
- Any updates to this Privacy Policy will be posted on this page with an updated effective date.

## Contact
If you have any questions about this Privacy Policy, please contact: **ajkiefte@gmail.com**.
