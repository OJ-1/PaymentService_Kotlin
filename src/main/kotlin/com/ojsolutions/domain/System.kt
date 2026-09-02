package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class System(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val status: OwnerStatus,
    val name: String,
    val description: String
)
