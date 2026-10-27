package com.example.test_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test_1.ui.theme.CalcolatriceTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.math.*

// ==========================================
// STRUTTURA E UTILITÀ PER COLORI (HSV & HEX)
// ==========================================
data class HsvColor(val hue: Float, val saturation: Float, val value: Float)

fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val hsv = floatArrayOf(
        hue.coerceIn(0f, 360f),
        saturation.coerceIn(0f, 1f),
        value.coerceIn(0f, 1f)
    )
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun colorToHsv(color: Color): HsvColor {
    val hsv = FloatArray(3)
    val colorInt = android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
    android.graphics.Color.colorToHSV(colorInt, hsv)
    return HsvColor(
        hue = hsv[0],
        saturation = hsv[1],
        value = hsv[2]
    )
}

fun colorToHex(color: Color): String {
    val r = (color.red * 255).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255).roundToInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

fun parseHexToColor(hex: String): Color? {
    val cleanHex = hex.trim().removePrefix("#")
    if (cleanHex.length == 6) {
        val colorInt = cleanHex.toLongOrNull(16)
        if (colorInt != null) {
            val r = ((colorInt shr 16) and 0xFF).toInt()
            val g = ((colorInt shr 8) and 0xFF).toInt()
            val b = (colorInt and 0xFF).toInt()
            return Color(r, g, b)
        }
    }
    return null
}

fun isLightColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance > 0.5f
}

// ==========================================
// MAIN ACTIVITY CON GESTIONE SPLASH SCREEN
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var isCustomColorActive by remember { mutableStateOf(false) }
            var customKeypadColor by remember { mutableStateOf(Color(0xFF81FF7A)) }
            var currentScreen by remember { mutableStateOf("splash") }

            CalcolatriceTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(durationMillis = 500),
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "splash" -> {
                                SplashScreen(
                                    onTimeout = { currentScreen = "calculator" }
                                )
                            }
                            "calculator" -> {
                                CalcolatriceApp(
                                    isCustomColorActive = isCustomColorActive,
                                    customKeypadColor = customKeypadColor,
                                    onOpenSettings = { currentScreen = "settings" }
                                )
                            }
                            "settings" -> {
                                ImpostazioniScreen(
                                    isDarkTheme = isDarkTheme,
                                    onThemeChange = { isDarkTheme = it },
                                    isCustomColorActive = isCustomColorActive,
                                    onCustomColorActiveChange = { isCustomColorActive = it },
                                    customKeypadColor = customKeypadColor,
                                    onCustomColorChange = { customKeypadColor = it },
                                    onBack = { currentScreen = "calculator" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2.seconds)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Logo App",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Calcolatrice",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sviluppato da Pietro",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

// ==========================================
// SCHERMATA IMPOSTAZIONI
// ==========================================
@Composable
fun ImpostazioniScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isCustomColorActive: Boolean,
    onCustomColorActiveChange: (Boolean) -> Unit,
    customKeypadColor: Color,
    onCustomColorChange: (Color) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Indietro",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Impostazioni", style = MaterialTheme.typography.titleLarge)
        }

        HorizontalDivider()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tema Applicazione", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isDarkTheme) "Modalità Scura" else "Modalità Chiara",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Colore Tastierino", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isCustomColorActive) "Personalizzato" else "Predefinito",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isCustomColorActive,
                        onCheckedChange = onCustomColorActiveChange
                    )
                }

                if (isCustomColorActive) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    ColorPickerGoogleStyle(
                        selectedColor = customKeypadColor,
                        onColorChanged = onCustomColorChange
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    KeypadPreviewCard(customColor = customKeypadColor)
                }
            }
        }
    }
}

// ==========================================
// SELETTORE COLORI (CORRETTO)
// ==========================================
@Composable
fun ColorPickerGoogleStyle(
    selectedColor: Color,
    onColorChanged: (Color) -> Unit
) {
    val initialHsv = remember(selectedColor) { colorToHsv(selectedColor) }

    var hue by remember(selectedColor) { mutableFloatStateOf(initialHsv.hue) }
    var saturation by remember(selectedColor) { mutableFloatStateOf(initialHsv.saturation) }
    var value by remember(selectedColor) { mutableFloatStateOf(initialHsv.value) }
    var hexText by remember(selectedColor) { mutableStateOf(colorToHex(selectedColor)) }

    fun updateFromHsv(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s
        value = v
        val newColor = hsvToColor(h, s, v)
        hexText = colorToHex(newColor)
        onColorChanged(newColor)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Selettore colori", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Colore",
                    color = if (isLightColor(selectedColor)) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(2.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                val pureHueColor = remember(hue) { hsvToColor(hue, 1f, 1f) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(hue) {
                            fun handleTouch(offset: Offset) {
                                val s = (offset.x / size.width).coerceIn(0f, 1f)
                                val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                                updateFromHsv(hue, s, v)
                            }
                            detectTapGestures { handleTouch(it) }
                        }
                        .pointerInput(hue) {
                            fun handleTouch(offset: Offset) {
                                val s = (offset.x / size.width).coerceIn(0f, 1f)
                                val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                                updateFromHsv(hue, s, v)
                            }
                            detectDragGestures { change, _ -> handleTouch(change.position) }
                        }
                ) {
                    drawRect(brush = Brush.horizontalGradient(colors = listOf(Color.White, pureHueColor)))
                    drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))

                    val thumbX = saturation * size.width
                    val thumbY = (1f - value) * size.height
                    drawCircle(color = Color.White, radius = 9.dp.toPx(), center = Offset(thumbX, thumbY), style = Stroke(3.dp.toPx()))
                    drawCircle(color = selectedColor, radius = 7.dp.toPx(), center = Offset(thumbX, thumbY))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Tinta", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        )
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            fun handleTouch(offset: Offset) {
                                val h = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                                updateFromHsv(h, saturation, value)
                            }
                            detectTapGestures { handleTouch(it) }
                        }
                        .pointerInput(Unit) {
                            fun handleTouch(offset: Offset) {
                                val h = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                                updateFromHsv(h, saturation, value)
                            }
                            detectDragGestures { change, _ -> handleTouch(change.position) }
                        }
                ) {
                    val thumbX = (hue / 360f) * size.width
                    drawCircle(
                        color = Color.White,
                        radius = 10.dp.toPx(),
                        center = Offset(thumbX.coerceIn(10.dp.toPx(), size.width - 10.dp.toPx()), size.height / 2),
                        style = Stroke(2.5f.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hexText,
                onValueChange = { input ->
                    hexText = input
                    val parsed = parseHexToColor(input)
                    if (parsed != null) {
                        val hsv = colorToHsv(parsed)
                        hue = hsv.hue
                        saturation = hsv.saturation
                        value = hsv.value
                        onColorChanged(parsed)
                    }
                },
                label = { Text("HEX") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                val r = (selectedColor.red * 255).roundToInt()
                val g = (selectedColor.green * 255).roundToInt()
                val b = (selectedColor.blue * 255).roundToInt()
                val hDeg = hue.roundToInt()
                val sPct = (saturation * 100).roundToInt()
                val vPct = (value * 100).roundToInt()

                Text(text = "RGB: $r, $g, $b", fontSize = 11.sp)
                Text(text = "HSV: $hDeg°, $sPct%, $vPct%", fontSize = 11.sp)
            }
        }
    }
}

// ==========================================
// MINI ANTEPRIMA TASTIERINO
// ==========================================
@Composable
fun KeypadPreviewCard(customColor: Color) {
    val textColor = if (isLightColor(customColor)) Color.Black else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "Anteprima tasti:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

        val previewRows = listOf(
            listOf("sin", "cos", "tan", "√"),
            listOf("AC", "⌫", "±", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ",", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            previewRows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { label ->
                        val weight = if (label == "0") 2f else 1f
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(customColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCHERMATA PRINCIPALE CALCOLATRICE
// ==========================================
@Composable
fun CalcolatriceApp(
    isCustomColorActive: Boolean,
    customKeypadColor: Color,
    onOpenSettings: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var num1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(true) }

    fun formatResult(v: Double): String = when {
        v.isNaN() || v.isInfinite() -> "Errore"
        v % 1.0 == 0.0 && abs(v) < 1e15 -> v.toLong().toString()
        else -> v.toString().replace('.', ',')
    }

    fun onDigitClick(digit: String) {
        if (isNewInput || display == "0" || display == "Errore") {
            display = digit
            isNewInput = false
        } else display += digit
    }

    fun onCommaClick() {
        if (isNewInput || display == "Errore") {
            display = "0,"
            isNewInput = false
        } else if (!display.contains(",")) display += ","
    }

    fun onBackspaceClick() {
        if (isNewInput || display == "Errore") {
            display = "0"
            isNewInput = true
        } else {
            display = if (display.length > 1) display.dropLast(1) else "0"
            if (display == "0" || display == "-") {
                display = "0"
                isNewInput = true
            }
        }
    }

    fun onClearClick() {
        display = "0"
        num1 = null
        pendingOp = null
        isNewInput = true
    }

    fun onToggleSignClick() {
        val valDouble = display.replace(',', '.').toDoubleOrNull() ?: return
        display = formatResult(-valDouble)
    }

    fun onScientificFuncClick(func: String) {
        if (func == "π") {
            display = formatResult(PI)
            isNewInput = true
            return
        }
        val currentVal = display.replace(',', '.').toDoubleOrNull() ?: return
        val res = when (func) {
            "sin" -> sin(Math.toRadians(currentVal))
            "cos" -> cos(Math.toRadians(currentVal))
            "tan" -> tan(Math.toRadians(currentVal))
            "√" -> if (currentVal >= 0) sqrt(currentVal) else Double.NaN
            "x²" -> currentVal * currentVal
            "ln" -> if (currentVal > 0) ln(currentVal) else Double.NaN
            else -> null
        }
        display = if (res != null && !res.isNaN()) formatResult(res) else "Errore"
        isNewInput = true
    }

    fun onOperatorClick(op: String) {
        val currentVal = display.replace(',', '.').toDoubleOrNull() ?: return
        if (num1 != null && pendingOp != null && !isNewInput) {
            val res = when (pendingOp) {
                "+" -> num1!! + currentVal
                "-" -> num1!! - currentVal
                "×" -> num1!! * currentVal
                "÷" -> if (currentVal != 0.0) num1!! / currentVal else Double.NaN
                "^" -> num1!!.pow(currentVal)
                else -> null
            }
            if (res != null && !res.isNaN()) {
                display = formatResult(res)
                num1 = res
            } else {
                display = "Errore"
                num1 = null
            }
        } else {
            num1 = currentVal
        }
        pendingOp = op
        isNewInput = true
    }

    fun onEqualsClick() {
        val currentVal = display.replace(',', '.').toDoubleOrNull()
        if (num1 != null && pendingOp != null && currentVal != null) {
            val res = when (pendingOp) {
                "+" -> num1!! + currentVal
                "-" -> num1!! - currentVal
                "×" -> num1!! * currentVal
                "÷" -> if (currentVal != 0.0) num1!! / currentVal else Double.NaN
                "^" -> num1!!.pow(currentVal)
                else -> null
            }
            display = if (res != null && !res.isNaN()) formatResult(res) else "Errore"
            num1 = null
            pendingOp = null
            isNewInput = true
        }
    }

    val activeColor = if (isCustomColorActive) customKeypadColor else Color(0xFF263238)
    val activeTextColor = if (isCustomColorActive) {
        if (isLightColor(customKeypadColor)) Color.Black else Color.White
    } else Color.White

    val opColor = if (isCustomColorActive) customKeypadColor else Color(0xFFFF8C00)
    val sciColor = if (isCustomColorActive) customKeypadColor else Color(0xFF009688)
    val clearColor = if (isCustomColorActive) customKeypadColor else Color(0xFFE53935)
    val equalsColor = if (isCustomColorActive) customKeypadColor else Color(0xFF1E88E5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Calcolatrice by Pietro", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Impostazioni",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (num1 != null && pendingOp != null) {
                    Text(
                        text = "${formatResult(num1!!)} $pendingOp",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.End
                    )
                }
                Text(
                    text = display,
                    fontSize = 38.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 3
                )
            }
        }

        val grid = listOf(
            listOf(
                BtnItem("sin", sciColor, activeTextColor, 1f) { onScientificFuncClick("sin") },
                BtnItem("cos", sciColor, activeTextColor, 1f) { onScientificFuncClick("cos") },
                BtnItem("tan", sciColor, activeTextColor, 1f) { onScientificFuncClick("tan") },
                BtnItem("√", sciColor, activeTextColor, 1f) { onScientificFuncClick("√") }
            ),
            listOf(
                BtnItem("x²", sciColor, activeTextColor, 1f) { onScientificFuncClick("x²") },
                BtnItem("^", sciColor, activeTextColor, 1f) { onOperatorClick("^") },
                BtnItem("ln", sciColor, activeTextColor, 1f) { onScientificFuncClick("ln") },
                BtnItem("π", sciColor, activeTextColor, 1f) { onScientificFuncClick("π") }
            ),
            listOf(
                BtnItem("AC", clearColor, activeTextColor, 1f) { onClearClick() },
                BtnItem("⌫", clearColor, activeTextColor, 1f) { onBackspaceClick() },
                BtnItem("±", sciColor, activeTextColor, 1f) { onToggleSignClick() },
                BtnItem("÷", opColor, activeTextColor, 1f) { onOperatorClick("÷") }
            ),
            listOf(
                BtnItem("7", activeColor, activeTextColor, 1f) { onDigitClick("7") },
                BtnItem("8", activeColor, activeTextColor, 1f) { onDigitClick("8") },
                BtnItem("9", activeColor, activeTextColor, 1f) { onDigitClick("9") },
                BtnItem("×", opColor, activeTextColor, 1f) { onOperatorClick("×") }
            ),
            listOf(
                BtnItem("4", activeColor, activeTextColor, 1f) { onDigitClick("4") },
                BtnItem("5", activeColor, activeTextColor, 1f) { onDigitClick("5") },
                BtnItem("6", activeColor, activeTextColor, 1f) { onDigitClick("6") },
                BtnItem("-", opColor, activeTextColor, 1f) { onOperatorClick("-") }
            ),
            listOf(
                BtnItem("1", activeColor, activeTextColor, 1f) { onDigitClick("1") },
                BtnItem("2", activeColor, activeTextColor, 1f) { onDigitClick("2") },
                BtnItem("3", activeColor, activeTextColor, 1f) { onDigitClick("3") },
                BtnItem("+", opColor, activeTextColor, 1f) { onOperatorClick("+") }
            ),
            listOf(
                BtnItem("0", activeColor, activeTextColor, 2f) { onDigitClick("0") },
                BtnItem(",", activeColor, activeTextColor, 1f) { onCommaClick() },
                BtnItem("=", equalsColor, activeTextColor, 1f) { onEqualsClick() }
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            grid.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { btn ->
                        CalcButton(
                            text = btn.text,
                            modifier = Modifier.weight(btn.weight),
                            containerColor = btn.color,
                            contentColor = btn.textColor,
                            onClick = btn.onClick
                        )
                    }
                }
            }
        }
    }
}

private data class BtnItem(
    val text: String,
    val color: Color,
    val textColor: Color,
    val weight: Float,
    val onClick: () -> Unit
)

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}