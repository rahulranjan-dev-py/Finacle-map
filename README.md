# Finacle Desk — Android app

An offline-first Android app for India Post counter staff: step-by-step Finacle
procedures for POSB work (SB, RD, TD, MIS, SCSS, PPF, SSA, NSC/KVP, clearing,
inventory, day-end), the full DOP menu-code list, fixes for common errors, and
a **What changed** tab tracking SB Orders issued by the Department of Posts.

The goal is fewer mistakes at the counter: every procedure marks which steps
are the supervisor's job, warns where the old manual is out of date, and the
search understands menu codes, task names and error messages.

Everything works with **no internet** — the whole reference is bundled inside
the APK and the app makes no network calls at all (it doesn't even ask for the
internet permission). New SB Orders and rate revisions ship as a new APK from
the **Releases** page; share that link and colleagues install over the old
version.

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

This is the routine you'll do most often. Everything lives in
**`app/src/main/assets/finacledesk.html`**, inside the `<script id="DATA">`
block.

1. Find `const NEWS = [` and add the new order at the **top** of the list:

   ```js
   {d:"18 Sep 2026", o:"SB Order 11/2026", t:"One-line title of what the order does", hi:1,
    u:"https://www.indiapost.gov.in/VAS/Pages/sborders.aspx",
    b:"The details, written for the counter. What changes, from when, what to do differently."},
   ```

   - `d` date, `o` order number, `t` title, `b` body (may contain `<b>` for emphasis).
   - `hi:1` marks it **"affects counter work"** (red highlight). Leave it out
     for routine orders.
   - `u` is optional — a link to the order PDF/circular; the app shows an
     "Open the order" link that opens in the phone's browser.
   - Mind the comma after the closing `}` — every entry but the last needs one.

2. Update `let REV = "…"` near the top of the block to today's date (shown in
   the app's masthead and disclaimer).
3. If the quarterly interest rates changed, edit `RATES` and `RATE_ASON` too.
4. Open the HTML in a browser on your computer once to make sure it still
   loads — a missing comma or quote is the usual slip.
5. Commit and push to `main`.

CI then builds a fresh APK onto the **Releases → latest** page. Share that
link (or the file) with colleagues; the new APK installs over the old one.

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

Newer entries added after the original research sit in a second block,
`<script id="DATA2">`, which appends to the same arrays — either place works.
Edit, push to `main`, and the next APK carries it. You can preview the HTML by
opening it in any browser on your computer.

---

## How the app is put together

```
app/src/main/assets/finacledesk.html   ← the whole app: UI + all content (offline)
app/src/main/java/.../MainActivity.java← thin WebView shell, no network code
.github/workflows/build-apk.yml        ← builds + signs + publishes the APK
```

The app is a single HTML file inside a WebView. It requests no permissions;
links inside it open in the phone's browser. Bookmarks, recently-viewed and
the theme choice are stored on the phone. Minimum Android version: 7.0
(API 24).

## Signing

The signing keystore (`signing/finacledesk.jks`) and its password
(`gradle.properties`) are committed **on purpose**, so that every build — CI
or a laptop — signs identically, which is what lets a new APK install over
the old one with nothing to configure.

What that means in practice: the app has no accounts, no network access and
no data of its own, so the key protects nothing except "who may publish an
update that installs over the existing app". With the repo public, anyone
could in principle build such an APK — but they would still have to get a
colleague to install it by hand. Share APKs only from this repo's Releases
page or directly from you, and that risk is contained. If you would rather
not have the key public at all, make the repo private again (Settings →
General → Danger Zone) and share the APK file itself instead of the release
link; nothing in the app depends on the repo being public.

If the key is ever changed, colleagues must uninstall and reinstall once —
Android refuses an update signed with a different key.

## Building locally

Open the project in Android Studio (or run `./gradlew assembleRelease` with
the Android SDK installed). Output: `app/build/outputs/apk/release/`.

---

*Content disclaimer: the app is a working aid, not an authority. Where it and
a current SB Order disagree, the SB Order wins.*
