# Coffee IDE for Java Developers (Open Alpha)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)

A Java IDE for running and debugging Java applications and source code. Contains basic features like multi-file editing, syntax highlighting, compilation and running of Java files, etc. This is still in early open alpha phases and will undergo heavy improvements in the near future.

## Features

- Run and debug Java files with ease
- Syntax highlighting for Java code
- Intuitive user interface for easy navigation
- Open-source and community-driven development

## Installation Guide

Make sure you have Git installed in your PC.
```
git clone https://github.com/polybit-tech/coffee-java-ide coffee-java-ide
cd coffee-java-ide
javac -cp ".\lib\*;" -d ".\bin" ".\src\tech\polybit\coffeeide\*"
java -cp ".\lib\*;.\bin;" tech.polybit.coffeeide.Main
```

## How To Use

- Open Coffee IDE.
- Click the "New Project" button to select the Java project directory you want to write Java programs.<br/>There will be a pre-generated `HelloWorld.java` file in the `/src` folder.
- Click the "Compile File" button to compile the Java file.
- Click the "Run File" button to execute the Java program.

## License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/PolybitRockzz/coffee-java-ide/blob/main/LICENSE) file for more information.

## Feedback And Support

If you have any feedback or need support, please open an issue on GitHub. We're always looking for ways to improve our project, and your feedback is valuable to us.
