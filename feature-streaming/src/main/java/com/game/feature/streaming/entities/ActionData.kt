package com.game.feature.streaming.entities

import androidx.compose.runtime.Immutable
import com.paulrybitskyi.gamedge.common.domain.websockets.ModActionData
import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModQueueMessage

/**
 * ModActionListImmutableCollection is a Wrapper object created specifically to handle the problem of the Compose compiler
 *  always marking List as unstable.
 *  - You can read more about this Wrapper solution, [HERE](https://developer.android.com/develop/ui/compose/performance/stability/fix#annotated-classes)
 *
 * @param modActionList a list of [ModActionData] objects.
 * */
@Immutable
data class ModActionListImmutableCollection(
    val modActionList: List<ModActionData>
)

val followerModeList = listOf(
    ListTitleValue("Off", null), ListTitleValue("0 minutes(any followers)", 0),
    ListTitleValue("10 minutes(most used)", 10),
    ListTitleValue("30 minutes", 30), ListTitleValue("1 hour", 60),
    ListTitleValue("1 day", 1440),
    ListTitleValue("1 week", 10080),
    ListTitleValue("1 month", 43200),
    ListTitleValue("3 months", 129600)

)

//1 week 10080
//1 month 43200
//3 months 129600
val slowModeList =listOf(
    ListTitleValue("Off",null),
    ListTitleValue("3s",3),
    ListTitleValue("5s",5),
    ListTitleValue("10s",10),
    ListTitleValue( "20s",20),
    ListTitleValue("30s",30),
    ListTitleValue("60s",60 )
)
val slowModeListImmutable = ImmutableModeList(slowModeList)

@Immutable
data class ImmutableModeList(
    val modeList:List<ListTitleValue>
)
val followerModeListImmutable = ImmutableModeList(followerModeList)

@Immutable
data class AutoModMessageListImmutableCollection(
    val autoModList: List<AutoModQueueMessage>
)

