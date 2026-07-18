import re

with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('Result.failure(lastException ?: Exception("Network request failed after $maxAttempts attempts"))', 'Result.failure(com.example.util.ChatErrorHandler.handleException(lastException ?: Exception("Network request failed after $maxAttempts attempts")))')

content = content.replace('Result.failure(e)\n        }\n    }', 'Result.failure(com.example.util.ChatErrorHandler.handleException(e))\n        }\n    }')

with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'w') as f:
    f.write(content)
