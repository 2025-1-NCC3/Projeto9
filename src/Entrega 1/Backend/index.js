import express from "express";
import bodyParser from "body-parser";
import mysql from "mysql2";
import cors from "cors";

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
    const { nome, idade, email } = req.body;
    console.log("Dados recebidos:", { nome, idade, email });

    if (!nome || !idade || !email) {
      return res
        .status(400)
        .json({ error: "Nome, idade e email são necessários!" });
    }

    const [result] = await pool.query(
      "INSERT INTO teste_link (nome, idade, email) VALUES (?, ?, ?)",
      [nome, idade, email]
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

async function getTeste() {
  const [rows] = await pool.query("SELECT * FROM teste_link");
  return rows;
}
const teste = await getTeste();

app.get("/tudo", function (req, res) {
  try {
    res.json(teste);
  } catch (error) {
    console.error("Error fetching data:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
  // console.log(
  //   `URL externa: https://${process.env.CODESANDBOX_HOST}.sse.codesandbox.io`
  // );
});
