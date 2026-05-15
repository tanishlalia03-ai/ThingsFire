package com.example.thingsfire.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thingsfire.DataModels.Feed
import com.example.thingsfire.Model.AppRepository
import kotlinx.coroutines.launch

data class ChannelUiState(
    val isLoading: Boolean = true,
    val channelFeed: Feed? = null,
    val errorMessage: String? = null
)

data class FieldChartUiState(
    val isLoading: Boolean = true,
    val fieldFeed: Feed? = null,
    val errorMessage: String? = null
)

class ChannelViewModel(
    private val channelId: String,
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ChannelUiState())
        private set

    init {
        loadChannel()
    }

    fun refresh() {
        loadChannel()
    }

    private fun loadChannel() {
        viewModelScope.launch {
            uiState = ChannelUiState(isLoading = true)

            runCatching {
                repository.getChannelFeed(channelId)
            }.onSuccess { feed ->
                uiState = ChannelUiState(channelFeed = feed, isLoading = false)
            }.onFailure { error ->
                uiState = ChannelUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load channel data."
                )
            }
        }
    }

    class Factory(
        private val channelId: String,
        private val repository: AppRepository = AppRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChannelViewModel(channelId, repository) as T
        }
    }
}

class FieldChartViewModel(
    private val channelId: String,
    private val fieldNumber: Int,
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    var uiState by mutableStateOf(FieldChartUiState())
        private set

    init {
        loadField()
    }

    fun refresh() {
        loadField()
    }

    private fun loadField() {
        viewModelScope.launch {
            uiState = FieldChartUiState(isLoading = true)

            runCatching {
                repository.getFieldFeed(channelId, fieldNumber)
            }.onSuccess { feed ->
                uiState = FieldChartUiState(fieldFeed = feed, isLoading = false)
            }.onFailure { error ->
                uiState = FieldChartUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load field data."
                )
            }
        }
    }

    class Factory(
        private val channelId: String,
        private val fieldNumber: Int,
        private val repository: AppRepository = AppRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FieldChartViewModel(channelId, fieldNumber, repository) as T
        }
    }
}
