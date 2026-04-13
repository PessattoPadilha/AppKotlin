🚀 Projeto Câmera com Gemini AI
Este projeto é uma aplicação Android desenvolvida em Kotlin que utiliza a inteligência artificial generativa do Google (Gemini) para identificar produtos através da câmera e buscar preços reais baseados na localização atual 
do usuário.

📱 Funcionalidades
Autenticação Simples: Tela de login para controle de acesso.

Captura de Imagem: Integração com a câmera do dispositivo para fotografar produtos.

Geolocalização: Identificação automática da cidade e estado do usuário para buscas contextuais.

Análise por IA: Processamento de imagem e geração de relatório de preços utilizando o modelo Gemini 3 Flash.

🛠️ Bibliotecas Utilizadas
Para o funcionamento do projeto, foram implementadas as seguintes bibliotecas e APIs:

    🤖 Inteligência Artificial
    Google Generative AI SDK: Responsável pela comunicação direta com o modelo Gemini para análise de imagens e texto.
    
    📸 Hardware e Câmera
    CameraX (Core, Lifecycle, View): Suite de bibliotecas da Jetpack para gerenciar a câmera com suporte a ciclos de vida do Android e visualização em tempo real.
    
    📍 Localização e Google Services
    Google Play Services Location: Utilizada para obter as coordenadas de GPS do dispositivo com alta precisão.
    
    Android Geocoder: Responsável por converter coordenadas (latitude/longitude) em nomes de cidades e estados.

⚙️ Arquitetura e Concorrência
        Kotlin Coroutines: Utilizadas para operações assíncronas (como chamadas de rede e processamento de imagem) sem travar a interface do usuário.
        
        Coroutines Play Services: Ponte para transformar as Tasks do Google em chamadas suspendíveis das Coroutines.
        
        Lifecycle Scope: Garante que as operações da IA sejam canceladas automaticamente se a Activity for destruída.

🏗️ Arquitetura do Projeto
Arquivos do projeto:

    GeminiRepository: Camada de dados que gerencia a lógica da API e GPS.
    
    MainActivity: Gerenciamento do hardware (CameraX) e fluxo de permissões.
    
    LoginActivity & MenuActivity: Controle de acesso e navegação.
    
    ResultadoActivity: Camada de apresentação dos dados processados.

🚀 Como Rodar o Projeto
        1- Clone o repositório:
        
        2- Obtenha uma chave de API no Google AI Studio.
        
        3 -Substitua a constante apiKey no arquivo GeminiRepository.kt pela sua chave.
        
        4- Sincronize o Gradle e execute em um dispositivo físico (recomendado para testar câmera e GPS) ou no emulador configurando localização.

Configurações de Dependências (build.gradle)
Para referência, as principais dependências declaradas são:

        Gradle
        dependencies {
            // Gemini AI
            implementation("com.google.ai.client.generativeai:generativeai:0.4.0")
        
            // CameraX
            val camerax_version = "1.3.0"
            implementation("androidx.camera:camera-core:${camerax_version}")
            implementation("androidx.camera:camera-camera2:${camerax_version}")
            implementation("androidx.camera:camera-lifecycle:${camerax_version}")
            implementation("androidx.camera:camera-view:${camerax_version}")
        
            // Location
            implementation("com.google.android.gms:play-services-location:21.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
        }
Desenvolvido por Gustavo Pessatto Padilha e Rafael de Oliveira klein
EstudanteS de Análise e Desenvolvimento de Sistemas (ADS)
