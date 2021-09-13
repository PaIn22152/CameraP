package com.af.camerap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.load
import com.af.camerap.databinding.ActivityCoilBinding
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread
import kotlin.random.Random

class CoilActivity : BaseActivity() {

    private lateinit var binding: ActivityCoilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coil)
        binding.ivCoil.load(R.mipmap.images)
//        binding.ivCoil.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS8qCXFO3oqDNCxu95bJlMvldkwzl1TE2f3Lg&usqp=CAU")
//        binding.ivCoil.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRrx7Ffni7WGMIpxhdxCaz1vvBK0W1Yt1najA&usqp=CAU"){
//            crossfade(true)
//            placeholder(R.mipmap.ic_launcher)
//            transformations(CircleCropTransformation())
//        }
//        lifecycle.addObserver(MyLifecycleObserver())

//        val com1 = Lifecycle.State.RESUMED.compareTo(Lifecycle.State.RESUMED)
//        val com2 = Lifecycle.State.RESUMED.compareTo(Lifecycle.State.DESTROYED)
//        val com3 = Lifecycle.State.DESTROYED.compareTo(Lifecycle.State.CREATED)
//        Log.d("16124", "com1=$com1  com2=$com2  com3=$com3")

        binding.ivCoil.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        //使用协程实现一个网络请求：
        //等待时显示 Loading；
        //请求成功或者出错让 Loading 消失；
        //请求失败需要提示用户请求失败了;
        //让你的协程写法上看上去像单线程。
        GlobalScope.launch(Dispatchers.Main) {
            showLoading()
            val code = withContext(Dispatchers.IO) {
                getData()
            }
            hideLoading()
            if (code != 200) {
                showToast()
            }
        }


    }

    fun showLoading() {

    }

    fun hideLoading() {

    }

    fun showToast() {

    }

    suspend fun getData(): Int {
        delay(2000)
        return if (java.util.Random().nextBoolean()) 200 else 0
    }

    override fun onPause() {
        Log.d("16124", "coil onpause  11")
        super.onPause()
        Log.d("16124", "coil onpause  22")
    }


    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true, priority = 100)
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun back(str: String) {

    }
}