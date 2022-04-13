package com.williamrai_zero.upic.util


fun getUrlImageCode(url: String): String {
    val str = "https://images.pexels.com/photos/186861/pexels-photo-186861.jpeg"
    val list = str.split("/")

    println("$list[4]")

    return list[4]
}