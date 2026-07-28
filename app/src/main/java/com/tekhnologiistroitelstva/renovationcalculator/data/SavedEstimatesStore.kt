package com.tekhnologiistroitelstva.renovationcalculator.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tekhnologiistroitelstva.renovationcalculator.ui.models.RoomDraftInput
import com.tekhnologiistroitelstva.renovationcalculator.ui.works.SummaryLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedEstimatesStore(private val context: Context) {
    var isLoading by mutableStateOf(false)
        private set

    var estimates by mutableStateOf<List<SavedEstimate>>(emptyList())
        private set

    val hasSavedEstimates: Boolean
        get() = estimates.isNotEmpty()

    fun reload() {
        isLoading = true
        estimates = runCatching { EstimateStorage.loadAll(context) }.getOrDefault(emptyList())
        isLoading = false
    }

    fun saveEstimate(
        total: Int,
        rooms: List<RoomDraftInput>,
        selectedItems: Map<String, Double>,
        lines: List<SummaryLine>,
    ): String {
        val savedLines = lines.map { line ->
            SavedEstimateLine(
                id = line.itemId,
                title = line.title,
                quantity = line.quantity,
                unit = line.unit,
                unitPrice = line.unitPrice,
                subtotal = line.subtotal
            )
        }

        val saved = EstimateStorage.save(
            context = context,
            total = total,
            rooms = rooms,
            selectedItems = selectedItems,
            lines = savedLines
        )
        reload()
        return "Смета сохранена от ${formatDate(saved.createdAt)}"
    }

    fun delete(id: String) {
        EstimateStorage.delete(context, id)
        reload()
    }

    fun formattedDate(dateMillis: Long): String {
        return formatDate(dateMillis)
    }

    private fun formatDate(dateMillis: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"))
        return formatter.format(Date(dateMillis))
    }
}
