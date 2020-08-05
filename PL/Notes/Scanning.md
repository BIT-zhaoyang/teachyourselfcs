# Scanning
## The Interpreter Framework
- > It’s a good engineering practice to separate the code that generates the errors from the code that reports them.

## Longer Lexemes
### Single Responsibility Principle
Consider this question: after we found a '/' character, we found another one following it. So, this is the starting of line comment. Thus, we need to keep searching, until we meet a '\n'. With this logic, we came up with following code:
```
case '/':
  if (match('/')) {
      while (peek() != '\n' && isAtEnd()) advance();
      // slot
  } else {
      addToken(SLASH);
  }
  break;

case ...
```
Now, after the `while` loop. We know the next character may be a `\n` (or the line could also be ill-posed so it runs out of character but never meet a `\n`), then shall we process the `\n` such as doing
```
if (peek() == '\n') {
  current++;
  line++;
}
```
at the slot place? Since we don't need to add `comment` as token, so it's logically correct to skip the current character and increase the line counting. **But**, a better way is to leave the `\n` character, and let a special case to handle that such as the following does:
```
case '\n':
  line++;
  break;
```
The reason is that, handling `\n` is a general task that is not specific to handling `comment`. **Also**, we should stick to the single responsibility principle. Do one thing at a time and do it while. So in the `/` case, when we found the end of the comment, the task is over. Let the `\n` case handle the changing line job.

### Be cautious to user input data
When we are handling the `/` case, we looped through the user input source code data. While we loop through the data, we used this termination condition: `peek() != '\n' && !isAtEnd()`. Why don't we just use `peek() != '\n'`? Isn't that enough? A comment always takes a line, and some other stuff follows that. Well, the assumption may not hold. Let's say, what if the input data is corrupted? So the comment line is truncated in the middle? In that case, the loop will never meet a `\n` and the loop continues until an NPE is thrown. So, keep in mind, always be cautious to user input data.

**Update**: in command line mode, just input `//` and press enter leads to a line without `\n`!

### Question: When to use FSM? When not to use them?
