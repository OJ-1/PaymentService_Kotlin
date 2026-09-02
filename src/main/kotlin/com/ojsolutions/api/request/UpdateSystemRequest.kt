package com.ojsolutions.api.request

import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateSystemRequest (
    val msgId: String,
    val status: OwnerStatus? = null,
    val name: String? = null,
    val description: String? = null
)