package com.ojsolutions.application

import com.ojsolutions.api.dto.SystemDto
import com.ojsolutions.api.request.CreateSystemRequest
import com.ojsolutions.api.request.UpdateSystemRequest
import com.ojsolutions.api.response.ApiError
import com.ojsolutions.api.response.ApiResponse
import com.ojsolutions.api.response.CreateReferenceResponse
import com.ojsolutions.api.response.SystemResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.api.response.SystemsResponse
import com.ojsolutions.domain.System
import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.infrastructure.database.repository.SystemRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class SystemService(private val systemRepository: SystemRepository) {

    // CREATE SYSTEM
    fun createSystem(request: CreateSystemRequest): ApiResponse<CreateReferenceResponse> = transaction {

        // Validate the System name does not already exist
        if (systemRepository.existsByName(request.name)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "SYSTEM_NAME_ALREADY_EXISTS",
                        description = "System Name already exists."
                    )
                )
            )
        }

        // Generate a SystemId
        val systemId = UUID.randomUUID()
        val now = java.time.LocalDateTime.now()

        val system = System(
            id = systemId,
            createdDate = now,
            updatedDate = now,
            status = OwnerStatus.ACTIVE,
            name = request.name,
            description = request.description
        )

        // Add the system record in SQL DB
        systemRepository.create(system)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(systemId.toString())
        )
    }

    // GET ALL SYSTEMS
    fun getSystems(): ApiResponse<SystemsResponse> = transaction {

        // Get all Systems from the DB
        val systems = systemRepository.getAll()

        val response = systems.map { system ->
            SystemDto(
                id = system.id.toString(),
                createdDate = system.createdDate.toString(),
                updatedDate = system.updatedDate.toString(),
                status = system.status,
                name = system.name,
                description = system.description
            )
        }

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = SystemsResponse(
                systems = response
            )
        )
    }

    // GET SYSTEM
    fun getSystem(id: UUID): ApiResponse<SystemResponse> = transaction {

        // Check if the SystemId exists
        if (!systemRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "SYSTEM_NOT_FOUND",
                        description = "System does not exist."
                    )
                )
            )
        }

        // Retrieve the system info from the DB
        val system = systemRepository.get(id)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = SystemResponse(
                system = SystemDto(
                    id = system.id.toString(),
                    createdDate = system.createdDate.toString(),
                    updatedDate = system.updatedDate.toString(),
                    status = system.status,
                    name = system.name,
                    description = system.description
                )
            )
        )
    }

    // UPDATE SYSTEM
    fun updateSystem(id: UUID, request: UpdateSystemRequest): ApiResponse<CreateReferenceResponse> = transaction {

        // Check the SystemId exists
        if (!systemRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "SYSTEM_NOT_FOUND",
                        description = "System does not exist."
                    )
                )
            )
        }

        // Retrieve the system info
        val existing = systemRepository.get(id)

        // Validate if the Name already exists
        if (
            request.name != null &&
            request.name != existing.name &&
            systemRepository.existsByName(request.name)
        ) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "SYSTEM_NAME_ALREADY_EXISTS",
                        description = "System Name already exists."
                    )
                )
            )
        }

        val updatedSystem = System(
            id = existing.id,
            createdDate = existing.createdDate,
            updatedDate = java.time.LocalDateTime.now(),
            status = request.status ?: existing.status,
            name = request.name ?: existing.name,
            description = request.description ?: existing.description
        )

        // Update the system record in the DB
        systemRepository.update(updatedSystem)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(
                updatedSystem.id.toString()
            )
        )
    }


    //===
}