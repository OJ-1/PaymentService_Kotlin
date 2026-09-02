package com.ojsolutions.api.response

import com.ojsolutions.api.dto.SystemDto
import kotlinx.serialization.Serializable

@Serializable
data class SystemResponse(
    val system: SystemDto
)