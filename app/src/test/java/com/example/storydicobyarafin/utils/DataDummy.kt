package com.example.storydicobyarafin.utils

import com.example.storydicobyarafin.data.api.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..10) {
            val story = ListStoryItem(
                id = "story-$i",
                name = "Author $i",
                description = "Description $i",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-$i.png",
                createdAt = "2022-01-08T06:34:18.598Z",
                lat = -10.212,
                lon = -16.002
            )
            items.add(story)
        }
        return items
    }
}
