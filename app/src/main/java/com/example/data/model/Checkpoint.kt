package com.example.data.model

data class Checkpoint(
    val dateTime: String,
    val description: String,
    val location: String = "",
    val status: String = "ON_PROCESS" // "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "FAILED"
)

data class TrackingResult(
    val success: Boolean,
    val message: String = "",
    val courierCode: String,
    val courierName: String,
    val waybill: String,
    val status: String, // "DELIVERED", "ON_PROCESS", "PENDING", "RETURNED"
    val statusDescription: String,
    val isDelivered: Boolean,
    val origin: String = "",
    val destination: String = "",
    val shipper: String = "",
    val receiver: String = "",
    val checkpoints: List<Checkpoint> = emptyList()
)
