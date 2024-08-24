package com.paulrybitskyi.gamedge.common.ui

sealed class DeviceOrientation {
    abstract val orientation: Int

    data class Portrait(
        override val orientation: Int
    ) : DeviceOrientation()

    data class ReverseLandscape(
        override val orientation: Int
    ) : DeviceOrientation()

    data class Landscape(
        override val orientation: Int
    ) : DeviceOrientation()
}
