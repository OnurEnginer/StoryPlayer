package com.example.storyplayer.ui.storygroups

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.storyplayer.databinding.FragmentStoryGroupsBinding
import com.example.storyplayer.ui.main.MainViewModel
import com.example.storyplayer.ui.story.StoryFragment


class StoryGroupsFragment : Fragment() {

    private var _binding: FragmentStoryGroupsBinding? = null
    private val binding get() = _binding!!
    companion object {
        fun newInstance() = StoryGroupsFragment()
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         _binding = FragmentStoryGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.list = mainViewModel.getStories()
        viewPager = binding.vpStories
        viewPager.adapter = StorySlidePagerAdapter(this).apply {
            mainViewModel.list.forEach {
                addFragment(StoryFragment.newInstance(it))
            }
        }

        viewPager.setPageTransformer(CubeOutPageTransformer())
        viewPager.registerOnPageChangeCallback(object: OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mainViewModel.setStoryGroup(position)
            }
        })
        mainViewModel.storyGroupIndexLiveData.observe(viewLifecycleOwner) {
            if (it == -1)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            else {
                viewPager.post { viewPager.setCurrentItem(it, false)}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.hide()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                actionBar?.show()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.apply {
                    show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                actionBar?.hide()
            }
        }
    }

    private inner class StorySlidePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = fragmentList.size

        private val fragmentList: MutableList<Fragment> = mutableListOf()

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }
        override fun createFragment(position: Int): Fragment = fragmentList[position]
    }
    class CubeOutPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, pos: Float) {
            page.pivotX = if (pos < 0f) page.width.toFloat() else 0f
            page.pivotY = page.height * 0.5f
            page.rotationY = 90f * pos
        }
    }
}