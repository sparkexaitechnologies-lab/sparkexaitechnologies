import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    text = f.read()

permissions = """    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />"""

text = text.replace('<uses-permission android:name="android.permission.INTERNET" />', permissions)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(text)

