package com.charlesma.spellee.test.abrsm.datamodel

import com.google.gson.annotations.SerializedName

class ABRSMCloudConfiguration(
    @SerializedName("piano_abrsm_chapters")
    val abrsmBook: ABRSMBook,
    @SerializedName("award_gif_list")
    val awardGifList: Array<String>,
    @SerializedName("piano_abrsm_after_words")
    val pianoAbrsmAfterWords: String
) {

}