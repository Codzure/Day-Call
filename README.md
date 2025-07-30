# Day Call - Fully Functional Challenge-Based Alarm App

A modern alarm app that forces users to solve challenges to dismiss their alarms, ensuring they're fully awake before the alarm stops. Now with **full audio integration** and **real alarm scheduling**.

## 🎵 **Audio Integration**

### **16 High-Quality Alarm Sounds**
- **Brain Teaser** - Labyrinth for the brain
- **Sci-Fi Circuits** - Futuristic circuit sounds
- **Cinematic Whoosh** - Epic transition sounds
- **Mega Horn** - Powerful cinematic horn
- **Downfall** - Dramatic orchestral piece
- **Rainy Day** - Peaceful nature sounds
- **Dark Future** - Atmospheric sci-fi
- **Reliable Safe** - Trustworthy tones
- **Relaxing Guitar** - Calming acoustic
- **And 7 more unique sounds...**

### **Audio Features**
- **Looping Playback** - Audio continues until challenge solved
- **Volume Escalation** - Automatically increases every 5 seconds
- **No Volume Control** - Users cannot reduce volume during challenge
- **High-Quality Files** - Professional audio from premium sources

## 🎯 **Challenge-Based Alarm Dismissal**

### **5 Challenge Types**
1. **Math Problems** - Solve arithmetic problems (addition, subtraction, multiplication, division)
2. **Memory Challenges** - Remember and repeat number sequences
3. **Pattern Recognition** - Complete number patterns (Fibonacci, powers of 2, etc.)
4. **Word Puzzles** - Unscramble morning-related words
5. **Logic Problems** - Solve logical reasoning questions

### **Challenge Features**
- **Random Generation** - Each challenge is dynamically created
- **30-Second Timer** - Time pressure adds urgency
- **New Challenges** - Failed challenges automatically generate new ones
- **No Easy Way Out** - Must solve to stop alarm

## 🔔 **Real Alarm System**

### **Android AlarmManager Integration**
- **Exact Alarm Scheduling** - Precise timing using Android's AlarmManager
- **Wake Lock Support** - Ensures alarms trigger even when device is sleeping
- **Repeating Alarms** - Set alarms for specific days of the week
- **Background Processing** - Alarms work even when app is closed

### **Alarm Features**
- **Custom Labels** - Name your alarms
- **Repeat Days** - Set alarms for specific days
- **Enable/Disable** - Toggle alarms on and off
- **Audio Selection** - Choose from 16 different alarm sounds
- **Challenge Types** - Select which type of challenge to solve

## 🎨 **Modern UI**

### **Material You Design**
- **Dynamic Theming** - Adapts to system colors
- **Smooth Animations** - Engaging transitions and effects
- **Gradient Backgrounds** - Beautiful visual design
- **Responsive Layout** - Works on all screen sizes

### **User Experience**
- **Intuitive Interface** - Easy to create and manage alarms
- **Visual Feedback** - Clear indicators for alarm status
- **Test Functionality** - Try alarms immediately with play button
- **Modern Cards** - Clean, organized alarm management

## 🛠 **Technical Architecture**

### **Built With**
- **Jetpack Compose** - Modern UI framework
- **Material 3** - Latest design system
- **Room Database** - Persistent alarm storage
- **Android AlarmManager** - System-level alarm scheduling
- **MediaPlayer** - High-quality audio playback
- **Kotlin Coroutines** - Asynchronous operations

### **Key Components**
- **AlarmScheduler** - Handles system alarm scheduling
- **AudioManager** - Manages audio playback and volume
- **AlarmRingingActivity** - Full-screen challenge interface
- **AlarmReceiver** - Broadcast receiver for alarm triggers
- **ChallengeGenerator** - Creates random challenges

## 📱 **How It Works**

1. **Create an Alarm** - Set time, label, repeat days, audio file, and challenge type
2. **Alarm Schedules** - System schedules the alarm using Android's AlarmManager
3. **Alarm Triggers** - When time comes, AlarmRingingActivity launches
4. **Audio Plays** - Selected audio file starts looping with increasing volume
5. **Challenge Appears** - Random challenge must be solved to stop alarm
6. **Volume Escalates** - Every 5 seconds, volume increases automatically
7. **Solve to Stop** - Only solving the challenge stops the audio
8. **Snooze Option** - After solving, snooze button becomes available

## 🚀 **Getting Started**

1. **Clone the repository**
2. **Open in Android Studio**
3. **Build and run on your device**
4. **Create your first alarm** with custom audio and challenge
5. **Test the system** using the play button on any alarm
6. **Experience the full alarm** when your scheduled time arrives

## 🔧 **Permissions Required**

- **SCHEDULE_EXACT_ALARM** - For precise alarm scheduling
- **USE_EXACT_ALARM** - For reliable alarm triggering
- **WAKE_LOCK** - To wake device for alarms
- **VIBRATE** - For vibration feedback

## 🎮 **Challenge System**

The app uses a sophisticated challenge generation system that creates:
- **Math problems** with random operations and numbers
- **Memory sequences** of 4 random numbers
- **Pattern challenges** with mathematical sequences
- **Word scrambles** of morning-related vocabulary
- **Logic puzzles** requiring critical thinking

All challenges are designed to require active engagement and wake up the user's brain!

## 🔮 **Future Enhancements**

- **Shake challenges** using device sensors
- **Photo challenges** requiring specific actions
- **Social challenges** with friends
- **Custom challenge creation**
- **Challenge difficulty progression**
- **Achievement system** for successful wake-ups
- **Spotify integration** for custom music
- **Weather-based alarms**

---

**Wake with intention. Solve with purpose.** 