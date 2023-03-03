package com.dgsd.solguard.common.view

import android.content.Context
import android.icu.text.NumberFormat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dgsd.solguard.R

private const val MAX_NUMBER = 999

class NumberPickerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val recyclerView: RecyclerView
  private val selectionIndicator: View
  private val leftArrow: View
  private val rightArrow: View

  private var onPositionChangedListener: ((Int) -> Unit)? = null

  private val slowSmoothScroller = object : LinearSmoothScroller(context) {
    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
      return 200f / displayMetrics.densityDpi
    }

    override fun getHorizontalSnapPreference(): Int {
      return SNAP_TO_START
    }
  }

  private val fastSmoothScroller = LinearSmoothScroller(context)

  init {
    LayoutInflater.from(context).inflate(R.layout.view_number_picker, this, true)

    recyclerView = requireViewById(R.id.recyclerview)
    selectionIndicator = requireViewById(R.id.selectionIndicator)
    leftArrow = requireViewById(R.id.left)
    rightArrow = requireViewById(R.id.right)

    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    recyclerView.adapter = NumberPickerAdapter(
      onItemClicked = { position ->
        setSelected(position)
      }
    )

    recyclerView.addOnScrollListener(
      object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
          if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val selectedItem = getSelectedItem()
            if (selectedItem >= 0) {
              onPositionChangedListener?.invoke(selectedItem)
            }
          }
        }
      }
    )

    leftArrow.setOnClickListener {
      val selectedItem = getSelectedItem()
      if (selectedItem > 0) {
        setSelected(selectedItem - 1)
      }
    }

    rightArrow.setOnClickListener {
      val selectedItem = getSelectedItem()
      if (selectedItem > 0) {
        val maxPosition = checkNotNull(recyclerView.adapter).itemCount
        if (selectedItem < maxPosition - 1)
          setSelected(selectedItem + 1)
      }
    }

    LinearSnapHelper().attachToRecyclerView(recyclerView)
    setSelected(0)
  }

  fun setSelected(number: Int) {

    val currentItem = getSelectedItem()
    if (Math.abs(number - currentItem) < 20) {
      slowSmoothScroller.targetPosition = number
      recyclerView.layoutManager?.startSmoothScroll(slowSmoothScroller)
    } else {
      fastSmoothScroller.targetPosition = number
      recyclerView.layoutManager?.startSmoothScroll(fastSmoothScroller)
    }
  }

  fun setOnPositionChangedListener(listener: (Int) -> Unit) {
    onPositionChangedListener = listener
  }

  fun getSelectedItem(): Int {
    val view =
      recyclerView.findChildViewUnder(recyclerView.width / 2f, recyclerView.height / 2f)
    return if (view != null) {
      recyclerView.getChildAdapterPosition(view)
    } else {
      RecyclerView.NO_POSITION
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    val itemWidth = selectionIndicator.measuredWidth
    val availableWidth = w - leftArrow.measuredWidth - rightArrow.measuredWidth
    recyclerView.updatePaddingRelative(
      start = ((availableWidth - itemWidth) / 2),
      end = ((availableWidth - itemWidth) / 2)
    )
  }

  private class NumberPickerViewHolder(
    private val textView: TextView
  ) : RecyclerView.ViewHolder(textView) {

    private val numberFormatter = NumberFormat.getNumberInstance()

    fun bind(value: Int) {
      textView.text = numberFormatter.format(value.toLong())
    }

    companion object {

      fun create(parent: ViewGroup): NumberPickerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
          R.layout.li_number_picker_item,
          parent,
          false
        ) as TextView
        return NumberPickerViewHolder(view)
      }
    }
  }

  private class NumberPickerAdapter(
    private val onItemClicked: (Int) -> Unit,
  ) : RecyclerView.Adapter<NumberPickerViewHolder>() {

    init {
      setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberPickerViewHolder {
      return NumberPickerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: NumberPickerViewHolder, position: Int) {
      holder.bind(position)
      holder.itemView.setOnClickListener { onItemClicked.invoke(position) }
    }

    override fun getItemCount(): Int {
      return MAX_NUMBER
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }
  }
}