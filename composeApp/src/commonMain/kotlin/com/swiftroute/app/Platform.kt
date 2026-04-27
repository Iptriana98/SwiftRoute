package com.swiftroute.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform