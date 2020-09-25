// FILENAME : user.js

const express = require("express");
const { check, validationResult } = require("express-validator/check");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const router = express.Router();
const auth = require("../middleware/auth");

const User = require("../model/User");

/**
 * @method - POST
 * @param - /signup
 * @description - User Signup
 */

router.post(
    "/add",
    auth,
    [
        check("reqUserMail", "Please enter a valid id").isEmail()
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const {
            reqUserMail,
        } = req.body;

        try {
            const user = await User.findById(req.user.id);
            const reqUser = await User.findOne({email: reqUserMail});
            const reqUserId = reqUser._id;

            if (!user.friends.actual.find(request => request._id == reqUserId)) {
                if (!user.friends.requests.incoming.find(request => request._id == reqUserId)) {
                    if (!user.friends.requests.outgoing.find(request => request._id == reqUserId)) {
                        user.friends.requests.outgoing.push(reqUserId);
                        reqUser.friends.requests.incoming.push(req.user.id);

                        const payload = {
                            user: {
                                id: user.id
                            }
                        };

                        jwt.sign(
                            payload,
                            "secret", {
                            expiresIn: 3600
                        },
                            (err, token) => {
                                if (err) throw err;
                                res.status(200).json({
                                    token,
                                    message: 'Your friend request has been successfully sent'
                                });
                            }
                        );
                        user.save();
                        reqUser.save()
                        return;
                    }
                    jwt.sign(
                        payload,
                        "secret", {
                        expiresIn: 3600
                    },
                        (err, token) => {
                            if (err) throw err;
                            res.status(200).json({
                                token,
                                message: `You have already sent ${reqUser.username} a friend request!`
                            });
                        }
                    );
                    return;
                }
                user.friends.actual.push(reqUserId);
                reqUser.friends.actual.push(req.user.id);
                user.friends.requests.incoming.splice(user.friends.requests.incoming.indexOf(reqUserId), 1);
                reqUser.friends.requests.outgoing.splice(reqUser.friends.requests.outgoing.indexOf(req.user.id), 1);

                const payload = {
                    user: {
                        id: user.id
                    }
                };

                jwt.sign(
                    payload,
                    "secret", {
                    expiresIn: 3600
                },
                    (err, token) => {
                        if (err) throw err;
                        res.status(200).json({
                            token,
                            message: `${reqUser.username} and you are now friends!`
                        });
                    }
                );
                user.save();
                reqUser.save()
                return;
            }
            const payload = {
                user: {
                    id: user.id
                }
            };

            jwt.sign(
                payload,
                "secret", {
                expiresIn: 3600
            },
                (err, token) => {
                    if (err) throw err;
                    res.status(200).json({
                        token,
                        message: `${reqUser.username} and you are already friends!`
                    });
                }
            );
            user.save();
        } catch (e) {
            console.error(e);
            res.status(500).send("Error in adding");
        }
    }
);

/**
 * @method - POST
 * @description - Login User
 * @param - /user/login
 */

router.post(
    "/list",
    auth,
    async (req, res) => {
        const errors = validationResult(req);

        if (!errors.isEmpty()) {
            return res.status(400).json({
                errors: errors.array()
            });
        }
        try {
            const user = await User.findById(req.user.id);

            const _actual = user.friends.actual;
            const _outgoing = user.friends.requests.outgoing;
            const _incoming = user.friends.requests.incoming;

            let actual = [];
            let outgoing = [];
            let incoming = [];

            actual = await friendMap(_actual);

            outgoing = await reqMap(_outgoing);

            incoming = await reqMap(_incoming);

            const friends = {
                actual,
                outgoing,
                incoming
            }

            const payload = {
                user: {
                    id: user.id
                }
            };

            jwt.sign(
                payload,
                "secret",
                {
                    expiresIn: 3600
                },
                (err, token) => {
                    if (err) throw err;
                    res.status(200).json({
                        token,
                        friends
                    });
                }
            );
        } catch (e) {
            console.error(e);
            res.status(500).json({
                message: "Listing Error"
            });
        }
    }
);

const friendMap = async friends => {
    return new Promise(async (resolve, reject) => {
        let array = [];
        for (friend of friends) {
            user = await User.findById(friend._id);
            array.push({
                id: user._id,
                username: user.username,
                email: user.email,
                location: user.location,
            })
        }
        resolve(array);
    })
}

const reqMap = async requests => {
    return new Promise(async (resolve, reject) => {
        let array = [];
        for (request of requests) {
            user = await User.findById(request._id);
            array.push({
                id: user._id,
                username: user.username,
                email: user.email,
            })
        }
        resolve(array);
    })
}

/**
 * @method - GET
 * @description - Get LoggedIn User
 * @param - /user/me
 */

router.post(
    "/remove",
    auth,
    [
        check("reqUserId", "Please enter a valid id").isMongoId()
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const {
            reqUserId,
        } = req.body;

        try {
            const user = await User.findById(req.user.id);
            const reqUser = await User.findById(reqUserId);

            if (!user.friends.actual.find(friend => friend._id == reqUserId)) {
                const payload = {
                    user: {
                        id: user.id
                    }
                };
                jwt.sign(
                    payload,
                    "secret", {
                    expiresIn: 3600
                },
                    (err, token) => {
                        if (err) throw err;
                        res.status(200).json({
                            token,
                            message: `${reqUser.username} and you are no friends!`
                        });
                    }
                );
                return;
            }
            if (user.friends.outgoing) {
                if (user.friends.outgoing.find(request => request._id == reqUserId)) {
                    user.friends.outgoing.splice(indexOf(reqUserId), 1);
                    reqUser.friends.incoming.splice(indexOf(req.user.id), 1);
    
                    const payload = {
                        user: {
                            id: user.id
                        }
                    };
    
                    jwt.sign(
                        payload,
                        "secret", {
                        expiresIn: 3600
                    },
                        (err, token) => {
                            if (err) throw err;
                            res.status(200).json({
                                token,
                                message: `${reqUser.username}'s friend request has been deleted!`
                            });
                        }
                    );
                    user.save();
                    reqUser.save()
                    return;
                }
            }

            user.friends.actual.splice(user.friends.actual.indexOf(reqUserId), 1);
            reqUser.friends.actual.splice(user.friends.actual.indexOf(req.user.id), 1);

            const payload = {
                user: {
                    id: user.id
                }
            };

            jwt.sign(
                payload,
                "secret", {
                expiresIn: 3600
            },
                (err, token) => {
                    if (err) throw err;
                    res.status(200).json({
                        token,
                        message: `${reqUser.username} and you are no longer friends!`
                    });
                }
            );
            user.save();
            reqUser.save()
        } catch (e) {
            console.error(e);
            res.status(500).send("Error in removing");
        }
    }
);

router.post(
    "/favorite",
    auth,
    [
        check("reqUserId", "Please enter a valid id").isMongoId()
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const {
            reqUserId,
        } = req.body;

        try {
            const user = await User.findById(req.user.id);
            const reqUser = await User.findById(reqUserId);

            const favo = user.friends.actual.find(friend => friend._id == reqUserId).fav;

            if (favo) {
                user.friends.actual.find(friend => friend._id == reqUserId).fav = false;
            } else {
                user.friends.actual.find(friend => friend._id == reqUserId).fav = true;
            }

            const payload = {
                user: {
                    id: user.id
                }
            };

            jwt.sign(
                payload,
                "secret", {
                expiresIn: 3600
            },
                (err, token) => {
                    if (err) throw err;
                    res.status(200).json({
                        token,
                        message: !favo ? `${reqUser.username} is now one of your favorites!` : `${reqUser.username} is no longer one of your favorites!`
                    });
                }
            );
            user.save();
        } catch (e) {
            console.error(e);
            res.status(500).send("Error in favoriting");
        }
    }
);

module.exports = router;
