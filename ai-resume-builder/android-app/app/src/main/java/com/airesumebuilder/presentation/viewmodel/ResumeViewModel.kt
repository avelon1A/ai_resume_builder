package com.airesumebuilder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airesumebuilder.domain.model.*
import com.airesumebuilder.domain.repository.GenerateResumeResponse
import com.airesumebuilder.domain.repository.ResumeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResumeViewModel(
    private val resumeRepository: ResumeRepository
) : ViewModel() {

    private val _resumeState = MutableStateFlow<Resource<GenerateResumeResponse>>(Resource.Loading)
    val resumeState: StateFlow<Resource<GenerateResumeResponse>> = _resumeState.asStateFlow()

    private val _resumesList = MutableStateFlow<Resource<List<Resume>>>(Resource.Loading)
    val resumesList: StateFlow<Resource<List<Resume>>> = _resumesList.asStateFlow()

    private val _coverLetterState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val coverLetterState: StateFlow<Resource<String>> = _coverLetterState.asStateFlow()

    private val _analysisState = MutableStateFlow<Resource<ResumeAnalysis>>(Resource.Loading)
    val analysisState: StateFlow<Resource<ResumeAnalysis>> = _analysisState.asStateFlow()

    private val _currentResume = MutableStateFlow<Resume?>(null)
    val currentResume: StateFlow<Resume?> = _currentResume.asStateFlow()

    fun generateResume(request: GenerateResumeRequest) {
        viewModelScope.launch {
            _resumeState.value = Resource.Loading
            _resumeState.value = resumeRepository.generateResume(request)
        }
    }

    fun generateCoverLetter(request: GenerateCoverLetterRequest) {
        viewModelScope.launch {
            _coverLetterState.value = Resource.Loading
            _coverLetterState.value = resumeRepository.generateCoverLetter(request)
        }
    }

    fun analyzeResume(resumeText: String) {
        viewModelScope.launch {
            _analysisState.value = Resource.Loading
            _analysisState.value = resumeRepository.analyzeResume(resumeText)
        }
    }

    fun loadResumes() {
        viewModelScope.launch {
            _resumesList.value = Resource.Loading
            _resumesList.value = resumeRepository.getResumes()
        }
    }

    fun loadResume(resumeId: String) {
        viewModelScope.launch {
            when (val result = resumeRepository.getResume(resumeId)) {
                is Resource.Success -> _currentResume.value = result.data
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun updateResume(resumeId: String, title: String, content: String, template: String) {
        viewModelScope.launch {
            when (val result = resumeRepository.updateResume(resumeId, title, content, template)) {
                is Resource.Success -> {
                    _currentResume.value = result.data
                    loadResumes()
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            resumeRepository.deleteResume(resumeId)
            loadResumes()
        }
    }

    fun clearResumeState() {
        _resumeState.value = Resource.Loading
    }

    fun clearCoverLetterState() {
        _coverLetterState.value = Resource.Loading
    }
}
