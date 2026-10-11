package com.example.labs

import java.util.Random

data class Bug(
    val id: Int,
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val imageRes: Int,
    val size: Float = 80f
)

val bugImages = listOf(
    R.drawable.bug1
)

private val random = Random()

fun createBug(id: Int, screenWidth: Float, screenHeight: Float): Bug {
    val size = 80f
    return Bug(
        id = id,
        x = random.nextFloat() * (screenWidth - size),
        y = random.nextFloat() * (screenHeight - size),
        speedX = (random.nextFloat() * 10f) - 5f,
        speedY = (random.nextFloat() * 10f) - 5f,
        imageRes = bugImages[random.nextInt(bugImages.size)],
        size = size
    )
}

fun updateBugPosition(bug: Bug, screenWidth: Float, screenHeight: Float) {
    bug.x += bug.speedX
    bug.y += bug.speedY

    if (bug.x <= 0f) {
        bug.x = 0f
        bug.speedX = -bug.speedX
    }
    if (bug.x >= screenWidth - bug.size) {
        bug.x = screenWidth - bug.size
        bug.speedX = -bug.speedX
    }
    if (bug.y <= 0f) {
        bug.y = 0f
        bug.speedY = -bug.speedY
    }
    if (bug.y >= screenHeight - bug.size) {
        bug.y = screenHeight - bug.size
        bug.speedY = -bug.speedY
    }
}