package com.inkrodriguez.notfall.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inkrodriguez.notfall.databinding.ActivityPerguntasFrequentesBinding

class PerguntasFrequentesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerguntasFrequentesBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerguntasFrequentesBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}