package com.ojsolutions.api

import com.ojsolutions.api.request.CreateSystemRequest
import com.ojsolutions.api.request.UpdateSystemRequest
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.application.SystemService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID
import kotlin.getValue

fun Route.systemRoutes() {

    val systemService by inject<SystemService>()

    route("/systems") {

        /**
         * Create a system entity.
         *
         * Summary: Create a system entity
         * Description: Creates a system entity - USED FOR INTERNAL SYSTEM ACCOUNTS - the system entity exists independently of the accounts (one to many).
         * Tags: Systems
         */
        post {

            val request = call.receive<CreateSystemRequest>()

            val response = systemService.createSystem(request)

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * Get all system entities.
         *
         * Summary: Get all entities
         * Tags: Systems
         */
        get {

            val response = systemService.getSystems()

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Get a system entity using the system id.
         *
         * Summary: Get a system entity using system id
         * Tags: Systems
         */
        get("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_SYSTEM_ID",
                    "System ID must be a valid UUID."
                )
            }

            val response = systemService.getSystem(id)

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Update a system entity using the system id.
         *
         * Summary: Update a system entity using system id
         * Tags: Systems
         */
        put("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_SYSTEM_ID",
                    "System ID must be a valid UUID."
                )
            }

            val request = call.receive<UpdateSystemRequest>()

            val response = systemService.updateSystem(
                id = id,
                request = request
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}