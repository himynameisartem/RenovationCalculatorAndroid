package com.skfamily.renovationcalculatir.data

import com.skfamily.renovationcalculatir.ui.models.RoomDraftInput

data class SavedEstimateLine(
    val id: String,
    val title: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val subtotal: Double,
)

data class SavedEstimate(
    val id: String,
    val createdAt: Long,
    val total: Int,
    val rooms: List<RoomDraftInput>,
    val selectedItems: Map<String, Double>,
    val lines: List<SavedEstimateLine>,
) {
    companion object
}
