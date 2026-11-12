# Abrir o GerenciaCaogusta no curso

Este projeto foi configurado para o **Android Studio Otter 2025.2.1**.

## Primeira abertura

1. Clone o repositório ou atualize sua cópia com **Git → Pull**. Se houver alterações locais, faça um commit ou guarde uma cópia antes de atualizar.
2. Abra a pasta principal que contém `settings.gradle.kts`, e não apenas a pasta `app`.
3. Em **Settings → Build, Execution, Deployment → Build Tools → Gradle**, escolha **JDK 17** e a distribuição pelo **Gradle Wrapper**. Baixe o JDK 17 nessa tela caso não esteja instalado.
4. Em **Tools → SDK Manager**, instale **Android 16 / API 36**, **Android SDK Build-Tools 35.0.0** e **Android SDK Platform-Tools**. Não é necessário baixar uma imagem de emulador se for usar um celular conectado.
5. Com internet disponível e o modo offline do Gradle desativado, sincronize o projeto. O primeiro download pode demorar.
6. Selecione a configuração **app**, escolha o celular ou emulador e clique em **Run**.

## Versões compartilhadas

| Componente | Versão |
| --- | --- |
| AGP | 8.10.0 |
| Gradle Wrapper | 8.11.1 |
| Kotlin e plugin Compose | 2.2.10 |
| Compose BOM | 2025.10.01 |
| compileSdk e targetSdk | 36 |
| minSdk | 24 |
| Java para executar Gradle | 17 |

Java e Kotlin geram bytecode de nível 11, conforme a configuração do aplicativo. Isso não altera a exigência de JDK 17 para executar o Gradle.

Não aceite uma atualização automática para AGP 9 enquanto a equipe estiver utilizando o Otter 2025.2.1. Mantenha os mesmos arquivos Gradle em todos os computadores.

O caminho do SDK de cada computador fica em `local.properties`, que é local e ignorado pelo Git. O Android Studio deve configurá-lo para a máquina do curso. Não copie caminhos pessoais de outro computador.

Se a sincronização falhar, confira a primeira mensagem de erro na janela **Build**. A opção **app** pode não aparecer antes de a sincronização terminar.

Referências: [compatibilidade Android Studio e AGP](https://developer.android.com/studio/releases), [requisitos do AGP 8.10](https://developer.android.com/build/releases/agp-8-10-0-release-notes) e [compatibilidade Kotlin](https://kotlinlang.org/docs/gradle-configure-project.html).

## Validacao desta configuracao

APK de desenvolvimento gerado com sucesso usando Gradle 8.11.1 e JDK 17. Testes locais executados com sucesso e fontes dos testes Android compiladas. A abertura no Android Studio do curso e a execucao dos testes em aparelho ainda precisam ser conferidas naquele ambiente.
