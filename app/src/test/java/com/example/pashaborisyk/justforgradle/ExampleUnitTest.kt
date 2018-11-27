package com.example.pashaborisyk.justforgradle

import android.net.LocalServerSocket
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        println("Creating file C:/pashaborisyk/neAndroidFile.txt")
        val file = File("C:/pashaborisyk/neAndroidFile.txt")
        file.createNewFile()

        println("Creating output stream")
        val fileOutputStream = FileOutputStream(file)

        println("Obtaining LocalServerSocket for fileOS $fileOutputStream")
        val serverSocket = LocalServerSocket(fileOutputStream.fd)
        val socket = serverSocket.accept()
        println("Socket accepted : $socket")

    }
}
