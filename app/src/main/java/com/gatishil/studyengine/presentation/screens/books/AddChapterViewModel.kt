package com.gatishil.studyengine.presentation.screens.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Chapter
import com.gatishil.studyengine.domain.model.CreateChapterRequest
import com.gatishil.studyengine.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddChapterUiState(
    val isLoading: Boolean = false,
    val bookId: String = "",
    val title: String = "",
    val startPage: String = "",
    val endPage: String = "",
    val orderIndex: Int = 0,  // 0-based index for backend compatibility
    val existingChapters: List<Chapter> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddChapterViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddChapterUiState())
    val uiState: StateFlow<AddChapterUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("bookId")?.let { bookId ->
            _uiState.update { it.copy(bookId = bookId) }
            loadExistingChapters(bookId)
        }
    }

    private fun loadExistingChapters(bookId: String) {
        viewModelScope.launch {
            bookRepository.getChapters(bookId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val chapters = resource.data
                        _uiState.update {
                            it.copy(
                                existingChapters = chapters,
                                orderIndex = chapters.size  // 0-based: next index after existing
                            )
                        }
                    }
                    is Resource.Error -> {
                        // Ignore errors loading chapters, just start with order 0
                        _uiState.update { it.copy(orderIndex = 0) }
                    }
                    is Resource.Loading -> { /* Loading */ }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateStartPage(page: String) {
        _uiState.update { it.copy(startPage = page) }
    }

    fun updateEndPage(page: String) {
        _uiState.update { it.copy(endPage = page) }
    }

    fun addChapter() {
        val state = _uiState.value

        // Validation
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Chapter title is required") }
            return
        }

        val startPage = state.startPage.toIntOrNull()
        if (startPage == null || startPage <= 0) {
            _uiState.update { it.copy(error = "Valid start page is required") }
            return
        }

        val endPage = state.endPage.toIntOrNull()
        if (endPage == null || endPage <= 0) {
            _uiState.update { it.copy(error = "Valid end page is required") }
            return
        }

        if (endPage < startPage) {
            _uiState.update { it.copy(error = "End page must be greater than or equal to start page") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateChapterRequest(
                title = state.title.trim(),
                startPage = startPage,
                endPage = endPage,
                orderIndex = state.orderIndex
            )

            when (val result = bookRepository.addChapter(state.bookId, request)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    val errorMessage = when {
                        result.message?.contains("400") == true ->
                            "Bad request. Please check the page numbers are valid."
                        result.message?.contains("401") == true ->
                            "Authentication required. Please sign in again."
                        result.message?.contains("404") == true ->
                            "Book not found."
                        result.message?.contains("409") == true ->
                            "Chapter with overlapping pages already exists."
                        else -> result.message ?: "Failed to add chapter"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
                is Resource.Loading -> { /* Already loading */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                title = "",
                startPage = "",
                endPage = "",
                orderIndex = it.existingChapters.size,  // 0-based: next index after existing chapters
                isSuccess = false,
                error = null
            )
        }
    }
}

