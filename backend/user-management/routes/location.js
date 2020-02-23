
const express = require("express");
const { check, validationResult, body } = require("express-validator/check");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const router = express.Router();
const auth = require("../middleware/auth");

const User = require("../model/User");

const precision = a => {
    if (!isFinite(a)) return 0;
    let e = 1, p = 0;
    while (Math.round(a * e) / e !== a) { e *= 10; p++; }
    return p;
}

const digits = x => {
    return (Math.log10((x ^ (x >> 31)) - (x >> 31)) | 0) + 1;
}

function checkCoordinates(coordinate) {
    console.log("checking");
    return precision(coordinate) <= 6 && digits(coordinate) == 2;
}

router.get(
    "/get/",
    auth,
    [
        check("reqUser", "Please provide a valid username").isEmail(),
    ],
    async (req, res) => {
        try {
            const errors = validationResult(req);

            if (!errors.isEmpty()) {
                return res.status(400).json({
                    errors: errors.array()
                });
            }

            const user = await User.findById(req.user.id);
            res.json(user.location)
        } catch (e) {
            res.send({ message: "Error in fetching location" });
            console.error(e);
        }
    }
)

router.post(
    "/set/",
    auth,
    [
        check("lat").custom(value => {
            if (!checkCoordinates(value)) 
                return new Error("Please provide a valid latitude");

            return true;
        }),
        check("long").custom(value => {
            if (!checkCoordinates(value)) 
                return new Error("Please provide a valid longitude");

            return true;
        }),
    ],
    async (req, res) => {
        try {
            const errors = validationResult(req);

            if (!errors.isEmpty()) {
                return res.status(400).json({
                    errors: errors.array()
                });
            }

            const { lat, long } = req.body;

            const user = await User.findById(req.user.id);
            user.location.lat = lat;
            user.location.long = long;
            await user.save();

            console.log(`lat: ${lat}, long: ${long}`);
            
            res.json({ success: true });
        } catch (e) {
            res.send({ message: "Error in fetching user" });
            console.error(e);
        }
    }
)

module.exports = router;
