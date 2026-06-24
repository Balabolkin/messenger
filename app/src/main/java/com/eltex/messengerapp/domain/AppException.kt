package com.eltex.messengerapp.domain

sealed class AppException(message: String? = null) : Exception(message) {
    class NetworkException : AppException()
    class Forbidden : AppException()
    class UnknownException(val code: Int, message: String? = null) : AppException(message)
}