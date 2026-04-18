import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getArticles } from '../api';
import ArticleForm from '../components/ArticleForm';
import SearchBar from '../components/SearchBar';

export default function Home() {
    const [articles, setArticles] = useState([]);
    const [filters, setFilters] = useState({});

    const load = (f = filters) => getArticles(f).then(setArticles);

    useEffect(() => { load(); }, [filters]);

    return (
        <div>
            <h1>Статті</h1>
            <SearchBar onChange={setFilters} />
            <ArticleForm onCreated={load} />
            {articles.map(a => (
                <div key={a.id}>
                    <h2><Link to={`/article/${a.id}`}>{a.title}</Link></h2>
                    <small>{a.author} · {a.category} · {a.created_at}</small>
                </div>
            ))}
        </div>
    );
}

