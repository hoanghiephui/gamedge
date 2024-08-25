package com.paulrybitskyi.gamedge.common.data.repository

import com.android.model.StreamData
import com.android.model.UserModel
import com.github.michaelbull.result.mapEither
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.common.data.common.ApiErrorMapper
import com.paulrybitskyi.gamedge.common.data.maper.StreamMapper
import com.paulrybitskyi.gamedge.common.data.maper.mapToDomainStreams
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.repository.StreamRepository
import com.paulrybitskyi.gamedge.igdb.api.stream.StreamEndpoint
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse
import com.paulrybitskyi.gamedge.igdb.api.stream.model.UserDataResponse
import com.paulrybitskyi.hiltbinder.BindType
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BindType
internal class StreamRepositoryImpl @Inject constructor(
    private val streamEndpoint: StreamEndpoint,
    private val dispatcherProvider: DispatcherProvider,
    private val apiErrorMapper: ApiErrorMapper,
    private val streamMapper: StreamMapper
) : StreamRepository {
    override suspend fun getStreamItems(cursorPage: String?): DomainResult<StreamData> {
        return streamEndpoint.getStreamItems(cursorPage)
            .toDataStoreResult()
    }

    override suspend fun getUserInformation(userId: String?): DomainResult<UserModel> {
        return streamEndpoint.getUserInformation(userId)
            .toUserInfoDataStoreResult()

    }

    private suspend fun ApiResult<UserDataResponse>.toUserInfoDataStoreResult(): DomainResult<UserModel> {
        return withContext(dispatcherProvider.io) {
            mapEither(streamMapper::mapToDomainUserInfo, apiErrorMapper::mapToDomainError)
        }
    }

    private suspend fun ApiResult<StreamsResponse>.toDataStoreResult(): DomainResult<StreamData> {
        return withContext(dispatcherProvider.io) {
            mapEither(streamMapper::mapToDomainStreams, apiErrorMapper::mapToDomainError)
        }
    }
}
