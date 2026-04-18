import { useState } from 'react';
import { addComment } from '../api';

export default function CommentForm({ articleId, onAdded }) {
    const [text, setText] = useState('');

    const submit = async () => {
        const user = JSON.parse(localStorage.getItem('user') || 'null');
        await addComment(articleId, { text, author_id: user?.id });
        setText('');
        onAdded?.();
    };

    return (
        <div>
            <input
                placeholder="Коментар..."
                value={text}
                onChange={e => setText(e.target.value)}
            />
            <button onClick={submit}>Додати</button>
        </div>
    );
}
