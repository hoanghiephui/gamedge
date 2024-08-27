package com.paulrybitskyi.gamedge.common.data.di

import com.paulrybitskyi.gamedge.common.data.usecase.TwitchEmoteUseCaseImpl
import com.paulrybitskyi.gamedge.common.domain.live.usecases.TwitchEmoteUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface UseCaseModule {
    @Binds
    fun bindTwitchEmoteUseCase(
        binding: TwitchEmoteUseCaseImpl
    ): TwitchEmoteUseCase
}
