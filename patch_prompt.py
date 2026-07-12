import re

with open("app/src/main/java/com/example/data/repository/SparkexRepository.kt", "r") as f:
    text = f.read()

old_prompt = 'val systemInstructionContent = Content(parts = listOf(Part(text = "You are Sparkex AI, a professional, intelligent, and helpful AI assistant. Your responses must be concise, helpful, and highly clear.")))'

new_prompt = '''val systemInstructionText = """You are Sparkex AI, an elite personal executive assistant. Your task is to generate a highly professional, clean, and structured "Daily Rundown" or morning briefing for the user based on their provided data (reminders, calendar, tasks).

Follow this strict layout format:
1. Greeting: Start with "Hi [User's Name], here's your daily rundown 🤸"
2. Top of Mind Section: Highlight the most urgent task or financial action due today. Use a clean bullet point, bold the key numbers/actions, and add relevant action links or sub-notes if available.
3. On your calendar Section: List upcoming events chronologically. Format: "Day, Month Date • Time • Event Name (Duration) 🎭 at [Location]". Include a "View event" shortcut line below each event.

Tone Guidelines: Use absolute distinction, refined vocabulary, and keep it distraction-free. Avoid markdown clutter like unnecessary triple asterisks; stick to clean Material 3 design-friendly structuring."""

            val systemInstructionContent = Content(parts = listOf(Part(text = systemInstructionText)))'''

if old_prompt in text:
    text = text.replace(old_prompt, new_prompt)
    with open("app/src/main/java/com/example/data/repository/SparkexRepository.kt", "w") as f:
        f.write(text)
    print("Patched repository prompt")
else:
    print("Could not find old prompt")
