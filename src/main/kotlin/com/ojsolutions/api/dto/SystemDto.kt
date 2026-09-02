package com.ojsolutions.api.dto

import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class SystemDto (
    val id: String,
    val createdDate: String,
    val updatedDate: String,
    val status: OwnerStatus,
    val name: String,
    val description: String
)