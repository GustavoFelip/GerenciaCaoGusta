package com.example.gerenciacao

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta baseada na identidade original do GerenciaCÃO.
private val Fundo = Color(0xFFFFFCF7)
private val Laranja = Color(0xFFFFB21C)
private val Marrom = Color(0xFF6B3919)
private val TextoSecundario = Color(0xFF8B7B6C)
private val Borda = Color(0xFFE8DFD4)

@Composable
fun TelaLogin(
    aoAcessar: () -> Unit = {},
    aoAbrirCadastro: () -> Unit = {},
    aoRecuperarSenha: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }

    val contexto = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(26.dp))

        // Marca original do aplicativo.
        MarcaLogin()

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Acesse sua conta",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Marrom,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Continue organizando seu abrigo.",
            fontSize = 14.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(33.dp))

        // Campo de e-mail.
        CampoLogin(
            titulo = "E-mail",
            valor = email,
            aoAlterar = { email = it },
            exemplo = "Seu e-mail",
            tipoTeclado = KeyboardType.Email
        )

        Spacer(Modifier.height(16.dp))

        // Campo de senha.
        Text(
            text = "Senha",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Marrom
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp),
            placeholder = {
                Text(
                    "Digite sua senha",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            visualTransformation = if (mostrarSenha) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        mostrarSenha = !mostrarSenha
                    }
                ) {
                    Text(
                        text = if (mostrarSenha) "Ocultar" else "Mostrar",
                        fontSize = 12.sp,
                        color = Marrom
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(14.dp),
            colors = coresCampo()
        )

        // Recuperação de senha.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = aoRecuperarSenha) {
                Text(
                    text = "Esqueci minha senha",
                    color = Marrom,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Botão principal.
        Button(
            onClick = {
                if (email.isBlank() || senha.isBlank()) {
                    Toast.makeText(
                        contexto,
                        "Preencha o e-mail e a senha.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Ainda não existe autenticação conectada.
                    Toast.makeText(
                        contexto,
                        "A autenticação será implementada em breve.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Laranja,
                contentColor = Marrom
            )
        ) {
            Text(
                text = "Entrar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        // Link para o cadastro.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ainda não tem conta?",
                fontSize = 13.sp,
                color = TextoSecundario
            )

            TextButton(
                onClick = aoAbrirCadastro,
                contentPadding = PaddingValues(
                    horizontal = 5.dp
                )
            ) {
                Text(
                    text = "Criar minha conta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Marrom
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Separador de login social.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Borda
            )

            Text(
                text = "ou continue com",
                modifier = Modifier.padding(
                    horizontal = 12.dp
                ),
                fontSize = 12.sp,
                color = TextoSecundario
            )

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Borda
            )
        }

        Spacer(Modifier.height(22.dp))

        // Login com outras plataformas.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BotaoSocial(
                plataforma = "Google",
                aoClicar = {
                    avisoLoginSocial(contexto, "Google")
                }
            )

            BotaoSocial(
                plataforma = "Microsoft",
                aoClicar = {
                    avisoLoginSocial(contexto, "Microsoft")
                }
            )

            BotaoSocial(
                plataforma = "Facebook",
                aoClicar = {
                    avisoLoginSocial(contexto, "Facebook")
                }
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

// Exibe o logotipo e as letras originais usadas na Splash.
@Composable
private fun MarcaLogin() {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logolaranja),
            contentDescription = "Logotipo do GerenciaCÃO",
            modifier = Modifier.size(68.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(5.dp))

        // Letras originais: Gerencia.
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            val letras = listOf(
                Triple(R.drawable.g, 36f, 42f),
                Triple(R.drawable.e, 19f, 24f),
                Triple(R.drawable.r, 17f, 22f),
                Triple(R.drawable.e, 19f, 24f),
                Triple(R.drawable.n, 23f, 24f),
                Triple(R.drawable.c, 19f, 23f),
                Triple(R.drawable.i, 13f, 30f),
                Triple(R.drawable.a, 22f, 23f)
            )

            letras.forEach { (imagem, largura, altura) ->
                Image(
                    bitmap = carregarImagemSemMargem(imagem),
                    contentDescription = null,
                    modifier = Modifier.size(
                        largura.dp,
                        altura.dp
                    ),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        // Letras originais: Cão.
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val letras = listOf(
                Triple(R.drawable.c, 36f, 40f),
                Triple(R.drawable.acifrao, 25f, 30f),
                Triple(R.drawable.o, 31f, 34f)
            )

            letras.forEach { (imagem, largura, altura) ->
                Image(
                    bitmap = carregarImagemSemMargem(imagem),
                    contentDescription = null,
                    modifier = Modifier.size(
                        largura.dp,
                        altura.dp
                    ),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

// Campo reutilizável.
@Composable
private fun CampoLogin(
    titulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    exemplo: String,
    tipoTeclado: KeyboardType
) {
    Column {
        Text(
            text = titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Marrom
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = aoAlterar,
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp),
            placeholder = {
                Text(
                    text = exemplo,
                    fontSize = 14.sp,
                    color = TextoSecundario
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = tipoTeclado
            ),
            shape = RoundedCornerShape(14.dp),
            colors = coresCampo()
        )
    }
}

// Cores dos campos.
@Composable
private fun coresCampo() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Laranja,
        unfocusedBorderColor = Borda,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = Marrom,
        focusedTextColor = Marrom,
        unfocusedTextColor = Marrom
    )

// Ícones sociais em círculos.
@Composable
private fun BotaoSocial(
    plataforma: String,
    aoClicar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = aoClicar,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, Borda),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            when (plataforma) {
                "Google" -> {
                    Text(
                        text = "G",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }

                "Microsoft" -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            QuadradoMicrosoft(Color(0xFFF25022))
                            QuadradoMicrosoft(Color(0xFF7FBA00))
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            QuadradoMicrosoft(Color(0xFF00A4EF))
                            QuadradoMicrosoft(Color(0xFFFFB900))
                        }
                    }
                }

                "Facebook" -> {
                    Text(
                        text = "f",
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1877F2)
                    )
                }
            }
        }

        Spacer(Modifier.height(7.dp))

        Text(
            text = plataforma,
            fontSize = 11.sp,
            color = TextoSecundario
        )
    }
}

@Composable
private fun QuadradoMicrosoft(cor: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(cor)
    )
}

private fun avisoLoginSocial(
    contexto: android.content.Context,
    plataforma: String
) {
    Toast.makeText(
        contexto,
        "Login com $plataforma ainda não configurado.",
        Toast.LENGTH_SHORT
    ).show()
}
