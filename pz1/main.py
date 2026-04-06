from datetime import date

for i in range(1, 11):
    print(i)
print()

# a = float(input("Введіть перше число: "))
# b = float(input("Введіть друге число: "))
# c = float(input("Введіть третє число: "))
# average = (a + b + c) / 3
nums = list(map(float, input("числа: ").split(",")))
average = sum(nums) / len(nums)
print(f"Середнє значення: {average:.2f}\n")


date_str = input("Введіть дату народження (РРРР.ММ.ДД): ")
year, month, day = map(int, date_str.split("."))
date: date = date(year, month, day)
today = date.today()
age = today.year - date.year - ((today.month, today.day) < (date.month, date.day))
print(f"Ваш вік: {age} років\n")


class Book:
    def __init__(self, title: str, author: str, year: int):
        self.title = title
        self.author = author
        self.year = year

    def __str__(self) -> str:
        return f'"{self.title}" - {self.author} ({self.year})'


book = Book("Nineteen Eighty-Four", "George Orwell", 1949)
print(f"Назва:       {book.title}")
print(f"Автор:       {book.author}")
print(f"Рік видання: {book.year}")
print(f"Повний опис: {book}")
