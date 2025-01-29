package com.example.framereality.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.framereality.R
import com.example.framereality.databinding.ActivityMainBinding
import com.example.framereality.fragments.ChatsListFragment
import com.example.framereality.fragments.FavouriteListFragment
import com.example.framereality.fragments.HomeFragment
import com.example.framereality.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showHomeFragment()

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            when(itemId){
                R.id.item_home -> {
                    showHomeFragment()
                }
                R.id.item_chats -> {
                    showChatsListFragment()
                }
                R.id.item_favourite -> {
                    showFavouriteListFragment()
                }
                R.id.item_profile -> {
                    showProfileFragment()
                }
            }
            true
        }
    }

    private fun showHomeFragment(){
        binding.toolbarTitleTv.text = "Home"
        val homeFragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,homeFragment,"Home")
        fragmentTransaction.commit()
    }

    private fun showChatsListFragment(){
        binding.toolbarTitleTv.text = "Chats"
        val chatsListFragment = ChatsListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,chatsListFragment,"ChatsList")
        fragmentTransaction.commit()
    }

    private fun showFavouriteListFragment(){
        binding.toolbarTitleTv.text = "Favourites"
        val favouriteListFragment = FavouriteListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,favouriteListFragment,"FavouriteList")
        fragmentTransaction.commit()
    }

    private fun showProfileFragment(){
        binding.toolbarTitleTv.text = "Profile"
        val profileFragment = ProfileFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,profileFragment,"Profile")
        fragmentTransaction.commit()
    }
}