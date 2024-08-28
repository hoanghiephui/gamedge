/*
 * Copyright 2022 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
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

package com.paulrybitskyi.gamedge.common.ui.images

import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Call
import okhttp3.OkHttpClient
import javax.inject.Inject

interface ImageLoaderInitializer {
    fun init()
}

@BindType
internal class CoilInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpCallFactory: dagger.Lazy<OkHttpClient>,
) : ImageLoaderInitializer {

    override fun init() {
        Coil.setImageLoader(
            ImageLoader.Builder(context)
                .callFactory { okHttpCallFactory.get() }
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(MEMORY_CACHE_MAX_HEAP_PERCENTAGE)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache"))
                        .maxSizePercent(0.02)
                        .build()
                }
                .build(),
        )
    }

    private companion object {
        const val MEMORY_CACHE_MAX_HEAP_PERCENTAGE = 0.5
    }
}
