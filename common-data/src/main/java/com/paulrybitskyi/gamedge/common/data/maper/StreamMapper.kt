/*
 * Copyright 2020 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.common.data.maper

import com.paulrybitskyi.gamedge.common.domain.games.entities.StreamData
import com.paulrybitskyi.gamedge.common.domain.games.entities.StreamItem
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse
import javax.inject.Inject

internal class StreamMapper @Inject constructor() {

    fun mapToDomainStream(streamsResponse: StreamsResponse): StreamData {
        return StreamData(
            cursorPage = streamsResponse.pagination.cursor,
            items = streamsResponse.data.map { dataItem ->
                StreamItem(
                    id = dataItem.id,
                    userName = dataItem.userName,
                    language = dataItem.language,
                    isMature = dataItem.isMature,
                    type = dataItem.type,
                    title = dataItem.title,
                    thumbnailUrl = generateLinks(dataItem.thumbnailUrl),
                    tags = dataItem.tags,
                    gameName = dataItem.gameName,
                    userId = dataItem.userId,
                    userLogin = dataItem.userLogin,
                    startedAt = dataItem.startedAt,
                    viewerCount = dataItem.viewerCount,
                    gameId = dataItem.gameId,
                )
            }
        )
    }
}

internal fun StreamMapper.mapToDomainStreams(streamsResponse: StreamsResponse): StreamData {
    return mapToDomainStream(streamsResponse)
}

fun generateLinks(baseUrl: String): List<String> {
    val sizes = listOf(
        Pair(80, 45),
        Pair(320, 180),
        Pair(640, 360)
    )

    return sizes.map { (width, height) ->
        baseUrl.replace("{width}", width.toString()).replace("{height}", height.toString())
    }
}
