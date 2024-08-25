package com.game.feature.streaming.entities

import com.android.model.websockets.ChatSettingsData
import com.paulrybitskyi.gamedge.core.Response
import com.paulrybitskyi.gamedge.core.WebSocketResponse

data class ModViewViewModelUIState(
    val showSubscriptionEventError: Response<Boolean> = Response.Loading,
    val showAutoModMessageQueueErrorMessage:Boolean = false,
    val chatSettings: ChatSettingsData = ChatSettingsData(false,null,false,null,false,false),
    val enabledChatSettings:Boolean = true,
    val selectedSlowMode:ListTitleValue =ListTitleValue("Off",null),
    val selectedFollowerMode:ListTitleValue =ListTitleValue("Off",null),
    val modViewTotalNotifications:Int =0,

    val modActionNotifications:Boolean = true,
    val autoModMessagesNotifications:Boolean = true,


    val emoteOnly:Boolean = false, //todo: THESE TWO ARE REALLY MESSING THINGS UP
    val subscriberOnly:Boolean = false,//todo: THESE TWO ARE REALLY MESSING THINGS UP

)
data class ListTitleValue(
    val title:String,
    val value:Int?
)

data class RequestIds(
    val oAuthToken:String ="",
    val clientId:String="",
    val broadcasterId:String="",
    val moderatorId:String ="",
    val sessionId:String =""
)

data class ModViewStatus(
    val modActions: WebSocketResponse<Boolean> = WebSocketResponse.Loading,
    val autoModMessageStatus: WebSocketResponse<Boolean> = WebSocketResponse.Loading
)
