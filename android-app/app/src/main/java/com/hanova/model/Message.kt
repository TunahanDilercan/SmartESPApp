package com.hanova.model

data class Message(
    val sender: String,
    val content: String,
    val timestamp: String,
    val isFromUser: Boolean
)
