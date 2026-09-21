package com.example.labs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.labs.ui.theme.LabsTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// структура игрока
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
                        return sign;
                    } else {
                        // Козерог (переход через год)
                        if (month == sign.startMonth && day >= sign.startDay) return sign
                        if (month == sign.endMonth && day <= sign.endDay) return sign
                        if (month > sign.startMonth || month < sign.endMonth) return sign
                    }
                }
                return CAPRICORN;
            }
            return TODO("Provide the return value")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LabsTheme {
                RegistrationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen() {
    // Состояние полей
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Мужской") }
    var course by remember { mutableStateOf("1") }
    var difficulty by remember { mutableStateOf(50f) }
    var day by remember { mutableStateOf("1") }
    var month by remember { mutableStateOf("1") }
    var year by remember { mutableStateOf("2000") }

    // Состояния для выпадающего списка
    var courseExpanded by remember { mutableStateOf(false) }

    // Результат
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

        // ФИО
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Пол (RadioButton)
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

        // Курс (ComboBox / Dropdown)
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

        // Уровень сложности (SeekBar → Slider)
        Text("Сложность: ${difficulty.toInt()}%", fontWeight = FontWeight.Medium)
        Slider(
            value = difficulty,
            onValueChange = { difficulty = it },
            valueRange = 0f..100f,
            steps = 19,
            modifier = Modifier.fillMaxWidth()
        )

        // Дата рождения (вместо CalendarView — три поля, т.к. в Compose нет встроенного CalendarView)
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

        // Кнопка
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

        // Вывод результата (TextView)
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
                    Text("Знак зодиака: ${player!!.zodiacSign.nameUser}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Изображение знака зодиака (ImageBox)
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
