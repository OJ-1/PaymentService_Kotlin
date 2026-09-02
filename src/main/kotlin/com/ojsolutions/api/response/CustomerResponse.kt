package com.ojsolutions.api.response

import com.ojsolutions.api.dto.CustomerDto
import kotlinx.serialization.Serializable

@Serializable
data class CustomerResponse(
    val customer: CustomerDto
)