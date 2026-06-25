# MATH INVADER 🚀🧮

> Jogo arcade educacional inspirado no minigame "Type Invader" de Fears to Fathom.
> Resolva contas matemáticas para destruir inimigos antes que cheguem até você!

---

## 🎮 Como Jogar

- Inimigos com **contas matemáticas** caem do topo da tela
- Digite a **resposta correta** e pressione **ENTER**
- Acertou → inimigo explode + pontos!
- Errou → inimigo continua avançando
- Se um inimigo chegar ao fundo → você perde 1 **vida**
- Sem vidas → Game Over!

### Controles
| Tecla | Ação |
|---|---|
| Dígitos / `-` | Digitar resposta |
| ENTER | Confirmar resposta |
| P / ESC | Pausar |
| M | Ativar/desativar música |
| S | Ativar/desativar sons |
| ↑ ↓ | Navegar menus |

---

## 🏆 Sistema de Dificuldade

| Nível | Operações |
|---|---|
| 1–3 (Fácil) | Adição e Subtração |
| 4–7 (Médio) | + Multiplicação |
| 8–10 (Difícil) | + Divisão e contas mistas |

Conforme você acerta e sobe de nível:
- Inimigos ficam **mais rápidos**
- Contas ficam **mais difíceis**
- Aparecem tipos especiais de inimigos

### Tipos de Inimigos
- 🟩 **Normal** — velocidade padrão
- 🔵 **Veloz** — move-se rapidamente, menor tamanho
- 🟣 **Tank** — lento mas grande, contas difíceis
- 🔴 **Boss** — muito grande, chega raramente no nível 10+

### Power-ups ✨
Colete os itens que caem voando pela tela:
- ☗ **Escudo** — recupera 1 vida
- ⌛ **Lento** — inimigos ficam lentos por 10 segundos
- ✦ **Limpar** — destroi todos os inimigos instantaneamente
- ✕ **Dobro** — pontos em dobro por 10 segundos

---

## 🚀 Como Executar

### Pré-requisito
- **Java 17+** instalado ([download aqui](https://adoptium.net/))

### Opção 1 — JAR Executável (mais simples)
```bash
# Windows
run.bat

# Linux / macOS
chmod +x run.sh && ./run.sh

# Ou diretamente:
java -jar MathInvader.jar
```

### Opção 2 — Compilar do código-fonte
```bash
mkdir out
javac --release 17 -d out $(find src -name "*.java")
java -cp out mathinvader.Main
```

### Opção 3 — IDE (Eclipse, IntelliJ, NetBeans)
1. Importe o projeto (File → Open/Import)
2. Defina `src` como pasta de fontes
3. Execute `mathinvader.Main`

---

## 📁 Estrutura do Projeto

```
MathInvader/
├── src/
│   └── mathinvader/
│       ├── Main.java                    ← Ponto de entrada
│       ├── core/
│       │   ├── GamePanel.java           ← Loop e lógica principal
│       │   ├── MathQuestion.java        ← Gerador de questões matemáticas
│       │   └── PlayerStats.java         ← Pontuação, vidas, combo, nível
│       ├── entities/
│       │   ├── Enemy.java               ← Inimigos (4 tipos)
│       │   └── PowerUp.java             ← Power-ups (4 tipos)
│       ├── effects/
│       │   ├── ParticleSystem.java      ← Explosões e partículas
│       │   ├── FloatingTextSystem.java  ← Textos flutuantes (+pontos, COMBO!)
│       │   └── ScreenFlash.java         ← Flash de tela (acerto/erro)
│       ├── audio/
│       │   └── SoundManager.java        ← Sons gerados proceduralmente
│       ├── data/
│       │   └── HighScoreManager.java    ← Ranking local (arquivo highscores.dat)
│       └── ui/
│           ├── GameWindow.java          ← Janela principal
│           ├── HUD.java                 ← Interface (pontuação, vidas, input)
│           └── Background.java          ← Cenário animado (estrelas, grade)
├── MathInvader.jar                      ← Executável pronto
├── run.bat                              ← Script Windows
├── run.sh                               ← Script Linux/macOS
└── README.md
```

---

## 💡 Dicas para Modificar

- **Velocidade dos inimigos**: `Enemy.java` → método `baseSpeed()`
- **Dificuldade das contas**: `MathQuestion.java` → métodos `generateEasy/Medium/Hard()`
- **Intervalo de spawn**: `GamePanel.java` → campo `spawnInterval` e `updateSpawnInterval()`
- **Sistema de pontos**: `PlayerStats.java` → método `registerKill()`
- **Cores e visual**: `Enemy.java` → método `colorForType()`

---

Desenvolvido com Java Swing — 100% offline, sem bibliotecas externas. 🎓
