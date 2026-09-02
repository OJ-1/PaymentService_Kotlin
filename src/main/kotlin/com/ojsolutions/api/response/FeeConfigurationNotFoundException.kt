package com.ojsolutions.api.response

class FeeConfigurationNotFoundException(
    message: String = "Fee configuration not found."
) : RuntimeException(message)