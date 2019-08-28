package com.charlesma.spellee.util

interface EventConstant {
    companion object{
        const val EVENT_PIANO_DRILL_START = "piano_practice"
        const val EVENT_PIANO_DRILL_COMPLETE = "piano_practice"
        const val EVENT_PIANO_DRILL_FAILED = "piano_practice"



        const val PARAM_PIANO_BOOK = "Book"
        const val PARAM_PIANO_CHAPTER = "Chapter"
        const val PARAM_PIANO_CHAPTER_DRILL_COUNT = "drilled"
        const val PARAM_PIANO_CHAPTER_DRILL_DURATION = "duration"

    }
}