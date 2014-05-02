package funl.indentation

import java.{lang => boxed}

import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import util.parsing.combinator.lexical.StdLexical
import util.parsing.input.Reader


object ToyParser extends StandardTokenParsers with PackratParsers
{
	override val lexical =
		new IndentationLexical( false, true, List("[", "("), List("]", ")") )
		{
			override def token: Parser[Token] = decimalParser | super.token

			private def decimalParser: Parser[Token] =
				rep1(digit) ~ optFraction ~ optExponent ^^
					{case intPart ~ frac ~ exp =>
						NumericLit( (intPart mkString "") :: frac :: exp :: Nil mkString "")} |
				fraction ~ optExponent ^^
					{case frac ~ exp => NumericLit( frac + exp )}

			private def chr( c: Char ) = elem( "", ch => ch == c )

			private def sign = chr( '+' ) | chr( '-' )

			private def optSign = opt( sign ) ^^
				{
					case None => ""
					case Some(sign) => sign
				}

			private def fraction = ('.' <~ not('.')) ~ rep( digit ) ^^
				{case dot ~ ff => dot :: (ff mkString "") :: Nil mkString ""}

			private def optFraction = opt( fraction ) ^^
				{
					case None => ""
					case Some( fraction ) => fraction
				}

			private def exponent = (chr( 'e' ) | chr( 'E' )) ~ optSign ~ rep1( digit ) ^^
				{case e ~ optSign ~ exp => e :: optSign :: (exp mkString "") :: Nil mkString ""}

			private def optExponent = opt( exponent ) ^^
				{
					case None => ""
					case Some( exponent ) => exponent
				}

			reserved += ("if", "then", "else", "elif", "true", "false", "or", "and", "not")
			delimiters += ("+", "*", "-", "/", "^", "(", ")", "[", "]", ",", "=", "/=", "<", ">", "<=", ">=")
		}

	def parse( r: Reader[Char] ) = phrase( source )( lexical.read(r) )

	import lexical.{Newline, Indent, Dedent}

	lazy val onl = opt(Newline)

	lazy val source =
		(statement+) |
		Newline ^^^ List( (null, null) )

	lazy val statement: PackratParser[Any] =
		ident ~ "=" ~ expr ~ Newline ^^ {case v ~ _ ~ e ~ _ => ("#assign", (v, e))} |
		expr <~ Newline

	lazy val expr: PackratParser[Any] =
		expr5

	lazy val elif =
		(onl ~ "elif") ~> (expr7 <~ "then") ~ expr ^^ {case c ~ t => (c, t)}

	lazy val expr5: PackratParser[Any] =
		"if" ~> (expr7 <~ "then") ~ expr ~ rep(elif) ~ opt((onl ~ "else") ~> expr) ^^
			{case c ~ t ~ ei ~ e => ("#if", c, t, ei, e)} |
		expr7

	lazy val expr7 =
		expr7a ~ rep("or" ~ expr7a) ^^
			{case lhs ~ list => (lhs /: list){case (x, op ~ y) => (op, (x, y))}}

	lazy val expr7a =
		expr7b ~ rep("and" ~ expr7b) ^^
			{case lhs ~ list => (lhs /: list){case (x, op ~ y) => (op, (x, y))}}

	lazy val expr7b: PackratParser[Any] =
		"not" ~ expr7b ^^ {case op ~ e => (op, e)} |
	expr7c

	lazy val expr7c: PackratParser[Any] =
		expr10 ~ ("=" | "/=" | "<" | ">" | "<=" | ">=") ~ expr10 ^^
			{case l ~ o ~ r => (o, (l, r))} |
		expr10

	lazy val expr10: PackratParser[Any] =
		expr10 ~ ("+" | "-") ~ expr20 ^^ {case l ~ o ~ r => (o, (l, r))} |
		expr20

	lazy val expr20: PackratParser[Any] =
		expr20 ~ ("*" | "/") ~ expr30 ^^ {case l ~ o ~ r => (o, (l, r))} |
		expr20 ~ expr30 ^^ {case l ~ r => ("*", (l, r))} |
		expr30

	lazy val expr30: PackratParser[Any] =
		expr30 ~ "^" ~ expr40 ^^ {case l ~ o ~ r => (o, (l, r))} |
		expr40

 	lazy val expr40: PackratParser[Any] =
		"-" ~> expr40 ^^ (("-", _)) |
		expr50

	lazy val expr50 =
		primaryExpr

	lazy val primaryExpr =
		ident ~ "(" ~ repsep(expr, ",") ~ ")" ^^ {case id ~ _ ~ e ~ _ => (id, e)} |
		numericLit ^^
			(n =>
				if (n matches ".*(\\.|e|E).*")
					n.toDouble.asInstanceOf[boxed.Double]
				else
					n.toInt.asInstanceOf[boxed.Integer]) |
		stringLit |
		("true" | "false") ^^ (_.toBoolean) |
		"[" ~> repsep(expr, ",") <~ "]" ^^ {case l => ("#list", l)} |
		ident ^^ {case v => ("#var", v)} |
		"(" ~> expr <~ ")" |
		Indent ~> (statement+) <~ Dedent ^^ {case s => ("#block", s)}
}