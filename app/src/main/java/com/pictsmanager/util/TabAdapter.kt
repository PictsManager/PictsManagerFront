package com.pictsmanager.util

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pictsmanager.ui.main.AlbumFragment
import com.pictsmanager.ui.main.ImageFragment

class TabAdapter(context: Context, manager: FragmentManager): FragmentPagerAdapter(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var ctx: Context = context


    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ImageFragment(ctx)
            }
            1 -> {
                AlbumFragment(ctx)
            }
            else -> AlbumFragment(ctx)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = "Images"
        } else {
            title = "Albums"
        }
        return title
    }
}