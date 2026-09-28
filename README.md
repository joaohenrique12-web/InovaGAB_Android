# InovaGAB - Aplicativo Android

Aplicativo nativo Android desenvolvido para a Sprint 2 do projeto InovaGAB. A solução apoia a gestão de inovação da Águia Branca, permitindo que operadores registrem ideias e problemas, gestores avaliem iniciativas e líderes acompanhem projetos, rankings e indicadores estratégicos.

## Objetivo

Transformar o protótipo da Sprint 1 em um aplicativo integrado a um backend real, removendo mocks dos fluxos principais e consumindo APIs REST protegidas por autenticação JWT.

## Principais Funcionalidades

### Operador

- Login com perfil operacional.
- Visualização da tela inicial com nome, pontuação, ranking e orientações estratégicas.
- Cadastro de ideias ou problemas enfrentados na operação.
- Consulta das próprias ideias.
- Apoio de IA na criação da ideia, consumindo o endpoint do backend.
- Consulta de orientações estratégicas publicadas pela liderança.
- Visualização de perfil, pontuação e posição no ranking.

### Gestor

- Login com perfil de gestor.
- Dashboard com ideias para avaliação, urgências, projetos ativos e funil de inovação.
- Consulta de ideias pendentes e histórico.
- Avaliação de ideias com aprovação, priorização ou recusa.
- Análise técnica com IA para apoiar a tomada de decisão.
- Criação e acompanhamento de projetos a partir de ideias aprovadas.
- Consulta de orientações estratégicas.

### Líder

- Login com perfil de liderança.
- Dashboard executivo com economia total, ROI médio, projetos ativos, ideias e projetos concluídos.
- Ranking de colaboradores.
- Consulta e acompanhamento de projetos.
- Gestão de orientações estratégicas.
- Visualização do perfil com bottom bar adequada ao perfil de liderança.

## Tecnologias Utilizadas

- Kotlin
- Android nativo
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel e StateFlow
- Retrofit
- Gson Converter
- OkHttp
- Coil
- Biometric API
- Backend REST com autenticação JWT

## Estrutura do Projeto

```text
InovaGAB/
├── app/
│   ├── src/main/java/br/com/aguiabranca/inovagab/
│   │   ├── data/
│   │   │   ├── model/
│   │   │   └── remote/
│   │   ├── navigation/
│   │   ├── ui/
│   │   │   ├── components/
│   │   │   ├── diretoria/
│   │   │   ├── gestor/
│   │   │   ├── onboarding/
│   │   │   └── operacional/
│   │   ├── viewmodel/
│   │   └── MainActivity.kt
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Integração com Backend

O app consome o backend através do `ApiClient`, localizado em:

```text
app/src/main/java/br/com/aguiabranca/inovagab/data/remote/ApiClient.kt
```

URL padrão usada no emulador Android:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8081/api/"
```

Quando o app roda em emulador, `10.0.2.2` aponta para o `localhost` da máquina. Portanto, o backend precisa estar rodando localmente na porta `8081`.

Para testar em celular físico, altere temporariamente a URL para o IP da máquina na rede local:

```kotlin
private const val BASE_URL = "http://SEU_IP_LOCAL:8081/api/"
```

## Endpoints Consumidos pelo App

### Autenticação

| Método | Rota | Uso |
|---|---|---|
| GET | `/auth/health` | Verificação de disponibilidade |
| POST | `/auth/login` | Login dos três perfis |
| GET | `/auth/me` | Dados do usuário autenticado |

### Dashboard

| Método | Rota | Uso |
|---|---|---|
| GET | `/dashboard/resumo` | KPIs de gestor/liderança |

### Ideias

| Método | Rota | Uso |
|---|---|---|
| GET | `/ideias` | Listar ideias por status/categoria |
| GET | `/ideias/minhas` | Listar ideias do operador autenticado |
| GET | `/ideias/{id}` | Buscar detalhe da ideia |
| POST | `/ideias` | Criar ideia |
| PUT | `/ideias/{id}` | Editar ideia |
| DELETE | `/ideias/{id}` | Remover ideia |
| PUT | `/ideias/{id}/status` | Aprovar, recusar ou priorizar ideia |

### Orientações Estratégicas

| Método | Rota | Uso |
|---|---|---|
| GET | `/orientacoes` | Consultar orientações ativas |
| POST | `/orientacoes` | Criar orientação |
| PUT | `/orientacoes/{id}` | Editar orientação |
| DELETE | `/orientacoes/{id}` | Desativar orientação |

### Projetos

| Método | Rota | Uso |
|---|---|---|
| GET | `/projetos` | Listar projetos |
| POST | `/projetos` | Criar projeto |
| PUT | `/projetos/{id}` | Atualizar projeto |
| DELETE | `/projetos/{id}` | Remover projeto |
| PUT | `/projetos/{id}/status` | Alterar status do projeto |
| PUT | `/projetos/{id}/resultados` | Registrar resultado financeiro |

### Ranking

| Método | Rota | Uso |
|---|---|---|
| GET | `/usuarios/ranking` | Ranking de colaboradores |

### Inteligência Artificial

| Método | Rota | Uso |
|---|---|---|
| POST | `/ia/sugerir-ideia` | Sugestão para operador durante criação de ideia |
| POST | `/ia/analisar-ideia` | Análise técnica para gestor avaliar ideia |

## Autenticação

Após o login, o backend retorna um token JWT. O app armazena a sessão em `UserSession` e o `ApiClient` adiciona automaticamente o token em todas as requisições autenticadas:

```http
Authorization: Bearer <token>
```

## Perfis e Navegação

| Perfil Backend | Tela inicial |
|---|---|
| `OPERADOR` ou `OPERACIONAL` | Home do operador |
| `GESTOR` | Home do gestor |
| `LIDER` ou `LIDERANCA` | Dashboard da liderança |

## Inteligência Artificial no App

A IA é consumida sempre pelo backend. O app não possui chave de Gemini nem chama serviços externos diretamente.

Fluxos implementados:

- Operador escreve uma ideia e pode tocar em "Analisar ideia com IA".
- O app chama `/ia/sugerir-ideia`.
- Gestor abre a tela de avaliação e pode solicitar análise técnica.
- O app chama `/ia/analisar-ideia`.

Isso mantém a chave da IA protegida no backend e evita exposição no APK.

## Como Executar o App

1. Suba o backend na porta `8081`.
2. Abra o projeto no Android Studio.
3. Confirme a URL em `ApiClient.kt`.
4. Sincronize o Gradle.
5. Execute em emulador ou dispositivo físico.

Comando útil:

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build de Entrega

Para gerar APK pelo Android Studio:

```text
Build > Generate Signed Bundle / APK > APK
```

Para zipar o projeto, não incluir:

```text
.git/
.gradle/
build/
app/build/
local.properties
google-services.json
```

Arquivos importantes para incluir:

```text
README.md
build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat
gradle/wrapper/
app/src/
```

## Observações de Escopo

Os fluxos principais da Sprint 2 estão integrados ao backend real:

- Login por perfil.
- Ideias.
- Avaliação por gestor.
- Projetos.
- Orientações estratégicas.
- Dashboard.
- Ranking.
- Integração com IA via backend.

Funcionalidades deixadas como evolução futura:

- Anexos de ideia.
- Foto de perfil.
- Notificações em tempo real.
- Recuperação de senha.

Essas funcionalidades foram tratadas como expansão futura porque exigem endpoints específicos e armazenamento de arquivos/notificações.

## Checklist de Testes Manuais

- Login como operador.
- Criar ideia.
- Usar análise de IA na criação da ideia.
- Conferir se a ideia aparece em "Minhas ideias".
- Login como gestor.
- Avaliar ideia com IA.
- Aprovar ou recusar ideia.
- Criar projeto a partir de uma ideia aprovada.
- Alterar status do projeto.
- Login como líder.
- Conferir dashboard.
- Consultar ranking.
- Criar, editar e desativar orientação estratégica.
- Conferir se operador e gestor visualizam orientações atualizadas.

## Segurança

- O app não deve conter chave do Gemini.
- O app não deve conter URI real do MongoDB.
- O app não deve conter `google-services.json` se Firebase não estiver sendo usado.
- Tokens JWT são enviados apenas no header `Authorization`.

## Autores

Projeto acadêmico desenvolvido para a Sprint 2 do InovaGAB.

Equipe

Integrantes
Vitor Pavan
RM 564012

João Henrique
RM 566372

Gustavo Gomes
RM 566105

Giovanni Luiz
RM 563071

Willian Arakaki
RM 561418