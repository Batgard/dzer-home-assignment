package com.deezer.exoapplication.playlist.framework.utils

import android.webkit.URLUtil
import com.deezer.exoapplication.playlist.domain.utils.UrlValidator

class AndroidUrlValidator : UrlValidator {
    override fun isUrlValid(url: String) = URLUtil.isValidUrl(url)
}