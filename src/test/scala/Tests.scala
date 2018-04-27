package xyz.hyperreal.indentation_lexical

import util.parsing.input.CharSequenceReader

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers
{
	val l = new IndentationLexical( false, true, List("[", "("), List("]", ")"), ";;", "/*", "*/" )

	case class PARSE_FAILURE( msg: String )
	
	def parse( s: String ) =
	{
	val p = new ToyParser
	
 		p.parse( new CharSequenceReader(s) ) match
		{
			case p.Success( tree, _ ) => ToyInterpreter( tree )
			case p.NoSuccess( error, _ ) => PARSE_FAILURE( error )
		}
	}
	
	import l.{Newline => nl, Indent => ind, Dedent => ded, num}

	"newlines" in
	{
		l scan "1" shouldBe List(num("1"), nl)
		l scan "1\n2" shouldBe List(num("1"), nl, num("2"), nl)
		l scan "1\n" shouldBe List(num("1"), nl)
		l scan "1\n2\n" shouldBe List(num("1"), nl, num("2"), nl)
		l scan "\n1" shouldBe List(num("1"), nl)
		l scan "\n1\n2" shouldBe List(num("1"), nl, num("2"), nl)
		l scan "\n1\n" shouldBe List(num("1"), nl)
		l scan "\n1\n2\n" shouldBe List(num("1"), nl, num("2"), nl)

    l scan "1\r\n2" shouldBe List(num("1"), nl, num("2"), nl)
    l scan "1\r\n" shouldBe List(num("1"), nl)
    l scan "1\r\n2\r\n" shouldBe List(num("1"), nl, num("2"), nl)
    l scan "\r\n1" shouldBe List(num("1"), nl)
    l scan "\r\n1\r\n2" shouldBe List(num("1"), nl, num("2"), nl)
    l scan "\r\n1\r\n" shouldBe List(num("1"), nl)
    l scan "\r\n1\r\n2\r\n" shouldBe List(num("1"), nl, num("2"), nl)
	}

	"indentation" in
	{
		l scan "1\n\t2" shouldBe List(num("1"), ind, num("2"), nl, ded, nl)
		l scan "\t1\n" shouldBe List(num("1"), nl)
		l scan "\t1\n2\n" shouldBe List(num("1"), nl, num("2"), nl)
		l scan "\n\t1" shouldBe List(num("1"), nl)
		l scan "\n1\n\t2" shouldBe List(num("1"), ind, num("2"), nl, ded, nl)
		l scan "\n1\n\t" shouldBe List(num("1"), nl)
		l scan "\n1\n\t2\n" shouldBe List(num("1"), ind, num("2"), nl, ded, nl)
	}

	"parsing" in
	{
		parse( "123 + 5" ) shouldBe 128
		parse( "[1, 2, 3]" ) shouldBe List( 1, 2, 3 )
		parse(
			"""
			|a = 3 ;; asdf
			|b = 4
			|c =
			|	if a + b == 7 then
			|		5a
			|
			|c
			|""".stripMargin ) shouldBe 15
	}
}