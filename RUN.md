# Run Commands

## One Command to Run Everything

```bash
./run.sh
```

This starts:
- **Frontend** at `http://localhost:3000`
- **Backend** at `http://localhost:8080`
- **Android App** on emulator (if running)

### Android Prerequisites (first time only)

```bash
cd ai-resume-builder/android-app
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```
