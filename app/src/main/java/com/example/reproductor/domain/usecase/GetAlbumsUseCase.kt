package com.example.reproductor.domain.usecase

import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<List<Album>> {
        return repository.getAllAlbums()
    }
}