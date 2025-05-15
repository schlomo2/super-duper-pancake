package com.instantiasoft.nqueens.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BestTimes(
    val times: Map<Int, Long> = mapOf()
)
