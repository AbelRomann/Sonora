package com.example.reproductor.domain.usecase

import com.example.reproductor.domain.repository.MusicRepository
import javax.inject.Inject

class RefreshMusicUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke() {
        repository.refreshMusic()
    }
}