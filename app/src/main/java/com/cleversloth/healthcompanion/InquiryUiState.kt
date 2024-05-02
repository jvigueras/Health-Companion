package com.cleversloth.healthcompanion

import androidx.compose.runtime.toMutableStateList


class InquiryUiState(
    msg_list: List<InquiryMessage> = emptyList()
) {
    private val _msg_list: MutableList<InquiryMessage> = msg_list.toMutableStateList()
    val msg_list: List<InquiryMessage> = _msg_list

    fun addMsg(msg: InquiryMessage) {
        _msg_list.add(msg)
    }

}