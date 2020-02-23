const express = require("express");
const { check, validationResult } = require("express-validator/check");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const router = express.Router();
const auth = require("../middleware/auth");

const User = require("../model/User");
const Group = require("../model/Group");

router.post(
    "/create",
    auth,
    [
        check("groupName", "Please provide a valid Group Name")
            .not()
            .isEmpty(),
        check("members", "Please provide a valid member array").isArray()
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const { groupName, members } = req.body;
        const owner = req.user.id;

        try {
            let group = new Group({
                groupName,
                owner,
                members,
            })

            await group.save();

            res.status(200).json({
                success: true,
            });
        } catch (e) {
            console.error(e.message);
            res.status(500).send("Error in creating group");
        }
    }
);

router.post(
    "/add",
    auth,
    [
        check("groupId", "Please provide a valid Group Name").isMongoId(),
        check("memberIds", "Please provide a valid member array").isArray(),
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const { groupId, memberIds } = req.body;
        const owner = req.user.id;

        try {
            const user = await User.findById(owner);

            const group = await Group.findById(groupId);
            if (!Group.find({ owner: user.id }))
                return res.status(401).send("You are not the Owner of the specified group!")

            for (let member of memberIds) {
                try {
                    const user = await User.findById(member);
                    if (!user) {
                        res.status(400).send("At least one of the IDs provided are wrong");
                        return;
                    }

                    if (group.members.indexOf(member) != -1) {
                        memberIds.splice(memberIds.indexOf(member), 1);
                    }
                } catch (e) {
                    console.error(e);
                    return res.status(400).send("At least one of the IDs provided are wrong");
                }
            }

            group.members = [...group.members, ...memberIds];

            group.save();

            res.json({ success: true });
        } catch (e) {
            res.send({ message: "Error in adding group members" });
            console.error(e);
        }
    }
);

router.get(
    "/get",
    auth,
    [
        check("groupId", "Please provide a valid Group Name").isMongoId(),
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({
                error: errors.array()
            });
        }

        const { groupId } = req.body;
        const user = req.user.id;

        try {
            const group = await Group.findById(groupId);

            if (group.members.indexOf(user) === -1)
                return res.status(401).send("You are not a member of the specified group!");

            const members = group.members;

            res.json({ members });
        } catch (e) {
            res.send({ message: "Error in getting group member" });
            console.error(e);
        }
    }
);

module.exports = router;