package com.example.thingsfire.Model

import com.example.thingsfire.DataModels.Feed
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("channels/{channelId}/feeds.json")
    suspend fun getChannelFeed(
        @Path("channelId") channelId: String,
        @Query("results") results: Int = 100
    ): Feed

    @GET("channels/{channelId}/fields/{fieldNumber}.json")
    suspend fun getFieldFeed(
        @Path("channelId") channelId: String,
        @Path("fieldNumber") fieldNumber: Int,
        @Query("results") results: Int = 100
    ): Feed
}
