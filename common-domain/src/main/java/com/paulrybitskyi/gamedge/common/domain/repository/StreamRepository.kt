package com.paulrybitskyi.gamedge.common.domain.repository

import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.games.entities.StreamData

interface StreamRepository {
    suspend fun getStreamItems(cursorPage: String?): DomainResult<StreamData>
}
