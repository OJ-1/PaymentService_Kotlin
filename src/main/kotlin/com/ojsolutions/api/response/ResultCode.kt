package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
enum class ResultCode {
    SUCCESS,
    FAILED
}