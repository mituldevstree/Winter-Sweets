package com.app.development.winter.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentIntroBinding
import com.app.development.winter.ui.user.model.Intro

class TutorialFragment : Fragment() {

    private lateinit var mBinding: FragmentIntroBinding
    private var introModel: Intro? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentIntroBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        introModel?.let { setTutorialImage(it) }
        mBinding.data = introModel
    }

    private fun setTutorialImage(introModel: Intro) {
        when (introModel.index) {
            0 -> introModel.imageResource = R.drawable.icon_tutorial_1
            1 -> introModel.imageResource = R.drawable.icon_tutorial_2
            2 -> introModel.imageResource = R.drawable.icon_tutorial_3
            else -> introModel.imageResource = R.drawable.app_logo
        }
    }

    companion object {
        fun newInstance(intro: Intro?): TutorialFragment {
            val fragment = TutorialFragment()
            fragment.introModel = intro
            return fragment
        }
    }
}