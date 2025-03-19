package com.example.myapplication


class practice2 {
    fun hello(){
        println("hello2")
    }
}

fun main(){
    println("hello")
    val k = practice2()
    k.hello()
    practice2().hello()
    val fish = 2
    println(fish.times(6))
    // converting primitive value to an object
    val boxer: Number = 1
    val num : Int = 3
    val dob : Double = 2.0
    var str = 8
    var a = 5
    println(a+str)
    val b: Byte = 1
    val c: Int = b.toInt()
    println(c)
    val oneMillion = 1_000_000
    var rocks: Int? = null
    var rock: Int? = null
    var listoffish: List<String?> = listOf(null,null)
    var l: List<String> = listOf("name","mausham","kumar")
    println(l.last())
    println(l)




}
