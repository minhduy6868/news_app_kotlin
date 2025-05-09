package com.gk.news_pro.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName

@IgnoreExtraProperties
data class RadioStation(
    @SerializedName("stationuuid") val stationuuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("favicon") val favicon: String? = null,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("countrycode") val countrycode: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("clickcount") val clickcount: Int = 0,
    @SerializedName("lastchecktime") val lastchecktime: String? = null,
    @SerializedName("changeuuid") val changeuuid: String? = null,
    @SerializedName("serveruuid") val serveruuid: String? = null,
    @SerializedName("url_resolved") val urlResolved: String? = null,
    @SerializedName("homepage") val homepage: String? = null,
    @SerializedName("languagecodes") val languagecodes: String? = null,
    @SerializedName("votes") val votes: Int? = null,
    @SerializedName("lastchangetime") val lastchangetime: String? = null,
    @SerializedName("codec") val codec: String? = null,
    @SerializedName("bitrate") val bitrate: Int? = null,
    @SerializedName("hls") val hls: Int? = null,
    @SerializedName("lastcheckok") val lastcheckok: Int? = null,
    @SerializedName("clicktimestamp") val clicktimestamp: String? = null,
    @SerializedName("clicktrend") val clicktrend: Int? = null,
    @SerializedName("ssl_error") val sslError: Int? = null,
    @SerializedName("geo_lat") val geoLat: Double? = null,
    @SerializedName("geo_long") val geoLong: Double? = null,
    @SerializedName("has_extended_info") val hasExtendedInfo: Boolean? = null
)