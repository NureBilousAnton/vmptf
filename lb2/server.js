const express = require('express');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

// Order is important, the most specific goes first
app.use('/api/articles/:id/comments', require('./routes/comments'));
app.use('/api/articles', require('./routes/articles'));
app.use('/api/categories', require('./routes/categories'));
app.use('/api/auth', require('./routes/auth'));

app.listen(3000, () => console.log('API: http://localhost:3000'));
