package com.tekhnologiistroitelstva.renovationcalculator.data

data class CatalogCategory(
    val id: String,
    val title: String,
    val sections: List<CatalogSection>,
)

data class CatalogSection(
    val id: String,
    val title: String,
    val items: List<CatalogItem>,
)

data class CatalogItem(
    val id: String,
    val title: String,
    val unit: String,
    val price: Double,
    val description: String?,
    val photos: List<String>,
)

