package com.example.thingsfire.DataModels

data class WeatherStation(
    val channel: Channel = Channel(),
    val feeds: List<FeedX> = emptyList()
)
