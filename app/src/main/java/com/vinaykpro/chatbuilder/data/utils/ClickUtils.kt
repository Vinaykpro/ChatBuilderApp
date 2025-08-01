package com.vinaykpro.chatbuilder.data.utils

object DebounceClickHandler {
    private var lastClickTime = 0L

    fun run(debounceTime: Long = 500L, block: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= debounceTime) {
            lastClickTime = now
            block()
        }
    }
}

