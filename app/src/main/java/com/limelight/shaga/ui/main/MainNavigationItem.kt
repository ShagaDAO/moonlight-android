package com.limelight.shaga.ui.main

import androidx.annotation.DrawableRes
import com.limelight.R

enum class MainNavigationItem(@DrawableRes val icon: Int) {
    HOME(R.drawable.ic_home),
    FRIENDS(R.drawable.ic_people),
    CHAT(R.drawable.ic_chat),
    TALK(R.drawable.ic_mic),
    PROFILE(R.drawable.ic_profile),
}