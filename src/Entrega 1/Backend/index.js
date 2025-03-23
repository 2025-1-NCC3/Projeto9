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

app.get("/tudo", function (req, res) {
    res.send(teste)
  });

app.listen(port, () => {
    console.log(`Servidor rodando na porta ${port}`);
  });