package com.example.mynotesformsc.Model

data class Notes (
    var text: String? = "",
    var isCompleted: Boolean? = false,
    var currentTime:Long? = 0
)