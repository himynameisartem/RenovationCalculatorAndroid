package com.skfamily.renovationcalculatir.ui.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val REQUEST_ENDPOINT = "https://functions.yandexcloud.net/d4etr5cmivffs85lr4d3"
private const val POLICY_URL = "https://skfamily.moscow/Privacy_Policy.pdf"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestFormSheet(
    estimateLinesText: String?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var agreeToPolicy by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val digitsOnlyPhone = phone.filter(Char::isDigit)
    val isPhoneValid = digitsOnlyPhone.length >= 10
    val isEmailValid = email.trim().isEmpty() || Regex("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email.trim())
    val canSubmit = isPhoneValid && isEmailValid && agreeToPolicy

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Заявка на расчет",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Оставьте контакты, и мы свяжемся с вами.",
                color = Color(0xFF6E737D),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Телефон") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (phone.isNotEmpty() && !isPhoneValid) {
                Text(
                    text = "Некорректный телефон",
                    color = Color(0xFFE53935),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            if (email.isNotBlank() && !isEmailValid) {
                Text(
                    text = "Некорректный email",
                    color = Color(0xFFE53935),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Комментарий (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.size(14.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                Checkbox(
                    checked = agreeToPolicy,
                    onCheckedChange = { agreeToPolicy = it }
                )
                val policyLabel = "политикой конфиденциальности"
                val policyStart = "Ознакомлен(а) с ".length
                val policyEnd = policyStart + policyLabel.length
                val policyText = buildAnnotatedString {
                    append("Ознакомлен(а) с политикой конфиденциальности и согласен(а) на обработку персональных данных")
                    addStyle(
                        style = SpanStyle(color = Color(0xFF2A6FF3), textDecoration = TextDecoration.Underline),
                        start = policyStart,
                        end = policyEnd
                    )
                    addStringAnnotation(
                        tag = "policy",
                        annotation = POLICY_URL,
                        start = policyStart,
                        end = policyEnd
                    )
                }
                ClickableText(
                    text = policyText,
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = { offset ->
                        policyText
                            .getStringAnnotations("policy", offset, offset)
                            .firstOrNull()
                            ?.let { uriHandler.openUri(it.item) }
                    }
                )
            }

            Text(
                text = "* Обязательные поля",
                color = Color(0xFF8A8E98),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 12.dp)
            )

            Button(
                onClick = {
                    isSending = true
                    scope.launch {
                        sendToYandex(
                            name = name,
                            phone = phone,
                            email = email,
                            comment = comment,
                            estimateLinesText = estimateLinesText
                        ).fold(
                            onSuccess = {
                                alertTitle = "Успешно"
                                alertMessage = "Заявка принята!"
                                showAlert = true
                            },
                            onFailure = { error ->
                                alertTitle = "Ошибка"
                                alertMessage = error.message ?: "Не удалось отправить заявку"
                                showAlert = true
                            }
                        )
                        isSending = false
                    }
                },
                enabled = canSubmit && !isSending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSubmit && !isSending) Color(0xFF2A6FF3) else Color(0xFFD7D8DD)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp)
            ) {
                if (isSending) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Заказать расчет", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
                if (alertTitle == "Успешно") onDismiss()
            },
            title = { Text(alertTitle) },
            text = { Text(alertMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showAlert = false
                        if (alertTitle == "Успешно") onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

private suspend fun sendToYandex(
    name: String,
    phone: String,
    email: String,
    comment: String,
    estimateLinesText: String?,
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val url = URL(REQUEST_ENDPOINT)
        val safeEstimate = estimateLinesText ?: "Пусто"
        val fullEstimate = """
            Имя: $name
            Комментарий: $comment
            Смета: $safeEstimate
        """.trimIndent()

        val body = JSONObject(
            mapOf(
                "phone" to phone,
                "email" to email,
                "estimate" to fullEstimate
            )
        ).toString().toByteArray()

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 5000
            readTimeout = 5000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            outputStream.use { it.write(body) }
        }

        val code = connection.responseCode
        if (code != 200) {
            throw IllegalStateException("Статус: $code")
        }
    }
}
