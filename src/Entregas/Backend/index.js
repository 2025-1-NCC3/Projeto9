import express from "express";
import bodyParser from "body-parser";
import mysql from "mysql2";
import cors from "cors";

//curl https://api.ipify.org?format=json
console.log("hello world!");

var app = express();
var port = process.env.PORT || 8080;
app.use(cors({}));

//json
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.get("/test", (req, res) => {
  res.json({ status: "online", timestamp: new Date() });
});

// Banco de Dados
const pool = mysql
  .createPool({
    port: process.env.DB_PORT,
    host: process.env.HOST,
    user: process.env.USER,
    password: process.env.PASSWORD,
    database: process.env.DB,
  })
  .promise();

pool
  .query("SELECT 1")
  .then(() => console.log("✅ Conexão com MySQL bem-sucedida"))
  .catch((err) => console.error("❌ Falha na conexão com MySQL:", err));

app.get("/", (req, res) => {
  res.send("Olá do server!");
});


app.post("/add", async (req, res) => {
  console.log("✅ Requisição POST recebida em /add");
  console.log("📦 Body recebido:", req.body); // Deve mostrar { nome, idade, email }
  console.log("🔌 Conexão com DB:", pool !== undefined); // Deve ser 'true'

  try {
    // Descriptografa os dados recebidos do app
    const nome = decryptFromApp(req.body.nome);
    const idade = req.body.idade; 
    const email = decryptFromApp(req.body.email);

    console.log("Dados descriptografados:", { nome, idade, email });

    if (!nome || !idade || !email) {
      return res
        .status(400)
        .json({ error: "Nome, idade e email são necessários!" });
    }

    const [result] = await pool.query(
      "INSERT INTO teste_link (nome, idade, email) VALUES (?, ?, ?)",
      [nome, idade, email],
    );
    console.log("Dados inseridos no MySQL:", result); // Log do resultado

    res.status(201).json({
      message: "Salvo com sucesso!",
      id: result.insertId,
    });
  } catch (error) {
    console.error("Erro ao inserir data:", error);
    res.status(500).json({ error: error.message });
  }
});


app.get("/tudo", async function (req, res) {
  try {
    const [rows] = await pool.query("SELECT * FROM teste_link");
    const encryptedRows = rows.map(row => ({
        ...row,
        nome: encryptForApp(row.nome),
        email: encryptForApp(row.email)
    }));

    res.json(encryptedRows);
  } catch (error) {
    console.error("Error fetching data:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});

app.get("/tudonormal", async function (req, res){
  try {
      const [rows] = await pool.query("SELECT * FROM teste_link");
      res.json(rows);
    } catch (error) {
      console.error("Error fetching data:", error);
      res.status(500).json({ error: "Internal server error" });
    }
});


// Chaves de criptografia
const SERVER_ENCRYPT_KEY = 5;  // Servidor criptografa (para enviar ao app)
const SERVER_DECRYPT_KEY = 7;   // Servidor descriptografa (para receber do app)
  
  
// Funções de criptografia no servidor  
function cifraCesar(input, shift) {
    if (!input) return input;

    let output = "";
    shift = shift % 26; // Garante que o shift esteja entre 0-25

    for (let i = 0; i < input.length; i++) {
        let c = input.charCodeAt(i);

        if (c >= 65 && c <= 90) { // Maiúsculas
            output += String.fromCharCode(((c - 65 + shift + 26) % 26) + 65);
        } else if (c >= 97 && c <= 122) { // Minúsculas
            output += String.fromCharCode(((c - 97 + shift + 26) % 26) + 97);
        } else {
            output += input.charAt(i);
        }
    }

    return output;
}

// Encriptar dados para enviar ao app
function encryptForApp(input) {
    return cifraCesar(input, SERVER_ENCRYPT_KEY);
}

// Desencriptar dados recebidos do app
function decryptFromApp(input) {
    return cifraCesar(input, -SERVER_DECRYPT_KEY);
}
