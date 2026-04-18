import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import Article from './pages/Article';
import Login from './pages/Login';

export default function App() {
    return (
        <BrowserRouter>
            <nav>
                <Link to="/">Блог</Link> | <Link to="/login">Увійти</Link>
            </nav>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/article/:id" element={<Article />} />
                <Route path="/login" element={<Login />} />
            </Routes>
        </BrowserRouter>
    );
}
