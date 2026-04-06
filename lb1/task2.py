from blog.models import Article

# Article.objects.create(title="Стаття 1", text="Текст 1", author="Іван")
# Article.objects.create(title="Стаття 2", text="Текст 2", author="Іван")
# Article.objects.create(title="Стаття 3", text="Текст 3", author="Марія")

for a in Article.objects.filter(author="Іван"):
    print(a)
