package com.inkrodriguez.notfall.view

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.databinding.ActivityPrimeirosSocorrosBinding

class PrimeirosSocorrosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrimeirosSocorrosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrimeirosSocorrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = binding.webView
        webView.settings.javaScriptEnabled = true

        val progressBar = binding.progressBar

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }

        val listMovies: MutableList<String> = mutableListOf("k6GnPibgCiE", "tC-OiNlHpcI", "BpXpjpPH4Bc&t=10s", "YgancAzs_4c")

        val videoUrls = listMovies.map { "https://www.youtube.com/embed/$it" }

        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<html><head>")
        htmlBuilder.append("<style>iframe { margin-bottom: 10px; }</style>")
        htmlBuilder.append("</head><body>")

        for (videoUrl in videoUrls) {
            htmlBuilder.append("<iframe width=\"100%\" height=\"315\" src=\"$videoUrl\" frameborder=\"0\" allowfullscreen></iframe>")
        }

        htmlBuilder.append("</body></html>")

        val html = htmlBuilder.toString()
        webView.loadData(html, "text/html", "utf-8")

    }
}
