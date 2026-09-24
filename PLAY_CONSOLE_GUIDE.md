# Google Play Console પર એપ મુકવા માટેની સંપૂર્ણ માર્ગદર્શિકા (Step-by-Step Guide)

આ માર્ગદર્શિકા તમને તમારા **Parallax 3D Wallpaper** એપને Google Play Console પર સફળતાપૂર્વક અપલોડ અને પબ્લિશ કરવા માટે મદદ કરશે. આ પ્રોજેક્ટ Google ના તમામ નવા નિયમો (**Target SDK 35 / Android 15 Ready, App Bundle format, Data Safety**) મુજબ પહેલેથી જ કન્ફિગર કરેલો છે.

---

## ૧. રીલીઝ કી-સ્ટોર (Keystore) જનરેટ કરવું

Google Play Store પર એપ અપલોડ કરવા માટે એપનું સહી થયેલું (Signed) બંડલ હોવું જરૂરી છે.

નીચે આપેલ કમાન્ડ ટર્મિનલ (PowerShell) માં રન કરો:

```powershell
keytool -genkey -v -keystore app/release-key.jks -alias androidkey -keyalg RSA -keysize 2048 -validity 10000
```
> [!NOTE]
> - આ કમાન્ડ રન કરતી વખતે પાસવર્ડ પૂછશે (દા.ત. `myAppPass123`). પાસવર્ડ યાદ રાખવો અથવા ક્યાંક નોંધી રાખવો.
> - `release-key.jks` ફાઇલ તમારા `app/` ફોલ્ડરમાં સેવ થશે. આ ફાઇલ ક્યારેય ડિલીટ ન કરવી, કારણ કે ભવિષ્યમાં એપ અપડેટ કરવા માટે આ જ કી ની જરૂર પડશે.

---

## ૨. Android App Bundle (.aab) તૈયાર કરવું

Google Play Console હવે `.apk` સ્વીકારતું નથી, તેના માટે `.aab` (Android App Bundle) જરૂરી છે.

ટર્મિનલમાં આ કમાન્ડ ચલાવો:
```powershell
$env:KEYSTORE_PASSWORD="તમારો_પાસવર્ડ"
$env:KEY_ALIAS="androidkey"
$env:KEY_PASSWORD="તમારો_પાસવર્ડ"
.\gradlew.bat bundleRelease
```
અથવા જો તમે Android Studio વાપરો છો:
1. મેનુમાં જાઓ: **Build** > **Generate Signed Bundle / APK...**
2. **Android App Bundle** સિલેક્ટ કરીને **Next** આપો.
3. તમે બનાવેલ `release-key.jks` ફાઇલ અને પાસવર્ડ પસંદ કરો.
4. **release** વેરિઅન્ટ પસંદ કરીને **Finish** આપો.

તમારું તૈયાર થયેલું `.aab` બંડલ અહીં મળશે:
📁 `app/build/outputs/bundle/release/app-release.aab`

---

## ૩. Google Play Console માં નવું App બનાવવું

1. [Google Play Console](https://play.google.com/console) માં લૉગિન કરો.
2. **Create app** પર ક્લિક કરો.
3. વિગતો ભરો:
   - **App name**: `Parallax 3D Wallpaper - 4K & Live Wallpapers`
   - **Default language**: English (United States) અથવા તમારી પસંદ મુજબ
   - **App or Game**: App
   - **Free or Paid**: Free
   - Declarations સ્વીકારીને **Create app** કરો.

---

## ૪. Set up your app (મહત્વપૂર્ણ સેટિંગ્સ)

Google Play Console ના ડેશબોર્ડ પર **Set up your app** સેક્શન પૂર્ણ કરવું જરૂરી છે:

### A. Privacy Policy (ગોપનીયતા નીતિ)
- Google Play સ્ટોર પર Privacy Policy લિંક આપવી ફરજિયાત છે.
- તમે [Privacypolicies.com](https://www.privacypolicies.com/) અથવા GitHub Pages / Firebase Hosting પર ફ્રીમાં હોસ્ટ કરી શકો છો.
- આપણી એપમાં `SettingsScreen.kt` માં સંપૂર્ણ Privacy Policy ટેક્સ્ટ પહેલેથી જ સામેલ છે, તે જ ટેક્સ્ટ તમે તમારા બ્લોગ કે પેજ પર મૂકીને લિંક સેટ કરી શકો છો.

### B. App Access
- સિલેક્ટ કરો: **"All functionality is available without special access"** (કારણ કે આ એપમાં કોઈ લૉગિન કે પ્રાઇવેટ ક્રેડેન્શિયલ જરૂરી નથી).

### C. Ads (જાહેરાતો)
- જો તમે AdMob ઉમેર્યું નથી તો **"No, my app does not contain ads"** સિલેક્ટ કરો.
- ભવિષ્યમાં એડ્સ ઉમેરો તો Yes કરી શકો છો.

### D. Content Ratings (કન્ટેન્ટ રેટિંગ)
- પ્રશ્નાવલી શરૂ કરો (Start questionnaire).
- તમારો ઈમેઈલ એડ્રેસ લખો.
- કેટેગરી: **Utility, Productivity, Communication, or Other**.
- હિંસા, અશ્લીલતા કે ડ્રગ્સ જેવા તમામ પ્રશ્નોમાં **No** સિલેક્ટ કરો.
- Save કરીને **Next** આપો અને રેટિંગ સબમિટ કરો (તમને `Everyone / 3+` નું રેટિંગ મળશે).

### E. Target Audience (લક્ષ્ય પ્રેક્ષકો)
- Target age: **13-15, 16-17, 18 and above** (13+ રાખવાથી બાળકો માટેની કડક નીતિઓ લાગુ નહીં પડે).
- Neutral age screen: **No**.

### F. Data Safety Form (ડેટા સેફ્ટી)
આ સેક્શનમાં આ મુજબના જવાબો આપો:
1. *Does your app collect or share any user data?* -> **No** (આપણી એપ કોઈ વ્યક્તિગત ડેટા કલેક્ટ કે સર્વર પર સ્ટોર કરતી નથી).
2. *All data collected is ephemeral?* -> N/A.
3. *Sensor data*: માત્ર ડિવાઇસ પર લોકલી ટિલ્ટ કેલ્ક્યુલેટ થાય છે, ક્યાંય મોકલવામાં આવતું નથી.

---

## ૫. Store Presence (સ્ક્રીનશોટ્સ અને ગ્રાફિક્સ)

Play Store ના લિસ્ટિંગ માટે આ વસ્તુઓ જરૂરી રહેશે:
1. **App Icon**: 512 x 512 px PNG (32-bit color, up to 1024KB).
2. **Feature Graphic**: 1024 x 500 px PNG/JPEG.
3. **Phone Screenshots**: ઓછામાં ઓછા 2 થી 8 સ્ક્રીનશોટ્સ (1080 x 2400 px આસપાસના રેશિયોમાં).
4. **Short Description** (80 અક્ષરો સુધી):
   > *Stunning 3D Parallax live wallpapers with dynamic tilt depth and 4K AMOLED themes.*
5. **Full Description** (4000 અક્ષરો સુધી):
   > *Transform your home screen with Parallax 3D Wallpaper! Experience real-time multi-layer gyroscopic depth wallpapers, AMOLED dark themes, Cyberpunk neon, nature, and space backgrounds. Set wallpapers directly to Home and Lock screens or apply interactive 3D live wallpapers.*

---

## ૬. Production / Testing Release માં `.aab` અપલોડ કરવું

1. ડાબી બાજુના મેનુમાંથી **Release** > **Production** (અથવા **Closed Testing**) પર ક્લિક કરો.
2. **Create new release** પસંદ કરો.
3. તમારું `app-release.aab` અપલોડ કરો.
4. Release name આપો (દા.ત. `1.0.0`).
5. Release notes લખો (દા.ત. `Initial release of Parallax 3D Wallpaper App`).
6. **Next** આપીને **Save** અને **Start rollout** કરો.

> [!IMPORTANT]
> જો તમારું Google Play Console એકાઉન્ટ નવું પર્સનલ એકાઉન્ટ (Personal Developer Account) હોય, તો Google ના નવા નિયમ મુજબ Production પહેલાં **14 દિવસ માટે 20 ટેસ્ટર્સ (Closed Testing)** જરૂરી હોઈ શકે છે.
