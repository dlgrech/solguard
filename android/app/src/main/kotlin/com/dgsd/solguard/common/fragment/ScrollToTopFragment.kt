package com.dgsd.solguard.common.fragment

/**
 * Interface for fragments with scrolling content that want to expose a way to scroll to the top
 */
interface ScrollToTopFragment {

  /**
   * Smooth scroll the contents of this tab to the top
   */
  fun scrollToTop() = Unit
}
