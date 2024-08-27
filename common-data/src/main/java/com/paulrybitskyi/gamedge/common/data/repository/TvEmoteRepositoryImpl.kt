package com.paulrybitskyi.gamedge.common.data.repository

import com.android.model.BetterTTVChannelEmotes
import com.android.model.IndivBetterTTVEmote
import com.github.michaelbull.result.mapEither
import com.paulrybitskyi.gamedge.common.data.common.ApiErrorMapper
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.live.TvEmoteRepository
import com.paulrybitskyi.gamedge.igdb.api.emote.BetterTTVEmoteEndpoint
import com.paulrybitskyi.hiltbinder.BindType
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BindType
internal class TvEmoteRepositoryImpl @Inject constructor(
    private val betterTTVEmoteEndpoint: BetterTTVEmoteEndpoint,
    private val dispatcherProvider: DispatcherProvider,
    private val apiErrorMapper: ApiErrorMapper,
) : TvEmoteRepository {

    override suspend fun getGlobalEmotes(): DomainResult<List<IndivBetterTTVEmote>> {
        return withContext(dispatcherProvider.io) {
            betterTTVEmoteEndpoint.getGlobalEmotes().mapEither(
                { response -> response },
                apiErrorMapper::mapToDomainError
            )
        }
    }

    override suspend fun getChannelEmotes(broadcasterId: String): DomainResult<BetterTTVChannelEmotes> {
        return withContext(dispatcherProvider.io) {
            betterTTVEmoteEndpoint.getChannelEmotes(broadcasterId)
                .mapEither(
                    { response -> response },
                    apiErrorMapper::mapToDomainError
                )
        }
    }
}
