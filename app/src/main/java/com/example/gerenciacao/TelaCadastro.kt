
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
import androidx.compose.ui.graphics.ImageBitmap
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

// CORES DO GERENCIACÃO
private val FundoCadastro = Color(0xFFFFFCF7)
private val LaranjaCadastro = Color(0xFFFFB21C)
private val MarromCadastro = Color(0xFF6B3919)
private val CinzaCadastro = Color(0xFF8B7B6C)
private val BordaCadastro = Color(0xFFE8DFD4)

@Composable
fun TelaCadastro(
    aoVoltarLogin: () -> Unit = {},
    // Conclui o formulario validado e permite ao chamador definir a proxima tela.
    aoConcluirCadastro: (String) -> Unit
) {
    // Guarda os dados enquanto a tela está aberta.
    var nome by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmarSenha by rememberSaveable {
        mutableStateOf("")
    }

    val contexto = LocalContext.current

    // Retorna ao login quando o usuario usa o botao Voltar do Android.
    BackHandler {
        aoVoltarLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoCadastro)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // CABEÇALHO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
        ) {

            TextButton(
                onClick = aoVoltarLogin,
                modifier = Modifier.align(
                    Alignment.CenterStart
                )
            ) {
                Text(
                    text = "←",
                    fontSize = 26.sp,
                    color = MarromCadastro
                )
            }

            Logolaranja(
                modifier = Modifier.align(
                    Alignment.Center
                ),
            )
        }

        // TÍTULO
        Text(
            text = "Criar sua conta",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = MarromCadastro
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "É rápido. Vamos começar?",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            color = CinzaCadastro
        )

        Spacer(Modifier.height(24.dp))

        // NOME COMPLETO
        CampoCadastro(
            titulo = "Nome completo",
            valor = nome,
            aoAlterar = { nome = it },
            exemplo = "Como podemos chamar você?",
            tipoTeclado = KeyboardType.Text
        )

        Spacer(Modifier.height(15.dp))

        // TELEFONE
        CampoCadastro(
            titulo = "Telefone",
            valor = telefone,
            aoAlterar = {
                telefone = formatarTelefoneCadastro(it)
            },
            exemplo = "(11) 99999-9999",
            tipoTeclado = KeyboardType.Phone
        )

        Spacer(Modifier.height(15.dp))

        // E-MAIL
        CampoCadastro(
            titulo = "E-mail",
            valor = email,
            aoAlterar = { email = it },
            exemplo = "seuemail@exemplo.com",
            tipoTeclado = KeyboardType.Email
        )

        Spacer(Modifier.height(15.dp))

        // SENHA
        CampoCadastro(
            titulo = "Senha",
            valor = senha,
            aoAlterar = { senha = it },
            exemplo = "Crie uma senha segura",
            tipoTeclado = KeyboardType.Password,
            campoSenha = true
        )

        Spacer(Modifier.height(15.dp))

        // CONFIRMAR SENHA
        CampoCadastro(
            titulo = "Confirmar senha",
            valor = confirmarSenha,
            aoAlterar = { confirmarSenha = it },
            exemplo = "Digite novamente",
            tipoTeclado = KeyboardType.Password,
            campoSenha = true
        )

        Spacer(Modifier.height(25.dp))

        // BOTÃO CADASTRAR
        Button(
            onClick = {
                val mensagemErro = when {
                    nome.isBlank() ->
                        "Informe seu nome completo."

                    telefone.filter {
                        it.isDigit()
                    }.length < 10 ->
                        "Informe um telefone válido."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches() ->
                        "Informe um e-mail válido."

                    senha.length < 8 ->
                        "A senha deve ter pelo menos 8 caracteres."

                    senha != confirmarSenha ->
                        "As senhas não são iguais."

                    else -> null
                }

                if (mensagemErro != null) {
                    Toast.makeText(
                        contexto,
                        mensagemErro,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Conclui o formulario apenas quando todos os campos sao validos.
                    aoConcluirCadastro(email.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaranjaCadastro,
                contentColor = MarromCadastro
            )
        ) {
            Text(
                text = "Cadastrar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // VOLTAR PARA O LOGIN
        TextButton(
            onClick = aoVoltarLogin
        ) {
            Text(
                text = "Já tem uma conta? Entrar",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MarromCadastro,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

// Exibe o logotipo existente no cabecalho sem interromper a abertura do cadastro.
@Composable
fun Logolaranja(modifier: Modifier) {
    Image(
        painter = painterResource(R.drawable.logolaranja),
        contentDescription = "Logotipo do GerenciaCÃO",
        modifier = modifier.size(68.dp),
        contentScale = ContentScale.Fit
    )
}

// CAMPO REUTILIZÁVEL
@Composable
private fun CampoCadastro(
    titulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    exemplo: String,
    tipoTeclado: KeyboardType,
    campoSenha: Boolean = false
) {
    var mostrarSenha by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MarromCadastro
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = aoAlterar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = exemplo,
                    fontSize = 14.sp,
                    color = CinzaCadastro
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = tipoTeclado
            ),
            visualTransformation = if (
                campoSenha && !mostrarSenha
            ) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (campoSenha) {
                {
                    TextButton(
                        onClick = {
                            mostrarSenha = !mostrarSenha
                        }
                    ) {
                        Text(
                            text = if (mostrarSenha)
                                "Ocultar" else "Mostrar",
                            fontSize = 12.sp,
                            color = MarromCadastro
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LaranjaCadastro,
                unfocusedBorderColor = BordaCadastro,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = MarromCadastro,
                focusedTextColor = MarromCadastro,
                unfocusedTextColor = MarromCadastro
            )
        )
    }
}

// FORMATA O TELEFONE
private fun formatarTelefoneCadastro(
    valor: String
): String {
    val numeros = valor.filter {
        it.isDigit()
    }.take(11)

    return when {
        numeros.isEmpty() -> ""

        numeros.length <= 2 ->
            "($numeros"

        numeros.length <= 6 ->
            "(${numeros.take(2)}) ${numeros.drop(2)}"

        numeros.length <= 10 ->
            "(${numeros.take(2)}) " +
                    "${numeros.drop(2).take(4)}-" +
                    numeros.drop(6)

        else ->
            "(${numeros.take(2)}) " +
                    "${numeros.drop(2).take(5)}-" +
                    numeros.drop(7)
    }
}

// LOGOTIPO COM AS LETRAS ORIGINAIS
@Composable
private fun LogoCadastro(
    modifier: Modifier = Modifier,
    carregarImagemSemMargem: (Int) -> ImageBitmap
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Row(
            modifier = Modifier.align(Alignment.End),
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
