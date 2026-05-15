package com.example.thingsfire.DataModels

data class Feed(
    val channel: Channel = Channel(),
    val feeds: List<FeedX> = emptyList()
)
