import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getArticle } from '../api';
import CommentForm from '../components/CommentForm';

export default function Article() {
    const { id } = useParams();
    const [data, setData] = useState(null);

    const load = () => getArticle(id).then(setData);
    useEffect(() => { load(); }, [id]);

    if (!data) return <p>Завантаження...</p>;

    return (
        <div>
            <h1>{data.title}</h1>
            <small>{data.author} · {data.category} · {data.created_at}</small>
            <p>{data.content}</p>
            <h3>Коментарі ({data.comments.length})</h3>
            {data.comments.map(c => (
                <div key={c.id}><b>{c.author ?? 'Анонім'}:</b> {c.text}</div>
            ))}
            <CommentForm articleId={id} onAdded={load} />
        </div>
    );
}

