# Indentation Lexical

This is a lexical component that can be used instead of `StdLexical` to build a parser for a language that has "indentation syntax" or "off-side rule syntax".

Refer to `ToyParser.scala` in the project as an example of how to add this to your parser.  There is nothing special about the "toy" language that has been included: it doesn't even have loop constructs or a way to define functions. It was just created to demonstrate how to use `class IndentationLexical`.

In `ToyParser.scala`, the main source lines of interest are

	def parse( r: Reader[Char] ) = phrase( source )( lexical.read(r) )

	import lexical.{Newline, Indent, Dedent}

Although `IndentationLexical` extends `StdLexical`, you do not create an instance of `Scanner` as you would normally.  `IndentationLexical` has it's own scanner.