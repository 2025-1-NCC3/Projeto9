import express from "express";
import bodyParser from "body-parser";
import mysql from "mysql2";
import cors from "cors";

//curl https://api.ipify.org?format=json

// ==============================================
// 1. Configurações Iniciais
// ==============================================
const app = express();
const port = process.env.PORT || 8080;

// Middlewares
app.use(cors()); // Habilita CORS para todas as rotas
app.use(bodyParser.json()); // Para parsear JSON
app.use(bodyParser.urlencoded({ extended: true })); // Para parsear formulários

// ==============================================
// 2. Configuração do Banco de Dados
// ==============================================
const pool = mysql
  .createPool({
    port: process.env.DB_PORT,
    host: process.env.HOST,
    user: process.env.USER,
    password: process.env.PASSWORD,
    database: process.env.DB,
  })
  .promise();

// Testa a conexão com o banco
pool
  .query("SELECT 1")
  .then(() => console.log("✅ Conexão com MySQL bem-sucedida"))
  .catch((err) => console.error("❌ Falha na conexão com MySQL:", err));

// ==============================================
// 3. Constantes e Funções Auxiliares
// ==============================================
const SERVER_ENCRYPT_KEY = 5; // Para criptografar dados enviados ao app
const SERVER_DECRYPT_KEY = 7; // Para descriptografar dados recebidos do app

/**
 * Implementa a cifra de César para criptografia
 * @param {string} input - Texto a ser criptografado/descriptografado
 * @param {number} shift - Número de posições para shift (positivo para encrypt, negativo para decrypt)
 */
function cifraCesar(input, shift) {
  if (!input) return input;

  let output = "";
  shift = shift % 26; // Normaliza o shift

  for (let i = 0; i < input.length; i++) {
    let c = input.charCodeAt(i);

    // Apenas aplica a cifra para letras (A-Z, a-z)
    if (c >= 65 && c <= 90) {
      // Maiúsculas
      output += String.fromCharCode(((c - 65 + shift + 26) % 26) + 65);
    } else if (c >= 97 && c <= 122) {
      // Minúsculas
      output += String.fromCharCode(((c - 97 + shift + 26) % 26) + 97);
    } else {
      output += input.charAt(i); // Mantém outros caracteres
    }
  }

  return output;
}

// Funções de criptografia específicas
const encryptForApp = (input) => cifraCesar(input, SERVER_ENCRYPT_KEY);
const decryptFromApp = (input) => cifraCesar(input, -SERVER_DECRYPT_KEY);

// ==============================================
// 4. Rotas da API
// ==============================================

/**
 * Rota de teste para verificar se o servidor está online
 */
app.get("/test", (req, res) => {
  res.json({
    status: "online",
    timestamp: new Date(),
    environment: process.env.NODE_ENV || "development",
  });
});

/**
 * Rota principal
 */
app.get("/", (req, res) => {
  res.send("Bem-vindo ao servidor de autenticação!");
});

/**
 * Cadastro de novos usuários
 * Recebe: { nome, email, senha } (todos criptografados)
 * Retorna: { message, id } ou erro
 */
app.post("/cadastro", async (req, res) => {
  console.log("📨 Requisição de cadastro recebida:", req.body);

  try {
    // Validação básica
    if (!req.body.nome || !req.body.email || !req.body.senha) {
      return res.status(400).json({
        sucesso: false,
        mensagem: "Nome, email e senha são obrigatórios",
      });
    }

    // Descriptografa os dados
    const usuario = {
      nome: decryptFromApp(req.body.nome),
      email: decryptFromApp(req.body.email),
      senha: decryptFromApp(req.body.senha),
    };

    console.log("🔓 Dados descriptografados:", usuario);

    // Insere no banco de dados
    const [result] = await pool.query(
      "INSERT INTO usuario (nome, email, senha) VALUES (?, ?, ?)",
      [usuario.nome, usuario.email, usuario.senha]
    );

    console.log("💾 Usuário cadastrado com ID:", result.insertId);

    // Resposta de sucesso (com dados criptografados se necessário)
    res.status(201).json({
      sucesso: true,
      mensagem: "Usuário cadastrado com sucesso",
      IDUsuario: result.insertId,
    });
  } catch (error) {
    console.error("🔥 Erro no cadastro:", error);

    // Trata erros específicos do MySQL (como email duplicado)
    if (error.code === "ER_DUP_ENTRY") {
      return res.status(400).json({
        sucesso: false,
        mensagem: "Este email já está cadastrado",
      });
    }

    res.status(500).json({
      sucesso: false,
      mensagem: "Erro interno no servidor",
    });
  }
});

/**
 * Autenticação de usuários
 * Recebe: { email, senha } (criptografados)
 * Retorna: { sucesso, mensagem, usuario }
 */

app.post("/login", async (req, res) => {
  console.log("📨 Requisição de login recebida");
  console.log("✅ Body recebido:", req.body);

  try {
    // 1. Verifique se os campos existem no body
    if (!req.body.email || !req.body.senha) {
      return res.status(400).json({
        sucesso: false,
        mensagem: "Email e senha são obrigatórios",
      });
    }

    // 2. Descriptografar
    const email = decryptFromApp(req.body.email);
    const senha = decryptFromApp(req.body.senha);

    console.log("🔓 Dados descriptografados:", { email, senha });

    // 3. Buscar usuário (consulta segura)
    const [rows] = await pool.query(
      "SELECT IDUsuario, nome, email FROM usuario WHERE email = ? AND senha = ? LIMIT 1",
      [email, senha]
    );

    console.log("🔍 Resultado da consulta:", rows); // Verifique se encontrou o usuário

    // 4. Resposta
    if (rows.length > 0) {
      res.json({
        sucesso: true,
        mensagem: "Login bem-sucedido",
        usuario: {
          IDUsuario: rows[0].IDUsuario,
          nome: encryptForApp(rows[0].nome),
          email: encryptForApp(rows[0].email),
        },
      });
    } else {
      res.status(401).json({
        sucesso: false,
        mensagem: "Credenciais inválidas",
      });
    }
  } catch (error) {
    console.error("🔥 ERRO NO LOGIN:", error);
    res.status(500).json({
      sucesso: false,
      mensagem: "Erro interno: " + error.message,
    });
  }
});

/**
 * Lista todos os usuários (com dados criptografados)
 * Uso: apenas para desenvolvimento/debug
 */
app.get("/tudo", async (req, res) => {
  try {
    const [rows] = await pool.query("SELECT * FROM usuario");

    // Criptografa dados sensíveis antes de enviar
    const dadosProtegidos = rows.map((row) => ({
      IDUsuario: row.IDUsuario,
      nome: encryptForApp(row.nome),
      email: encryptForApp(row.email),
      senha: encryptForApp(row.senha),
    }));

    res.json(dadosProtegidos);
  } catch (error) {
    console.error("Erro ao buscar usuários:", error);
    res.status(500).json({
      sucesso: false,
      mensagem: "Erro ao buscar usuários",
    });
  }
});

/**
 * Lista todos os usuários (dados originais)
 * Uso: apenas para desenvolvimento/debug
 */
app.get("/tudonormal", async (req, res) => {
  try {
    const [rows] = await pool.query("SELECT * FROM usuario");
    res.json(rows);
  } catch (error) {
    console.error("Erro ao buscar usuários:", error);
    res.status(500).json({
      sucesso: false,
      mensagem: "Erro ao buscar usuários",
    });
  }
});

// ==============================================
// 5. Inicialização do Servidor
// ==============================================
app.listen(port, () => {
  console.log(`🚀 Servidor rodando na porta ${port}`);
  console.log(`🔒 Chave de criptografia: ${SERVER_DECRYPT_KEY}`);
});
