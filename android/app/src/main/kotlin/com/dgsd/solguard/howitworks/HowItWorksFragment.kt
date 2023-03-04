package com.dgsd.solguard.howitworks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dgsd.solguard.R
import com.dgsd.solguard.common.fragment.navigateBack
import com.google.android.material.tabs.TabLayout

class HowItWorksFragment : Fragment(R.layout.frag_how_it_works) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val pager = view.requireViewById<ViewPager>(R.id.pager)
    val pagerIndicator = view.requireViewById<TabLayout>(R.id.pager_indicator)

    view.requireViewById<View>(R.id.close).setOnClickListener {
      navigateBack()
    }

    pager.adapter = Adapter()
    pagerIndicator.setupWithViewPager(pager)
  }

  private class Adapter : PagerAdapter() {

    override fun getCount(): Int {
      return 4
    }

    override fun isViewFromObject(view: View, item: Any): Boolean {
      return view == item
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
      val view = createView(container, position)
      container.addView(view)
      return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
      container.removeView(view as View)
    }

    private fun createView(container: ViewGroup, position: Int): View {
      val layoutInflater = LayoutInflater.from(container.context)
      return when (position) {
        PAGER_SOLGUARD -> layoutInflater.createViewForSolguard(container)
        PAGER_SELECT_APP -> layoutInflater.createViewForSelectApp(container)
        PAGER_LIMIT_YOURSELF -> layoutInflater.createViewForLimitYourself(container)
        PAGER_FEEL_GOOD -> layoutInflater.createViewForFeelGood(container)
        else -> error("Unknown position: $position")
      }
    }

    private fun LayoutInflater.createViewForSolguard(container: ViewGroup): View {
      return inflate(R.layout.view_how_it_works_page_solguard, container, false)
    }

    private fun LayoutInflater.createViewForSelectApp(container: ViewGroup): View {
      return inflate(R.layout.view_how_it_works_page_select_app, container, false)
    }

    private fun LayoutInflater.createViewForLimitYourself(container: ViewGroup): View {
      return inflate(R.layout.view_how_it_works_page_limit_yourself, container, false)
    }

    private fun LayoutInflater.createViewForFeelGood(container: ViewGroup): View {
      return inflate(R.layout.view_how_it_works_page_feel_good, container, false)
    }
  }

  companion object {

    private const val PAGER_SOLGUARD = 0
    private const val PAGER_SELECT_APP = 1
    private const val PAGER_LIMIT_YOURSELF = 2
    private const val PAGER_FEEL_GOOD = 3


    fun newInstance() = HowItWorksFragment()
  }
}