# Finacle Desk — Android app

An offline-first Android app for India Post counter staff: step-by-step Finacle
procedures for POSB work (SB, RD, TD, MIS, SCSS, PPF, SSA, NSC/KVP, clearing,
inventory, day-end), the full DOP menu-code list, fixes for common errors, and
a **What changed** tab tracking SB Orders issued by the Department of Posts.

The goal is fewer mistakes at the counter: every procedure marks which steps
are the supervisor's job, warns where the old manual is out of date, and the
search understands menu codes, task names and error messages.

Everything works with **no internet** — the whole reference is bundled inside
the APK. When the phone does have network, the app also checks this repository
for a newer `updates.json` (new SB Orders, revised interest rates) and applies
it without needing a reinstall.

---

## Getting the APK

Every push to `main` builds a signed APK automatically (GitHub Actions).

1. Go to the repo's **Releases** page and open **"Finacle Desk — latest APK"**
   (or open the **Actions** tab → newest "Build APK" run → download the
   `finacle-desk-apk` artifact).
2. Download `finacle-desk.apk` and send it to the phone (WhatsApp, USB, etc.).
3. On the phone, tap the file and install. Android will ask once to allow
   installs from that app ("unknown sources") — allow it.
4. Newer APKs install **over** the old one (same signing key, rising version
   number). No uninstall needed; nothing is lost.

---

## Adding a new SB Order / DoP update

This is the routine you'll do most often. One file holds all of it:

**`app/src/main/assets/updates.json`**

1. Add the new order at the **top** of the `"news"` array:

   ```json
   {
    "d": "18 Aug 2026",
    "o": "SB Order 11/2026",
    "t": "One-line title of what the order does",
    "b": "The details, written for the counter. What changes, from when, what to do differently.",
    "hi": 1,
    "u": "https://www.indiapost.gov.in/VAS/Pages/sborders.aspx"
   }
   ```

   - `d` date, `o` order number, `t` title, `b` body.
   - `hi: 1` marks it **"affects counter work"** (red highlight). Leave it out
     for routine orders.
   - `u` is optional — a link to the order PDF/circular; the app shows an
     "Open the order" link that opens in the phone's browser.

2. **Increase `"version"` by 1** (top of the file). The app only applies data
   with a higher version than what it already has — forget this and phones
   will ignore the change.
3. Update `"rev"` to today's date (shown in the app's masthead and disclaimer).
4. If the quarterly interest rates changed, edit `"rates"` and `"rateAson"` too.
5. Commit and push to `main`.

What happens next:

- **CI builds a fresh APK** with the new data baked in (Releases → latest).
- **Phones that already have the app** fetch the new `updates.json` over the
  air on next launch — *if the file is publicly reachable* (see below).

> **Note — this repo is currently private.** Over-the-air updates fetch
> `updates.json` from this repo's raw URL, which requires the repo to be
> **public**. Until then, updates reach colleagues only via a rebuilt APK.
> Two options:
> 1. Make the repo public (Settings → General → Danger Zone → Change
>    visibility) — OTA then works as-is, nothing to change.
> 2. Keep it private and host `updates.json` anywhere public (e.g. a GitHub
>    Gist), then point `UPDATES_URL` in
>    `app/src/main/java/com/finacledesk/app/MainActivity.java` at that URL.

Where to watch for new orders: the India Post **SB Orders** page
(<https://www.indiapost.gov.in/VAS/Pages/sborders.aspx>) and your circle's
official channels.

---

## Editing procedures, menu codes, fixes, reference tables

That content lives in **`app/src/main/assets/finacledesk.html`** inside the
`<script id="DATA">` block — plain JavaScript arrays with comments:

- `PROC` — step-by-step procedures (`sup:1` = supervisor step, gold rail)
- `MENUS` — the menu-code list by group
- `FIX` — error → what actually clears it
- `RATES`, `SCHEMES`, `OFFACC`, `CHAPTERS` — reference tables

Edit, push to `main`, and the next APK carries it. (These need a reinstall to
reach phones — only `updates.json` travels over the air.) You can preview the
HTML by opening it in any browser on your computer.

---

## How the app is put together

```
app/src/main/assets/finacledesk.html   ← the whole reference UI (offline)
app/src/main/assets/updates.json       ← SB Orders + rates (the file you edit)
app/src/main/java/.../MainActivity.java← WebView shell + update fetcher
.github/workflows/build-apk.yml        ← builds + signs + publishes the APK
signing/finacledesk.jks                ← shared signing key (see note)
```

On launch the app loads the HTML, then applies the newest of: bundled
`updates.json` → last downloaded copy cached on the phone → freshly fetched
copy. Links inside the app open in the phone's browser. Minimum Android
version: 7.0 (API 24).

**Signing note:** the keystore and its password are committed on purpose so
that every build — CI or a teammate's laptop — signs identically, which is
what lets new APKs install over old ones. The key signs this internal app
and protects nothing else. If it ever leaks in a way that worries you,
generate a new keystore; colleagues then uninstall/reinstall once.

## Building locally

Open the project in Android Studio (or run `./gradlew assembleRelease` with
the Android SDK installed). Output: `app/build/outputs/apk/release/`.

---

*Content disclaimer: the app is a working aid, not an authority. Where it and
a current SB Order disagree, the SB Order wins.*
