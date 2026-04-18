const router = require('express').Router({ mergeParams: true });
const db = require('../db');
const authMiddleware = require('../middleware/auth');
const { invalidate } = require('../middleware/cache');

router.post('/', authMiddleware, (req, res) => {
    const { text } = req.body;
    if (!text) return res.status(400).json({ error: 'text обовʼязковий' });
    const result = db.prepare(
        'INSERT INTO comments (article_id, author_id, text) VALUES (?, ?, ?)'
    ).run(req.params.id, req.user.id, text);
    invalidate(`/api/articles/${req.params.id}`);
    res.status(201).json({ id: result.lastInsertRowid });
});

module.exports = router;
