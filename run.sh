#!/bin/bash

if [ -z "$OPENAI_API_KEY" ]; then
    echo "Set OPENAI_API_KEY first: export OPENAI_API_KEY=your_key"
    exit 1
fi

# Export for background processes
export OPENAI_API_KEY

# Install frontend dependencies
bun install

# Start backend with OpenAI API key
cd ai-resume-builder/backend
./gradlew run &
cd ../..

# Start frontend
bun dev &

# Check for emulator and install Android app
sleep 5
if adb devices | grep -q "emulator"; then
    echo "Emulator detected. Installing Android app..."
    (cd ai-resume-builder/android-app && ./gradlew installDebug && adb shell am start -n com.airesumebuilder/.MainActivity)
else
    echo "No emulator detected. Skipping Android app. Start an emulator and run:"
    echo "cd ai-resume-builder/android-app && ./gradlew installDebug && adb shell am start -n com.airesumebuilder/.MainActivity"
fi

wait
