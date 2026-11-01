package com.example.labs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.labs.data.*
import com.example.labs.ui.theme.LabsTheme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

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

        fun getByName(name: String): ZodiacSign {
            return values().find { it.nameUser == name } ?: CAPRICORN
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LabsTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val tabs = listOf("Регистрация", "Рекорды", "Правила", "Авторы", "Настройки", "ИГРА")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    var activeUser by remember { mutableStateOf<UserEntity?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> RegistrationTab(db = db, activeUser = activeUser, onUserSelected = { activeUser = it })
                1 -> RecordsTab(db = db, activeUser = activeUser)
                2 -> RulesTab()
                3 -> AuthorsTab()
                4 -> SettingsTab()
                5 -> GameTab(
                    db = db,
                    activeUser = activeUser,
                    onNavigateToTab = { tabIndex ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabIndex)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationTab(
    db: AppDatabase,
    activeUser: UserEntity?,
    onUserSelected: (UserEntity?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userDao = db.userDao()
    val dbUsers by userDao.getAllUsers().collectAsState(initial = emptyList())

    var mode by remember { mutableStateOf("new") } // "new" or "select"

    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Мужской") }
    var course by remember { mutableStateOf("1") }
    var difficulty by remember { mutableStateOf(50f) }
    var day by remember { mutableStateOf("1") }
    var month by remember { mutableStateOf("1") }
    var year by remember { mutableStateOf("2000") }

    var courseExpanded by remember { mutableStateOf(false) }
    val courses = listOf("1", "2", "3", "4")
    val genders = listOf("Мужской", "Женский")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Регистрация и профиль", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { mode = "new" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (mode == "new") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Новый игрок")
            }
            Button(
                onClick = { mode = "select" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (mode == "select") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Выбрать из БД (${dbUsers.size})")
            }
        }

        if (activeUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Текущий активный игрок:", fontWeight = FontWeight.Bold)
                    Text("ФИО: ${activeUser.fullName}")
                    Text("Курс: ${activeUser.course}, Сложность: ${activeUser.difficulty}%")
                    Text("Знак: ${activeUser.zodiacSignName}")
                }
            }
        }

        if (mode == "select") {
            Text("Выберите игрока из базы данных Room:", fontWeight = FontWeight.Bold)
            if (dbUsers.isEmpty()) {
                Text("В базе данных пока нет зарегистрированных игроков.", color = Color.Gray)
            } else {
                dbUsers.forEach { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Курс: ${user.course} | Сложность: ${user.difficulty}% | ${user.zodiacSignName}")
                            }
                            Row {
                                Button(onClick = {
                                    onUserSelected(user)
                                    Toast.makeText(context, "Игрок выбран активным", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Выбрать")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        userDao.deleteUser(user)
                                        if (activeUser?.id == user.id) {
                                            onUserSelected(null)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        } else {
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
                    val newUser = UserEntity(
                        fullName = fullName.ifEmpty { "Аноним" },
                        gender = gender,
                        course = course,
                        difficulty = difficulty.toInt(),
                        birthDay = d,
                        birthMonth = m,
                        birthYear = y,
                        zodiacSignName = zodiac.nameUser
                    )
                    coroutineScope.launch {
                        val insertedId = userDao.insertUser(newUser)
                        val savedUser = userDao.getUserById(insertedId) ?: newUser
                        onUserSelected(savedUser)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Зарегистрировать и сохранить в БД", fontSize = 16.sp)
            }

            if (activeUser != null) {
                val zodiac = ZodiacSign.getByName(activeUser.zodiacSignName)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Успешно сохранено в Room БД:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ФИО: ${activeUser.fullName}")
                        Text("Пол: ${activeUser.gender}")
                        Text("Курс: ${activeUser.course}")
                        Text("Сложность: ${activeUser.difficulty}%")
                        Text("Дата рождения: ${activeUser.birthDay}.${activeUser.birthMonth}.${activeUser.birthYear}")
                        Text("Знак зодиака: ${activeUser.zodiacSignName}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            painter = painterResource(id = zodiac.imageRes),
                            contentDescription = zodiac.nameUser,
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordsTab(db: AppDatabase, activeUser: UserEntity?) {
    val coroutineScope = rememberCoroutineScope()
    val recordDao = db.scoreRecordDao()
    val records by recordDao.getAllRecords().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var inputPoints by remember { mutableStateOf("100") }
    var inputDiff by remember { mutableStateOf("50") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Таблица рекордов", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Person, contentDescription = "Добавить рекорд")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Добавить рекорд")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val demoUser = activeUser?.fullName ?: "Студент ИТМО"
                        val diff = activeUser?.difficulty ?: 50
                        recordDao.insertRecord(
                            ScoreRecordEntity(
                                userId = activeUser?.id ?: 0,
                                userName = demoUser,
                                points = (50..300).random(),
                                difficulty = diff,
                                speed = 50,
                                roundDuration = 60
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Демо")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Пока нет записей рекордов", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Нажмите 'Добавить рекорд' или 'Демо', чтобы заполнить таблицу результатов.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records.indices.toList()) { index ->
                    val record = records[index]
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "${index + 1}."
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(medal, fontSize = 22.sp, modifier = Modifier.width(36.dp))
                                Column {
                                    Text(record.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(record.dateTimestamp))
                                    Text("Сложность: ${record.difficulty}% | $dateStr", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${record.points} очков", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Скорость: ${record.speed}%", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        recordDao.clearAllRecords()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Очистить все рекорды", color = Color.Red)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Добавить новый результат") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Игрок: ${activeUser?.fullName ?: "Гость (Аноним)"}")
                    OutlinedTextField(
                        value = inputPoints,
                        onValueChange = { inputPoints = it },
                        label = { Text("Очки") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inputDiff,
                        onValueChange = { inputDiff = it },
                        label = { Text("Сложность (%)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val pts = inputPoints.toIntOrNull() ?: 100
                    val df = inputDiff.toIntOrNull() ?: (activeUser?.difficulty ?: 50)
                    val name = activeUser?.fullName ?: "Аноним"
                    coroutineScope.launch {
                        recordDao.insertRecord(
                            ScoreRecordEntity(
                                userId = activeUser?.id ?: 0,
                                userName = name,
                                points = pts,
                                difficulty = df,
                                speed = 50,
                                roundDuration = 60
                            )
                        )
                    }
                    showAddDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun RulesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            Image(
                painter = painterResource(id = author.photoRes),
                contentDescription = "Фото ${author.name}",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
            )
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
                    Context.MODE_PRIVATE
                )
                sharedPreferences.edit().apply {
                    putInt("game_speed", gameSpeed)
                    putInt("max_cockroaches", maxCockroaches.toIntOrNull() ?: 10)
                    putInt("bonus_interval", bonusInterval.toIntOrNull() ?: 5)
                    putInt("round_duration", roundDuration.toIntOrNull() ?: 60)
                }.apply()
                Toast.makeText(
                    context,
                    "Настройки успешно применены",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Сохранить настройки", fontSize = 16.sp)
        }
    }
}

@Composable
fun GameTab(db: AppDatabase, activeUser: UserEntity?, onNavigateToTab: (Int) -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val recordDao = db.scoreRecordDao()

    var score by remember { mutableStateOf(0) }
    var misses by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }
    var isPlaying by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var scoreSaved by remember { mutableStateOf(false) }

    val sharedPreferences = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
    val maxBugs = sharedPreferences.getInt("max_cockroaches", 10)
    val roundDuration = sharedPreferences.getInt("round_duration", 60)
    val gameSpeed = sharedPreferences.getInt("game_speed", 50)

    val bugs = remember { mutableStateListOf<Bug>() }

    var fieldWidthPx by remember { mutableStateOf(0f) }
    var fieldHeightPx by remember { mutableStateOf(0f) }

    // Bonus & Tilt state
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    var isTiltActive by remember { mutableStateOf(false) }
    var tiltSecondsLeft by remember { mutableStateOf(0) }
    var bonusItem by remember { mutableStateOf<BonusItem?>(null) }

    DisposableEffect(isTiltActive) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    tiltX = event.values[0]
                    tiltY = event.values[1]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (isTiltActive) {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(isTiltActive) {
        if (isTiltActive) {
            tiltSecondsLeft = 10
            while (tiltSecondsLeft > 0 && isTiltActive) {
                delay(1000)
                tiltSecondsLeft--
            }
            isTiltActive = false
        }
    }

    // Spawn bonus every 15 seconds
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(15000)
                if (isPlaying && fieldWidthPx > 0 && fieldHeightPx > 0 && bonusItem == null) {
                    val size = 70f
                    bonusItem = BonusItem(
                        id = (0..1000).random(),
                        x = Random.nextFloat() * (fieldWidthPx - 150f) + 50f,
                        y = Random.nextFloat() * (fieldHeightPx - 150f) + 50f,
                        size = size
                    )
                    coroutineScope.launch {
                        delay(6000)
                        if (bonusItem != null) {
                            bonusItem = null
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isPlaying = false
            bugs.clear()
            bonusItem = null
            isTiltActive = false
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(32)
                bugs.forEach { bug ->
                    updateBugPosition(bug, fieldWidthPx, fieldHeightPx, tiltX, tiltY, isTiltActive)
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && bugs.size < maxBugs) {
                delay(1000)
                if (bugs.size < maxBugs && fieldWidthPx > 0 && fieldHeightPx > 0) {
                    bugs.add(createBug(bugs.size, fieldWidthPx, fieldHeightPx))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Игра", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Очки: $score", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Промахи: $misses", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Время: $timeLeft", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (isTiltActive) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "БОНУС АКТИВЕН: Наклон учитывается! ($tiltSecondsLeft сек)",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray)
                .onSizeChanged { size ->
                    fieldWidthPx = size.width.toFloat()
                    fieldHeightPx = size.height.toFloat()
                }
                .pointerInput(isPlaying) {
                    if (isPlaying) {
                        detectTapGestures { offset ->
                            // Check bonus tap
                            val currentBonus = bonusItem
                            if (currentBonus != null &&
                                offset.x >= currentBonus.x && offset.x <= currentBonus.x + currentBonus.size &&
                                offset.y >= currentBonus.y && offset.y <= currentBonus.y + currentBonus.size
                            ) {
                                bonusItem = null
                                isTiltActive = true
                                try {
                                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                Toast.makeText(context, "КРИК ЖУКОВ! Наклон телефона активен!", Toast.LENGTH_SHORT).show()
                                return@detectTapGestures
                            }

                            // Check bug tap
                            var hit = false
                            for (i in bugs.indices.reversed()) {
                                val bug = bugs[i]
                                if (offset.x >= bug.x &&
                                    offset.x <= bug.x + bug.size &&
                                    offset.y >= bug.y &&
                                    offset.y <= bug.y + bug.size
                                ) {
                                    bugs.removeAt(i)
                                    score += 10
                                    hit = true
                                    break
                                }
                            }
                            if (!hit) {
                                misses++
                                score = (score - 5).coerceAtLeast(0)
                            }
                        }
                    }
                }
        ) {
            bugs.forEach { bug ->
                Image(
                    painter = painterResource(id = bug.imageRes),
                    contentDescription = "Жук",
                    modifier = Modifier
                        .size(with(density) { bug.size.toDp() })
                        .offset(
                            x = with(density) { bug.x.toDp() },
                            y = with(density) { bug.y.toDp() }
                        )
                )
            }

            // Render Bonus Item
            if (bonusItem != null) {
                val b = bonusItem!!
                Box(
                    modifier = Modifier
                        .size(with(density) { b.size.toDp() })
                        .offset(
                            x = with(density) { b.x.toDp() },
                            y = with(density) { b.y.toDp() }
                        )
                        .background(Color.Yellow, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 24.sp)
                }
            }

            if (showMenu) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Меню", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showMenu = false
                                onNavigateToTab(2)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Правила")
                        }

                        Button(
                            onClick = {
                                showMenu = false
                                onNavigateToTab(3)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Авторы")
                        }

                        Button(
                            onClick = {
                                showMenu = false
                                onNavigateToTab(4)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Настройки")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showMenu = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Закрыть")
                        }
                    }
                }
            }

            if (!gameStarted) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            gameStarted = true
                            isPlaying = true
                            timeLeft = roundDuration
                            score = 0
                            misses = 0
                            scoreSaved = false
                            bonusItem = null
                            isTiltActive = false
                            bugs.clear()
                        }) {
                            Text("Начать игру", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Нажмите ☰ для меню", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            // Экран конца игры
            if (gameStarted && !isPlaying && timeLeft == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Игра окончена!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Счёт: $score", fontSize = 20.sp)
                        Text("Промахи: $misses", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!scoreSaved) {
                                    scoreSaved = true
                                    coroutineScope.launch {
                                        recordDao.insertRecord(
                                            ScoreRecordEntity(
                                                userId = activeUser?.id ?: 0,
                                                userName = activeUser?.fullName ?: "Гость",
                                                points = score,
                                                difficulty = activeUser?.difficulty ?: 50,
                                                speed = gameSpeed,
                                                roundDuration = roundDuration
                                            )
                                        )
                                    }
                                }
                                onNavigateToTab(1) // Переход на вкладку Рекорды
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(if (scoreSaved) "Результат сохранен! Посмотреть рекорды" else "Сохранить в рекорды")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                score = 0
                                misses = 0
                                timeLeft = roundDuration
                                bugs.clear()
                                scoreSaved = false
                                bonusItem = null
                                isTiltActive = false
                                isPlaying = true
                                gameStarted = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Играть снова", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
