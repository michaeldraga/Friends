// File : app.js

const express = require("express");
const bodyParser = require("body-parser");
const user = require("./routes/user");
const location = require("./routes/location");
const InititateMongoServer = require("./config/db");

// Initiate Mongo Server
InititateMongoServer();

const app = express();

// PORT
const PORT = process.env.PORT || 4000;

// Middleware
app.use(bodyParser.json());

app.get("/", (req, res) => {
    res.json({ message: "API Working"});
});

/**
 * Router Middleware
 * Router - /user/*
 * Method - *
 */
app.use("/user", user);
app.use("/location", location);

app.listen(PORT, (req, res) => {
    console.log(`Server started at PORT ${PORT}`);
})