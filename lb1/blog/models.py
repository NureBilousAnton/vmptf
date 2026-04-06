from django.db import models


class Article(models.Model):
    title = models.CharField(max_length=200, verbose_name="Заголовок")
    text = models.TextField(verbose_name="Текст")
    pub_date = models.DateTimeField(auto_now_add=True, verbose_name="Дата публікації")
    author = models.CharField(max_length=100, default="Невідомий", verbose_name="Автор")

    class Meta:
        ordering = ["-pub_date"]
        verbose_name = "Стаття"
        verbose_name_plural = "Статті"

    def __str__(self):
        return f"{self.title} ({self.author})"
