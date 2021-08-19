package se.umu.arsu0013.prtracker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeDeleteCallback(context: Context): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#F44336")

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)
    private val delIconWidth = deleteIcon?.intrinsicWidth
    private val delIconHeight = deleteIcon?.intrinsicHeight

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // don't want support for reordering items by moving them up and down
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        background.color = backgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)

        // calculate pos for delete icon
        val delIconTop = itemView.top + (itemHeight - delIconHeight!!) / 2
        val delIconMargin = (itemHeight - delIconHeight) / 2
        val delIconLeft = itemView.right - delIconMargin - delIconWidth!!
        val delIconRight = itemView.right - delIconMargin
        val delIconBottom = delIconTop + delIconHeight

        deleteIcon!!.setBounds(delIconLeft, delIconTop, delIconRight, delIconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        val clearPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}