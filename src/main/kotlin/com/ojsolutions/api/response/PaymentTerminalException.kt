package com.ojsolutions.api.response

class PaymentTerminalException(
    val code: String,
    override val message: String
) : RuntimeException(message)