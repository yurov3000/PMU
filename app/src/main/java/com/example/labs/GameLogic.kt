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

data class BonusItem(
    val id: Int,
    var x: Float,
    var y: Float,
    var size: Float = 70f
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

fun updateBugPosition(bug: Bug, screenWidth: Float, screenHeight: Float, tiltX: Float = 0f, tiltY: Float = 0f, useTilt: Boolean = false) {
    if (useTilt) {
        bug.speedX += tiltX * 0.6f
        bug.speedY -= tiltY * 0.6f
        bug.speedX = bug.speedX.coerceIn(-30f, 30f)
        bug.speedY = bug.speedY.coerceIn(-30f, 30f)
    }

    bug.x += bug.speedX
    bug.y += bug.speedY

    if (bug.x <= 0f) {
        bug.x = 0f
        bug.speedX = -bug.speedX * 0.8f
    }
    if (bug.x >= screenWidth - bug.size) {
        bug.x = screenWidth - bug.size
        bug.speedX = -bug.speedX * 0.8f
    }
    if (bug.y <= 0f) {
        bug.y = 0f
        bug.speedY = -bug.speedY * 0.8f
    }
    if (bug.y >= screenHeight - bug.size) {
        bug.y = screenHeight - bug.size
        bug.speedY = -bug.speedY * 0.8f
    }
}
