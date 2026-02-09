package com.d104.pnt.domain.model

data class NaverReverseGeocodeResponse(
    val status: Status,
    val results: List<NaverResult>
)

data class Status(
    val code: Int,
    val name: String,
    val message: String
)

data class NaverResult(
    val name: String,
    val region: Region,
    val code: Code
)

data class Region(
    val area1: Area,
    val area2: Area,
    val area3: Area,
    val area4: Area
)

data class Area(
    val name: String,
    val coords: Coords
)

data class Coords(
    val center: Center
)

data class Center(
    val crs: String,
    val x: Float,
    val y: Float
)

data class Code(
    val id: String,
    val type: String,
    val mappingId: String
)