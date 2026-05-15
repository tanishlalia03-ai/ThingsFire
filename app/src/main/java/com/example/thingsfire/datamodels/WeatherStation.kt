package com.example.thingsfire.datamodels

data class WeatherStation(
    val channel: Channel,
    val feeds: List<Feed>
)