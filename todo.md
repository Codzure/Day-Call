# Day Call – TODO

## Product Vision
Wake with vibes. Live with intention. Day Call helps Gen Z start each day with style, mindfulness, and community.

---

## Epic 1 — Alarm Experience
- [ ] Set Alarm: single or recurring alarms with custom name, tone, and vibe
- [ ] Vibe Selector: choose alarm themes (Chill, Hustle, Cosmic, Nature, Chaos)
- [ ] Tone Packs: built-in sounds, custom upload, or Spotify link
- [ ] Smart Volume: gradual sound increase for gentle wake
- [ ] Mood-Responsive: auto-select vibe based on mood/sleep data

## Epic 2 — Wake-Up Challenges
- [ ] Mini-Games: puzzles, emoji matches, meme tests to dismiss alarm
- [ ] Physical Challenges: shake phone or walk steps (Health API)
- [ ] Custom Challenges: user-created tasks (text/audio)
- [ ] Anti-Snooze Mode: lock snooze until task completed

## Epic 3 — Daily Vibes & Intentions
- [ ] Mood Picker: choose how you feel on wake (emoji slider)
- [ ] Daily Prompt: 5-second intention question
- [ ] Vibe Card: generate daily vibe card from alarm, mood, prompt
- [ ] Mindset Journal: light journaling with auto summaries

## Epic 4 — Social Wake-Up Circle
- [ ] Wake-Up Circle: group alarms with friends (e.g., “7AM Club”)
- [ ] Message Drops: leave audio/video notes post-alarm
- [ ] Streak Leaderboard: track group consistency
- [ ] Alarm Reminders: gentle ping if a friend misses wake-up

## Epic 5 — Gamification & Motivation
- [ ] Wake Streaks: track personal consistency
- [ ] Unlockables: earn new vibes, tones, pets, and themes
- [ ] Wake for a Cause: donate/impact on streak milestones
- [ ] Shareable Cards: post vibes, moodboard, or streaks

## Epic 6 — Sound & Media Personalization
- [ ] Spotify Integration: wake to playlists
- [ ] AI Tones: daily AI-generated alarm sounds
- [ ] Alarm Mixer: blend voice notes, lo-fi, nature, etc.
- [ ] Offline Support: reliable fallback tones without internet

## Epic 7 — Neurodiverse-Friendly Mode
- [ ] Sensory Mode: low-stimulation visuals & soft tones
- [ ] Delay Logic: gentle ramping notifications
- [ ] Custom Alarm Flow: flexible dismiss options
- [ ] Haptic Feedback: tuned tactile cues (toggleable)

## Epic 8 — Sleep Trends & Journaling
- [ ] Sleep Log: record alarm time, dismiss delay, mood
- [ ] Mood Trends: visualize mood over time
- [ ] Weekly Digest: weekly “How was your week?” report
- [ ] Dream Journal: optional voice/text dream entries

## Future Wishlist
- [ ] Dream-to-Vibe AI: generate tones from recorded dreams
- [ ] Morning Selfie Tracker: track your morning look
- [ ] Alarm Pet: virtual buddy evolving with your habits
- [ ] Breath-to-Wake Mode: calm breathing animation wake-ups
- [ ] Weather-Based Alarm: match tone to local weather 

## Design System — UI Components & Styles
- [ ] Define global Shapes (RoundedCornerShape 16.dp) in DayCallTheme
- [ ] Create reusable DayCallCard composable with recommended modifiers (rounded 16dp, 2dp shadow, padding 16dp, optional border & adaptive background)
- [ ] Refactor AlarmItem to use DayCallCard for consistent styling
- [ ] Implement adaptive background color logic based on item status (Resolved, Pending, etc.) 