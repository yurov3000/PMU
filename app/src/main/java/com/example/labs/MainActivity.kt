package com.example.labs

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.labs.ui.theme.LabsTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

data class Player(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val zodiacSign: ZodiacSign
)

enum class ZodiacSign(
    val nameUser: String,
    val startMonth: Int, val startDay: Int,
    val endMonth: Int, val endDay: Int,
    val imageRes: Int
) {
    ARIES("Овен", 3, 21, 4, 19, R.drawable.aries),
    TAURUS("Телец", 4, 20, 5, 20, R.drawable.taurus),
    GEMINI("Близнецы", 5, 21, 6, 20, R.drawable.gemini),
    CANCER("Рак", 6, 21, 7, 22, R.drawable.cancer),
    LEO("Лев", 7, 23, 8, 22, R.drawable.leo),
    VIRGO("Дева", 8, 23, 9, 22, R.drawable.virgo),
    LIBRA("Весы", 9, 23, 10, 22, R.drawable.libra),
    SCORPIO("Скорпион", 10, 23, 11, 21, R.drawable.scorpio),
    SAGITTARIUS("Стрелец", 11, 22, 12, 21, R.drawable.sagittarius),
    CAPRICORN("Козерог", 12, 22, 1, 19, R.drawable.capricorn),
    AQUARIUS("Водолей", 1, 20, 2, 18, R.drawable.aquarius),
    PISCES("Рыбы", 2, 19, 3, 20, R.drawable.pisces);

    companion object {
        fun getZodiac(month: Int, day: Int): ZodiacSign {
            for (sign in values()) {
                if (sign.startMonth < sign.endMonth) {
                    if ((month == sign.startMonth && day >= sign.startDay) ||
                        (month == sign.endMonth && day <= sign.endDay) ||
                        (month > sign.startMonth && month < sign.endMonth)
                    ) {
                        return sign
                    }
                } else {
                    if ((month == sign.startMonth && day >= sign.startDay) ||
                        (month == sign.endMonth && day <= sign.endDay)
                    ) {
                        return sign
                    }
                }
            }
            return CAPRICORN
        }
    }
}

data class GameSettings(
    var gameSpeed: Int = 50,
    var maxCockroaches: Int = 10,
    var bonusInterval: Int = 5,
    var roundDuration: Int = 60
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LabsTheme {
                MainScreen();
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Регистрация", "Правила", "Авторы", "Настройки")

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTab) {
            0 -> RegistrationTab()
            1 -> RulesTab()
            2 -> AuthorsTab()
            3 -> SettingsTab()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationTab() {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Мужской") }
    var course by remember { mutableStateOf("1") }
    var difficulty by remember { mutableStateOf(50f) }
    var day by remember { mutableStateOf("1") }
    var month by remember { mutableStateOf("1") }
    var year by remember { mutableStateOf("2000") }

    var courseExpanded by remember { mutableStateOf(false) }

    var player by remember { mutableStateOf<Player?>(null) }

    val courses = listOf("1", "2", "3", "4", "5", "6")
    val genders = listOf("Мужской", "Женский")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Регистрация игрока", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Пол:", fontWeight = FontWeight.Medium)
        Row {
            genders.forEach { g ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (gender == g),
                        onClick = { gender = g }
                    )
                    Text(g, modifier = Modifier.padding(end = 16.dp))
                }
            }
        }


        Text("Курс:", fontWeight = FontWeight.Medium)
        ExposedDropdownMenuBox(
            expanded = courseExpanded,
            onExpandedChange = { courseExpanded = !courseExpanded }
        ) {
            OutlinedTextField(
                value = course,
                onValueChange = {},
                readOnly = true,
                label = { Text("Выберите курс") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) }
            )
            ExposedDropdownMenu(
                expanded = courseExpanded,
                onDismissRequest = { courseExpanded = false }
            ) {
                courses.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("$c курс") },
                        onClick = {
                            course = c
                            courseExpanded = false
                        }
                    )
                }
            }
        }

        Text("Сложность: ${difficulty.toInt()}%", fontWeight = FontWeight.Medium)
        Slider(
            value = difficulty,
            onValueChange = { difficulty = it },
            valueRange = 0f..100f,
            steps = 19,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Дата рождения:", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = day,
                onValueChange = { day = it },
                label = { Text("День") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = month,
                onValueChange = { month = it },
                label = { Text("Месяц") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Год") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val d = day.toIntOrNull() ?: 1
                val m = month.toIntOrNull() ?: 1
                val y = year.toIntOrNull() ?: 2000
                val zodiac = ZodiacSign.getZodiac(m, d)
                player = Player(
                    fullName = fullName,
                    gender = gender,
                    course = course,
                    difficulty = difficulty.toInt(),
                    birthDay = d,
                    birthMonth = m,
                    birthYear = y,
                    zodiacSign = zodiac
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Зарегистрироваться", fontSize = 16.sp)
        }

        if (player != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Данные игрока:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ФИО: ${player!!.fullName}")
                    Text("Пол: ${player!!.gender}")
                    Text("Курс: ${player!!.course}")
                    Text("Сложность: ${player!!.difficulty}%")
                    Text("Дата рождения: ${player!!.birthDay}.${player!!.birthMonth}.${player!!.birthYear}")
                    Text(
                        "Знак зодиака: ${player!!.zodiacSign.nameUser}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = painterResource(id = player!!.zodiacSign.imageRes),
                        contentDescription = player!!.zodiacSign.nameUser,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun RulesTab() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Правила игры", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    loadUrl("file:///android_asset/rules.html")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AuthorsTab() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Авторы проекта", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(authorsList) { author ->
                AuthorCard(author)
            }
        }
    }
}

@Composable
fun AuthorCard(author: Author) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Фото автора
            Image(
                painter = painterResource(id = author.photoRes),
                contentDescription = "Фото ${author.name}",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
            )
            // Имя автора
            Text(
                text = author.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SettingsTab() {
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)

    val savedSpeed = sharedPreferences.getInt("game_speed", 50)
    val savedCockroaches = sharedPreferences.getInt("max_cockroaches", 10).toString()
    val savedBonus = sharedPreferences.getInt("bonus_interval", 5).toString()
    val savedDuration = sharedPreferences.getInt("round_duration", 60).toString()

    var gameSpeed by remember { mutableStateOf(savedSpeed) }
    var maxCockroaches by remember { mutableStateOf(savedCockroaches) }
    var bonusInterval by remember { mutableStateOf(savedBonus) }
    var roundDuration by remember { mutableStateOf(savedDuration) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Настройки игры", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Text("Скорость игры: $gameSpeed%", fontWeight = FontWeight.Medium)
        Slider(
            value = gameSpeed.toFloat(),
            onValueChange = { gameSpeed = it.toInt() },
            valueRange = 10f..100f,
            steps = 17,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = maxCockroaches,
            onValueChange = { maxCockroaches = it },
            label = { Text("Макс. тараканов на экране") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = bonusInterval,
            onValueChange = { bonusInterval = it },
            label = { Text("Интервал бонусов (сек)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = roundDuration,
            onValueChange = { roundDuration = it },
            label = { Text("Длительность раунда (сек)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val sharedPreferences = context.getSharedPreferences(
                    "GameSettings",
                    Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putInt("game_speed", gameSpeed)
                    putInt("max_cockroaches", maxCockroaches.toIntOrNull() ?: 10)
                    putInt("bonus_interval", bonusInterval.toIntOrNull() ?: 5)
                    putInt("round_duration", roundDuration.toIntOrNull() ?: 60)
                }.apply()
                android.widget.Toast.makeText(
                    context,
                    "Настройки успешно применены",
                    android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Сохранить настройки", fontSize = 16.sp)
        }
    }
}


