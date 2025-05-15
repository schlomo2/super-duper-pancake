package com.instantiasoft.nqueens.data.model

data class Paths(
    val list: Array<Array<List<Move>>> = emptyArray()
) {
    fun getSquarePath(row: Int, col: Int): List<Move>? {
        return list.getOrNull(row)?.getOrNull(col)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Paths

        return list.contentDeepEquals(other.list)
    }

    override fun hashCode(): Int {
        return list.contentDeepHashCode()
    }

    companion object {
        fun getPathArray(size: Int): Array<Array<List<Move>>> {
            return Array(size) {
                Array(size) {
                    emptyList()
                }
            }
        }

        fun getPaths(size: Int): Paths {
            return Paths(getPathArray(size))
        }
    }
}