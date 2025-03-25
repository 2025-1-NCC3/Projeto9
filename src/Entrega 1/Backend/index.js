import express from 'express';
import bodyParser from "body-parser";
import mysql from 'mysql2';

import dotenv from 'dotenv';
dotenv.config()


console.log('hello world!')


var app = express()
var port = process.env.PORT || 3000;

//json
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Banco de Dados
const pool = mysql.createPool({
    port: process.env.PORT,
    host: process.env.HOST,
    user: process.env.USER,
    password: process.env.PASSWORD,
    database: process.env.DB
}).promise()


async function getTeste() {
    const [rows] = await pool.query("SELECT * FROM teste_link")
    return rows
}
const teste = await getTeste()

app.get('/', (req, res) => {
    res.send("Olá do server!")
})

app.post("/add", async (req, res) => {
    try {
        const { nome, idade, email } = req.body; 

        if (!nome || !idade || !email) {
            return res.status(400).json({ error: "Nome, idade e email são necessários!" });
        }

        const [result] = await pool.query(
            "INSERT INTO teste_link (nome, idade, email) VALUES (?, ?, ?)",
            [nome, idade, email]
        );

        res.status(201).json({
            message: "Salvo com sucesso!",
            id: result.insertId,
        });
    } catch (error) {
        console.error("Erro ao inserir data:", error);
        res.status(500).json({ error: "Internal server error" });
    }
});

app.get("/tudo", function (req, res) {
    try {
        res.json(teste)
    } catch (error) {
        console.error("Error fetching data:", error)
        res.status(500).json({ error: "Internal server error" })
    }
  })

app.listen(port, () => {
    console.log(`Servidor rodando na porta ${port}`)
  })