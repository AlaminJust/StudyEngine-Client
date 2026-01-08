package com.gatishil.studyengine.presentation.screens.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Book
import com.gatishil.studyengine.domain.model.CreateBookRequest
import com.gatishil.studyengine.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BooksUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String? = null
)

data class AddBookUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val subject: String = "",
    val totalPages: String = "",
    val difficulty: Int = 1,
    val priority: Int = 1,
    val targetEndDate: LocalDate? = null,
    val error: String? = null,
    val createdBookId: String? = null
)

data class BookDetailUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val error: String? = null
)

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        observeBooks()
    }

    private fun observeBooks() {
        viewModelScope.launch {
            bookRepository.getBooks().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(books = resource.data, isLoading = false)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message, isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            bookRepository.refreshBooks()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject) }
    }

    fun updateTotalPages(pages: String) {
        _uiState.update { it.copy(totalPages = pages) }
    }

    fun updateDifficulty(difficulty: Int) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun updatePriority(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun updateTargetEndDate(date: LocalDate?) {
        _uiState.update { it.copy(targetEndDate = date) }
    }

    fun createBook() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }

        if (state.subject.isBlank()) {
            _uiState.update { it.copy(error = "Subject is required") }
            return
        }

        val totalPages = state.totalPages.toIntOrNull()
        if (totalPages == null || totalPages <= 0) {
            _uiState.update { it.copy(error = "Valid page count is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateBookRequest(
                title = state.title.trim(),
                subject = state.subject.trim(),
                totalPages = totalPages,
                difficulty = state.difficulty,
                priority = state.priority,
                targetEndDate = state.targetEndDate
            )

            when (val result = bookRepository.createBook(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, createdBookId = result.data.id)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = bookRepository.getBookById(bookId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(book = result.data, isLoading = false)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun deleteBook(bookId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = bookRepository.deleteBook(bookId)) {
                is Resource.Success -> {
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

