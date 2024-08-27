package com.paulrybitskyi.gamedge.core


import com.android.model.Authentication401Exception
import com.android.model.NetworkAuthResponse
import com.android.model.NetworkNewUserResponse
import com.android.model.NoNetworkException
import com.android.model.Response
import kotlinx.coroutines.flow.FlowCollector


/**
 * handleException is a extension function on [FlowCollector]. This function is used to emit [Response] in response
 * to http requests
 *
 * @param cause the [Throwable] object that will be used to emit the proper response
 * */
 suspend fun <T>FlowCollector<Response<T>>.handleException(cause: Throwable) {
    when (cause) {
        is NoNetworkException -> {

            emit(Response.Failure(Exception("Network error, please try again later")))
        }
        is Authentication401Exception ->{
            emit(Response.Failure(Exception("Improper Authentication")))
        }

        else -> {
            emit(Response.Failure(Exception("Error! Please try again")))
        }
    }
}

/**
 * handleException is a extension function on [FlowCollector]. This function is used to emit [Response] in response
 * to http requests
 *
 * @param cause the [Throwable] object that will be used to emit the proper response
 * */


/**
 * handleException is a extension function on [FlowCollector]. This function is used to emit [Response] in response
 * to http requests
 *
 * @param cause the [Throwable] object that will be used to emit the proper response
 * */
suspend fun <T>FlowCollector<NetworkAuthResponse<T>>.handleNetworkAuthExceptions(cause: Throwable) {
    when (cause) {
        is NoNetworkException -> {
            emit(NetworkAuthResponse.NetworkFailure(Exception("Network error, please try again later")))
        }
        is Authentication401Exception ->{
            emit(NetworkAuthResponse.Auth401Failure(Exception("Authentication error, please try again later")))
        }

        else -> {
            emit(NetworkAuthResponse.Failure(Exception("Error! Please try again")))
        }
    }
}

suspend fun <T>FlowCollector<NetworkNewUserResponse<T>>.handleNetworkNewUserExceptions(cause: Throwable) {
    when (cause) {
        is NoNetworkException -> {
            emit(NetworkNewUserResponse.NetworkFailure(Exception("Network error! Pull down to refresh")))
        }
        is Authentication401Exception ->{
            emit(NetworkNewUserResponse.Auth401Failure(Exception("Error! Re-login with Twitch")))
        }

        else -> {
            emit(NetworkNewUserResponse.Failure(Exception("Error! Please try again")))
        }
    }
}
