package com.example.gerenciacao

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Exibe a confirmação de e-mail com a marca centralizada.
// A confirmação e o reenvio poderão ser conectados futuramente.
@Composable
fun TelaConfirmacaoCadastro(
    email: String,
    aoVoltarLogin: () -> Unit,
    aoConfirmarCodigo: ((String) -> Unit)? = null,
    aoReenviarCodigo: (() -> Unit)? = null
) {
    // Define as cores originais do GerenciaCão.
    val fundo = Color(0xFFFFFCF7)
    val laranja = Color(0xFFFFB21C)
    val marrom = Color(0xFF6B3919)
    val cinza = Color(0xFF8B7B6C)
    val creme = Color(0xFFFFF0C9)
    val borda = Color(0xFFE8DFD4)

    // Preserva o código ao girar a tela.
    var codigo by rememberSaveable {
        mutableStateOf("")
    }

    val contexto = LocalContext.current

    // Retorna ao login pelo botão Voltar do Android.
    BackHandler(onBack = aoVoltarLogin)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fundo)
    ) {
        // Mantém a faixa laranja no topo da tela.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(laranja)
        )

        // Permite rolar o conteúdo quando o teclado estiver aberto.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostra uma seta maior junto do texto de retorno.
            TextButton(
                onClick = aoVoltarLogin,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(
                    horizontal = 0.dp,
                    vertical = 8.dp
                )
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = marrom
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Voltar ao login",
                    fontSize = 14.sp,
                    color = marrom
                )
            }

            Spacer(Modifier.height(36.dp))

            // Exibe somente a tipografia GerenciaCão, centralizada.
            Image(
                painter = painterResource(
                    R.drawable.tipografialogin
                ),
                contentDescription = "GerenciaCão",
                modifier = Modifier
                    .width(230.dp)
                    .height(95.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(32.dp))

            // Identifica a etapa de confirmação.
            Text(
                text = "Confirme seu e-mail",
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = marrom,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            // Orienta o preenchimento sem afirmar que houve envio real.
            Text(
                text = "Digite o código de seis números " +
                        "para confirmar seu cadastro.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = cinza,
                textAlign = TextAlign.Center
            )

            // Mostra o endereço informado no cadastro.
            if (email.isNotBlank()) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = email,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = marrom,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(36.dp))

            // Recebe até seis números em um único campo,
            // apresentando cada número em uma caixa separada.
            BasicTextField(
                value = codigo,
                onValueChange = { novoValor ->
                    codigo = novoValor
                        .filter { it in '0'..'9' }
                        .take(6)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                cursorBrush = SolidColor(marrom),
                decorationBox = { campoInterno ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Mantém o campo de edição real disponível
                        // para o teclado e o foco de digitação.
                        Box(
                            modifier = Modifier
                                .size(1.dp)
                                .alpha(0f)
                        ) {
                            campoInterno()
                        }

                        // Desenha as seis posições do código.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(6) { indice ->
                                val preenchido =
                                    indice < codigo.length

                                val proximoCampo =
                                    indice == codigo.length

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .border(
                                            width = if (proximoCampo) {
                                                2.dp
                                            } else {
                                                1.dp
                                            },
                                            color = if (proximoCampo) {
                                                laranja
                                            } else {
                                                borda
                                            },
                                            shape = RoundedCornerShape(
                                                12.dp
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (preenchido) {
                                            codigo[indice].toString()
                                        } else {
                                            "•"
                                        },
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = marrom
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(30.dp))

            // Fica apagado enquanto faltarem números.
            // Com seis números, permite solicitar a verificação.
            Button(
                onClick = {
                    val confirmar = aoConfirmarCodigo

                    if (confirmar != null) {
                        confirmar(codigo)
                    } else {
                        Toast.makeText(
                            contexto,
                            "A confirmação ainda não está disponível.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = codigo.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = laranja,
                    contentColor = marrom,
                    disabledContainerColor = creme,
                    disabledContentColor = marrom.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Text(
                    text = "Confirmar código",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Solicita um novo código quando o serviço for conectado.
            TextButton(
                onClick = {
                    val reenviar = aoReenviarCodigo

                    if (reenviar != null) {
                        reenviar()
                    } else {
                        Toast.makeText(
                            contexto,
                            "O reenvio ainda não está disponível.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text(
                    text = "Reenviar código",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = marrom
                )
            }

            Spacer(Modifier.height(12.dp))

            // Apresenta uma orientação para localizar a mensagem.
            Text(
                text = "Não recebeu? Confira a caixa de spam.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = cinza,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

// Mostra a tela no Preview sem conectá-la ao botão Cadastrar.
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PreviewConfirmacaoCadastro() {
    MaterialTheme {
        TelaConfirmacaoCadastro(
            email = "teste@exemplo.com",
            aoVoltarLogin = {}
        )
    }
}