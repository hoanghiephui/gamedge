package com.paulrybitskyi.gamedge.common.domain.repository

import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.android.model.StreamData
import com.android.model.UserModel

interface StreamRepository {
    suspend fun getStreamItems(cursorPage: String?): DomainResult<StreamData>
    suspend fun getUserInformation(userId: String? = null): DomainResult<UserModel>
}
