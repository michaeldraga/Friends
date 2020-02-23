// FILENAME : db.js

const mongoose = require("mongoose");

const MONGOURI = "mongodb://localhost:27017/node-auth"

const InititateMongoServer = async () => {
    try {
        await mongoose.connect(MONGOURI, {
            useNewUrlParser: true
        });
        console.log("Connected to DB !!");
    } catch (e) {
        console.log(e);
        throw e;
    }
};

module.exports = InititateMongoServer;