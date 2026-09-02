package com.ojsolutions.api.response

import com.ojsolutions.api.dto.CustomerDto
import kotlinx.serialization.Serializable

@Serializable
data class CustomersResponse(
    val customers: List<CustomerDto>
)