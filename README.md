# Wordle Terminal

A terminal-based clone of Wordle, written in Java.

Originally built as a class project, later cleaned up for my portfolio.

## How to Play

The game prompts you to guess a word. After each guess, it returns the word along with color-coded feedback for each letter:

- 🟩 **Green** — correct letter, correct position
- 🟨 **Yellow** — correct letter, wrong position
- Unchanged — letter is not in the word

## How to Run

Make sure `words.txt`, `game_history.txt`, and `Wordle.java` are all in the same directory. Then, from a terminal:

```bash
javac Wordle.java
java Wordle
```

## Known Issues

- Previously guessed letters aren't visually distinguished from unguessed ones, which can make it a little harder to track which letters you've already tried.
