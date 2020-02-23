// FILENAME : Group.js

const mongoose = require("mongoose");

const GroupSchema = mongoose.Schema({
    groupName: {
        type: String,
        required: true,
    },
    createdAt: {
        type: Date,
        default: Date.now(),
    },
    owner: String,
    members: [String],
})

module.exports = mongoose.model("group", GroupSchema);