with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('systemInstructionText + "\n\n7. USER MEMORY & PERSONALIZED FACTS:\n" + profileData.aiMemory', 'systemInstructionText + "\\n\\n7. USER MEMORY & PERSONALIZED FACTS:\\n" + profileData.aiMemory')

with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'w') as f:
    f.write(content)
