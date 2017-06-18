package com.timetracker.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import com.timetracker.R
import android.support.v4.view.ViewPager



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MainPagerAdapter(supportFragmentManager)
        val pager = findViewById(R.id.pager) as ViewPager
        pager.adapter = adapter
    }

    class MainPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment =
            when (position) {
                0 -> TrackersFragment()
                1 -> Goals()
                else -> throw IllegalArgumentException("Unknown pager item")
            }


        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence =
            when (position) {
                0 -> "Trackers"
                1 -> "Goals"
                else -> throw IllegalArgumentException("Unknown pager item")
            }

    }
}
