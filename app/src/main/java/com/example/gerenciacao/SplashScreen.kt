
package com.example.gerenciacao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// GERENCIACÃO — NOVO PROJETO
// Splash Screen reaproveitada do projeto original.

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                var telaAtual by rememberSaveable {
                    mutableStateOf("abertura")
                }

                when (telaAtual) {

                    // SPLASH SCREEN
                    "abertura" -> TelaAbertura(
                        aoTerminar = {
                            telaAtual = "login"
                        }
                    )

                    // LOGIN
                    "login" -> TelaLogin(
                        aoAbrirCadastro = {
                            telaAtual = "cadastro"
                        }
                    )

                    // CADASTRO
                    "cadastro" -> TelaCadastro(
                        aoVoltarLogin = {
                            telaAtual = "login"
                        }
                    )
                    "recuperar_senha" -> TelaEsqueciSenha(
                        aoVoltarLogin = {
                            telaAtual = "login"
                        }
                    )
                }
            }
        }
    }
}

    // Guarda as posições das letras do logotipo.
private data class LetraSplash(
    val imagem: Int,
    val x: Float,
    val y: Float,
    val largura: Float,
    val altura: Float
)

// SPLASH SCREEN ORIGINAL
@Composable
fun TelaAbertura(
    aoTerminar: () -> Unit = {}
) {

    var letrasVisiveis by remember {
        mutableIntStateOf(0)
    }

    // Posições retiradas do frame original 412 × 917.
    val letras = remember {
        listOf(
            // GERENCIA
            LetraSplash(R.drawable.g, 100f, 537f, 44f, 51f),
            LetraSplash(R.drawable.e, 145f, 559f, 22f, 26f),
            LetraSplash(R.drawable.r, 168f, 562f, 19f, 23f),
            LetraSplash(R.drawable.e, 188f, 559f, 22f, 26f),
            LetraSplash(R.drawable.n, 211f, 559f, 26f, 26f),
            LetraSplash(R.drawable.c, 238f, 560f, 21f, 24f),
            LetraSplash(R.drawable.i, 256f, 557f, 15f, 34f),
            LetraSplash(R.drawable.a, 269f, 559f, 25f, 23f),

            // CÃO
            LetraSplash(R.drawable.c, 243f, 582f, 41f, 44f),
            LetraSplash(R.drawable.acifrao, 285f, 588f, 26f, 31f),
            LetraSplash(R.drawable.o, 306f, 582f, 35.06f, 37.77f)
        )
    }

    // Animação: uma letra de cada vez.
    LaunchedEffect(Unit) {
        delay(500L)

        repeat(letras.size) {
            letrasVisiveis++
            delay(200L)
        }

        // Mantém o nome completo na tela.
        delay(1000L)

        // Abre a próxima tela.
        aoTerminar()
    }

    var tamanhoTela by remember {
        mutableStateOf(IntSize.Zero)
    }

    val densidade = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EF))
            .onSizeChanged {
                tamanhoTela = it
            },
        contentAlignment = Alignment.Center
    ) {

        // Ajusta o frame original ao tamanho do celular.
        val escala = with(densidade) {
            if (tamanhoTela == IntSize.Zero) {
                1f
            } else {
                minOf(
                    tamanhoTela.width.toDp().value / 412f,
                    tamanhoTela.height.toDp().value / 917f
                )
            }
        }

        Box(
            modifier = Modifier.size(
                width = (412f * escala).dp,
                height = (917f * escala).dp
            )
        ) {

            // Logotipo original.
            Image(
                painter = painterResource(
                    id = R.drawable.logolaranja
                ),
                contentDescription = "Logotipo GerenciaCÃO",
                modifier = Modifier
                    .offset(
                        x = (86.25f * escala).dp,
                        y = (310f * escala).dp
                    )
                    .size(
                        width = (239.5f * escala).dp,
                        height = (222.35f * escala).dp
                    ),
                contentScale = ContentScale.Fit
            )

            // Ampliação proporcional das letras.
            val tamanhoNome = 1.10f

            letras.forEachIndexed { indice, letra ->

                val posicaoX =
                    220.5f + (letra.x - 220.5f) * tamanhoNome

                val posicaoY =
                    525f + (letra.y - 537f) * tamanhoNome

                Image(
                    bitmap = carregarImagemSemMargem(
                        letra.imagem
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(
                            x = (posicaoX * escala).dp,
                            y = (posicaoY * escala).dp
                        )
                        .size(
                            width = (
                                    letra.largura *
                                            tamanhoNome *
                                            escala
                                    ).dp,
                            height = (
                                    letra.altura *
                                            tamanhoNome *
                                            escala
                                    ).dp
                        )
                        .alpha(
                            if (indice < letrasVisiveis) {
                                1f
                            } else {
                                0f
                            }
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// Remove margens transparentes das letras.
@Composable
fun carregarImagemSemMargem(
    recurso: Int
): ImageBitmap {

    val recursos = LocalContext.current.resources

    return remember(recurso, recursos) {

        val imagem = BitmapFactory.decodeResource(
            recursos,
            recurso
        )

        val largura = imagem.width
        val altura = imagem.height

        val pixels = IntArray(largura * altura)

        imagem.getPixels(
            pixels,
            0,
            largura,
            0,
            0,
            largura,
            altura
        )

        var esquerda = largura
        var topo = altura
        var direita = -1
        var base = -1

        for (y in 0 until altura) {
            for (x in 0 until largura) {

                val pixel = pixels[y * largura + x]

                if ((pixel ushr 24) != 0) {
                    esquerda = minOf(esquerda, x)
                    topo = minOf(topo, y)
                    direita = maxOf(direita, x)
                    base = maxOf(base, y)
                }
            }
        }

        if (direita >= esquerda && base >= topo) {

            Bitmap.createBitmap(
                imagem,
                esquerda,
                topo,
                direita - esquerda + 1,
                base - topo + 1
            ).asImageBitmap()

        } else {
            imagem.asImageBitmap()
        }
    }
}

// Tela provisória.
// Depois será substituída pelo login aprovado no PDF.

