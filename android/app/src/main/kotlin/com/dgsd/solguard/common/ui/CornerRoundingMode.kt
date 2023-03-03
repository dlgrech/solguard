package com.dgsd.solguard.common.ui

enum class CornerRoundingMode(
  val topLeft: Boolean,
  val topRight: Boolean,
  val bottomLeft: Boolean,
  val bottomRight: Boolean,
) {
  NONE(topLeft = false, topRight = false, bottomLeft = false, bottomRight = false),
  ALL_CORNERS(topLeft = true, topRight = true, bottomLeft = true, bottomRight = true),
  TOP_CORNERS(topLeft = true, topRight = true, bottomLeft = false, bottomRight = false),
  BOTTOM_CORNERS(topLeft = false, topRight = false, bottomLeft = true, bottomRight = true),
  TOP_LEFT_ONLY(topLeft = true, topRight = false, bottomLeft = false, bottomRight = false),
  TOP_RIGHT_ONLY(topLeft = false, topRight = true, bottomLeft = false, bottomRight = false),
  BOTTOM_LEFT_ONLY(topLeft = false, topRight = false, bottomLeft = true, bottomRight = false),
  BOTTOM_RIGHT_ONLY(topLeft = false, topRight = false, bottomLeft = false, bottomRight = true);

  companion object {

    fun compute(
      totalItemCount: Int,
      itemIndex: Int
    ): CornerRoundingMode {
      return when {
        totalItemCount == 1 -> ALL_CORNERS
        itemIndex == 0 -> TOP_CORNERS
        itemIndex == totalItemCount - 1 -> BOTTOM_CORNERS
        else -> NONE
      }
    }

    fun computeForGrid(
      totalItemCount: Int,
      columnCount: Int,
      itemIndex: Int
    ): CornerRoundingMode {
      return if (itemIndex == 0) {
        TOP_LEFT_ONLY
      } else if (itemIndex < columnCount && itemIndex % columnCount == (columnCount - 1)) {
        TOP_RIGHT_ONLY
      } else if (itemIndex >= (totalItemCount - columnCount)) {
        if (itemIndex % columnCount == 0) {
          BOTTOM_LEFT_ONLY
        } else if (itemIndex % columnCount == (columnCount - 1)) {
          BOTTOM_RIGHT_ONLY
        } else {
          NONE
        }
      } else {
        NONE
      }
    }
  }
}