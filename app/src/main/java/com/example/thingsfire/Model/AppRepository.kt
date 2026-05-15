package com.example.thingsfire.Model

import com.example.thingsfire.DataModels.Feed

class AppRepository(
    private val apiService: ApiService = RetrofitInstance.apiService
) {
    suspend fun getChannelFeed(channelId: String): Feed {
        return apiService.getChannelFeed(channelId = channelId)
    }

    suspend fun getFieldFeed(channelId: String, fieldNumber: Int): Feed {
        return apiService.getFieldFeed(
            channelId = channelId,
            fieldNumber = fieldNumber
        )
    }
}
