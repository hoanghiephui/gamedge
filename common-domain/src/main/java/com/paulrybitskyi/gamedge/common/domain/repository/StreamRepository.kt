package com.paulrybitskyi.gamedge.common.domain.repository

import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.android.model.StreamData

interface StreamRepository {
    suspend fun getStreamItems(cursorPage: String?): DomainResult<StreamData>
}
