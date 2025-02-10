package com.deezer.exoapplication.playlist.domain.utils

interface UrlValidator {
    fun isUrlValid(url: String): Boolean
}