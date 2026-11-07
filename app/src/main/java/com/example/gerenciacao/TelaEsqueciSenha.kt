
package com.example.gerenciacao

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// CORES ORIGINAIS DO GERENCIACÃO
private val FundoRecuperar = Color(0xFFFFFCF7)
private val LaranjaRecuperar = Color(0xFFFFB21C)
private val MarromRecuperar = Color(0xFF6B3919)
private val CinzaRecuperar = Color(0xFF8B7B6C)
private val BordaRecuperar = Color(0xFFE8DFD4)

@Composable
fun TelaEsqueciSenha(
    aoVoltarLogin: () -> Unit = {}
) {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    val contexto = LocalContext.current

    // Volta ao login pelo botão Voltar do Android.
    BackHandler {
        aoVoltarLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoRecuperar)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {

        Spacer(Modifier.height(15.dp))

        // SETA PARA VOLTAR AO LOGIN
        TextButton(
            onClick = aoVoltarLogin,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "← Voltar",
                fontSize = 16.sp,
                color = MarromRecuperar,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(30.dp))

        // LOGOTIPO ORIGINAL
        Image(
            painter = painterResource(
                id = R.drawable.tipografialogin
            ),
            contentDescription = "GerenciaCÃO",
            modifier = Modifier
                .width(230.dp)
                .height(85.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(45.dp))

        // TÍTULO
        Text(
            text = "Recuperar senha",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MarromRecuperar
        )

        Spacer(Modifier.height(12.dp))

        // EXPLICAÇÃO
        Text(
            text = "Digite o e-mail da sua conta. " +
                    "Vamos enviar as instruções para " +
                    "criar uma nova senha.",
            fontSize = 15.sp,
            color = CinzaRecuperar,
            lineHeight = 23.sp
        )

        Spacer(Modifier.height(38.dp))

        // CAMPO DE E-MAIL
        Text(
            text = "E-mail",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MarromRecuperar
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "seuemail@exemplo.com",
                    fontSize = 14.sp,
                    color = CinzaRecuperar
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LaranjaRecuperar,
                unfocusedBorderColor = BordaRecuperar,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = MarromRecuperar,
                unfocusedTextColor = MarromRecuperar,
                cursorColor = MarromRecuperar
            )
        )

        Spacer(Modifier.height(32.dp))

        // BOTÃO ENVIAR INSTRUÇÕES
        Button(
            onClick = {
                if (
                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches()
                ) {
                    Toast.makeText(
                        contexto,
                        "Informe um e-mail válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // O envio real será implementado
                    // quando configurarmos a autenticação.
                    Toast.makeText(
                        contexto,
                        "O envio de e-mail ainda " +
                                "não está configurado.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaranjaRecuperar,
                contentColor = MarromRecuperar
            )
        ) {
            Text(
                text = "Enviar instruções",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(18.dp))

        // VOLTAR PARA ENTRAR
        TextButton(
            onClick = aoVoltarLogin,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        ) {
            Text(
                text = "Voltar para entrar",
                fontSize = 14.sp,
                color = MarromRecuperar,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

