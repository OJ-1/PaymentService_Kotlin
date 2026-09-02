package com.ojsolutions.api.response

class InvalidRequestException(
    val code: String,
    override val message: String
) : RuntimeException(message)