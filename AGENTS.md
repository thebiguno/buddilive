# Coding style

- Put `if`, `else if`, and `else` clauses on their own lines. Place `else if` and `else` on the line after the preceding block's closing brace, never on the same line as `}`.
- Always use a `{}` block for every `if` and `else if` condition and every `else` clause, even when the block contains only one statement.
- Never use deprecated methods. Use supported replacements when adding or changing code.
- Check warnings as well as errors during builds and verification. Investigate and address warnings introduced or exposed by a change.

```java
if (firstCondition) {
	firstAction();
}
else if (secondCondition) {
	secondAction();
}
else {
	defaultAction();
}
```
