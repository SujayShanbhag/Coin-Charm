package com.courage.CoinCharm.model

import java.time.LocalDateTime
import java.util.Date

data class Expense (
    val place : String,
    val amount : String,
    val date : String,
    val dateAndTime : String,
    val sign : Boolean
)