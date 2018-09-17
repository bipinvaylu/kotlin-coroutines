package com.bipin.coroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.suspendCoroutine

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        threadButton.setOnClickListener { view ->
            runThread()
        }

        launchBlockingCoroutine.setOnClickListener { view ->
            launchBlockingCoroutine()
        }

        launchCoroutine.setOnClickListener { view ->
            launchCoroutine("Hey, post this first random msg: " + Random().nextInt())
            launch(UI) {
                postData("Hey post this second random msg: " + Random().nextInt())
            }
        }

        asyncCoroutine.setOnClickListener { view ->
            // UI = CoroutineDispatcher
            launch(UI) {
                var token = ""
                async(CommonPool) { token = getAccessToken() }.await()
                Log.d("MainActivity","New token: $token")
                val deferredAccessToken = async { getAccessToken() }
                Log.d("MainActivity","New deferred token value: ${deferredAccessToken.await()}")
            }
        }

        callbackWrapper.setOnClickListener { view ->
            // UI = CoroutineDispatcher
            launch(UI) {
                val data = downloadDataAsync()
                Log.d("MainActivity","Callbackwrapper data: $data")
            }
        }
    }


    private fun runThread() = runBlocking<Unit> {
        val jobs = List(1000) {
            thread {
                Thread.sleep(1000L)
                Log.d("MainActivity","runThread.")
            }
        }
        jobs.forEach { it.join() }
    }


    private fun launchBlockingCoroutine() = runBlocking<Unit> {
        val jobs = List(1000) {
            launch {
                delay(1000L)
                Log.d("MainActivity","launchBlockingCoroutine.")
            }
        }
        jobs.forEach { it.join() }
    }

    private fun launchCoroutine(data: String) {
        launch {
            delay(1000L)
            postData(data)
            Log.d("MainActivity","launchCoroutine.")
        }
    }

    private suspend fun postData(data: String) {
        delay(1000L)
        Log.d("MainActivity","postData: $data.")
    }

    private fun getAccessToken(): String {
        // Here we will have access token api call
//        delay(1000L)
        Thread.sleep(1000)
        return Random().nextLong().toString()
    }


    private suspend fun downloadDataAsync(): String {
        return suspendCoroutine<String> { cont ->
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("http://jsonplaceholder.typicode.com/posts")
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resume(response.body()?.string() ?: "")
                }
            })
        }
    }
}
