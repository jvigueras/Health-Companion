package com.cleversloth.healthcompanion

import java.util.UUID

enum class Role {
    USER,
    MODEL
}

data class InquiryMessage(
    val id: String = UUID.randomUUID().toString(),
    var msg: String = "",
    val role: Role = Role.USER
)


