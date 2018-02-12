package com.dziankow.offertracker.utils

import org.jsoup.Jsoup
import java.io.File
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import java.io.FileOutputStream



fun getPage(searchUrl: String, timeout: Int): String {
    val doc = Jsoup.connect(searchUrl).timeout(timeout).get()
    return doc.html()
}

fun getAndSaveImage(imageUrl: String, dirName: String, timeout: Int) {
    //Open a URL Stream
    val resultImageResponse = Jsoup.connect(imageUrl).timeout(timeout)
            .ignoreContentType(true).execute()

    var fileName = imageUrl.substringAfterLast("/")

    // output here
    val out = FileOutputStream(java.io.File("$dirName/$fileName"))
    out.write(resultImageResponse.bodyAsBytes())  // resultImageResponse.body() is where the image's contents are.
    out.close()
}

fun saveFile(fileName: String, content: String) {
    val file = File(fileName)
    // create dirs, just in case dir does not exists
    file.parentFile.mkdirs()
    file.bufferedWriter().use { out ->
        out.append(content)
    }
}

fun readFile(fileName: String): String {
    return File(fileName).bufferedReader().use { it.readText() }
}