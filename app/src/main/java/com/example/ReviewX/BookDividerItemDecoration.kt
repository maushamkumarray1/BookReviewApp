package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat

class BookDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, R.color.light_gray) // Set the color
        paint.strokeWidth = 3f // Set the thickness of the line
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        val left = parent.paddingLeft.toFloat()
        val right = parent.width - parent.paddingRight.toFloat()

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = (child.bottom + params.bottomMargin).toFloat()
            val bottom = top + paint.strokeWidth

            canvas.drawLine(left, top, right, bottom, paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = 10 // Space between items
    }
}
