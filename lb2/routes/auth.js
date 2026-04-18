const router = require('express').Router();
const db = require('../db');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const SECRET = process.env.JWT_SECRET || 'dev_secret_change_in_prod';

router.post('/register', (req, res) => {
    const { username, password } = req.body;
    if (!username || !password)
        return res.status(400).json({ error: 'username і password обов`язкові' });
    try {
        const hash = bcrypt.hashSync(password, 10);
        const result = db.prepare('INSERT INTO users (username, password) VALUES (?, ?)').run(username, hash);
        res.status(201).json({ id: result.lastInsertRowid, username });
    } catch {
        res.status(409).json({ error: 'Логін вже зайнятий' });
    }
});

router.post('/login', (req, res) => {
    const { username, password } = req.body;
    const user = db.prepare('SELECT * FROM users WHERE username = ?').get(username);
    if (!user || !bcrypt.compareSync(password, user.password))
        return res.status(401).json({ error: 'Невірний логін або пароль' });
    const token = jwt.sign({ id: user.id, username: user.username }, SECRET, { expiresIn: '24h' });
    res.json({ token, user: { id: user.id, username: user.username } });
});

module.exports = router;
