package skacce.rs.kollago.input.text

interface TextInputProvider {
    enum class InputType {
        TEXT, NUMBER, PASSWORD
    }

    fun registerListener(textInputListener: TextInputListener)
    fun removeListener(textInputListener: TextInputListener)
    fun openTextInput(placeholder: String, text: String, type: InputType, maxChars: Int)
    fun closeTextInput()
}
