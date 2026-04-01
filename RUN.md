# Run Commands

## One Command to Run Everything

```bash
export OPENAI_API_KEY=YOUR_KEY_HERE
bun install && (cd ai-resume-builder/backend && OPENAI_API_KEY=$OPENAI_API_KEY ./gradlew run) & bun dev & (cd ai-resume-builder/android-app && ./gradlew installDebug && adb shell am start -n com.airesumebuilder/.MainActivity)
```

This starts:
- **Frontend** at `http://localhost:3000`
- **Backend** at `http://localhost:8080`
- **Android App** on emulator/device

### Android Prerequisites (first time only)

```bash
cd ai-resume-builder/android-app
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```
