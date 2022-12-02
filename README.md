# Coffee IDE for Java Developers

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)

A Java IDE for running and debugging Java applications and source code. Contains basic features like multi-file editing, syntax highlighting, compilation and running of Java files, etc. This is still in early open alpha phases and will undergo heavy improvements in the near future.

## Installation Guide

Make sure you have Git installed in your PC.
```
git clone https://github.com/polybit-tech/coffee-java-ide coffee-java-ide
cd coffee-java-ide
javac -cp ".\lib\*;" -d ".\bin" ".\src\tech\polybit\coffeeide\*"
java -cp ".\lib\*;.\bin;" tech.polybit.coffeeide.Main
```
