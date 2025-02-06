package com.deezer.exoapplication.playlist.fwk.utils

import android.webkit.URLUtil
import com.deezer.exoapplication.playlist.domain.usecases.UrlValidator

class AndroidUrlValidator() : UrlValidator {
    override fun isUrlValid(url: String) = URLUtil.isValidUrl(url)
}