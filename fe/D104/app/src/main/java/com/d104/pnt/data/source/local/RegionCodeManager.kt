package com.d104.pnt.data.source.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RegionCodeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val regionMap = mutableMapOf<String, String>()

    private val hierarchyMap = mutableMapOf<String, MutableList<String>>()

    val majorRegions: List<String>
        get() = hierarchyMap.keys.sorted()

    init {
        loadCsvData()
    }

    private fun loadCsvData() {
        try {
            context.assets.open("region_codes.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")

                    val code = tokens[0].trim()
                    val major = tokens[1].trim()
                    val middle = tokens[2].trim()

                    val key = "$major $middle"
                    regionMap[key] = code

                    val list = hierarchyMap.getOrPut(shortenMajor(major)) { mutableListOf() }
                    list.add(middle)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shortenMajor(major: String): String {
        val shortedMajor = when (major) {
            "서울특별시" -> "서울"
            "인천광역시" -> "인천"
            "강원특별자치도" -> "강원도"
            "경상북도" -> "경북"
            "경상남도" -> "경남"
            "전북특별자치도" -> "전북"
            "전라남도" -> "전남"
            "충청북도" -> "충북"
            "충청남도" -> "충남"
            "제주특별자치도" -> "제주도"
            "대구광역시" -> "대구"
            "부산광역시" -> "부산"
            "광주광역시" -> "광주"
            "대전광역시" -> "대전"
            "울산광역시" -> "울산"
            "세종특별자치시" -> "세종시"
            else -> major
        }
        return shortedMajor
    }

    fun getRegionCode(major: String, middle: String): String? {
        val key = "$major $middle"

        return regionMap[key]
    }

    fun getMiddleRegions(major: String): List<String> {
        return hierarchyMap[major]?.sorted() ?: emptyList()
    }
}