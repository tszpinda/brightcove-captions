package com.example.videocaption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.brightcove.player.edge.Catalog
import com.brightcove.player.edge.CatalogError
import com.brightcove.player.edge.PlaylistListener
import com.brightcove.player.event.Event
import com.brightcove.player.event.EventListener
import com.brightcove.player.event.EventType
import com.brightcove.player.model.*
import com.brightcove.player.view.BrightcoveExoPlayerVideoView

class MainActivity : AppCompatActivity() {
    private lateinit var player: BrightcoveExoPlayerVideoView
    private val policyKey =
        ""
    private var accountId = ""
    private var videoForecastPlaylist = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        player = findViewById<com.brightcove.player.view.BrightcoveExoPlayerVideoView>(R.id.brightcove_video_view);
        player.eventEmitter.on(EventType.SELECT_SOURCE, EventListener {
                processEvent(it)
        })
        var catalog = Catalog.Builder(player.eventEmitter, accountId)
            .setPolicy(policyKey)
            .build()

        catalog.findPlaylistByID(
            videoForecastPlaylist,
            object : PlaylistListener() {
                override fun onPlaylist(playlist: Playlist) {
                    try {
                        player.add(playlist.videos[0])
                    } catch (t: Throwable) {
                        System.err.println("VIDEO ERROR - unable to add to playlist")
                    }
                }

                override fun onError(errors: List<CatalogError>) {
                    super.onError(errors)
                    System.err.println("VIDEO ERROR !!!!")
                }
            })
    }

    private fun processEvent(event: Event) {
        val httpsDeliveryType = "application/vnd.apple.mpegurl"
        val https = "https://"

        when (event.type) {
            EventType.SELECT_SOURCE -> {
                event.preventDefault()
                val video = event.properties["video"] as Video?
                var newSource: Source? = null
                val map = video!!.sourceCollections
                for ((key, sc) in map) {
                    if (key.toString() == httpsDeliveryType) {
                        val set = sc.sources
                        for (src in set) {
                            if (src.url.startsWith(https)) {
                                newSource = src
                                break
                            }
                        }
                    }
                }
                event.properties[Event.SOURCE] = newSource
                player.eventEmitter.respond(event)
            }
        }
    }
}