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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
                // Estado demonstrativo compartilhado entre home e perfil.
                var dadosMeuPerfil by rememberSaveable { mutableStateOf(DadosMeuPerfil()) }
                val fotoDaPessoa = lembrarFotoMeuPerfil(dadosMeuPerfil.fotoUri)
                var origemPerfil by rememberSaveable { mutableStateOf("inicio") }

                var telaAtual by rememberSaveable {
                    mutableStateOf("abertura")
                }

                // Guarda as fichas da demonstracao durante a sessao e a rotacao da tela.
                // O Tico ja inicia adotado para combinar com seu processo demonstrativo em acompanhamento.
                var animais by rememberSaveable {
                    mutableStateOf(ArrayList(AnimaisExemplo.map { if (it.nome == "Tico") it.copy(situacao = "Adotado") else it }))
                }
                // Cria os exemplos uma vez e mantem seus vinculos por ID durante a sessao.
                var processosAdocao by rememberSaveable {
                    mutableStateOf(criarProcessosAdocaoExemplo(animais))
                }
                var origemNovaFicha by rememberSaveable { mutableStateOf("animais") }

                // Lembra de onde o usuario abriu o painel de acoes rapidas.
                var origemAdicionar by rememberSaveable { mutableStateOf("inicio") }

                // Identifica a ficha por ID para separar ate animais com o mesmo nome.
                var animalSelecionadoId by rememberSaveable { mutableStateOf<String?>(null) }
                var animalEmEdicaoId by rememberSaveable { mutableStateOf<String?>(null) }
                // Lembra o processo e as origens para os botoes Voltar.
                var processoSelecionadoId by rememberSaveable { mutableStateOf<String?>(null) }
                var origemAcompanhamento by rememberSaveable { mutableStateOf("adocoes") }
                var origemFicha by rememberSaveable { mutableStateOf("animais") }
                val estadoLista = rememberSaveableStateHolder()
                // A rede de conexoes e separada da equipe e das permissoes do proprio abrigo.
                var abrigoRedeSelecionadoId by rememberSaveable { mutableStateOf<String?>(null) }
                var conexoesRede by rememberSaveable { mutableStateOf(arrayListOf("cantinho-focinhos")) }

                // Dados adicionais ficam apenas no estado local salvo pelo Android.
                var dadosRestantes by rememberSaveable { mutableStateOf(dadosRestantesExemplo(animais)) }
                var pilhaRestante by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
                val abrirRestante: (String) -> Unit = { destino ->
                    if (destino != telaAtual) {
                        pilhaRestante = ArrayList(pilhaRestante + telaAtual)
                        telaAtual = destino
                    }
                }
                val contextoRestante = ContextoAbrigo(
                    dados = dadosRestantes,
                    animais = animais,
                    atualizar = { dadosRestantes = it },
                    abrir = abrirRestante,
                    voltar = {
                        estadoLista.removeState(telaAtual)
                        val anterior = pilhaRestante.lastOrNull() ?: "inicio"
                        pilhaRestante = ArrayList(pilhaRestante.dropLast(1))
                        telaAtual = anterior
                    },
                    principal = { destino ->
                        estadoLista.removeState(telaAtual)
                        if (destino == "adicionar") origemAdicionar = telaAtual
                        pilhaRestante = arrayListOf()
                        telaAtual = destino
                    },
                    verAnimal = { id ->
                        origemFicha = telaAtual
                        animalSelecionadoId = id
                        telaAtual = "ficha_animal"
                    }
                )

                // Disponibiliza o destino Financas ao menu de todas as telas.
                CompositionLocalProvider(
                    LocalAbrirFinancas provides { telaAtual = "financas" },
                    LocalAbrirLoja provides { pilhaRestante = arrayListOf(); telaAtual = "loja" },
                    LocalCabecalhoApp provides DadosCabecalhoApp(
                        nome = dadosMeuPerfil.nome,
                        categoria = "${dadosRestantes.membros.firstOrNull { it.id == dadosRestantes.usuarioLocalId }?.funcao?.titulo ?: dadosMeuPerfil.funcao} · ${dadosRestantes.abrigo.nome}",
                        foto = fotoDaPessoa,
                        aoPerfil = {
                            if (telaAtual != "perfil") { origemPerfil = telaAtual; telaAtual = "perfil" }
                        },
                        aoParcerias = { if (telaAtual != "parcerias") abrirRestante("parcerias") },
                        aoSair = { pilhaRestante = arrayListOf(); telaAtual = "login" },
                        aoDescartarRascunho = { estadoLista.removeState(telaAtual) }
                    )
                ) {
                    when (telaAtual) {
                        // A vitrine conserva busca e categorias quando o usuario abre uma oferta.
                        "loja" -> estadoLista.SaveableStateProvider("loja") {
                            TelaLoja(contextoRestante)
                        }
                        // Conserva busca, filtros, aba e posicao de rolagem ao abrir e fechar uma ficha.
                        "redes_conexoes" -> estadoLista.SaveableStateProvider("redes_conexoes") {
                            TelaRedesConexoes(
                                ctx = contextoRestante,
                                abrigos = AbrigosRedeExemplo,
                                conexoes = conexoesRede.toSet(),
                                aoAbrirAbrigo = { id ->
                                    abrigoRedeSelecionadoId = id
                                    abrirRestante("informacoes_abrigo")
                                }
                            )
                        }
                        // A mesma ficha de informacoes atende qualquer abrigo escolhido pelo ID.
                        "informacoes_abrigo" -> estadoLista.SaveableStateProvider("informacoes_abrigo") {
                            val abrigo = AbrigosRedeExemplo.firstOrNull { it.id == abrigoRedeSelecionadoId }
                            TelaInformacoesAbrigo(
                                ctx = contextoRestante,
                                abrigo = abrigo,
                                conectado = abrigo?.id in conexoesRede,
                                aoDefinirConexao = { conectar ->
                                    if (abrigo != null) {
                                        conexoesRede = ArrayList(if (conectar) (conexoesRede + abrigo.id).distinct()
                                        else conexoesRede.filterNot { it == abrigo.id })
                                    }
                                }
                            )
                        }
                        // Os formularios compartilham estado, mas cada rota conserva seu rascunho.
                        in DestinosRestantes -> estadoLista.SaveableStateProvider(telaAtual) {
                            RotasRestantes(telaAtual, contextoRestante)
                        }

                        // SPLASH SCREEN
                        "abertura" -> TelaAbertura(
                            aoTerminar = {
                                telaAtual = "login"
                            }
                        )

                        // LOGIN
                        "login" -> TelaLogin(
                            // Abre o inicio do prototipo depois de validar os campos de acesso.
                            aoAcessar = {
                                telaAtual = "inicio"
                            },
                            // Abre o cadastro quando o usuario toca em criar conta.
                            aoAbrirCadastro = {
                                telaAtual = "cadastro"
                            },
                            // Abre a recuperacao de senha pelo link da tela de login.
                            aoRecuperarSenha = {
                                telaAtual = "recuperar_senha"
                            }
                        )

                        // CADASTRO
                        "cadastro" -> TelaCadastro(
                            aoVoltarLogin = {
                                telaAtual = "login"
                            },
                            // Retorna ao login apos validar o cadastro, sem criar conta real.
                            aoConcluirCadastro = {
                                telaAtual = "login"
                            }
                        )
                        // Mantem a tela inicial separada dos formularios de acesso.
                        "inicio" -> TelaInicio(
                            // Liga o cartao Encontre cuidadores perto de voce ao novo fluxo.
                            aoEncontrarCuidadores = { abrirRestante("redes_conexoes") },
                            // O banner abre a lista de parcerias pela navegacao que ja existe.
                            imagemParceria = R.drawable.pedigree,
                            aoAbrirParcerias = { abrirRestante("parcerias") },
                            compromissos = dadosRestantes.compromissos,
                            aoRegistrarDoacao = { abrirRestante("registrar_doacao") },
                            aoRegistrarDespesa = { abrirRestante("registrar_despesa") },
                            // Reflete os dados editados no perfil, sem fixar nome ou foto.
                            nomePessoa = dadosMeuPerfil.nome,
                            categoriaPessoa = "${dadosRestantes.membros.firstOrNull { it.id == dadosRestantes.usuarioLocalId }?.funcao?.titulo ?: dadosMeuPerfil.funcao} · ${dadosRestantes.abrigo.nome}",
                            fotoPerfil = fotoDaPessoa,
                            aoAbrirPerfil = {
                                origemPerfil = "inicio"
                                telaAtual = "perfil"
                            },
                            // Liga o item Agenda ao calendario.
                            aoAgenda = { telaAtual = "agenda" },
                            aoSair = {
                                telaAtual = "login"
                            },
                            // Abre a lista tanto pelo menu quanto pelo atalho de bichinhos.
                            aoAbrirAnimais = { telaAtual = "animais" },
                            // O botao central abre as acoes; o atalho de cadastro permanece direto.
                            aoAdicionar = {
                                origemAdicionar = "inicio"
                                telaAtual = "adicionar"
                            },
                            aoNovaFicha = {
                                animalEmEdicaoId = null
                                origemNovaFicha = "inicio"
                                telaAtual = "nova_ficha"
                            }
                        )
                        // Retorna ao inicio pelo menu inferior ou pelo botao Voltar do Android.
                        "animais" -> estadoLista.SaveableStateProvider("lista_animais") { TelaAnimais(
                            // Liga o item Agenda ao calendario.
                            aoAgenda = { telaAtual = "agenda" },
                            aoVoltarInicio = { telaAtual = "inicio" },
                            aoAdocoes = { telaAtual = "adocoes" },
                            animais = animais,
                            aoAdicionar = {
                                origemAdicionar = "animais"
                                telaAtual = "adicionar"
                            },
                            aoAbrirFicha = { id ->
                                origemFicha = "animais"
                                animalSelecionadoId = id
                                telaAtual = "ficha_animal"
                            }
                        ) }
                        // Mantem a busca e os filtros do painel ao alternar entre destinos.
                        "adocoes" -> estadoLista.SaveableStateProvider("adocoes") {
                            TelaAdocoes(
                                animais = animais,
                                processos = processosAdocao,
                                aoVoltar = { telaAtual = "animais" },
                                aoInicio = { telaAtual = "inicio" },
                                aoAnimais = { telaAtual = "animais" },
                                aoAgenda = { telaAtual = "agenda" },
                                aoAdicionar = {
                                    origemAdicionar = "adocoes"
                                    telaAtual = "adicionar"
                                },
                                aoAbrirProcesso = { id ->
                                    processoSelecionadoId = id
                                    origemAcompanhamento = "adocoes"
                                    telaAtual = "acompanhar_adocao"
                                }
                            )
                        }
                        // Resolve sempre o processo e o animal atuais pelo ID.
                        "acompanhar_adocao" -> {
                            val processo = processosAdocao.firstOrNull { it.id == processoSelecionadoId }
                            val animal = animais.firstOrNull { it.id == processo?.animalId && !it.arquivado }
                            if (processo != null && animal != null) {
                                TelaAcompanharAdocao(
                                    animal = animal,
                                    processo = processo,
                                    aoVoltar = { telaAtual = origemAcompanhamento },
                                    aoInicio = { telaAtual = "inicio" },
                                    aoAnimais = { telaAtual = "animais" },
                                    aoAgenda = { telaAtual = "agenda" },
                                    aoAdicionar = {
                                        origemAdicionar = "acompanhar_adocao"
                                        telaAtual = "adicionar"
                                    },
                                    aoVerFicha = {
                                        animalSelecionadoId = animal.id
                                        origemFicha = "acompanhar_adocao"
                                        telaAtual = "ficha_animal"
                                    },
                                    aoAlterarEtapa = { etapa ->
                                        // Modifica apenas este processo; a confirmacao de adocao tem acao propria.
                                        processosAdocao = ArrayList(processosAdocao.map {
                                            if (it.id == processo.id) it.copy(etapa = etapa, proximaAcao = proximaAcaoAdocao(etapa)) else it
                                        })
                                    },
                                    aoConfirmarAdocao = {
                                        // Mantem a ficha e o processo coerentes na mesma atualizacao local.
                                        animais = ArrayList(animais.map {
                                            if (it.id == animal.id) it.copy(situacao = "Adotado") else it
                                        })
                                        processosAdocao = ArrayList(processosAdocao.map {
                                            if (it.id == processo.id) it.copy(etapa = EtapaAdocao.ACOMPANHAMENTO,
                                                proximaAcao = proximaAcaoAdocao(EtapaAdocao.ACOMPANHAMENTO)) else it
                                        })
                                    }
                                )
                            } else {
                                // Nao cria um processo ficticio para animais ainda sem interessado.
                                androidx.activity.compose.BackHandler { telaAtual = origemAcompanhamento }
                                Column(Modifier.fillMaxSize().background(Color(0xFFFFFCF7)).statusBarsPadding()
                                    .navigationBarsPadding().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Nenhum processo de adoção disponível para este bichinho.")
                                    androidx.compose.material3.TextButton(onClick = { telaAtual = origemAcompanhamento }) {
                                        Text("Voltar")
                                    }
                                }
                            }
                        }
                        // Usa uma unica tela para qualquer animal escolhido, inclusive novas fichas.
                        "ficha_animal" -> {
                            val animal = animais.firstOrNull { it.id == animalSelecionadoId && !it.arquivado }
                            if (animal != null) {
                                TelaFichaAnimal(
                                    // Liga o item Agenda ao calendario.
                                    aoAgenda = { telaAtual = "agenda" },
                                    animal = animal,
                                    aoVoltar = { telaAtual = origemFicha },
                                    aoInicio = { telaAtual = "inicio" },
                                    aoAdicionar = {
                                        origemAdicionar = "ficha_animal"
                                        telaAtual = "adicionar"
                                    },
                                    aoAcompanharAdocao = {
                                        processoSelecionadoId = processosAdocao.firstOrNull { it.animalId == animal.id }?.id
                                        origemAcompanhamento = "ficha_animal"
                                        telaAtual = "acompanhar_adocao"
                                    },
                                    aoEditar = {
                                        animalEmEdicaoId = animal.id
                                        telaAtual = "editar_ficha"
                                    }
                                )
                            } else {
                                LaunchedEffect(animalSelecionadoId) { telaAtual = "animais" }
                            }
                        }
                        // Edita o registro escolhido e devolve a ficha atualizada ao detalhe.
                        "editar_ficha" -> {
                            val animal = animais.firstOrNull { it.id == animalEmEdicaoId && !it.arquivado }
                            if (animal != null) {
                                TelaEditarFicha(
                                    animal = animal,
                                    aoVoltar = { telaAtual = "ficha_animal" },
                                    aoInicio = { telaAtual = "inicio" },
                                    aoAnimais = { telaAtual = "animais" },
                                    aoAgenda = { telaAtual = "agenda" },
                                    aoAdicionar = {
                                        origemAdicionar = "editar_ficha"
                                        telaAtual = "adicionar"
                                    },
                                    aoSalvar = { atualizado ->
                                        // Substitui pelo ID, sem criar uma segunda ficha.
                                        animais = ArrayList(animais.map { if (it.id == atualizado.id) atualizado else it })
                                        animalSelecionadoId = atualizado.id
                                        animalEmEdicaoId = null
                                        telaAtual = "ficha_animal"
                                    },
                                    aoArquivar = { id ->
                                        // Oculta a ficha da lista ativa e conserva os dados na sessao.
                                        animais = ArrayList(animais.map { if (it.id == id) it.copy(arquivado = true) else it })
                                        animalSelecionadoId = null
                                        animalEmEdicaoId = null
                                        telaAtual = "animais"
                                    }
                                )
                            } else {
                                LaunchedEffect(animalEmEdicaoId) { telaAtual = "animais" }
                            }
                        }
                        // Valida a nova ficha e adiciona apenas a lista local, sem banco de dados.
                        "nova_ficha" -> TelaNovaFicha(
                            // Liga o item Agenda ao calendario.
                            aoAgenda = { telaAtual = "agenda" },
                            animalInicial = animais.firstOrNull { it.id == animalEmEdicaoId },
                            aoVoltar = { telaAtual = origemNovaFicha },
                            aoInicio = { telaAtual = "inicio" },
                            aoAnimais = { telaAtual = "animais" },
                            aoAdicionar = {
                                // Preserva a origem quando o formulario foi aberto pelo atalho direto.
                                if (origemNovaFicha != "adicionar") origemAdicionar = origemNovaFicha
                                telaAtual = "adicionar"
                            },
                            aoSalvar = { animal ->
                                // Atualiza a ficha existente ou inclui uma nova sem duplicar a edicao.
                                animais = if (animalEmEdicaoId != null) {
                                    ArrayList(animais.map { if (it.id == animalEmEdicaoId) animal else it })
                                } else ArrayList(animais + animal)
                                animalEmEdicaoId = null
                                telaAtual = "animais"
                            }
                        )
                        // Centraliza os atalhos do botao mais e liga o cadastro ja existente.
                        "adicionar" -> TelaAdicionar(
                            aoRegistrarDoacao = { abrirRestante("registrar_doacao") },
                            aoRegistrarDespesa = { abrirRestante("registrar_despesa") },
                            aoAgendarCuidado = {
                                dadosRestantes = dadosRestantes.copy(editarCompromissoId = null)
                                abrirRestante("novo_compromisso")
                            },
                            aoCriarPostagem = { abrirRestante("criar_postagem") },
                            aoParcerias = { abrirRestante("parcerias") },
                            aoDivulgacao = { abrirRestante("divulgacao") },
                            // Liga o item Agenda ao calendario.
                            aoAgenda = { telaAtual = "agenda" },
                            aoVoltar = { telaAtual = origemAdicionar },
                            aoInicio = { telaAtual = "inicio" },
                            aoAnimais = { telaAtual = "animais" },
                            aoCadastrarBichinho = {
                                animalEmEdicaoId = null
                                origemNovaFicha = "adicionar"
                                telaAtual = "nova_ficha"
                            }
                        )
                        // Conserva o mes e o dia selecionados ao alternar entre as telas.
                        "agenda" -> estadoLista.SaveableStateProvider("agenda") {
                            TelaAgendaIntegrada(contextoRestante)
                        }
                        // Preserva o periodo e o filtro quando o usuario troca de tela.
                        "financas" -> estadoLista.SaveableStateProvider("financas") {
                            TelaFinancas(
                                lancamentos = dadosRestantes.lancamentos,
                                aoRegistrarDoacao = { abrirRestante("registrar_doacao") },
                                aoRegistrarDespesa = { abrirRestante("registrar_despesa") },
                                aoRelatorios = { abrirRestante("relatorios") },
                                aoPerfil = {
                                    origemPerfil = "financas"
                                    telaAtual = "perfil"
                                },
                                aoInicio = { telaAtual = "inicio" },
                                aoAnimais = { telaAtual = "animais" },
                                aoAgenda = { telaAtual = "agenda" },
                                aoAdicionar = {
                                    origemAdicionar = "financas"
                                    telaAtual = "adicionar"
                                }
                            )
                        }
                        // Perfil usa uma unica fonte de dados compartilhada com a home.
                        "perfil" -> TelaMeuPerfil(
                            dados = dadosMeuPerfil.copy(abrigo = dadosRestantes.abrigo.nome,
                                funcao = dadosRestantes.membros.firstOrNull { it.id == dadosRestantes.usuarioLocalId }?.funcao?.titulo ?: dadosMeuPerfil.funcao),
                            aoAbrirAbrigo = { abrirRestante("meu_abrigo") },
                            aoAbrirEquipe = { abrirRestante("equipe_abrigo") },
                            // Compartilha o nome pessoal com o membro correspondente na equipe.
                            aoAtualizarPerfil = { atualizados ->
                                dadosMeuPerfil = atualizados
                                dadosRestantes = dadosRestantes.copy(membros = dadosRestantes.membros.map {
                                    if (it.id == dadosRestantes.usuarioLocalId) it.copy(nome = atualizados.nome) else it
                                })
                            },
                            aoVoltar = { telaAtual = origemPerfil },
                            aoInicio = { telaAtual = "inicio" },
                            aoAnimais = { telaAtual = "animais" },
                            aoAgenda = { telaAtual = "agenda" },
                            aoAdicionar = {
                                origemAdicionar = "perfil"
                                telaAtual = "adicionar"
                            },
                            aoSair = {
                                dadosMeuPerfil = DadosMeuPerfil()
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