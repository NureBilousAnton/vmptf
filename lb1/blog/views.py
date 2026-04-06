from django.shortcuts import get_object_or_404, render
from rest_framework import generics

from .models import Article
from .serializers import ArticleSerializer


def article_list(request):
    articles = Article.objects.all()
    return render(request, "blog/article_list.html", {"articles": articles})


def article_detail(request, pk):
    article = get_object_or_404(Article, pk=pk)
    return render(request, "blog/article_detail.html", {"article": article})


class ArticleListAPI(generics.ListAPIView):
    queryset = Article.objects.all()
    serializer_class = ArticleSerializer
