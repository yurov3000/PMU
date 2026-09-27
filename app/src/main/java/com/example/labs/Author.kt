package com.example.labs

data class Author (val name: String,
                    val photoRes: Int)

val authorsList = listOf(
    Author("Юров Данил Андреевич ИП-313", R.drawable.danil),
    Author("Чуйков Иван Владимирович ИП-313", R.drawable.ivan),
)

