with open("app/src/main/java/com/example/ui/viewmodel/SparkexViewModel.kt", "r") as f:
    text = f.read()

old_loop = """                var currentText = ""
                val words = fullMessage.text.split(Regex("(?<=\\\\s)"))
                for (word in words) {
                    kotlinx.coroutines.delay(20)
                    currentText += word
                    _currentStreamingMessage.value = streamMsg.copy(text = currentText)
                }"""

new_loop = """                var currentText = ""
                for (char in fullMessage.text) {
                    kotlinx.coroutines.delay(10)
                    currentText += char
                    _currentStreamingMessage.value = streamMsg.copy(text = currentText)
                }"""

text = text.replace(old_loop, new_loop)

with open("app/src/main/java/com/example/ui/viewmodel/SparkexViewModel.kt", "w") as f:
    f.write(text)
