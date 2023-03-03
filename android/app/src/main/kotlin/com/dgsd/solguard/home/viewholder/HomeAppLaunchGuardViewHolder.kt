package com.dgsd.solguard.home.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.*
import com.dgsd.solguard.common.view.ToggleableItemTouchListenerRecyclerView
import com.dgsd.solguard.home.model.HomeItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.math.MathUtils
import kotlin.math.absoluteValue

class HomeAppLaunchGuardViewHolder private constructor(
  itemView: View,
  private val onClickListener: (item: HomeItem.AppLaunchGuardItem) -> Unit,
  private val onDeleteClicked: (packageName: String) -> Unit,
  private val onBlackoutClicked: (item: HomeItem.AppLaunchGuardItem) -> Unit,
  private val onEnableDisableClicked: (packageName: String) -> Unit,
) : HomeItemViewHolder<HomeItem.AppLaunchGuardItem>(itemView),
  ToggleableItemTouchListenerRecyclerView.TouchInterceptAwareViewHolder {

  private val card = itemView.requireViewById<MaterialCardView>(R.id.card)
  private val backgroundActions =
    itemView.requireViewById<MaterialCardView>(R.id.background_actions)
  private val backgroundActionsContainer =
    itemView.requireViewById<View>(R.id.background_actions_container)
  private val header = itemView.requireViewById<View>(R.id.header)
  private val appIcon = itemView.requireViewById<ImageView>(R.id.icon)
  private val appName = itemView.requireViewById<TextView>(R.id.app_name)
  private val primaryMessage = itemView.requireViewById<TextView>(R.id.primary_message)
  private val subtitle = itemView.requireViewById<TextView>(R.id.subtitle)
  private val blackoutModeEnabledIndicator =
    itemView.requireViewById<View>(R.id.blackout_mode_enabled_indicator)

  private val actionDelete = itemView.requireViewById<View>(R.id.background_action_delete)
  private val actionDeleteIcon =
    itemView.requireViewById<ImageView>(R.id.background_action_delete_icon)
  private val actionDeleteText =
    itemView.requireViewById<TextView>(R.id.background_action_delete_text)

  private val actionBlackout = itemView.requireViewById<View>(R.id.background_action_blackout)
  private val actionBlackoutIcon =
    itemView.requireViewById<ImageView>(R.id.background_action_blackout_icon)
  private val actionBlackoutText =
    itemView.requireViewById<TextView>(R.id.background_action_blackout_text)

  private val actionEnableDisable =
    itemView.requireViewById<View>(R.id.background_action_enable_disable)
  private val actionEnableDisableIcon =
    itemView.requireViewById<ImageView>(R.id.background_action_enable_disable_icon)
  private val actionEnableDisableText =
    itemView.requireViewById<TextView>(R.id.background_action_enable_disable_text)

  private val actionViews = arrayOf(actionDelete, actionBlackout, actionEnableDisable)
  private val actionTexts = arrayOf(actionDeleteText, actionBlackoutText, actionEnableDisableText)
  private val actionIcons = arrayOf(actionDeleteIcon, actionBlackoutIcon, actionEnableDisableIcon)

  private var isDraggingEnabled = false

  init {
    appIcon.circle()
  }

  override fun bind(item: HomeItem.AppLaunchGuardItem) {
    isDraggingEnabled = !item.isBlackedOut

    val context = itemView.context

    card.setOnClickListener { onClickListener.invoke(item) }
    actionDelete.setOnClickListener { onDeleteClicked.invoke(item.packageName) }
    actionBlackout.setOnClickListener { onBlackoutClicked.invoke(item) }
    actionEnableDisable.setOnClickListener { onEnableDisableClicked.invoke(item.packageName) }

    actionEnableDisableText.setText(
      if (item.isGuardEnabled) {
        R.string.home_app_launch_guard_disable
      } else {
        R.string.home_app_launch_guard_enable
      }
    )
    actionEnableDisableIcon.setImageResource(
      if (item.isGuardEnabled) {
        R.drawable.ic_baseline_unpublished_24
      } else {
        R.drawable.ic_baseline_circle_check_24
      }
    )

    val primaryColor = context.getColorAttr(R.attr.colorPrimary)
    val primaryTextColor = context.getColorAttr(android.R.attr.textColorPrimary)
    val paletteSwatch = item.palette.vibrantSwatch ?: item.palette.dominantSwatch
    val titleTextColor = ColorUtils.setAlphaComponent(
      paletteSwatch?.titleTextColor ?: primaryTextColor,
      255
    )

    appIcon.setImageDrawable(item.icon)
    appName.text = item.displayName
    appName.setTextColor(titleTextColor)

    actionTexts.forEach { it.setTextColor(titleTextColor) }
    actionIcons.forEach { it.imageTintList = ColorStateList.valueOf(titleTextColor) }

    val backgroundColor = paletteSwatch?.rgb
      ?.let { ColorUtils.blendARGB(it, Color.WHITE, 0.4f) }
      ?: primaryColor

    header.setBackgroundColor(backgroundColor)
    backgroundActions.setCardBackgroundColor(
      ColorUtils.blendARGB(backgroundColor, Color.WHITE, 0.4f)
    )

    val highlightColor = paletteSwatch?.rgb ?: primaryTextColor

    primaryMessage.text =
      TextUtils.expandTemplate(
        context.getString(R.string.home_tab_item_primary_message_template),
        item.displayName.bold().colored(highlightColor),
        context.resources.getQuantityString(
          R.plurals.common_x_times,
          item.numberOfLaunchesAllowedPerDay,
          item.numberOfLaunchesAllowedPerDay,
        ).bold().colored(highlightColor),
        PaymentTokenFormatter.format(item.amount).bold().colored(highlightColor)
      )

    subtitle.text = when {
      item.isBlackedOut -> null
      !item.isGuardEnabled ->
        itemView.resources.getString(
          R.string.home_tab_app_launch_guard_item_subtitle_disabled
        ).bold()
      item.numberOfLaunchesToday == 0 -> {
        itemView.resources.getString(
          R.string.home_tab_app_launch_guard_item_subtitle_zero_launches
        )
      }
      item.hasExceededDailyLimit -> {
        itemView.resources.getString(
          R.string.home_tab_app_launch_guard_item_daily_limit_reached
        ).bold()
      }
      else -> {
        itemView.resources.getQuantityString(
          R.plurals.home_tab_app_launch_guard_item_subtitle,
          item.numberOfLaunchesToday,
          item.numberOfLaunchesToday,
        )
      }
    }

    val subtitleIcon = when {
      !item.isGuardEnabled -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_unpublished_24)
      item.hasExceededDailyLimit -> ContextCompat.getDrawable(context, R.drawable.solguard_icon)
      else -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_circle_check_24)
    }?.let {
      it.mutate().apply {
        val ratio = if (it.intrinsicHeight > 0) {
          it.intrinsicWidth.toFloat() / it.intrinsicHeight
        } else {
          1f
        }

        val size = context.resources.getDimensionPixelSize(R.dimen.home_tab_item_subtitle_icon_size)
        setBounds(0,0, (size * ratio).toInt(), size)
      }
    }
    subtitle.setCompoundDrawablesRelative(subtitleIcon, null, null, null)

    blackoutModeEnabledIndicator.isVisible = item.isBlackedOut
    primaryMessage.isInvisible = item.isBlackedOut
    subtitle.isInvisible = item.isBlackedOut

    itemView.setGreyscale(item.isBlackedOut)
    card.setGreyscale(!item.isGuardEnabled)
  }

  @Suppress("ConvertTwoComparisonsToRangeCheck")
  override fun allowTouchIntercept(x: Float, y: Float): Boolean {
    val cardLeft = card.x
    val cardRight = cardLeft + card.width

    // If the touch event is over the card, we allow interception..
    if (x >= cardLeft && x <= cardRight) {
      return true
    }

    val actionContainerX = itemView.x + backgroundActionsContainer.x
    val actionContainerY = itemView.y + backgroundActionsContainer.y
    // If the touches is over one of our actions, dont allow the intercept
    for (actionView in actionViews) {
      val viewX = actionContainerX + actionView.x
      val viewY = actionContainerY + actionView.y
      if (x >= viewX &&
        x <= (viewX + actionView.width) &&
        y >= viewY &&
        y <= (viewY + actionView.height)
      ) {
        return false
      }
    }

    return true
  }

  fun isDraggingEnabled(): Boolean {
    return isDraggingEnabled
  }

  fun onDrag(dX: Float) {
    val availableWidth = card.width
    val cardEdgeVisibility = availableWidth * 0.1f
    val percentageThroughDrag = clamp(dX.absoluteValue / availableWidth, 0f, 1f)
    val backgroundActionsScale = MathUtils.lerp(0.95f, 1f, percentageThroughDrag)
    val cardScale = MathUtils.lerp(1f, 0.95f, percentageThroughDrag)
    val translationX = clamp(
      dX,
      -availableWidth + cardEdgeVisibility,
      availableWidth - cardEdgeVisibility
    )

    backgroundActions.alpha = percentageThroughDrag
    card.translationX = translationX
    card.scaleX = cardScale
    card.scaleY = cardScale
    backgroundActions.scaleX = backgroundActionsScale
    backgroundActions.scaleY = backgroundActionsScale
  }

  companion object {
    fun create(
      parent: ViewGroup,
      onClickListener: (item: HomeItem.AppLaunchGuardItem) -> Unit,
      onDeleteClicked: (packageName: String) -> Unit,
      onBlackoutClicked: (item: HomeItem.AppLaunchGuardItem) -> Unit,
      onEnableDisableClicked: (packageName: String) -> Unit,
    ): HomeAppLaunchGuardViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(
        R.layout.li_home_tab_app_launch_guard,
        parent,
        false
      )

      view.clipToOutline = true

      return HomeAppLaunchGuardViewHolder(
        view,
        onClickListener,
        onDeleteClicked,
        onBlackoutClicked,
        onEnableDisableClicked,
      )
    }
  }
}