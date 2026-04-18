from blog.models import Article

Article.objects.create(title="Стаття 5", text="Довгий Текст 5", author="Павло")
Article.objects.create(title="Стаття 6", text="Довгий Текст 6", author="Іван")
Article.objects.create(title="Стаття 7", text="Довгий Текст 7", author="Марія")

for a in Article.objects.filter(author="Іван"):
    print(a)
