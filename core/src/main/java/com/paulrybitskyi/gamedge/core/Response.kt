package com.paulrybitskyi.gamedge.core

import javax.annotation.concurrent.Immutable

/**
 * Represents a network response
 *
 * This class represents the 3 possible states of network responses in this application.
 * - [Response.Loading]
 * - [Response.Success]
 * - [Response.Failure]
 *
 * @param T the value returned from the network request
 */

sealed class Response<out T> {

    data object Loading : Response<Nothing>()

    data class Success<out T>(
        val data: T
    ) : Response<T>()

    data class Failure(
        val e: Exception
    ) : Response<Nothing>()
}

@Immutable
sealed class WebSocketResponse<out T> {

    data object Loading : WebSocketResponse<Nothing>()

    data class Success<out T>(
        val data: T
    ) : WebSocketResponse<T>()

    data class FailureAuth403(
        val e: Exception
    ) : WebSocketResponse<Nothing>()

    data class Failure(
        val e: Exception
    ) : WebSocketResponse<Nothing>()

}

/**
 * Represents a network response and its specific error
 *
 * This class represents the 4 possible states of network responses in this application.
 * - [NetworkResponse.Loading]
 * - [NetworkResponse.Success]
 * - [NetworkResponse.Failure]
 * - [NetworkResponse.NetworkFailure]
 *
 * @param T the value returned from the network request
 */
sealed class NetworkResponse<out T> {

    data object Loading : NetworkResponse<Nothing>()

    data class Success<out T>(
        val data: T
    ) : NetworkResponse<T>()

    data class Failure(
        val e: Exception
    ) : NetworkResponse<Nothing>()

    data class NetworkFailure(
        val e: Exception
    ) : NetworkResponse<Nothing>()
}

/**
 * Represents a network response and specific response code errors
 *
 * This class represents the 5 possible states of network responses in this application.
 * - [NetworkAuthResponse.Loading]
 * - [NetworkAuthResponse.Success]
 * - [NetworkAuthResponse.Failure]
 * - [NetworkAuthResponse.NetworkFailure]
 * - [NetworkAuthResponse.Auth401Failure]
 *
 * @param T the value returned from the network request
 */
sealed class NetworkAuthResponse<out T> {

    data object Loading : NetworkAuthResponse<Nothing>()

    data class Success<out T>(
        val data: T
    ) : NetworkAuthResponse<T>()

    data class Failure(
        val e: Exception
    ) : NetworkAuthResponse<Nothing>()

    data class NetworkFailure(
        val e: Exception
    ) : NetworkAuthResponse<Nothing>()

    data class Auth401Failure(
        val e: Exception
    ) : NetworkAuthResponse<Nothing>()
}


/**
 * Represents a network response and its specific error
 *
 * This class represents the 4 possible states of network responses in this application.
 * - [NetworkNewUserResponse.Loading]
 * - [NetworkNewUserResponse.Success]
 * - [NetworkNewUserResponse.Failure]
 * - [NetworkNewUserResponse.NetworkFailure]
 *
 * @param T the value returned from the network request
 *
 *
 */
sealed class NetworkNewUserResponse<out T> {

    data object Loading : NetworkNewUserResponse<Nothing>()



    data class Success<out T>(
        val data: T
    ) : NetworkNewUserResponse<T>()




    data class Failure(
        val e: Exception
    ) : NetworkNewUserResponse<Nothing>()

    data class NetworkFailure(
        val e: Exception
    ) : NetworkNewUserResponse<Nothing>()

    data class Auth401Failure(
        val e: Exception
    ) : NetworkNewUserResponse<Nothing>()
}


/**
 * Represents a network response and its specific 401 unauthorized error
 *
 * This class represents the 4 possible states of network responses in this application.
 * - [UnAuthorizedResponse.Loading]
 * - [UnAuthorizedResponse.Success]
 * - [UnAuthorizedResponse.Failure]
 * - [UnAuthorizedResponse.Auth401Failure]
 *
 * @param T the value returned from the network request
 *
 *
 */
sealed class UnAuthorizedResponse<out T> {
    data object Loading : UnAuthorizedResponse<Nothing>()
    data class Success<out T>(
        val data: T
    ) : UnAuthorizedResponse<T>()
    data class Failure(
        val e: Exception
    ) : UnAuthorizedResponse<Nothing>()
    data class Auth401Failure(
        val e: Exception
    ) : UnAuthorizedResponse<Nothing>()
}

