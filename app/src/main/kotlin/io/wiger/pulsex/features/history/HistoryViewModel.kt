package io.wiger.pulsex.features.history

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.data.SessionRepository
import io.wiger.pulsex.data.local.db.SessionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = sessionRepository.allSessions
        .map { sessions -> HistoryUiState(sessions = sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
        }
    }

    fun restoreSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionRepository.restoreSession(session)
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            sessionRepository.clearAllSessions()
        }
    }
}

@Immutable
data class HistoryUiState(
    val sessions: List<SessionEntity> = emptyList()
)
