package com.app.development.winter.ui.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivityIntroductionBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.ViewPagerAdapter
import com.app.development.winter.shared.base.activitybase.ToolBarBaseActivity
import com.app.development.winter.shared.extension.getInt
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.show
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.google.android.material.tabs.TabLayoutMediator

class TutorialActivity :
    ToolBarBaseActivity<ActivityIntroductionBinding, UserEvents, UserState, UserViewModel>(
        ActivityIntroductionBinding::inflate, UserViewModel::class.java
    ), View.OnClickListener {

    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var intentType = REQUEST_DEFAULT
    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding?.root)
    }

    override fun initDATA() {
        intentType = intent.getInt(INTENT_KEY, REQUEST_DEFAULT)
        mBinding?.layoutToolbar?.showBack = intentType == REQUEST_TUTORIAL
    }

    override fun initUI() {
        mBinding?.clickListener = this
        setupViewPagerAdapter()
    }

    private fun setupViewPagerAdapter() {
        viewPagerAdapter = ViewPagerAdapter(this, getFragmentList())
        mBinding?.viewPager?.apply {
            adapter = viewPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    manageButton(position)
                    manageWindowBackground(position)
                }
            })
            mBinding?.tabLayout?.let {
                TabLayoutMediator(it, this) { tab, position ->
                    tab.text = ""
                }.attach()
            }
            offscreenPageLimit = 3
        }
    }

    private fun manageWindowBackground(position: Int) {
        /*when (position) {
            0 -> {
                setWindowBackground(R.drawable.game_screen_background)
            }

            2 -> {
                setWindowBackground(R.drawable.tutorial_background)
            }

            else -> {
                setWindowBackground(R.drawable.window_background)
            }
        }*/
    }

    private fun manageButton(position: Int) {
        when (position) {
            0 -> {
                mBinding?.btnSkip?.text = getString(R.string.next)
                mBinding?.btnNext?.show()
                mBinding?.btnPrevious?.invisible()
            }

            getLastIndex() -> {
                mBinding?.btnSkip?.text = getString(R.string.finish)
                mBinding?.btnNext?.invisible()
                mBinding?.btnPrevious?.show()
            }

            else -> {
                mBinding?.btnSkip?.text = getString(R.string.next)
                mBinding?.btnNext?.show()
                mBinding?.btnPrevious?.show()
            }
        }
    }

    private fun getLastIndex(): Int {
        return viewPagerAdapter?.itemCount?.minus(1) ?: 0
    }

    private fun getFragmentList(): ArrayList<Fragment> {
        val list = ArrayList<Fragment>()
        if (LocalDataHelper.getUserConfig()?.tutorial.isNullOrEmpty().not()) {
            LocalDataHelper.getUserConfig()?.tutorial?.forEachIndexed { index, intro ->
                intro.index = index
                list.add(TutorialFragment.newInstance(intro))
            }
        }
        return list
    }

    override fun render(state: UserState) {

    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnNext -> {
                mBinding?.viewPager?.let { pager ->
                    if (pager.currentItem < (viewPagerAdapter?.itemCount ?: 0)) {
                        pager.setCurrentItem(pager.currentItem.plus(1), true)
                    }
                }
            }

            mBinding?.btnPrevious -> {
                mBinding?.viewPager?.let { pager ->
                    if (pager.currentItem > 0) {
                        pager.setCurrentItem(pager.currentItem.minus(1), true)
                    }
                }
            }

            mBinding?.btnSkip -> {
                mBinding?.viewPager?.let { pager ->
                    if (pager.currentItem < (viewPagerAdapter?.itemCount ?: 0).minus(1)) {
                        pager.setCurrentItem(pager.currentItem.plus(1), true)
                    } else {
                        if (intentType == REQUEST_DEFAULT) {
                            openHomeActivity()
                        } else {
                            finish()
                        }
                    }
                }
            }
        }
    }
}