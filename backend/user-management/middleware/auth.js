const jwt = require("jsonwebtoken");

module.exports = (req, res, next) => {
    const token = req.header("token");
    if (!token) return res.status(401).json({ message: "Auth Error" });

    try {
        const decoded = jwt.verify(token, "secret");
        req.user = decoded.user;
        next();
    } catch (e) {
        if (e.name === "TokenExpiredError")
            console.error("Token expired")
        else 
            console.error(e);
        res.status(500).send({ message: "Invalid Token" });
    }
};